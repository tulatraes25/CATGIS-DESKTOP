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
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            return false;
        }
        if (toolName == null || toolName.isBlank()) {
            return false;
        }
        String name = toolName.trim().toLowerCase(java.util.Locale.ROOT);
        if ("whitebox_tools".equals(name) || "wbt".equals(name)
                || "whitebox_tools.exe".equals(name) || "wbt.exe".equals(name)) {
            return findWhiteboxTools() != null;
        }
        // Availability checks are intentionally limited to reviewed optional
        // GIS commands and trusted install directories, never an arbitrary PATH.
        return WhiteboxExecutableResolver.isOtherApprovedToolPresent(name);
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
        if (toolName == null || !toolName.matches("[A-Za-z][A-Za-z0-9_]{0,79}")) {
            return new ToolResult(false, "",
                    "Identificador WhiteboxTools invalido; operacion cancelada.", -1);
        }
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            return new ToolResult(false, "",
                    "La herramienta debe ejecutarse fuera del hilo de interfaz.", -1);
        }
        String wbtPath = findWhiteboxTools();
        if (wbtPath == null) {
            return new ToolResult(false, "",
                    "WhiteboxTools no disponible. Instala la herramienta o configura "
                            + "CATGIS_WHITEBOX_BIN con una ruta absoluta.", -1);
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
        // Filesystem probes are I/O; callers use ToolAvailabilityProbe to
        // keep them off the Swing event dispatch thread.
        return javax.swing.SwingUtilities.isEventDispatchThread()
                ? null : WhiteboxExecutableResolver.resolve();
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
