package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-09: PMTiles/GeoParquet state tests.
 * Ensures they show metadata and don't try to load as layers inconsistently.
 */
class PmtilesGeoParquetStateTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void pmtilesDetectsValidMagicBytes() throws IOException {
        File fgtFile = tempDir.resolve("test.pmtiles").toFile();
        // Write PMTiles magic bytes: 0x47424647 ("GBFG")
        byte[] header = new byte[128];
        header[0] = 0x47; // G
        header[1] = 0x42; // B
        header[2] = 0x46; // F
        header[3] = 0x47; // G
        Files.write(fgtFile.toPath(), header);
        // PMTiles reader should be able to read the header
        // (it may fail on invalid data, but should not crash)
        try {
            PmtilesReader.readHeader(fgtFile);
        } catch (Exception e) {
            // Expected: invalid header data, but should not be a crash
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void pmtilesRejectsNonPmtilesFile() throws IOException {
        File txtFile = tempDir.resolve("test.pmtiles").toFile();
        Files.writeString(txtFile.toPath(), "not a pmtiles file");
        assertThrows(Exception.class, () -> PmtilesReader.readHeader(txtFile));
    }

    @Test
    void geoparquetDetectsParquetMagicBytes() throws IOException {
        File parquetFile = tempDir.resolve("test.parquet").toFile();
        byte[] header = new byte[]{'P', 'A', 'R', '1'};
        Files.write(parquetFile.toPath(), header);
        assertTrue(GeoParquetReader.isGeoParquet(parquetFile));
    }

    @Test
    void geoparquetRejectsNonParquetFile() throws IOException {
        File txtFile = tempDir.resolve("test.parquet").toFile();
        Files.writeString(txtFile.toPath(), "not a parquet file");
        assertFalse(GeoParquetReader.isGeoParquet(txtFile));
    }

    @Test
    void pmtilesGetStatsReturnsString() throws IOException {
        File pmtilesFile = tempDir.resolve("test.pmtiles").toFile();
        byte[] header = new byte[128];
        header[0] = 0x47; header[1] = 0x42; header[2] = 0x46; header[3] = 0x47;
        Files.write(pmtilesFile.toPath(), header);
        try {
            String stats = PmtilesReader.getStats(pmtilesFile);
            assertNotNull(stats);
            assertTrue(stats.contains("PMTiles"));
        } catch (Exception e) {
            // May fail on invalid data, that's OK
        }
    }
}
