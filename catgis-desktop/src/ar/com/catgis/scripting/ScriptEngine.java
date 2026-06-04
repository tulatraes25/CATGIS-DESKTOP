package ar.com.catgis.scripting;

import ar.com.catgis.*;
import ar.com.catgis.layout.LayoutModel;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Basic Python scripting engine for CATGIS.
 * Executes Python scripts that can interact with the GIS.
 */
public final class ScriptEngine {

    private ScriptEngine() {}

    /**
     * Execute a Python script file.
     */
    public static ScriptResult executeScript(File scriptFile) {
        if (!scriptFile.exists()) {
            return new ScriptResult(false, "Script file not found: " + scriptFile.getAbsolutePath(), "");
        }

        try {
            String python = findPython();
            if (python == null) {
                return new ScriptResult(false, "Python not found. Install Python 3.x and add to PATH.", "");
            }

            ProcessBuilder pb = new ProcessBuilder(python, scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.directory(scriptFile.getParentFile());

            // Set environment variables for CATGIS
            Map<String, String> env = pb.environment();
            env.put("CATGIS_PROJECT", CatgisDesktopApp.currentProject != null ?
                    CatgisDesktopApp.currentProject.getName() : "");
            env.put("CATGIS_PROJECT_FILE", CatgisDesktopApp.currentProject != null &&
                    CatgisDesktopApp.currentProject.getProjectFile() != null ?
                    CatgisDesktopApp.currentProject.getProjectFile().getAbsolutePath() : "");

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return new ScriptResult(exitCode == 0, output.toString(), exitCode != 0 ? "Exit code: " + exitCode : "");

        } catch (Exception ex) {
            return new ScriptResult(false, "", ex.getMessage());
        }
    }

    /**
     * Execute a Python script string.
     */
    public static ScriptResult executeCode(String code) {
        try {
            File tempFile = File.createTempFile("catgis_script_", ".py");
            tempFile.deleteOnExit();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                pw.println("# Auto-generated CATGIS script");
                pw.println("import sys");
                pw.println("sys.path.insert(0, r'" + System.getProperty("java.class.path") + "')");
                pw.println();
                pw.println(code);
            }
            return executeScript(tempFile);
        } catch (Exception ex) {
            return new ScriptResult(false, "", ex.getMessage());
        }
    }

    /**
     * Find Python executable on PATH.
     */
    private static String findPython() {
        String[] candidates = {"python3", "python", "py"};
        for (String cmd : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                int exit = p.waitFor();
                if (exit == 0) return cmd;
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Script execution result.
     */
    public record ScriptResult(boolean success, String output, String error) {}
}
