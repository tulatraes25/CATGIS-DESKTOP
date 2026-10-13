package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        byte[] payload = realGeoTiff(1024, 1024);
        assertTrue(payload.length > 2 * 1024 * 1024,
                "The generated raster must exercise streaming with multi-megabyte data.");
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "image/tiff", payload);
        });
        server.start();

        Path destination = tempDir.resolve("existing-coverage.tif");
        Files.writeString(destination, "old content", StandardCharsets.UTF_8);

        WcsClient.downloadCoverageToFile(origin, "dem",
                "-70,-46,-69,-45", "EPSG:4326", 1024, 1024, destination.toFile());

        assertArrayEquals(payload, Files.readAllBytes(destination));
        assertEquals(1, requests.get());
        assertNoStagingFile();
    }

    @Test
    void fallsBackToWcs10AndKeepsOnlyTheCompletedRaster() throws Exception {
        openServer();
        byte[] payload = realGeoTiff(16, 16);
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
        GridCoverage2D decoded = RasterCoverageSupport.readCoverage(destination.toFile());
        assertEquals("EPSG:4326", RasterCoverageSupport.resolveCoverageCrsCode(decoded, null));
        assertEquals(16, decoded.getRenderedImage().getWidth());
        assertEquals(16, decoded.getRenderedImage().getHeight());
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

    @Test
    void randomBinaryAdvertisedAsTiffCannotOverwriteExistingCoverage() throws Exception {
        openServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "image/tiff", new byte[]{1, 2, 3, 4, 5});
        });
        server.start();
        Path destination = tempDir.resolve("protected-raster.tif");
        Files.writeString(destination, "original valid file", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("original valid file", Files.readString(destination, StandardCharsets.UTF_8));
        assertEquals(2, requests.get());
        assertNoStagingFile();
    }

    @Test
    void tiffHeaderWithoutImageDirectoryCannotBePublished() throws Exception {
        openServer();
        server.createContext("/wcs", exchange ->
                respond(exchange, 200, "image/tiff",
                        new byte[]{73, 73, 42, 0, 0, 0, 0, 0}));
        server.start();
        Path destination = tempDir.resolve("truncated-header.tif");
        Files.writeString(destination, "original coverage", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("original coverage", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void realTiffWithWrongDimensionsCannotReplaceOldFile() throws Exception {
        byte[] mismatched = realGeoTiff(8, 8);
        openServer();
        server.createContext("/wcs", exchange ->
                respond(exchange, 200, "image/tiff", mismatched));
        server.start();
        Path destination = tempDir.resolve("wrong-grid.tif");
        Files.writeString(destination, "keep previous", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, destination.toFile()));
        assertEquals("keep previous", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void explicitPngFormatDoesNotRequireTiffSignature() throws Exception {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        image.setRGB(4, 5, Color.CYAN.getRGB());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        byte[] png = output.toByteArray();
        openServer();
        server.createContext("/wcs", exchange ->
                respond(exchange, 200, "image/png", png));
        server.start();
        Path destination = tempDir.resolve("explicit-png.png");
        WcsClient.downloadCoverageToFile(origin, "dem", null, null,
                16, 16, "image/png", destination.toFile());
        assertArrayEquals(png, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    private byte[] realGeoTiff(int width, int height) throws Exception {
        Path fixture = tempDir.resolve("fixture-" + width + "x" + height + ".tif");
        BandedSampleModel sampleModel =
                new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        long seed = 0x1234ABCDL;
        for (int i = 0; i < values.length; i++) {
            seed = (seed * 1664525L + 1013904223L) & 0xffffffffL;
            values[i] = (seed >>> 8) / 16777216.0f;
        }
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -70.0, -69.0, -46.0, -45.0,
                CRSDefinitions.decode("EPSG:4326", true));
        GridCoverage2D coverage = new GridCoverageFactory().create(
                "wcs-contract-fixture", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(fixture.toFile());
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }
        return Files.readAllBytes(fixture);
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
