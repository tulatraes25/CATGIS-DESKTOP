package ar.com.catgis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Actual filesystem identity and byte preservation, not fake GIS payloads. */
class BatchOutputPlanContractTest {

    @TempDir
    Path directory;
    private Path first;
    private Path second;
    private Path output;

    @BeforeEach
    void prepare() throws IOException {
        first = Files.createDirectory(directory.resolve("first input"));
        second = Files.createDirectory(directory.resolve("second input"));
        output = Files.createDirectory(directory.resolve("output folder"));
    }

    @Test
    void genuineCopiesKeepOrderBytesAndExistingUnrelatedOutputCompatibility() throws IOException {
        File a = write(first, "mapa norte.dat", "NORTH");
        File b = write(second, "r\u00edo.dat", "RIVER");
        write(output, a.getName(), "PREVIOUS RESULT");
        List<String> order = new ArrayList<>();
        List<Integer> progress = new ArrayList<>();
        BatchProcessor.BatchResult result = BatchProcessor.processBatch(job(List.of(a, b), output),
                (in, out) -> {
                    order.add(in.getName());
                    copy(in, out);
                }, progress::add);
        assertEquals(2, result.success());
        assertEquals(0, result.failed());
        assertTrue(result.errors().isEmpty());
        assertEquals(List.of(a.getName(), b.getName()), order);
        assertEquals(List.of(50, 100), progress);
        assertEquals("NORTH", Files.readString(output.resolve(a.getName())));
        assertEquals("RIVER", Files.readString(output.resolve(b.getName())));
        assertEquals("NORTH", Files.readString(a.toPath()));
        assertEquals("RIVER", Files.readString(b.toPath()));
    }

    @Test
    void sameBasenameRejectsWholeBatchWithoutChangingOldOutputOrSources() throws IOException {
        File a = write(first, "map.dat", "FIRST");
        File b = write(second, "map.dat", "SECOND");
        write(output, "map.dat", "KEEP OLD OUTPUT");
        assertRejected(List.of(a, b), output);
    }

    @Test
    void outputDirectoryEqualToInputCannotProcessInPlace() throws IOException {
        File a = write(first, "precious.dat", "SOURCE");
        assertRejected(List.of(a), first);
    }

    @Test
    void earlyTargetCannotBeHardLinkToAnotherInputScheduledLater() throws IOException {
        File a = write(first, "early.dat", "FIRST");
        File b = write(second, "later.dat", "LATER SOURCE");
        Files.createLink(output.resolve(a.getName()), b.toPath());
        assertTrue(Files.isSameFile(output.resolve(a.getName()), b.toPath()));
        assertRejected(List.of(a, b), output);
    }

    @Test
    void distinctOutputNamesCannotAliasTheSameExistingInode() throws IOException {
        File a = write(first, "a.dat", "A");
        File b = write(second, "b.dat", "B");
        File existing = write(output, "a.dat", "KEEP LINKED RESULT");
        Files.createLink(output.resolve("b.dat"), existing.toPath());
        assertRejected(List.of(a, b), output);
    }

    @Test
    void caseSpellingCollisionsAreRejectedOnEveryPlatform() throws IOException {
        File a = write(first, "map.dat", "LOWER");
        File b = write(second, "MAP.DAT", "UPPER");
        assertRejected(List.of(a, b), output);
    }

    @Test
    void canonicallyEquivalentUnicodeNamesCannotSelectOneOutput() throws IOException {
        File a = write(first, "r\u00edo.dat", "COMPOSED");
        File b = write(second, "ri\u0301o.dat", "DECOMPOSED");
        assertRejected(List.of(a, b), output);
    }

    @Test
    void callerListMutationDuringFirstCallbackCannotChangeTheValidatedPlan() throws IOException {
        File a = write(first, "a.dat", "A");
        File b = write(second, "b.dat", "B");
        List<File> inputs = new ArrayList<>(List.of(a, b));
        List<String> visited = new ArrayList<>();
        BatchProcessor.BatchResult result = BatchProcessor.processBatch(job(inputs, output),
                (in, out) -> {
                    inputs.clear();
                    visited.add(in.getName());
                    copy(in, out);
                });
        assertEquals(2, result.success());
        assertEquals(0, result.failed());
        assertEquals(List.of("a.dat", "b.dat"), visited);
        assertEquals("B", Files.readString(output.resolve("b.dat")));
    }

    @Test
    void missingOrNullOutputDirectoryIsNotCreatedByTheCorePreflight() throws IOException {
        File a = write(first, "a.dat", "A");
        Path missing = directory.resolve("not created");
        assertRejected(List.of(a), missing);
        assertFalse(Files.exists(missing));
        assertRejected(List.of(a), null);
    }

    @Test
    void directoryMasqueradingAsAFileTargetIsRejectedBeforeExecution() throws IOException {
        File a = write(first, "a.dat", "A");
        Files.createDirectory(output.resolve("a.dat"));
        assertRejected(List.of(a), output);
    }

    @Test
    void aMissingLateInputPreventsAllEarlierCallbacks() throws IOException {
        File a = write(first, "a.dat", "A");
        File missing = second.resolve("missing.dat").toFile();
        assertRejected(List.of(a, missing), output);
    }

    @Test
    void planIsImmutableReadOnlyAndKeepsEmptyInputCompatibility() throws IOException {
        File a = write(first, "a.dat", "A");
        Map<String, String> before = snapshot();
        List<BatchOutputPlan.Entry> plan = BatchOutputPlan.build(List.of(a), output.toFile());
        assertEquals(a.getAbsoluteFile(), plan.get(0).input());
        assertEquals(output.toRealPath().resolve("a.dat").toFile(), plan.get(0).output());
        assertThrows(UnsupportedOperationException.class, () -> plan.clear());
        assertEquals(before, snapshot());
        assertTrue(BatchOutputPlan.build(List.of(), null).isEmpty());
    }

    private void assertRejected(List<File> inputs, Path destination) throws IOException {
        Map<String, String> before = snapshot();
        AtomicInteger calls = new AtomicInteger();
        List<Integer> progress = new ArrayList<>();
        BatchProcessor.BatchResult result = BatchProcessor.processBatch(job(inputs, destination),
                (in, out) -> calls.incrementAndGet(), progress::add);
        assertEquals(0, calls.get(), "Invalid late entries must reject the entire plan.");
        assertTrue(progress.isEmpty(), "Nothing was processed; do not report processed progress.");
        assertEquals(0, result.success());
        assertEquals(inputs.size(), result.failed());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("rechazado antes de ejecutar"));
        assertEquals(before, snapshot(), "All original bytes and directory entries must survive.");
    }

    private static BatchProcessor.BatchJob job(List<File> inputs, Path destination) {
        return new BatchProcessor.BatchJob("safe batch", inputs,
                destination == null ? null : destination.toFile(), "copy");
    }

    private static File write(Path parent, String name, String text) throws IOException {
        return Files.writeString(parent.resolve(name), text).toFile();
    }

    private static void copy(File source, File target) {
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException failure) {
            throw new java.io.UncheckedIOException(failure);
        }
    }

    private Map<String, String> snapshot() throws IOException {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.toList()) {
                files.put(directory.relativize(path).toString(), Files.isDirectory(path)
                        ? "DIRECTORY" : Base64.getEncoder().encodeToString(Files.readAllBytes(path)));
            }
        }
        return files;
    }
}
