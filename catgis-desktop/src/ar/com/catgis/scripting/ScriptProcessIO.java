package ar.com.catgis.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Owns the temporary I/O of one external script, never Swing or project state.
 * File-backed stdin supplies EOF even without input and avoids dual-pipe
 * deadlock. Output is monitored and read with a byte cap; this is not an OS
 * disk quota, child-process sandbox or hard bound on process startup latency.
 */
final class ScriptProcessIO {
    static final int MAX_INPUT_BYTES = 1024 * 1024;
    static final int MAX_OUTPUT_BYTES = 1024 * 1024;
    private static final long POLL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

    record Outcome(boolean success, String output, String error) {}

    private ScriptProcessIO() {}

    static Outcome run(ProcessBuilder builder, long timeoutSeconds, String input) {
        return run(builder, timeoutSeconds, input, null);
    }

    /** The optional parent is a filesystem test seam, never a shared filename. */
    static Outcome run(ProcessBuilder builder, long timeoutSeconds, String input, Path temporaryParent) {
        if (builder == null || timeoutSeconds <= 0) {
            return new Outcome(false, "", "Script timeout must be positive and a command is required.");
        }
        if (Thread.currentThread().isInterrupted()) {
            return new Outcome(false, "", "Script execution interrupted before startup.");
        }
        if (input != null && input.length() > MAX_INPUT_BYTES) {
            return new Outcome(false, "", "Script input exceeds the 1 MiB UTF-8 limit.");
        }
        byte[] bytes = input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_INPUT_BYTES) {
            return new Outcome(false, "", "Script input exceeds the 1 MiB UTF-8 limit.");
        }

        Path directory = null;
        Path stdin = null;
        Path stdout = null;
        Process process = null;
        int exitCode = -1;
        String problem = "";
        String output = "";
        boolean interrupted = false;
        try {
            directory = temporaryParent == null
                    ? Files.createTempDirectory("catgis-script-io-")
                    : Files.createTempDirectory(temporaryParent, "catgis-script-io-");
            stdin = directory.resolve("stdin.utf8");
            stdout = directory.resolve("stdout.utf8");
            Files.write(stdin, bytes);
            Files.createFile(stdout);
            builder.redirectInput(stdin.toFile());
            builder.redirectOutput(stdout.toFile());
            builder.redirectErrorStream(true);
            process = builder.start();
            long started = System.nanoTime();
            long budget = TimeUnit.SECONDS.toNanos(timeoutSeconds);
            while (true) {
                if (Files.size(stdout) > MAX_OUTPUT_BYTES) {
                    problem = "Script output exceeds the 1 MiB capture limit.";
                    break;
                }
                long remaining = budget - (System.nanoTime() - started);
                if (remaining <= 0) {
                    problem = "Script execution timed out after " + timeoutSeconds + " seconds.";
                    break;
                }
                if (process.waitFor(Math.min(remaining, POLL_NANOS), TimeUnit.NANOSECONDS)) {
                    exitCode = process.exitValue();
                    if (exitCode != 0) {
                        problem = "Exit code: " + exitCode;
                    }
                    break;
                }
            }
        } catch (InterruptedException ex) {
            interrupted = true;
            problem = "Script execution interrupted.";
        } catch (IOException | RuntimeException ex) {
            // Never embed script contents, environment values or temporary paths.
            problem = "Script process I/O failed (" + ex.getClass().getSimpleName() + ").";
        } finally {
            // Clear only temporarily so cleanup can wait, then restore the signal.
            interrupted |= Thread.interrupted();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                try {
                    if (!process.waitFor(2, TimeUnit.SECONDS)) {
                        problem = append(problem, "The direct script process did not terminate.");
                    }
                } catch (InterruptedException ex) {
                    interrupted = true;
                    problem = append(problem, "Script cleanup interrupted.");
                }
            }
            if (stdout != null && Files.isRegularFile(stdout)) {
                try (InputStream source = Files.newInputStream(stdout)) {
                    byte[] captured = source.readNBytes(MAX_OUTPUT_BYTES + 1);
                    if (captured.length > MAX_OUTPUT_BYTES) {
                        problem = append(problem, "Script output exceeds the 1 MiB capture limit.");
                    }
                    int length = Math.min(captured.length, MAX_OUTPUT_BYTES);
                    output = normalizeOutput(new String(captured, 0, length, StandardCharsets.UTF_8));
                } catch (IOException | RuntimeException ex) {
                    problem = append(problem, "Script output could not be read.");
                }
            }
            Cleanup cleanup = cleanupOwned(new Path[]{stdin, stdout, directory}, 2000,
                    path -> Files.deleteIfExists(path));
            if (!cleanup.success()) {
                problem = append(problem, cleanup.error());
            }
            interrupted |= Thread.interrupted();
            if (interrupted) {
                problem = append(problem, "Script execution interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        return new Outcome(exitCode == 0 && problem.isEmpty(), output, problem);
    }

    private static String normalizeOutput(String text) {
        // Retain the previous BufferedReader line-ending/trailing-newline API.
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.isEmpty() || normalized.endsWith("\n") ? normalized : normalized + "\n";
    }

    private static String append(String previous, String next) {
        return previous.isEmpty() ? next : previous + " " + next;
    }

    @FunctionalInterface
    interface DeleteOperation {
        void delete(Path path) throws IOException;
    }

    record Cleanup(boolean success, String error) {}

    /**
     * Retry only owned paths for a bounded interval. A terminated child does
     * not guarantee immediate delete availability on every filesystem. Never
     * retry the script itself, recursively remove unknown files, or hide a
     * persistent failure. The deletion seam permits deterministic fault tests.
     */
    static Cleanup cleanupOwned(Path[] paths, long waitMillis, DeleteOperation deletion) {
        boolean interrupted = Thread.interrupted();
        long started = System.nanoTime();
        long budget = TimeUnit.MILLISECONDS.toNanos(Math.max(0, waitMillis));
        boolean[] removed = new boolean[paths.length];
        try {
            while (true) {
                boolean complete = true;
                boolean retryable = true;
                java.util.Set<String> failures = new java.util.LinkedHashSet<>();
                for (int i = 0; i < paths.length; i++) {
                    if (removed[i] || paths[i] == null) {
                        continue;
                    }
                    try {
                        deletion.delete(paths[i]);
                        removed[i] = true;
                    } catch (java.nio.file.NoSuchFileException absent) {
                        removed[i] = true;
                    } catch (IOException failure) {
                        complete = false;
                        failures.add(failure.getClass().getSimpleName());
                    } catch (RuntimeException failure) {
                        complete = false;
                        retryable = false;
                        failures.add(failure.getClass().getSimpleName());
                    }
                }
                if (complete) {
                    return new Cleanup(true, "");
                }
                long remaining = budget - (System.nanoTime() - started);
                if (!retryable || remaining <= 0) {
                    return new Cleanup(false, "Temporary script I/O could not be completely removed ("
                            + String.join(", ", failures) + ").");
                }
                try {
                    TimeUnit.NANOSECONDS.sleep(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(25)));
                } catch (InterruptedException signal) {
                    interrupted = true;
                }
            }
        } finally {
            interrupted |= Thread.interrupted();
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
