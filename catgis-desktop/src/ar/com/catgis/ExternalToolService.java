package ar.com.catgis;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing external CLI geoprocessing tools.
 * Supports WhiteboxTools, GRASS GIS, SAGA, and any command-line tool.
 */
public final class ExternalToolService {

    private static final Path TOOLS_DIR = Path.of(System.getProperty("user.home"), ".catgis", "tools");
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int MAX_OUTPUT_CHARS = 1_048_576;

    private ExternalToolService() {}

    public record ToolResult(boolean success, String output, String error, int exitCode) {}

    /**
     * Check if a tool is available on the system.
     */
    public static boolean isToolAvailable(String toolName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("where", toolName);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean found = p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
            p.destroyForcibly();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Execute a CLI command synchronously from a worker (never Swing EDT).
     * Drain output concurrently so the timeout is not bypassed by a process
     * that never closes its stdout stream.
     */
    public static ToolResult execute(String... command) {
        Process process = null;
        try {
            Files.createDirectories(TOOLS_DIR);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(TOOLS_DIR.toFile());
            process = pb.start();
            return collectProcess(process, TimeUnit.SECONDS.toMillis(DEFAULT_TIMEOUT_SECONDS));
        } catch (Exception failure) {
            return new ToolResult(false, "", failure.getMessage(), -1);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Package-private process boundary. A controlled fake Process makes timeout
     * and stdout-draining behavior testable without launching system binaries.
     */
    static ToolResult collectProcess(Process process, long timeoutMillis) {
        OutputDrain output = new OutputDrain(process.getInputStream());
        Thread drainThread = new Thread(output, "catgis-tool-output-drain");
        drainThread.setDaemon(true);
        drainThread.start();

        try {
            if (!process.waitFor(Math.max(1L, timeoutMillis), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                drainThread.join(1000L);
                return new ToolResult(false, output.snapshot(),
                        "Timeout after " + timeoutMillis + " ms", -1);
            }

            drainThread.join(1000L);
            String captured = output.snapshot();
            int exitCode = process.exitValue();
            return new ToolResult(exitCode == 0, captured,
                    exitCode == 0 ? "" : captured, exitCode);
        } catch (InterruptedException interrupted) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            return new ToolResult(false, output.snapshot(), "External tool interrupted", -1);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static final class OutputDrain implements Runnable {
        private final InputStream stream;
        private final StringBuilder collected = new StringBuilder();
        private boolean truncated;

        private OutputDrain(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                char[] chunk = new char[4096];
                int count;
                while ((count = reader.read(chunk)) != -1) {
                    synchronized (this) {
                        int remaining = MAX_OUTPUT_CHARS - collected.length();
                        if (remaining > 0) {
                            collected.append(chunk, 0, Math.min(remaining, count));
                        }
                        if (count > remaining) {
                            truncated = true;
                        }
                    }
                }
            } catch (IOException ignored) {
                // A timed-out process is killed and its stdout can close abruptly.
            }
        }

        private synchronized String snapshot() {
            return collected.toString() + (truncated ? "\\n[tool output truncated]" : "");
        }
    }

    /**
     * Execute a command asynchronously.
     */
    public static CompletableFuture<ToolResult> executeAsync(String... command) {
        return CompletableFuture.supplyAsync(() -> execute(command));
    }

    /**
     * Execute a WhiteboxTools command.
     */
    public static ToolResult executeWhitebox(String toolName, String... args) {
        String wbtPath = findWhiteboxTools();
        if (wbtPath == null) {
            return new ToolResult(false, "", "WhiteboxTools not found. Download from https://www.whiteboxgeo.com/download-whiteboxtools/", -1);
        }

        List<String> command = new ArrayList<>();
        command.add(wbtPath);
        command.add("--run=" + toolName);
        for (String arg : args) {
            command.add(arg);
        }

        return execute(command.toArray(new String[0]));
    }

    /**
     * Find the WhiteboxTools executable.
     * Returns an absolute path or null. Never returns a bare command name
     * that would be resolved via PATH at execution time.
     */
    static String findWhiteboxTools() {
        // 1. Check TOOLS_DIR for absolute paths
        String[] localCandidates = {
            TOOLS_DIR.resolve("whitebox_tools.exe").toString(),
            TOOLS_DIR.resolve("whitebox_tools").toString(),
            TOOLS_DIR.resolve("wbt.exe").toString()
        };
        for (String candidate : localCandidates) {
            if (new File(candidate).exists()) {
                return candidate;
            }
        }

        // 2. Resolve via 'where', returning absolute path only if file exists
        for (String exeName : new String[]{"whitebox_tools", "wbt"}) {
            String resolved = resolveOnPath(exeName);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    private static String resolveOnPath(String exeName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("where", exeName);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String path = reader.readLine();
                    if (path != null && !path.isBlank()) {
                        File file = new File(path.trim());
                        if (file.exists()) {
                            return file.getAbsolutePath();
                        }
                    }
                }
            }
            p.destroyForcibly();
        } catch (Exception ignored) { CatgisLogger.warn("ExternalToolService: operation failed", ignored); }
        return null;
    }

    /**
     * Get the tools directory, creating it if needed.
     */
    public static Path getToolsDirectory() {
        try {
            Files.createDirectories(TOOLS_DIR);
        } catch (Exception ignored) { CatgisLogger.warn("ExternalToolService: operation failed", ignored); }
        return TOOLS_DIR;
    }
}
