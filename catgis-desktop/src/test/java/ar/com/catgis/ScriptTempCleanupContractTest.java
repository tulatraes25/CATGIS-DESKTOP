package ar.com.catgis;

import ar.com.catgis.scripting.ScriptEngine;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#132: generated Python sources are cleaned up immediately, not at JVM exit. */
class ScriptTempCleanupContractTest {

    @Test
    void executeCodeDoesNotLeakGeneratedScripts() throws Exception {
        Path temp = Path.of(System.getProperty("java.io.tmpdir"));
        Set<String> before = generatedScripts(temp);
        ScriptEngine.ScriptResult result = ScriptEngine.executeCode("print('cleanup')", 5);
        assertNotNull(result);
        Set<String> leaked = generatedScripts(temp);
        leaked.removeAll(before);
        assertTrue(leaked.isEmpty(), "executeCode leaked temp scripts: " + leaked);
    }

    private static Set<String> generatedScripts(Path directory) throws IOException {
        Set<String> names = new HashSet<>();
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(directory, "catgis_script_*.py")) {
            for (Path path : stream) {
                names.add(path.getFileName().toString());
            }
        }
        return names;
    }
}
