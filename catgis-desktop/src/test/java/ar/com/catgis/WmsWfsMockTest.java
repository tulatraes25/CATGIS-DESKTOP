package ar.com.catgis;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for WMS/WFS GetCapabilities parsing against a local mock HTTP server.
 * <p>
 * Requires zero internet access. Uses {@code com.sun.net.httpserver.HttpServer}
 * to serve XML fixture files from {@code src/test/resources/}.
 */
class WmsWfsMockTest {

    private HttpServer server;
    private int port;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
        server.setExecutor(null); // default executor
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    // ---- WMS ----

    @Test
    void parsesWmsCapabilities() throws Exception {
        serveFile("/wms", "wms-capabilities.xml", 200, "text/xml");

        WmsCapabilities caps = WmsCapabilitiesService.fetchCapabilities(baseUrl + "/wms");
        assertNotNull(caps);
        assertEquals("CATGIS Test WMS Service", caps.getServiceTitle());
        assertEquals("1.3.0", caps.getVersion());
        assertFalse(caps.getFormats().isEmpty(), "expected image formats");
        assertTrue(caps.getFormats().contains("image/png"));

        assertEquals(1, caps.getLayers().size());
        WmsLayerInfo layer = caps.getLayers().get(0);
        assertEquals("test_layer", layer.getName());
        assertEquals("Test Layer", layer.getTitle());
    }

    @Test
    void handlesWmsHttp500() throws Exception {
        serveText("/wms", "Internal Server Error", 500, "text/plain");

        assertThrows(IllegalArgumentException.class, () ->
                WmsCapabilitiesService.fetchCapabilities(baseUrl + "/wms"));
    }

    @Test
    void handlesWmsInvalidXml() throws Exception {
        serveText("/wms", "<html><body>Not XML</body></html>", 200, "text/html");

        assertThrows(IllegalArgumentException.class, () ->
                WmsCapabilitiesService.fetchCapabilities(baseUrl + "/wms"));
    }

    // ---- WFS ----

    @Test
    void parsesWfsCapabilities() throws Exception {
        serveFile("/wfs", "wfs-capabilities.xml", 200, "text/xml");

        WfsCapabilities caps = WfsCapabilitiesService.fetchCapabilities(baseUrl + "/wfs");
        assertNotNull(caps);
        assertEquals("CATGIS Test WFS Service", caps.getServiceTitle());

        List<WfsFeatureTypeInfo> types = caps.getFeatureTypes();
        assertNotNull(types);
        assertEquals(2, types.size());

        WfsFeatureTypeInfo ft0 = types.get(0);
        assertEquals("test:pois", ft0.getName());
        assertEquals("Points of Interest", ft0.getTitle());
        assertEquals("EPSG:4326", ft0.getDefaultCrs());

        WfsFeatureTypeInfo ft1 = types.get(1);
        assertEquals("test:roads", ft1.getName());
        assertEquals("EPSG:4326", ft1.getDefaultCrs());
    }

    @Test
    void handlesWfsHttp500() throws Exception {
        serveText("/wfs", "Internal Server Error", 500, "text/plain");

        assertThrows(IllegalArgumentException.class, () ->
                WfsCapabilitiesService.fetchCapabilities(baseUrl + "/wfs"));
    }

    @Test
    void handlesWfsInvalidXml() throws Exception {
        serveText("/wfs", "<html><body>Not XML</body></html>", 200, "text/html");

        assertThrows(IllegalArgumentException.class, () ->
                WfsCapabilitiesService.fetchCapabilities(baseUrl + "/wfs"));
    }

    // ---- Server helpers ----

    private void serveFile(String path, String resource, int status, String contentType) {
        server.createContext(path, exchange -> {
            byte[] bytes = Files.readAllBytes(Path.of("src/test/resources/" + resource));
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    private void serveText(String path, String text, int status, String contentType) {
        server.createContext(path, exchange -> {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }
}
