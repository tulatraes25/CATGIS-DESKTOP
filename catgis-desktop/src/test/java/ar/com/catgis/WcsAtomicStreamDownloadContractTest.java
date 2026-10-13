package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WcsAtomicStreamDownloadContractTest {

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String origin;

    @AfterEach
    void close() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void streamsMultiMegabyteCoverageAndAtomicallyReplacesOldFile() throws Exception {
        openServer();
        byte[] payload = new byte[3 * 1024 * 1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i % 251);
        }
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "image/tiff", payload);
        });
        server.start();

        Path destination = tempDir.resolve("existing-coverage.tif");
        Files.writeString(destination, "old content", StandardCharsets.UTF_8);

        WcsClient.downloadCoverageToFile(origin, "dem",
                "-70,-46,-69,-45", "EPSG:4326", 100, 100, destination.toFile());

        assertArrayEquals(payload, Files.readAllBytes(destination));
        assertEquals(1, requests.get());
        assertNoStagingFile();
    }

    @Test
    void fallsBackToWcs10AndKeepsOnlyTheCompletedRaster() throws Exception {
        openServer();
        byte[] payload = new byte[]{1, 2, 3, 4, 5};
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 503, "text/plain", new byte[0]);
            } else {
                respond(exchange, 200, "application/octet-stream", payload);
            }
        });
        server.start();

        Path destination = tempDir.resolve("fallback.tif");
        WcsClient.downloadCoverageToFile(origin, "dem",
                "-70,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile());

        assertArrayEquals(payload, Files.readAllBytes(destination));
        assertEquals(2, requests.get());
        assertNoStagingFile();
    }

    @Test
    void failedVersionsPreserveThePreviousOutputAndCleanTemporaryFiles() throws Exception {
        openServer();
        server.createContext("/wcs", exchange ->
                respond(exchange, 503, "text/plain", new byte[0]));
        server.start();

        Path destination = tempDir.resolve("valuable-coverage.tif");
        byte[] original = "previous valid raster".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);

        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));

        assertArrayEquals(original, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void xmlErrorResponseCannotBeSilentlySavedAsRaster() throws Exception {
        openServer();
        server.createContext("/wcs", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 200, "application/xml",
                        "<ExceptionReport>Invalid coverage</ExceptionReport>"
                                .getBytes(StandardCharsets.UTF_8));
            } else {
                respond(exchange, 503, "text/plain", new byte[0]);
            }
        });
        server.start();

        Path destination = tempDir.resolve("existing-image.tif");
        Files.writeString(destination, "keep me", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));

        assertEquals("keep me", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }


    @Test
    void mislabeledXmlResponseMustNotOverwriteRaster() throws Exception {
        openServer();
        server.createContext("/wcs", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 200, "application/octet-stream",
                        "  <?xml version=\"1.0\"?><ExceptionReport>Denied</ExceptionReport>"
                                .getBytes(StandardCharsets.UTF_8));
            } else {
                respond(exchange, 503, "text/plain", new byte[0]);
            }
        });
        server.start();
        Path destination = tempDir.resolve("previous-coverage.tif");
        Files.writeString(destination, "unchanged", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("unchanged", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void mislabeledJsonResponseMustNotOverwriteRaster() throws Exception {
        openServer();
        server.createContext("/wcs", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 200, "image/tiff",
                        "{\"error\":\"no coverage\"}".getBytes(StandardCharsets.UTF_8));
            } else {
                respond(exchange, 503, "text/plain", new byte[0]);
            }
        });
        server.start();
        Path destination = tempDir.resolve("previous-json.tif");
        Files.writeString(destination, "unchanged", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("unchanged", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }


    @Test
    void utf8BomXmlDisguisedAsTiffCannotOverwriteRaster() throws Exception {
        openServer();
        server.createContext("/wcs", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 200, "image/tiff",
                        ("\uFEFF<?xml version=\"1.0\"?><ExceptionReport>Denied</ExceptionReport>")
                                .getBytes(StandardCharsets.UTF_8));
            } else {
                respond(exchange, 503, "text/plain", new byte[0]);
            }
        });
        server.start();
        Path destination = tempDir.resolve("bom-protected.tif");
        Files.writeString(destination, "previous good data", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("previous good data", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }


    @Test
    void serviceExceptionReportWithOctetStreamCannotReplaceExistingCoverage() throws Exception {
        openServer();
        server.createContext("/wcs", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                respond(exchange, 200, "application/octet-stream",
                        "<ServiceExceptionReport><ServiceException>Denied</ServiceException></ServiceExceptionReport>"
                                .getBytes(StandardCharsets.UTF_8));
            } else {
                respond(exchange, 503, "text/plain", new byte[0]);
            }
        });
        server.start();
        Path destination = tempDir.resolve("service-exception-protected.tif");
        Files.writeString(destination, "previous coverage", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("previous coverage", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    private void openServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        origin = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
    }

    private void assertNoStagingFile() throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(0L, files.filter(p ->
                    p.getFileName().toString().startsWith(".catgis-wcs-")).count());
        }
    }

    private static void respond(HttpExchange exchange, int code,
                                String contentType, byte[] payload) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        if (code != 200) {
            exchange.sendResponseHeaders(code, -1L);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(200, payload.length);
        try (var output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }
}
