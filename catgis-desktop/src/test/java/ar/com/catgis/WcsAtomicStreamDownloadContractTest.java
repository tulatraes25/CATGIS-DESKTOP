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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
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
        // GeoTools can keep the returned RenderedImage file-backed on Windows;
        // validate the staged TIFF using the production metadata-only reader.
        // It closes its ImageInputStream before JUnit removes the temp directory.
        WcsClient.validateStagedTiff(destination, 16, 16);
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
    void malformedV2TiffMustFallBackToGenuineV1CoverageWithoutStaleBytes() throws Exception {
        openServer();
        byte[] genuine = realGeoTiff(16, 16);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                // Valid TIFF magic, but no usable image directory; also longer
                // than fallback payload to prove the same staging file truncates.
                byte[] corrupt = new byte[genuine.length + 4096];
                corrupt[0] = 'I';
                corrupt[1] = 'I';
                corrupt[2] = 42;
                respond(exchange, 200, "image/tiff", corrupt);
            } else {
                respond(exchange, 200, "image/tiff", genuine);
            }
        });
        server.start();

        Path destination = tempDir.resolve("v1-after-invalid-v2.tif");
        Files.writeString(destination, "old coverage", StandardCharsets.UTF_8);
        WcsClient.downloadCoverageToFile(origin, "dem", "-70,-46,-69,-45",
                "EPSG:4326", 16, 16, destination.toFile());
        assertEquals(2, requests.get());
        assertArrayEquals(genuine, Files.readAllBytes(destination));
        WcsClient.validateStagedTiff(destination, 16, 16);
        assertNoStagingFile();
    }

    @Test
    void defaultedZeroGridCannotSilentlyAcceptWrongSizedGeoTiff() throws Exception {
        byte[] mismatched = realGeoTiff(16, 16);
        openServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            // URL requests 512x512 after default normalization; an unsolicited
            // 16x16 TIFF must not bypass validation because caller supplied 0.
            respond(exchange, 200, "image/tiff", mismatched);
        });
        server.start();

        Path destination = tempDir.resolve("default-grid-protected.tif");
        Files.writeString(destination, "previous raster", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 0, 0, destination.toFile()));
        assertEquals(2, requests.get());
        assertEquals("previous raster", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void geoserverTifAliasMustNotBypassTiffSignatureValidation() throws Exception {
        openServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "application/octet-stream",
                    new byte[]{9, 8, 7, 6, 5, 4, 3, 2});
        });
        server.start();

        Path destination = tempDir.resolve("alias-protected.tif");
        Files.writeString(destination, "old raster", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", null, null, 16, 16, "tif", destination.toFile()));
        assertEquals(2, requests.get(), "Both WCS versions must fail closed.");
        assertEquals("old raster", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void geoserverGeotiffAliasPublishesRealCoverage() throws Exception {
        byte[] genuine = realGeoTiff(16, 16);
        openServer();
        server.createContext("/wcs", exchange -> respond(exchange, 200, "image/tiff", genuine));
        server.start();

        Path destination = tempDir.resolve("geoserver-format-geotiff.tif");
        WcsClient.downloadCoverageToFile(origin, "dem", null, null,
                16, 16, "geotiff", destination.toFile());
        assertArrayEquals(genuine, Files.readAllBytes(destination));
        WcsClient.validateStagedTiff(destination, 16, 16);
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

    @Test
    void wrongGeoTiffEnvelopeMustNotReplaceValidRasterEvenWithCorrectPixelGrid() throws Exception {
        byte[] otherPlace = realGeoTiff(16, 16,
                -65.0, -64.0, -46.0, -45.0, "EPSG:4326");
        openServer();
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            attempts.incrementAndGet();
            respond(exchange, 200, "image/tiff", otherPlace);
        });
        server.start();

        Path destination = tempDir.resolve("protected-wrong-geo-envelope.tif");
        Files.writeString(destination, "previous scientific coverage", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile()));
        assertEquals(2, attempts.get(), "Both WCS versions must reject an unrelated extent.");
        assertEquals("previous scientific coverage",
                Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void wrongEmbeddedGeoTiffCrsMustNotReplaceExistingRaster() throws Exception {
        byte[] wrongCrs = realGeoTiff(16, 16,
                -70.0, -69.0, -46.0, -45.0, "EPSG:3857");
        openServer();
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            attempts.incrementAndGet();
            respond(exchange, 200, "application/octet-stream", wrongCrs);
        });
        server.start();

        Path destination = tempDir.resolve("protected-wrong-crs.tif");
        Files.writeString(destination, "prior valid file", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile()));
        assertEquals(2, attempts.get(), "CRS mismatch must reject both WCS versions.");
        assertEquals("prior valid file", Files.readString(destination, StandardCharsets.UTF_8));
        assertNoStagingFile();
    }

    @Test
    void matchingGeographicCoveragePublishesAndReleasesFileHandlesOnBothOperatingSystems()
            throws Exception {
        byte[] valid = realGeoTiff(16, 16);
        openServer();
        server.createContext("/wcs", exchange -> respond(exchange, 200, "image/tiff", valid));
        server.start();

        Path destination = tempDir.resolve("portable-georeference.tif");
        WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile());
        assertArrayEquals(valid, Files.readAllBytes(destination));
        WcsClient.validateStagedGeoMetadata(
                destination, "-70,-46,-69,-45", "EPSG:4326", 16, 16);

        // On Windows this rename/delete fails if a GeoTools/ImageIO reader
        // has retained the file handle from the metadata-only validation.
        Path moved = tempDir.resolve("portable-georeference-moved.tif");
        Files.move(destination, moved);
        assertArrayEquals(valid, Files.readAllBytes(moved));
        Files.delete(moved);
        assertNoStagingFile();
    }

    @Test
    void invalidRequestedGeographicEnvelopeFailsBeforePublication() throws Exception {
        Path fixture = tempDir.resolve("invalid-bbox-fixture.tif");
        Files.write(fixture, realGeoTiff(16, 16));
        assertThrows(IOException.class, () -> WcsClient.validateStagedGeoMetadata(
                fixture, "NaN,-46,-69,-45", "EPSG:4326", 16, 16));
        assertThrows(IOException.class, () -> WcsClient.validateStagedGeoMetadata(
                fixture, "-69,-46,-70,-45", "EPSG:4326", 16, 16));
        assertThrows(IOException.class, () -> WcsClient.validateStagedGeoMetadata(
                fixture, "-70,-46,-69", "EPSG:4326", 16, 16));
    }

    @Test
    void tiffWithValidDimensionsButPixelOffsetsOutsideFileCannotReplaceOldCoverage()
            throws Exception {
        byte[] broken = moveFirstPixelBlockPastEnd(realGeoTiff(16, 16));
        Path intactMetadata = tempDir.resolve("pixel-offset-corrupt.tif");
        Files.write(intactMetadata, broken);
        // The dimensions remain fully parseable even when strip data is not.
        try (var stream = ImageIO.createImageInputStream(intactMetadata.toFile())) {
            var readers = ImageIO.getImageReaders(stream);
            assertTrue(readers.hasNext());
            var reader = readers.next();
            try {
                reader.setInput(stream);
                assertEquals(16, reader.getWidth(0));
                assertEquals(16, reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        }

        openServer();
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            attempts.incrementAndGet();
            respond(exchange, 200, "image/tiff", broken);
        });
        server.start();

        Path destination = tempDir.resolve("must-preserve-existing-raster.tif");
        byte[] previous = realGeoTiff(16, 16);
        Files.write(destination, previous);
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45",
                "EPSG:4326", 16, 16, destination.toFile()));
        assertArrayEquals(previous, Files.readAllBytes(destination));
        assertEquals(2, attempts.get(), "WCS 2.0 and 1.0 both reject corrupt storage.");
        assertNoStagingFile();
    }

    @Test
    void invalidPixelLayoutInWcs20FallsBackToReadableWcs10WithoutStaleBytes()
            throws Exception {
        byte[] valid = realGeoTiff(16, 16);
        byte[] corrupt = moveFirstPixelBlockPastEnd(valid);
        openServer();
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            attempts.incrementAndGet();
            String query = exchange.getRequestURI().getRawQuery();
            respond(exchange, 200, "image/tiff",
                    query != null && query.contains("VERSION=2.0.1")
                            ? corrupt : valid);
        });
        server.start();

        Path destination = tempDir.resolve("fallback-with-valid-pixels.tif");
        Files.writeString(destination, "PREVIOUS");
        WcsClient.downloadCoverageToFile(origin, "dem", "-70,-46,-69,-45",
                "EPSG:4326", 16, 16, destination.toFile());
        assertArrayEquals(valid, Files.readAllBytes(destination));
        assertEquals(2, attempts.get());
        WcsClient.validateStagedTiff(destination, 16, 16);
        assertNoStagingFile();
    }

    /**
     * Mutate one primary TIFF IFD StripOffsets/TileOffsets value while leaving
     * magic, image dimensions, GeoKeys, BBOX and other metadata unchanged.
     * The test fixture is a genuinely generated little/big-endian classic TIFF.
     */
    private static byte[] moveFirstPixelBlockPastEnd(byte[] original) {
        byte[] broken = Arrays.copyOf(original, original.length);
        ByteOrder order = broken[0] == 'I' ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer buf = ByteBuffer.wrap(broken).order(order);
        assertEquals(42, Short.toUnsignedInt(buf.getShort(2)),
                "Test requires a classic GeoTIFF fixture with IFD storage.");
        long ifd = Integer.toUnsignedLong(buf.getInt(4));
        assertTrue(ifd + 2L < broken.length);
        int count = Short.toUnsignedInt(buf.getShort((int) ifd));
        for (int i = 0; i < count; i++) {
            int entry = (int) ifd + 2 + i * 12;
            assertTrue(entry + 12 <= broken.length);
            int tag = Short.toUnsignedInt(buf.getShort(entry));
            if (tag != 273 && tag != 324) {
                continue;
            }
            int type = Short.toUnsignedInt(buf.getShort(entry + 2));
            long values = Integer.toUnsignedLong(buf.getInt(entry + 4));
            assertTrue(values > 0);
            int valueAt = entry + 8;
            if (values > 1) {
                long pointer = Integer.toUnsignedLong(buf.getInt(valueAt));
                assertTrue(pointer <= broken.length - 4L);
                valueAt = (int) pointer;
            }
            if (type == 4) {
                buf.putInt(valueAt, broken.length + 1024);
                return broken;
            }
            if (type == 3 && broken.length < 65535) {
                buf.putShort(valueAt, (short) 65535);
                return broken;
            }
            throw new AssertionError("Unsupported fixture strip offset type " + type);
        }
        throw new AssertionError("GeoTiffWriter fixture must include strip/tile offsets.");
    }

    @Test
    void chunkedCoverageWithoutContentLengthCannotExhaustWcsDownloadBudget() throws Exception {
        byte[] payload = realGeoTiff(16, 16);
        long maxBytes = payload.length - 1L;
        openServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().set("Content-Type", "image/tiff");
            exchange.sendResponseHeaders(200, 0L); // chunked: no reliable Content-Length
            try (var output = exchange.getResponseBody()) {
                output.write(payload);
            } catch (IOException clientRejectedOverBudget) {
                // The downloader may close early, as it should.
            }
        });
        server.start();
        Path destination = tempDir.resolve("existing raster with spaces.tif");
        byte[] old = "PRESERVE_BEFORE_CHUNKED_RESPONSE".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, old);

        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326",
                16, 16, null, destination.toFile(), maxBytes));

        assertEquals(2, requests.get(), "Both WCS protocol attempts must honor the same limit.");
        assertArrayEquals(old, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void declaredLargeContentLengthRejectedWithoutReplacingOldCoverage() throws Exception {
        byte[] payload = realGeoTiff(16, 16);
        openServer();
        server.createContext("/wcs", exchange -> respond(exchange, 200, "image/tiff", payload));
        server.start();
        Path destination = tempDir.resolve("declared-budget.tif");
        // Old coverage must have different bytes, or replacement could pass unnoticed.
        byte[] old = realGeoTiff(8, 8);
        Files.write(destination, old);

        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326",
                16, 16, null, destination.toFile(), payload.length - 1L));
        assertArrayEquals(old, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void completeGeoTiffAtExactWcsBudgetIsPublished() throws Exception {
        byte[] payload = realGeoTiff(16, 16);
        openServer();
        server.createContext("/wcs", exchange -> respond(exchange, 200, "image/tiff", payload));
        server.start();
        Path destination = tempDir.resolve("exact-wcs-byte-budget.tif");
        Files.writeString(destination, "OLD", StandardCharsets.UTF_8);

        WcsClient.downloadCoverageToFile(origin, "dem",
                "-70,-46,-69,-45", "EPSG:4326", 16, 16, null,
                destination.toFile(), payload.length);

        assertArrayEquals(payload, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void failedVersionCannotResetAggregateBudgetForValidFallback() throws Exception {
        byte[] valid = realGeoTiff(16, 16);
        byte[] invalid = "NOT_TIFF_DATA".getBytes(StandardCharsets.UTF_8);
        openServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            String query = exchange.getRequestURI().getRawQuery();
            respond(exchange, 200, "image/tiff",
                    query != null && query.contains("VERSION=2.0.1") ? invalid : valid);
        });
        server.start();
        Path destination = tempDir.resolve("aggregate-versions.tif");
        byte[] original = "PRESERVE_AGGREGATE".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);

        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326",
                16, 16, null, destination.toFile(), valid.length + invalid.length - 1L));
        assertEquals(2, requests.get());
        assertArrayEquals(original, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void unsupportedAtomicReplacementPreservesPreviousCoverageAndCleansStaging() throws Exception {
        byte[] complete = realGeoTiff(16, 16);
        byte[] old = realGeoTiff(8, 8);
        openServer();
        server.createContext("/wcs", exchange -> respond(exchange, 200, "image/tiff", complete));
        server.start();

        Path destination = tempDir.resolve("previous important raster.tif");
        Files.write(destination, old);
        AtomicInteger attemptedMoves = new AtomicInteger();

        IOException error = assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "dem", "-70,-46,-69,-45", "EPSG:4326", 16, 16,
                null, destination.toFile(), complete.length, (staging, target) -> {
                    attemptedMoves.incrementAndGet();
                    throw new AtomicMoveNotSupportedException(
                            staging.toString(), target.toString(), "simulated unsupported filesystem");
                }));

        assertTrue(error.getMessage().contains("atomic replacement"));
        assertEquals(1, attemptedMoves.get());
        assertArrayEquals(old, Files.readAllBytes(destination));
        assertNoStagingFile();
    }

    @Test
    void directoryDestinationIsRejectedBeforeNetworkOrStaging() throws Exception {
        Path destination = Files.createDirectory(tempDir.resolve("not-a-raster.tif"));
        IOException error = assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                "http://127.0.0.1:1/wcs", "dem", "-70,-46,-69,-45",
                "EPSG:4326", 16, 16, destination.toFile()));
        assertTrue(error.getMessage().contains("not a regular file"));
        assertTrue(Files.isDirectory(destination));
        assertNoStagingFile();
    }

    @Test
    void symbolicLinkDestinationCannotOverwriteItsReferencedRaster() throws Exception {
        Path victim = tempDir.resolve("preserve-link-target.tif");
        byte[] old = realGeoTiff(8, 8);
        Files.write(victim, old);
        Path link = tempDir.resolve("linked-output.tif");
        boolean symlinkSupported = true;
        try {
            Files.createSymbolicLink(link, victim.getFileName());
        } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
            // On hardened Windows runners that cannot create symlinks, still
            // exercise fail-closed rejection of a nonregular output target.
            symlinkSupported = false;
            Files.createDirectory(link);
        }

        IOException error = assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                "http://127.0.0.1:1/wcs", "dem", "-70,-46,-69,-45",
                "EPSG:4326", 16, 16, link.toFile()));
        assertTrue(error.getMessage().contains("not a regular file"));
        assertArrayEquals(old, Files.readAllBytes(victim));
        if (symlinkSupported) {
            assertTrue(Files.isSymbolicLink(link));
        } else {
            assertTrue(Files.isDirectory(link));
        }
        assertNoStagingFile();
    }

    private byte[] realGeoTiff(int width, int height) throws Exception {
        return realGeoTiff(width, height, -70.0, -69.0, -46.0, -45.0, "EPSG:4326");
    }

    private byte[] realGeoTiff(int width, int height,
                               double minX, double maxX, double minY, double maxY,
                               String crsCode) throws Exception {
        Path fixture = tempDir.resolve("fixture-" + width + "x" + height + "-"
                + crsCode.replace(':', '-') + "-" + java.util.UUID.randomUUID() + ".tif");
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
                minX, maxX, minY, maxY,
                CRSDefinitions.decode(crsCode, true));
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
