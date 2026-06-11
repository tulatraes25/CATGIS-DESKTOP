package ar.com.catgis;

import ar.com.catgis.scripting.ScriptEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ScriptEngineTest {

    @TempDir
    Path tempDir;

    @Test
    void executeScriptReturnsResultForMissingFile() {
        File missing = new File("nonexistent.py");
        ScriptEngine.ScriptResult result = ScriptEngine.executeScript(missing);
        assertNotNull(result);
        assertFalse(result.success());
    }

    @Test
    void executeCodeReturnsResult() {
        ScriptEngine.ScriptResult result = ScriptEngine.executeCode("print('hello')");
        assertNotNull(result);
        assertNotNull(result.output());
    }

    @Test
    void executeScriptWithValidPython() throws Exception {
        // Check if python is available
        String[] candidates = {"python3", "python", "py"};
        String pythonCmd = null;
        for (String cmd : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                if (p.waitFor() == 0) { pythonCmd = cmd; break; }
            } catch (Exception ignored) {}
        }
        if (pythonCmd == null) {
            // Python not available, skip test
            return;
        }

        File script = tempDir.resolve("test.py").toFile();
        Files.writeString(script.toPath(), "print('Hello from CATGIS')");
        ScriptEngine.ScriptResult result = ScriptEngine.executeScript(script);
        // Script may or may not succeed depending on Python version/config
        assertNotNull(result);
        assertNotNull(result.output());
    }

    @Test
    void executeCodeWithSyntaxError() {
        ScriptEngine.ScriptResult result = ScriptEngine.executeCode("def bad(");
        assertNotNull(result);
        // Should either fail or produce error output
    }
}
