package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpatiaLiteLoader input validation and error handling.
 * <p>
 * Covers validateFile(), isSqliteDatabase() header validation,
 * UnsupportedFormatException for invalid files, and edge cases.
 * Does NOT require a real SpatiaLite database — uses synthetic files.
 */
class SpatiaLiteLoaderTest {

    @TempDir
    Path tempDir;

    // ---- validateFile ----

    @Test
    void validateFileNull() {
        ValidationResult vr = SpatiaLiteLoader.validateFile(null);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("no especificado"));
    }

    @Test
    void validateFileNonExistent() {
        ValidationResult vr = SpatiaLiteLoader.validateFile(new File("/nonexistent/test.db"));
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("no existe"));
    }

    @Test
    void validateFileTooSmall() throws IOException {
        File f = tempDir.resolve("small.db").toFile();
        Files.write(f.toPath(), new byte[50]);

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("demasiado pequeno") || vr.message().contains("pequeño"));
    }

    @Test
    void validateFileNotSqlite() throws IOException {
        File f = tempDir.resolve("text.db").toFile();
        Files.writeString(f.toPath(), "not a SQLite database file at all, just random text content with enough bytes to pass the minimum size check");

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertFalse(vr.isValid());
        assertTrue(vr.message().contains("no es una base de datos SQLite"));
    }

    @Test
    void validateFileValidSqliteHeader() throws Exception {
        File f = File.createTempFile("spatialite_test_", ".db");
        f.deleteOnExit();
        byte[] data = new byte[200];
        byte[] header = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(header, 0, data, 0, header.length);

        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(data);
        }

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertTrue(vr.isValid());
        assertTrue(vr.message().contains("SQLite"));
    }

    @Test
    void validateFileEmptyFile() throws IOException {
        File f = tempDir.resolve("empty.db").toFile();
        Files.write(f.toPath(), new byte[0]);

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertFalse(vr.isValid());
    }

    // ---- listFeatureTypes ----

    @Test
    void listFeatureTypesNonExistentFile() {
        SpatiaLiteConnectionInfo info = new SpatiaLiteConnectionInfo();
        info.setFilePath("/nonexistent/spatial.db");

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.listFeatureTypes(info));
    }

    @Test
    void listFeatureTypesNotSqlite() throws IOException {
        File f = tempDir.resolve("notsqlite.db").toFile();
        Files.writeString(f.toPath(), "plain text, more than 100 bytes ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ...");

        SpatiaLiteConnectionInfo info = new SpatiaLiteConnectionInfo();
        info.setFilePath(f.getAbsolutePath());

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.listFeatureTypes(info));
    }

    // ---- loadLayerData ----

    @Test
    void loadLayerDataNullLayer() {
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.loadLayerData(null));
    }

    @Test
    void loadLayerDataNullTableName() {
        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", "/some/path.db");
        layer.setTableName(null);

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.loadLayerData(layer));
    }

    @Test
    void loadLayerDataBlankTableName() {
        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", "/some/path.db");
        layer.setTableName("   ");

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.loadLayerData(layer));
    }

    @Test
    void loadLayerDataNonExistentFile() {
        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", "/nonexistent/test.db");
        layer.setTableName("some_table");

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.loadLayerData(layer));
    }

    @Test
    void loadLayerDataNotSqlite() throws IOException {
        File f = tempDir.resolve("plain.db").toFile();
        Files.writeString(f.toPath(), "this is just a text file with more than 100 bytes of content, not a valid SQLite database header at all, just filler text to pass minimum size checks");

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", f.getAbsolutePath());
        layer.setTableName("test");

        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.loadLayerData(layer));
    }

    // ---- isSqliteDatabase (indirectly via validateFile) ----

    @Test
    void isSqliteDatabaseIncompleteHeader() throws IOException {
        File f = tempDir.resolve("short.db").toFile();
        byte[] data = new byte[8]; // less than 16 bytes header
        data[0] = 'S';
        data[1] = 'Q';
        data[2] = 'L';
        Files.write(f.toPath(), data);

        // File is > 100 bytes? No — 8 bytes. validateFile rejects at size check.
        // But if we pad it:
        byte[] padded = new byte[101];
        System.arraycopy(data, 0, padded, 0, data.length);
        Files.write(f.toPath(), padded);

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertFalse(vr.isValid());
    }

    @Test
    void isSqliteDatabaseWrongHeader() throws IOException {
        File f = tempDir.resolve("wrong.db").toFile();
        byte[] data = new byte[200];
        byte[] badHeader = "NotASQLiteFile!  ".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(badHeader, 0, data, 0, Math.min(badHeader.length, 16));

        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(data);
        }

        ValidationResult vr = SpatiaLiteLoader.validateFile(f);
        assertFalse(vr.isValid());
    }

    // ---- ValidationResult ----

    @Test
    void validationResultValid() {
        ValidationResult vr = ValidationResult.valid("todo ok");
        assertTrue(vr.isValid());
        assertEquals("todo ok", vr.message());
    }

    @Test
    void validationResultInvalid() {
        ValidationResult vr = ValidationResult.invalid("error grave");
        assertFalse(vr.isValid());
        assertEquals("error grave", vr.message());
    }
}
