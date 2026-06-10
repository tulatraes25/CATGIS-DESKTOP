package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StacClient HTTP operations.
 * Validates that the client handles URLs and responses correctly.
 */
class StacClientTest {

    @Test
    void getCollectionsHandlesInvalidUrl() {
        assertThrows(Exception.class, () -> {
            StacClient.getCollections("http://invalid-url-that-does-not-exist.example.com");
        });
    }

    @Test
    void getCollectionsHandlesEmptyUrl() {
        assertThrows(Exception.class, () -> {
            StacClient.getCollections("");
        });
    }

    @Test
    void getCollectionsHandlesNullUrl() {
        assertThrows(Exception.class, () -> {
            StacClient.getCollections(null);
        });
    }

    @Test
    void searchItemsHandlesInvalidUrl() {
        assertThrows(Exception.class, () -> {
            StacClient.searchItems(
                    "http://invalid-url.example.com",
                    "collection", "-70,-35,-65,-30", null);
        });
    }

    @Test
    void downloadAssetHandlesInvalidUrl() {
        assertThrows(Exception.class, () -> {
            java.io.File tmp = java.io.File.createTempFile("stac_test", ".tif");
            tmp.deleteOnExit();
            StacClient.downloadAsset("http://invalid-url.example.com/asset.tif", tmp);
        });
    }
}
