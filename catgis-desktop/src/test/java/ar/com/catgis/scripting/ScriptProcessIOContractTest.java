package ar.com.catgis.scripting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Real JVM children and real Python; no test assumes absent Python is PASS. */
class ScriptProcessIOContractTest {
    @TempDir Path directory;
    private Path owned;

    @BeforeEach
    void createOwnedParent() throws Exception {
        owned = Files.createDirectory(directory.resolve("owned io with spaces"));
    }

    @AfterEach
    void noOwnedTemporaryFilesRemain() throws Exception {
        try (var paths = Files.list(owned)) {
            assertEquals(0, paths.count(), "Owned stdin/stdout staging must be removed.");
        }
    }

    @Test
    void nullInputSuppliesEofInsteadOfWaitingForever() throws Exception {
        var result = run("eof", null);
        assertTrue(result.success(), result.error());
        assertEquals("EOF:0\n", result.output());
    }

    @Test
    void emptyInputAlsoSuppliesEof() throws Exception {
        var result = run("eof", "");
        assertTrue(result.success(), result.error());
        assertEquals("EOF:0\n", result.output());
    }

    @Test
    void utf8InputAndOutputRoundtripWithoutPlatformDefaultCharset() throws Exception {
        String text = "r\u00edo \u00f1 \u6c34\n";
        var result = run("echo", text);
        assertTrue(result.success(), result.error());
        assertEquals(text, result.output());
    }

    @Test
    void childCanWriteBeyondPipeCapacityBeforeReadingLargeStdin() throws Exception {
        String input = "i".repeat(768 * 1024);
        var result = run("duplex", input);
        assertTrue(result.success(), result.error());
        assertEquals("p".repeat(256 * 1024) + "\nINPUT:" + input.length() + "\n", result.output());
    }

    @Test
    void nonzeroExitPreservesActualMergedStderrAndExitCode() throws Exception {
        var result = run("error", null);
        assertFalse(result.success());
        assertEquals("controlled failure\n", result.output());
        assertEquals("Exit code: 7", result.error());
    }

    @Test
    void outputRetainsPreviousLineNormalizationAndFinalLine() throws Exception {
        var result = run("lines", null);
        assertTrue(result.success(), result.error());
        assertEquals("a\nb\nc\nlast\n", result.output());
    }

    @Test
    void oversizedOutputCannotBecomeSuccessfulTruncatedOutput() throws Exception {
        var result = run("oversized", null);
        assertFalse(result.success());
        assertTrue(result.error().contains("capture limit"), result.error());
        assertTrue(result.output().length() <= ScriptProcessIO.MAX_OUTPUT_BYTES + 1);
    }

    @Test
    void silentSuccessfulScriptHasEmptyOutputAndNoError() throws Exception {
        var result = run("silent", null);
        assertTrue(result.success(), result.error());
        assertEquals("", result.output());
        assertEquals("", result.error());
    }

    @Test
    void deadlineStopsDirectProcessAndDoesNotInventPythonOutput() throws Exception {
        Path ready = directory.resolve("timeout ready.txt");
        var result = ScriptProcessIO.run(command("wait", ready.toString()), 2, null, owned);
        assertFalse(result.success());
        assertTrue(result.error().contains("timed out"), result.error());
        assertEquals("", result.output(), "Timeout diagnostics belong in error, not fabricated stdout.");
        assertExitedWhenStarted(ready);
    }

    @Test
    void interruptionIsRestoredAndDirectProcessIsTerminated() throws Exception {
        Path ready = directory.resolve("interrupt ready.txt");
        AtomicReference<ScriptProcessIO.Outcome> result = new AtomicReference<>();
        AtomicBoolean restored = new AtomicBoolean();
        ProcessBuilder command = command("wait", ready.toString());
        Thread worker = new Thread(() -> {
            result.set(ScriptProcessIO.run(command, 60, null, owned));
            restored.set(Thread.currentThread().isInterrupted());
        }, "script-interruption-contract");
        worker.setDaemon(true);
        worker.start();
        try {
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!Files.exists(ready) && worker.isAlive() && System.nanoTime() < until) {
                Thread.sleep(10);
            }
            assertTrue(Files.exists(ready), "The real child must signal startup before cancellation.");
        } finally {
            worker.interrupt();
            worker.join(10000);
        }
        assertFalse(worker.isAlive(), "The interrupted caller must return after cleanup.");
        assertNotNull(result.get());
        assertFalse(result.get().success());
        assertTrue(result.get().error().contains("interrupted"));
        assertTrue(restored.get(), "Caller interrupt must survive cleanup.");
        assertExitedWhenStarted(ready);
    }

    @Test
    void preInterruptedCallerDoesNotStartOrCreateStaging() throws Exception {
        Path marker = directory.resolve("must not exist.txt");
        try {
            Thread.currentThread().interrupt();
            var result = ScriptProcessIO.run(command("mark", marker.toString()), 10, null, owned);
            assertFalse(result.success());
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(Files.exists(marker));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void nonpositiveTimeoutsAreRejectedBeforeSideEffects() throws Exception {
        Path marker = directory.resolve("must not exist.txt");
        for (long timeout : new long[]{0, -1, Long.MIN_VALUE}) {
            var result = ScriptProcessIO.run(command("mark", marker.toString()), timeout, null, owned);
            assertFalse(result.success());
            assertTrue(result.error().contains("positive"));
        }
        assertFalse(Files.exists(marker));
    }

    @Test
    void oversizedInputIsRejectedBeforeProcessOrStaging() throws Exception {
        Path marker = directory.resolve("must not exist.txt");
        var result = ScriptProcessIO.run(command("mark", marker.toString()), 20,
                "x".repeat(ScriptProcessIO.MAX_INPUT_BYTES + 1), owned);
        assertFalse(result.success());
        assertTrue(result.error().contains("input exceeds"));
        assertFalse(Files.exists(marker));
    }

    @Test
    void utf8EncodedSizeNotOnlyCharacterCountControlsInputBudget() throws Exception {
        String input = "\u6c34".repeat(ScriptProcessIO.MAX_INPUT_BYTES / 3 + 1);
        assertTrue(input.length() < ScriptProcessIO.MAX_INPUT_BYTES);
        var result = run("eof", input);
        assertFalse(result.success());
        assertTrue(result.error().contains("input exceeds"));
    }

    @Test
    void exactInputByteLimitIsAccepted() throws Exception {
        var result = run("eof", "x".repeat(ScriptProcessIO.MAX_INPUT_BYTES));
        assertTrue(result.success(), result.error());
        assertEquals("EOF:" + ScriptProcessIO.MAX_INPUT_BYTES + "\n", result.output());
    }

    @Test
    void launchFailureIsExplicitAndRemovesItsTemporaryFiles() {
        var result = ScriptProcessIO.run(new ProcessBuilder(directory.resolve("absent-executable").toString()),
                10, "private test content", owned);
        assertFalse(result.success());
        assertEquals("", result.output());
        assertTrue(result.error().contains("I/O failed"));
        assertFalse(result.error().contains("private test content"));
        assertFalse(result.error().contains(directory.toString()));
    }

    @Test
    void parallelExecutionsDoNotShareInputOutputOrWorkingDirectory() throws Exception {
        var executor = Executors.newFixedThreadPool(4);
        try {
            List<java.util.concurrent.Future<ScriptProcessIO.Outcome>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                String input = "isolated-" + i;
                futures.add(executor.submit(() -> run("echo", input)));
            }
            for (int i = 0; i < futures.size(); i++) {
                var result = futures.get(i).get(60, TimeUnit.SECONDS);
                assertTrue(result.success(), result.error());
                assertEquals("isolated-" + i + "\n", result.output());
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void realPythonPublicApiReceivesEofWithNoInput() throws Exception {
        assertTrue(ScriptEngine.isPythonAvailable(), "This existing scripting CI contract requires Python.");
        Path script = directory.resolve("read eof.py");
        Files.writeString(script, "import sys\nprint('EOF:' + str(len(sys.stdin.read())))\n");
        byte[] original = Files.readAllBytes(script);
        var result = ScriptEngine.executeScript(script.toFile(), 30, null);
        assertTrue(result.success(), result.error());
        assertEquals("EOF:0\n", result.output());
        assertArrayEquals(original, Files.readAllBytes(script));
    }

    @Test
    void realPythonPublicApiLargeFullDuplexDoesNotDeadlock() throws Exception {
        Path script = directory.resolve("duplex.py");
        Files.writeString(script, "import sys\nsys.stdout.write('p' * 262144)\nsys.stdout.flush()\n"
                + "data = sys.stdin.read()\nprint('\\nINPUT:' + str(len(data)))\n");
        String input = "i".repeat(768 * 1024);
        var result = ScriptEngine.executeScript(script.toFile(), 30, input);
        assertTrue(result.success(), result.error());
        assertEquals("p".repeat(256 * 1024) + "\nINPUT:" + input.length() + "\n", result.output());
    }

    @Test
    void realPythonPublicApiUsesUtf8ForBothDirections() throws Exception {
        Path script = directory.resolve("unicode.py");
        Files.writeString(script, "import sys\nsys.stdout.write(sys.stdin.read())\n");
        String input = "r\u00edo \u00f1 \u6c34\n";
        var result = ScriptEngine.executeScript(script.toFile(), 30, input);
        assertTrue(result.success(), result.error());
        assertEquals(input, result.output());
    }

    @Test
    void temporaryDeletionFailureRetriesOwnedFilesAndActuallyRemovesThem() throws Exception {
        Path parent = Files.createDirectory(owned.resolve("controlled-lock"));
        Path input = Files.writeString(parent.resolve("input"), "bytes");
        Path output = Files.writeString(parent.resolve("output"), "result");
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        var cleanup = ScriptProcessIO.cleanupOwned(new Path[]{input, output, parent}, 2000, path -> {
            if (path.equals(input) && attempts.incrementAndGet() <= 2) {
                throw new java.nio.file.AccessDeniedException("synthetic handle lock");
            }
            Files.deleteIfExists(path);
        });
        assertTrue(cleanup.success(), cleanup.error());
        assertEquals("", cleanup.error());
        assertTrue(attempts.get() >= 3);
        assertFalse(Files.exists(parent));
    }

    @Test
    void permanentDeletionFailureStaysExplicitAndDoesNotLeakPrivatePaths() throws Exception {
        Path kept = Files.writeString(owned.resolve("keep"), "ORIGINAL");
        try {
            var cleanup = ScriptProcessIO.cleanupOwned(new Path[]{kept}, 0, path -> {
                throw new java.nio.file.AccessDeniedException("PRIVATE_PATH_TOKEN");
            });
            assertFalse(cleanup.success());
            assertTrue(cleanup.error().contains("AccessDeniedException"));
            assertFalse(cleanup.error().contains("PRIVATE_PATH_TOKEN"));
            assertEquals("ORIGINAL", Files.readString(kept));
        } finally {
            Files.deleteIfExists(kept);
        }
    }

    @Test
    void cleanupNeverRecursivelyDeletesAnUnknownDirectoryEntry() throws Exception {
        Path parent = Files.createDirectory(owned.resolve("unowned-entry"));
        Path unknown = Files.writeString(parent.resolve("do-not-delete"), "KEEP");
        try {
            var cleanup = ScriptProcessIO.cleanupOwned(new Path[]{parent}, 0, Files::deleteIfExists);
            assertFalse(cleanup.success());
            assertTrue(cleanup.error().contains("DirectoryNotEmptyException"));
            assertEquals("KEEP", Files.readString(unknown));
        } finally {
            Files.delete(unknown);
            Files.delete(parent);
        }
    }

    @Test
    void cleanupRestoresInterruptAndDoesNotPretendAnOpenFileWasRemoved() throws Exception {
        Path input = Files.writeString(owned.resolve("interrupted-cleanup"), "bytes");
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        try {
            var cleanup = ScriptProcessIO.cleanupOwned(new Path[]{input}, 2000, path -> {
                if (attempts.incrementAndGet() == 1) {
                    Thread.currentThread().interrupt();
                    throw new java.nio.file.AccessDeniedException("controlled lock");
                }
                Files.deleteIfExists(path);
            });
            assertTrue(cleanup.success(), cleanup.error());
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(Files.exists(input));
        } finally {
            Thread.interrupted();
            Files.deleteIfExists(input);
        }
    }

    @Test
    void cleanupAttemptsOtherOwnedFilesAfterOnePermanentFailure() throws Exception {
        Path locked = Files.writeString(owned.resolve("locked"), "KEEP");
        Path removable = Files.writeString(owned.resolve("removable"), "REMOVE");
        try {
            var cleanup = ScriptProcessIO.cleanupOwned(new Path[]{locked, null, removable}, 0, path -> {
                if (path.equals(locked)) {
                    throw new java.nio.file.AccessDeniedException("controlled failure");
                }
                Files.deleteIfExists(path);
            });
            assertFalse(cleanup.success());
            assertTrue(Files.exists(locked));
            assertFalse(Files.exists(removable));
        } finally {
            Files.deleteIfExists(locked);
            Files.deleteIfExists(removable);
        }
    }

    private ScriptProcessIO.Outcome run(String mode, String input) throws Exception {
        return ScriptProcessIO.run(command(mode), 30, input, owned);
    }

    private static ProcessBuilder command(String mode, String... extra) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin",
                File.separatorChar == '\\' ? "java.exe" : "java").toString();
        Path classes = Path.of(ScriptProcessFixture.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> args = new ArrayList<>(List.of(java, "-cp", classes.toString(),
                ScriptProcessFixture.class.getName(), mode));
        args.addAll(List.of(extra));
        return new ProcessBuilder(args);
    }

    private static void assertExitedWhenStarted(Path ready) throws Exception {
        if (Files.exists(ready)) {
            long pid = Long.parseLong(Files.readString(ready));
            assertFalse(ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false),
                    "The direct child must no longer be running.");
        }
    }
}

/** Minimal real subprocess; no test-library or CATGIS runtime dependency. */
class ScriptProcessFixture {
    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "eof" -> System.out.println("EOF:" + System.in.readAllBytes().length);
            case "echo" -> System.out.write(System.in.readAllBytes());
            case "duplex" -> {
                System.out.print("p".repeat(256 * 1024));
                System.out.flush();
                System.out.println("\nINPUT:" + System.in.readAllBytes().length);
            }
            case "error" -> {
                System.err.println("controlled failure");
                System.exit(7);
            }
            case "lines" -> System.out.write("a\r\nb\rc\nlast".getBytes(StandardCharsets.UTF_8));
            case "oversized" -> System.out.print("x".repeat(2 * 1024 * 1024));
            case "silent" -> { }
            case "wait" -> {
                Path ready = Path.of(args[1]);
                Path staging = ready.resolveSibling(ready.getFileName() + ".staging");
                Files.writeString(staging, Long.toString(ProcessHandle.current().pid()));
                // Existence now proves the complete PID was closed and published.
                // Never expose a created-but-empty marker to the cancelling parent.
                Files.move(staging, ready, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                Thread.sleep(60000);
            }
            case "mark" -> Files.writeString(Path.of(args[1]), "STARTED");
            default -> throw new IllegalArgumentException("Unknown fixture mode");
        }
    }
}
