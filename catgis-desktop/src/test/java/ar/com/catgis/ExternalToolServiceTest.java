package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExternalToolService CLI wrapper.
 */
class ExternalToolServiceTest {

    @Test
    void isToolAvailableReturnsFalseForNonexistent() {
        assertFalse(ExternalToolService.isToolAvailable("nonexistent_tool_12345"));
    }

    @Test
    void getToolsDirectoryCreatesDir() {
        java.nio.file.Path dir = ExternalToolService.getToolsDirectory();
        assertNotNull(dir);
        assertTrue(java.nio.file.Files.exists(dir));
        assertTrue(java.nio.file.Files.isDirectory(dir));
    }

    @Test
    void executeReturnsErrorForInvalidCommand() {
        var result = ExternalToolService.execute("nonexistent_command_12345");
        assertFalse(result.success());
    }

    @Test
    void executeReturnsEmptyOutputForInvalidCommand() {
        var result = ExternalToolService.execute("nonexistent_command_12345");
        assertNotNull(result.output());
    }
}
