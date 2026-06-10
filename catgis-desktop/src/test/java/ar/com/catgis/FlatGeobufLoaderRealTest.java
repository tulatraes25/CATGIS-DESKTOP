package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlatGeobufLoader tests.
 * Tests rejection of invalid inputs. Testing actual .fgb parsing requires
 * a real FlatGeobuf file, which is not included in the test suite.
 * These tests validate the loader's input validation logic.
 */
class FlatGeobufLoaderRealTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void loadNonExistentFileThrows() {
        assertThrows(Exception.class, () -> {
            FlatGeobufLoader.load(new File("/nonexistent/file.fgb"));
        });
    }

    @Test
    void loadNonFgbFileThrows() throws IOException {
        File fakeFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(fakeFile.toPath(), "not a flatgeobuf file");
        assertThrows(IllegalArgumentException.class, () -> {
            FlatGeobufLoader.load(fakeFile);
        });
    }

    @Test
    void loadEmptyFileThrows() throws IOException {
        File emptyFile = tempDir.resolve("empty.fgb").toFile();
        Files.writeString(emptyFile.toPath(), "");
        assertThrows(Exception.class, () -> {
            FlatGeobufLoader.load(emptyFile);
        });
    }

    @Test
    void loadNullFileThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            FlatGeobufLoader.load((File) null);
        });
    }

    @Test
    void loadStringPathDelegatesToFile() {
        assertThrows(Exception.class, () -> {
            FlatGeobufLoader.load("/nonexistent/file.fgb");
        });
    }
}
