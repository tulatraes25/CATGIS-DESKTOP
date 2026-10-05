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
        assertTrue(result.output().contains("hello"), "Output should contain 'hello'");
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
        assertNotNull(result);
        assertNotNull(result.output());
        assertTrue(result.success(), "Script should succeed when Python is available");
        assertTrue(result.output().contains("Hello from CATGIS"), "Output should contain the printed text");
    }

    @Test
    void executeCodeWithSyntaxError() {
        ScriptEngine.ScriptResult result = ScriptEngine.executeCode("def bad(");
        assertNotNull(result);
        assertFalse(result.success(), "Syntax error should cause failure");
        assertFalse(result.output().isEmpty(), "Error output should be present");
    }
}
