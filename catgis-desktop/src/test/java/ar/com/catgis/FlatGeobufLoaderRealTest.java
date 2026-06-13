package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FlatGeobufLoader.
 * Tests input validation and error handling.
 * Full round-trip testing requires real .fgb files.
 */
class FlatGeobufLoaderRealTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void loadNonExistentFileThrows() {
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load(new File("/nonexistent/file.fgb"));
        });
    }

    @Test
    void loadNonFgbFileThrows() throws IOException {
        File fakeFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(fakeFile.toPath(), "not a flatgeobuf file");
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load(fakeFile);
        });
    }

    @Test
    void loadEmptyFileThrows() throws IOException {
        File emptyFile = tempDir.resolve("empty.fgb").toFile();
        Files.writeString(emptyFile.toPath(), "");
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load(emptyFile);
        });
    }

    @Test
    void loadNullFileThrows() {
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load((File) null);
        });
    }

    @Test
    void loadStringPathDelegatesToFile() {
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load("/nonexistent/file.fgb");
        });
    }

    @Test
    void loadFileWithBadMagicThrows() throws IOException {
        File badFile = tempDir.resolve("bad.fgb").toFile();
        byte[] badMagic = new byte[]{0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        Files.write(badFile.toPath(), badMagic);
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load(badFile);
        });
    }

    @Test
    void loadFileWithCorrectMagicButInvalidHeaderThrows() throws IOException {
        // Valid magic "gbfg" but garbage header
        byte[] data = new byte[]{
                0x67, 0x62, 0x66, 0x67, // magic: "gbfg"
                0x10, 0x00, 0x00, 0x00, // header size: 16
                0x00, 0x00, 0x00, 0x00, // garbage header
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
        File invalidFile = tempDir.resolve("invalid_header.fgb").toFile();
        Files.write(invalidFile.toPath(), data);
        assertThrows(UnsupportedFormatException.class, () -> {
            FlatGeobufLoader.load(invalidFile);
        });
    }

    @Test
    void validateFileAcceptsNullGracefully() {
        ValidationResult vr = FlatGeobufLoader.validateFile(null);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("no especificado"));
    }

    @Test
    void validateFileRejectsNonFgbExtension() throws IOException {
        File fakeFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(fakeFile.toPath(), "not a flatgeobuf file");
        ValidationResult vr = FlatGeobufLoader.validateFile(fakeFile);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains(".fgb"));
    }

    @Test
    void validateFileRejectsTooSmall() throws IOException {
        File smallFile = tempDir.resolve("small.fgb").toFile();
        Files.write(smallFile.toPath(), new byte[]{0x00, 0x00, 0x00});
        ValidationResult vr = FlatGeobufLoader.validateFile(smallFile);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("pequeño"));
    }
}
