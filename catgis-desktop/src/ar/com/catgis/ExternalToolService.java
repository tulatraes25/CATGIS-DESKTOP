package ar.com.catgis;

import java.io.*;
import java.nio.file.*;
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
     * Execute a CLI command synchronously.
     */
    public static ToolResult execute(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(TOOLS_DIR.toFile());
            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = p.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return new ToolResult(false, output.toString(), "Timeout after " + DEFAULT_TIMEOUT_SECONDS + "s", -1);
            }

            int exitCode = p.exitValue();
            return new ToolResult(exitCode == 0, output.toString(), exitCode == 0 ? "" : output.toString(), exitCode);
        } catch (Exception e) {
            return new ToolResult(false, "", e.getMessage(), -1);
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
    private static String findWhiteboxTools() {
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String path = reader.readLine();
                reader.close();
                p.destroyForcibly();
                if (path != null && !path.isBlank()) {
                    File file = new File(path.trim());
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
            p.destroyForcibly();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Get the tools directory, creating it if needed.
     */
    public static Path getToolsDirectory() {
        try {
            Files.createDirectories(TOOLS_DIR);
        } catch (IOException ignored) {}
        return TOOLS_DIR;
    }
}
