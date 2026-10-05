package ar.com.catgis.integration;

import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.StacClient;
import ar.com.catgis.WcsClient;
import ar.com.catgis.data.raster.LocalRasterData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OnlineServicesIntegrationTest {

    private HttpServer server;

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void wcsCapabilitiesAndCoverageDownloadWorkAgainstLocalMock() throws Exception {
        byte[] rasterBytes = Files.readAllBytes(IntegrationFixtureFactory.ensureDemFixture());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/wcs", exchange -> handleWcs(exchange, rasterBytes));
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/wcs";
        List<WcsClient.WcsCoverage> coverages = WcsClient.getCoverages(baseUrl);

        assertFalse(coverages.isEmpty());
        assertEquals("test_dem", coverages.get(0).name());

        byte[] downloaded = WcsClient.downloadCoverage(baseUrl, "test_dem", "-58.6,-34.8,-58.2,-34.4", "EPSG:4326", 256, 256);
        assertNotNull(downloaded);
        assertTrue(downloaded.length > 0);

        Path out = IntegrationFixtureFactory.outputsDir().resolve("wcs-mock-download.tif");
        Files.write(out, downloaded);
        LocalRasterData data = RasterImageLoader.loadReal(out.toFile(), "EPSG:4326", "EPSG:4326");
        assertNotNull(data);
        assertEquals("EPSG:4326", data.getDisplayCRS());
        assertTrue(data.getWidth() > 0);
        assertTrue(data.getHeight() > 0);
    }

    @Test
    void stacCollectionsSearchAndAssetDownloadWorkAgainstLocalMock() throws Exception {
        byte[] rasterBytes = Files.readAllBytes(IntegrationFixtureFactory.ensureDemFixture());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/stac/collections", exchange -> respondJson(exchange, """
                {
                  "collections":[
                    {
                      "id":"demo-collection",
                      "title":"Demo Collection",
                      "description":"Synthetic STAC collection",
                      "license":"CC-BY-4.0",
                      "extent":{"spatial":{"bbox":[[-58.6,-34.8,-58.2,-34.4]]}}
                    }
                  ],
                  "links":[]
                }
                """));
        server.createContext("/stac/search", exchange -> {
            String href = "http://localhost:" + server.getAddress().getPort() + "/stac/assets/test_dem.tif";
            String body = "{\n" +
                    "  \"type\":\"FeatureCollection\",\n" +
                    "  \"features\":[\n" +
                    "    {\n" +
                    "      \"id\":\"item-001\",\n" +
                    "      \"collection\":\"demo-collection\",\n" +
                    "      \"properties\":{\"datetime\":\"2026-06-15T00:00:00Z\"},\n" +
                    "      \"assets\":{\"data\":{\"href\":\"" + href + "\"}}\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"context\":{\"matched\":1},\n" +
                    "  \"links\":[]\n" +
                    "}";
            respondJson(exchange, body);
        });
        server.createContext("/stac/assets/test_dem.tif", exchange -> respondBinary(exchange, "image/tiff", rasterBytes));
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/stac";
        List<StacClient.StacCollection> collections = StacClient.getCollections(baseUrl);
        assertEquals(1, collections.size());
        assertEquals("demo-collection", collections.get(0).id());

        List<StacClient.StacItem> items = StacClient.searchItems(baseUrl, "demo-collection", "-58.6,-34.8,-58.2,-34.4", "2026-06-01/2026-06-30");
        assertEquals(1, items.size());
        assertEquals("item-001", items.get(0).id());
        assertEquals(1, items.get(0).assetUrls().size());

        Path out = IntegrationFixtureFactory.outputsDir().resolve("stac-mock-download.tif");
        StacClient.downloadAsset(items.get(0).assetUrls().get(0), out.toFile());
        assertTrue(Files.exists(out));
        LocalRasterData data = RasterImageLoader.loadReal(out.toFile(), "EPSG:4326", "EPSG:4326");
        assertNotNull(data);
        assertTrue(data.getWidth() > 0);
        assertTrue(data.getHeight() > 0);
    }

    private static void handleWcs(HttpExchange exchange, byte[] rasterBytes) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        if (query != null && query.toUpperCase().contains("GETCAPABILITIES")) {
            respondXml(exchange, """
                    <WCS_Capabilities version="1.0.0">
                      <ContentMetadata>
                        <CoverageOffering>
                          <name>test_dem</name>
                          <label>Test DEM</label>
                          <description>Synthetic DEM</description>
                          <Format>image/tiff</Format>
                        </CoverageOffering>
                      </ContentMetadata>
                    </WCS_Capabilities>
                    """);
            return;
        }
        if (query != null && query.toUpperCase().contains("GETCOVERAGE")) {
            respondBinary(exchange, "image/tiff", rasterBytes);
            return;
        }
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    private static void respondJson(HttpExchange exchange, String body) throws IOException {
        respondText(exchange, "application/json", body);
    }

    private static void respondXml(HttpExchange exchange, String body) throws IOException {
        respondText(exchange, "text/xml", body);
    }

    private static void respondText(HttpExchange exchange, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void respondBinary(HttpExchange exchange, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
