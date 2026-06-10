package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PMTiles/GeoParquet state tests.
 * Tests validation logic and error handling.
 * Testing actual PMTiles parsing requires a real .pmtiles file
 * which is not included in the test suite.
 */
class PmtilesGeoParquetStateTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void pmtilesRejectsNonPmtilesFile() throws IOException {
        File txtFile = tempDir.resolve("test.pmtiles").toFile();
        Files.writeString(txtFile.toPath(), "not a pmtiles file");
        assertThrows(Exception.class, () -> PmtilesReader.readHeader(txtFile));
    }

    @Test
    void pmtilesRejectsTooSmallFile() throws IOException {
        File tinyFile = tempDir.resolve("tiny.pmtiles").toFile();
        Files.write(tinyFile.toPath(), new byte[]{0x50, 0x4D}); // Only 2 bytes
        assertThrows(Exception.class, () -> PmtilesReader.readHeader(tinyFile));
    }

    @Test
    void pmtilesReadHeaderReturnsHeaderRecord() throws Exception {
        // Create a minimal valid header with correct magic bytes
        File pmtilesFile = tempDir.resolve("test.pmtiles").toFile();
        byte[] header = new byte[128];
        // Magic: little-endian 0x4D50 = bytes [0x50, 0x4D]
        header[0] = 0x50;
        header[1] = 0x4D;
        // Set version to 3, numTiles to 5, zoomMin to 1, zoomMax to 10
        java.nio.ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .putInt(4, 3)    // version
            .putInt(12, 5)   // numTiles
            .putInt(16, 1)   // zoomMin
            .putInt(20, 10); // zoomMax
        Files.write(pmtilesFile.toPath(), header);

        var h = PmtilesReader.readHeader(pmtilesFile);
        assertNotNull(h);
        assertEquals(3, h.version());
        assertEquals(5, h.numTiles());
        assertEquals(1, h.zoomMin());
        assertEquals(10, h.zoomMax());
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
    void geoparquetGetSummaryReturnsString() throws Exception {
        File parquetFile = tempDir.resolve("test.parquet").toFile();
        // Write PAR1 magic + enough data to pass size check
        byte[] data = new byte[1024];
        data[0] = 'P'; data[1] = 'A'; data[2] = 'R'; data[3] = '1';
        Files.write(parquetFile.toPath(), data);
        String summary = GeoParquetReader.getSummary(parquetFile);
        assertNotNull(summary);
        assertTrue(summary.contains("GeoParquet"));
    }
}
