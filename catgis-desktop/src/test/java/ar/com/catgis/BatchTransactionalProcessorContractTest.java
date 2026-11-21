package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#114: opt-in all-files batch transaction with staging and rollback. */
class BatchTransactionalProcessorContractTest {

    @TempDir
    Path directory;

    @Test
    void successfulBatchPublishesEveryOutputAndCleansStaging() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(
                write(inputDir.resolve("a.txt"), "A"),
                write(inputDir.resolve("b.txt"), "B"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);

        List<Integer> progress = new java.util.ArrayList<>();
        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.writeString(output.toPath(),
                                Files.readString(input.toPath()), StandardCharsets.UTF_8);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, progress::add);

        assertEquals(2, result.success());
        assertEquals(0, result.failed());
        assertEquals("A", Files.readString(outputDir.resolve("a.txt")));
        assertEquals("B", Files.readString(outputDir.resolve("b.txt")));
        assertEquals(List.of(50, 100), progress);
        assertNoStaging(outputDir);
        assertEquals("A", Files.readString(inputDir.resolve("a.txt")),
                "inputs must never be modified");
    }

    @Test
    void operationFailurePublishesNothingAndPreservesExistingOutputs() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(
                write(inputDir.resolve("a.txt"), "A"),
                write(inputDir.resolve("b.txt"), "B"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);
        Files.writeString(outputDir.resolve("b.txt"), "OLD-B", StandardCharsets.UTF_8);

        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.writeString(output.toPath(), "NEW", StandardCharsets.UTF_8);
                        if (input.getName().equals("b.txt")) {
                            throw new IllegalStateException("fallo simulado");
                        }
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, null);

        assertEquals(0, result.success());
        assertEquals(2, result.failed());
        assertEquals("OLD-A", Files.readString(outputDir.resolve("a.txt")));
        assertEquals("OLD-B", Files.readString(outputDir.resolve("b.txt")));
        assertNoStaging(outputDir);
    }

    @Test
    void publishFailureRollsBackPreviouslyPublishedFiles() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(
                write(inputDir.resolve("a.txt"), "A"),
                write(inputDir.resolve("b.txt"), "B"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);
        Files.writeString(outputDir.resolve("b.txt"), "OLD-B", StandardCharsets.UTF_8);
        int[] published = {0};

        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.copy(input.toPath(), output.toPath());
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, null, () -> false,
                (staged, target) -> {
                    if (++published[0] == 2) {
                        throw new IOException("fallo de publicacion simulado");
                    }
                    Files.move(staged, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                });

        assertEquals(0, result.success());
        assertEquals(2, result.failed());
        assertEquals("OLD-A", Files.readString(outputDir.resolve("a.txt")),
                "first output must be rolled back to its previous bytes");
        assertEquals("OLD-B", Files.readString(outputDir.resolve("b.txt")));
        assertNoStaging(outputDir);
    }

    @Test
    void rejectedPlanNeverInvokesOperations() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        List<File> inputs = List.of(
                write(inputDir.resolve("same.txt"), "A"),
                write(Files.createDirectory(inputDir.resolve("nested")).resolve("same.txt"), "B"));
        int[] calls = {0};
        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, inputDir.toFile(), "copy"),
                (input, output) -> calls[0]++, null);
        assertEquals(0, calls[0]);
        assertEquals(2, result.failed());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void sidecarFamilyIsPublishedWithThePrimaryOutput() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(write(inputDir.resolve("a.txt"), "A"));

        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.writeString(output.toPath(), "NEW", StandardCharsets.UTF_8);
                        Files.writeString(output.toPath().resolveSibling(
                                output.getName() + ".aux.xml"), "SIDECAR",
                                StandardCharsets.UTF_8);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, null);

        assertEquals(1, result.success());
        assertEquals("NEW", Files.readString(outputDir.resolve("a.txt")));
        assertEquals("SIDECAR", Files.readString(outputDir.resolve("a.txt.aux.xml")));
        assertNoStaging(outputDir);
    }

    @Test
    void sidecarPublishFailureRollsBackPrimaryAndPreviousSidecar() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(write(inputDir.resolve("a.txt"), "A"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);
        Files.writeString(outputDir.resolve("a.txt.aux.xml"), "OLD-SIDECAR",
                StandardCharsets.UTF_8);

        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.writeString(output.toPath(), "NEW", StandardCharsets.UTF_8);
                        Files.writeString(output.toPath().resolveSibling(
                                output.getName() + ".aux.xml"), "NEW-SIDECAR",
                                StandardCharsets.UTF_8);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, null, () -> false,
                (staged, target) -> {
                    if (target.getFileName().toString().endsWith(".aux.xml")) {
                        throw new IOException("fallo de publicacion de sidecar simulado");
                    }
                    Files.move(staged, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                });

        assertEquals(0, result.success());
        assertEquals("OLD-A", Files.readString(outputDir.resolve("a.txt")));
        assertEquals("OLD-SIDECAR", Files.readString(outputDir.resolve("a.txt.aux.xml")));
        assertNoStaging(outputDir);
    }

    @Test
    void cancellationPublishesNothingAndPreservesExistingOutputs() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(
                write(inputDir.resolve("a.txt"), "A"),
                write(inputDir.resolve("b.txt"), "B"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);
        java.util.concurrent.atomic.AtomicInteger checks =
                new java.util.concurrent.atomic.AtomicInteger();
        BatchProcessor.BatchResult result = BatchTransactionalProcessor.process(
                new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                (input, output) -> {
                    try {
                        Files.copy(input.toPath(), output.toPath());
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }, null, () -> checks.incrementAndGet() > 1);

        assertEquals(0, result.success());
        assertEquals(2, result.failed());
        assertTrue(result.errors().get(0).toLowerCase(java.util.Locale.ROOT).contains("cancel"),
                result.errors().toString());
        assertEquals("OLD-A", Files.readString(outputDir.resolve("a.txt")));
        assertFalse(Files.exists(outputDir.resolve("b.txt")));
        assertNoStaging(outputDir);
    }

    @Test
    void interruptedThreadCancelsBeforePublication() throws Exception {
        Path inputDir = Files.createDirectory(directory.resolve("in"));
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        List<File> inputs = List.of(write(inputDir.resolve("a.txt"), "A"));
        Files.writeString(outputDir.resolve("a.txt"), "OLD-A", StandardCharsets.UTF_8);
        BatchProcessor.BatchResult result;
        try {
            result = BatchTransactionalProcessor.process(
                    new BatchProcessor.BatchJob("tx", inputs, outputDir.toFile(), "copy"),
                    (input, output) -> {
                        try {
                            Files.copy(input.toPath(), output.toPath());
                            Thread.currentThread().interrupt();
                        } catch (IOException io) {
                            throw new RuntimeException(io);
                        }
                    }, null);
        } finally {
            Thread.interrupted();
        }
        assertEquals(0, result.success());
        assertEquals("OLD-A", Files.readString(outputDir.resolve("a.txt")));
        assertNoStaging(outputDir);
    }

    @Test
    void interruptedBackupIsRestoredAndStagingCleanedOnRecovery() throws Exception {
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        Path staging = Files.createDirectory(outputDir.resolve(".catgis-batch-tx-crash"));
        Path target = outputDir.resolve("a.txt");
        Path backup = staging.resolve("backup-0-a.txt");
        Files.writeString(target, "NEW-PARTIAL", StandardCharsets.UTF_8);
        Files.writeString(backup, "OLD", StandardCharsets.UTF_8);
        Files.writeString(staging.resolve("0-a.txt"), "STAGED", StandardCharsets.UTF_8);
        Path journal = outputDir.resolve(".catgis-batch-tx-journal-fixed");
        Files.writeString(journal,
                "CATGIS_BATCH_TX|1|" + staging.toAbsolutePath().normalize() + "\n"
                        + "B|" + target.toAbsolutePath().normalize() + "|"
                        + backup.toAbsolutePath().normalize() + "\n"
                        + "P|" + target.toAbsolutePath().normalize() + "\n",
                StandardCharsets.UTF_8);

        assertEquals(1, BatchTransactionalProcessor.recoverInterruptedTransactions(
                outputDir.toFile()));
        assertEquals("OLD", Files.readString(target));
        assertFalse(Files.exists(staging), "staging must be deleted");
        assertFalse(Files.exists(journal), "journal must be deleted");
    }

    @Test
    void interruptedNewOutputIsRemovedOnRecovery() throws Exception {
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        Path staging = Files.createDirectory(outputDir.resolve(".catgis-batch-tx-new"));
        Path target = outputDir.resolve("new.txt");
        Files.writeString(target, "NEW-PARTIAL", StandardCharsets.UTF_8);
        Path journal = outputDir.resolve(".catgis-batch-tx-journal-new");
        Files.writeString(journal,
                "CATGIS_BATCH_TX|1|" + staging.toAbsolutePath().normalize() + "\n"
                        + "B|" + target.toAbsolutePath().normalize() + "|-\n",
                StandardCharsets.UTF_8);

        assertEquals(1, BatchTransactionalProcessor.recoverInterruptedTransactions(
                outputDir.toFile()));
        assertFalse(Files.exists(target));
        assertFalse(Files.exists(staging));
        assertFalse(Files.exists(journal));
    }

    @Test
    void journalPointingOutsideOutputIsRejectedWithoutTouchingVictims() throws Exception {
        Path outputDir = Files.createDirectory(directory.resolve("out"));
        Path other = Files.createDirectory(directory.resolve("other"));
        Path victim = other.resolve("victim.txt");
        Files.writeString(victim, "SAFE", StandardCharsets.UTF_8);
        Path journal = outputDir.resolve(".catgis-batch-tx-journal-evil");
        Files.writeString(journal,
                "CATGIS_BATCH_TX|1|" + other.toAbsolutePath().normalize() + "\n"
                        + "B|" + victim.toAbsolutePath().normalize() + "|-\n",
                StandardCharsets.UTF_8);

        assertThrows(IOException.class,
                () -> BatchTransactionalProcessor.recoverInterruptedTransactions(
                        outputDir.toFile()));
        assertEquals("SAFE", Files.readString(victim));
        assertTrue(Files.exists(journal), "invalid journal must not be silently deleted");
    }

    private static File write(Path path, String text) throws IOException {
        Files.writeString(path, text, StandardCharsets.UTF_8);
        return path.toFile();
    }

    private static void assertNoStaging(Path outputDir) throws IOException {
        try (var entries = Files.list(outputDir)) {
            assertTrue(entries.noneMatch(path -> path.getFileName().toString()
                    .startsWith(".catgis-batch-tx-")), "staging directories must be removed");
        }
    }
}
