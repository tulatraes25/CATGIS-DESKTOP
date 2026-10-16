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
        if (scriptFile == null || !scriptFile.isFile()) {
            return new ScriptResult(false, "", "Script file not found or not a regular file.");
        }
        if (timeoutSeconds <= 0 || Thread.currentThread().isInterrupted()) {
            return new ScriptResult(false, "", "Script execution interrupted or timeout is not positive.");
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

            // Standard I/O is explicitly UTF-8 on Windows and Unix alike.
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            ScriptProcessIO.Outcome result = ScriptProcessIO.run(pb, timeoutSeconds, stdinInput);
            return new ScriptResult(result.success(), result.output(), result.error());

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

        // Check common virtualenv / venv locations relative to working dir.
        // Windows stores Python under Scripts/, while Unix-like systems use bin/.
        String[] venvDirs = {".venv", "venv", ".env", "env"};
        String[] venvExecutables = File.separatorChar == '\\'
                ? new String[]{"Scripts" + File.separator + "python.exe"}
                : new String[]{"bin" + File.separator + "python3", "bin" + File.separator + "python"};
        for (String venv : venvDirs) {
            for (String executable : venvExecutables) {
                File venvPython = new File(venv, executable);
                if (isUsablePython(venvPython)) {
                    cachedPythonPath = venvPython.getAbsolutePath();
                    return cachedPythonPath;
                }
            }
        }

        // Check PATH — resolve to absolute path to avoid bare-name execution
        String[] candidates = {"python3", "python", "py"};
        for (String cmd : candidates) {
            try {
                String resolved = resolveOnPath(cmd);
                if (resolved != null) {
                    cachedPythonPath = resolved;
                    return cachedPythonPath;
                }
            } catch (Exception ignored) { CatgisLogger.warn("ScriptEngine: operation failed", ignored); }
        }

        return null;
    }

    private static String resolveOnPath(String cmd) {
        String pathValue = System.getenv("PATH");
        if (pathValue == null || pathValue.isBlank()) {
            return null;
        }

        boolean windows = File.separatorChar == '\\';
        String[] executableNames = windows && !cmd.toLowerCase().endsWith(".exe")
                ? new String[]{cmd + ".exe", cmd}
                : new String[]{cmd};

        for (String rawDirectory : pathValue.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            String directory = rawDirectory != null ? rawDirectory.trim() : "";
            if (directory.length() >= 2 && directory.startsWith("\"") && directory.endsWith("\"")) {
                directory = directory.substring(1, directory.length() - 1);
            }
            if (directory.isBlank()) {
                continue;
            }

            for (String executableName : executableNames) {
                File candidate = new File(directory, executableName);
                if (candidate.isFile() && (windows || candidate.canExecute())) {
                    return candidate.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static boolean isUsablePython(File candidate) {
        if (candidate == null || !candidate.isFile()) {
            return false;
        }
        if (File.separatorChar != '\\' && !candidate.canExecute()) {
            return false;
        }

        Process process = null;
        try {
            process = new ProcessBuilder(candidate.getAbsolutePath(), "--version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception ex) {
            CatgisLogger.warn("ScriptEngine: Python candidate failed", ex);
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
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
