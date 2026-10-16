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
            if (!delete(stdin) | !delete(stdout) | !delete(directory)) {
                problem = append(problem, "Temporary script I/O could not be completely removed.");
            }
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

    private static boolean delete(Path path) {
        if (path == null) {
            return true;
        }
        try {
            Files.deleteIfExists(path);
            return true;
        } catch (IOException | RuntimeException ex) {
            return false;
        }
    }
}
