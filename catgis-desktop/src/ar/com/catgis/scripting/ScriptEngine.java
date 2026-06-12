package ar.com.catgis.scripting;

import ar.com.catgis.CatgisLogger;

import ar.com.catgis.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Python scripting engine for CATGIS with timeout and venv support.
 * <p>
 * Executes Python scripts in an external Python 3.x process with:
 * <ul>
 *   <li>Configurable execution timeout</li>
 *   <li>Virtual environment auto-detection</li>
 *   <li>Standard input piping</li>
 *   <li>CATGIS context injection via environment variables</li>
 * </ul>
 * </p>
 */
public final class ScriptEngine {

    private ScriptEngine() {}

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static String cachedPythonPath;

    /**
     * Execute a Python script file with default timeout.
     */
    public static ScriptResult executeScript(File scriptFile) {
        return executeScript(scriptFile, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Execute a Python script file with a custom timeout.
     */
    public static ScriptResult executeScript(File scriptFile, long timeoutSeconds) {
        return executeScript(scriptFile, timeoutSeconds, null);
    }

    /**
     * Execute a Python script file with timeout and optional stdin input.
     */
    public static ScriptResult executeScript(File scriptFile, long timeoutSeconds, String stdinInput) {
        if (!scriptFile.exists()) {
            return new ScriptResult(false, "", "Script file not found: " + scriptFile.getAbsolutePath());
        }

        try {
            String python = resolvePython();
            if (python == null) {
                return new ScriptResult(false, "", "Python not found. Install Python 3.x and add to PATH.");
            }

            ProcessBuilder pb = new ProcessBuilder(python, scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.directory(scriptFile.getParentFile());

            // Inject CATGIS context
            injectCatgisEnvironment(pb.environment());

            Process process = pb.start();

            // Write stdin if provided
            if (stdinInput != null && !stdinInput.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdinInput.getBytes(StandardCharsets.UTF_8));
                }
            }

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            // Read output in a separate thread to avoid deadlock
            Thread reader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception ignored) { CatgisLogger.warn("ScriptEngine: operation failed", ignored); }
            });
            reader.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                reader.interrupt();
                return new ScriptResult(false, output.toString(),
                        "Script execution timed out after " + timeoutSeconds + " seconds.");
            }

            reader.join(2000); // Wait for reader to finish
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                error.append("Exit code: ").append(exitCode);
            }

            return new ScriptResult(exitCode == 0, output.toString(), error.toString());

        } catch (Exception ex) {
            return new ScriptResult(false, "", ex.getMessage());
        }
    }

    /**
     * Execute Python code string with default timeout.
     */
    public static ScriptResult executeCode(String code) {
        return executeCode(code, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Execute Python code string with timeout.
     */
    public static ScriptResult executeCode(String code, long timeoutSeconds) {
        try {
            File tempFile = File.createTempFile("catgis_script_", ".py");
            tempFile.deleteOnExit();

            // Write script with CATGIS bootstrap
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                pw.println("# -*- coding: utf-8 -*-");
                pw.println("# Auto-generated CATGIS script");
                pw.println("import os, sys, json");
                pw.println();
                pw.println("# CATGIS context from environment");
                pw.println("CATGIS_PROJECT = os.environ.get('CATGIS_PROJECT', '')");
                pw.println("CATGIS_PROJECT_FILE = os.environ.get('CATGIS_PROJECT_FILE', '')");
                pw.println("CATGIS_LAYERS = os.environ.get('CATGIS_LAYERS', '')");
                pw.println();
                pw.println(code);
            }

            return executeScript(tempFile, timeoutSeconds);
        } catch (Exception ex) {
            return new ScriptResult(false, "", ex.getMessage());
        }
    }

    /**
     * Check if Python is available, re-resolving if previously not found.
     */
    public static boolean isPythonAvailable() {
        if (cachedPythonPath == null) {
            resolvePython();
        }
        return cachedPythonPath != null;
    }

    /**
     * Force re-detection of Python path on next call.
     */
    public static void refreshPythonPath() {
        cachedPythonPath = null;
    }

    /**
     * Get the resolved Python path.
     */
    public static String getPythonPath() {
        return resolvePython();
    }

    // --- Private helpers ---

    /**
     * Resolve Python executable, checking PATH and common virtualenv locations.
     */
    private static String resolvePython() {
        if (cachedPythonPath != null) return cachedPythonPath;

        // Check custom CATGIS_PYTHON_PATH
        String customPath = System.getenv("CATGIS_PYTHON_PATH");
        if (customPath != null && new File(customPath).exists()) {
            cachedPythonPath = customPath;
            return cachedPythonPath;
        }

        // Check common virtualenv / venv locations relative to working dir
        String[] venvDirs = {".venv", "venv", ".env", "env"};
        for (String venv : venvDirs) {
            File venvPython = new File(venv + File.separator + "Scripts" + File.separator + "python.exe");
            if (venvPython.exists()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(venvPython.getAbsolutePath(), "--version");
                    Process p = pb.start();
                    if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) {
                        cachedPythonPath = venvPython.getAbsolutePath();
                        return cachedPythonPath;
                    }
                } catch (Exception ignored) { CatgisLogger.warn("ScriptEngine: operation failed", ignored); }
            }
        }

        // Check PATH
        String[] candidates = {"python3", "python", "py"};
        for (String cmd : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) {
                    cachedPythonPath = cmd;
                    return cachedPythonPath;
                }
            } catch (Exception ignored) { CatgisLogger.warn("ScriptEngine: operation failed", ignored); }
        }

        return null;
    }

    private static void injectCatgisEnvironment(Map<String, String> env) {
        try {
            if (AppContext.project() != null) {
                env.put("CATGIS_PROJECT", AppContext.project().getName());
                if (AppContext.project().getProjectFile() != null) {
                    env.put("CATGIS_PROJECT_FILE",
                            AppContext.project().getProjectFile().getAbsolutePath());
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("ScriptEngine: operation failed", ignored); }
    }

    /**
     * Script execution result.
     */
    public record ScriptResult(boolean success, String output, String error) {
        /** Get the first non-empty line of output, useful for quick results. */
        public String firstOutputLine() {
            if (output == null || output.isBlank()) return "";
            return output.lines().filter(l -> !l.isBlank()).findFirst().orElse("");
        }
    }
}
