package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WcsClient HTTP operations.
 * Validates that the client handles URLs and responses correctly.
 */
class WcsClientTest {

    @Test
    void getCoveragesHandlesInvalidUrl() {
        assertThrows(Exception.class, () -> {
            WcsClient.getCoverages("http://invalid-url-that-does-not-exist.example.com");
        });
    }

    @Test
    void getCoveragesHandlesEmptyUrl() {
        assertThrows(Exception.class, () -> {
            WcsClient.getCoverages("");
        });
    }

    @Test
    void getCoveragesHandlesNullUrl() {
        assertThrows(Exception.class, () -> {
            WcsClient.getCoverages(null);
        });
    }

    @Test
    void downloadCoverageHandlesInvalidUrl() {
        assertThrows(Exception.class, () -> {
            WcsClient.downloadCoverage(
                    "http://invalid-url.example.com",
                    "coverage", "-180,-90,180,90", "EPSG:4326", 100, 100);
        });
    }
}
