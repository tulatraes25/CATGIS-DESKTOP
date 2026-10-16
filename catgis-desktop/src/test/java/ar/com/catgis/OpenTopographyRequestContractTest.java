package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Request semantics and real filesystem preservation; no public service or credential. */
class OpenTopographyRequestContractTest {
    private static final String KEY = "SYNTHETIC_REQUEST_TEST_ONLY+&/\u00f1";
    @TempDir Path directory;

    @Test
    void canonicalParameterOrderEncodingAndSixDecimalCoordinatesStayCompatible() {
        URI uri = OpenTopographyDemService.buildDownloadUri(OpenTopographyDataset.COP30,
                new Envelope(-68.6, -68.4, -45.9, -45.7), "  " + KEY + "  ");
        Map<String, String> values = query(uri);
        assertEquals("https", uri.getScheme());
        assertEquals("portal.opentopography.org", uri.getHost());
        assertEquals("/API/globaldem", uri.getPath());
        assertEquals(List.of("demtype", "south", "north", "west", "east", "outputFormat", "API_Key"),
                new ArrayList<>(values.keySet()));
        assertEquals("COP30", values.get("demtype"));
        assertEquals("GTiff", values.get("outputFormat"));
        assertEquals(KEY, values.get("API_Key"));
        assertEquals("-68.600000", values.get("west"));
        assertEquals("-68.400000", values.get("east"));
        assertEquals("-45.900000", values.get("south"));
        assertEquals("-45.700000", values.get("north"));
    }

    @Test
    void everyNonfiniteAxisIsRejectedInsteadOfClippedOrEncoded() {
        for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            for (double[] values : new double[][] {
                    {invalid, 1, 0, 1}, {0, invalid, 0, 1}, {0, 1, invalid, 1}, {0, 1, 0, invalid}}) {
                Envelope area = new Envelope(values[0], values[1], values[2], values[3]);
                assertThrows(IllegalArgumentException.class, () -> uri(area));
            }
        }
    }

    @Test
    void valuesBeyondWorldEdgesAreNotSilentlyClipped() {
        for (Envelope area : List.of(new Envelope(-181, 1, 0, 1), new Envelope(0, 181, 0, 1),
                new Envelope(0, 1, -91, 1), new Envelope(0, 1, 0, 91))) {
            Envelope original = new Envelope(area);
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> uri(area));
            assertTrue(failure.getMessage().contains("No se recortaron"));
            assertEquals(original, area);
        }
    }

    @Test
    void absentEmptyOrZeroAreaIsNotAValidRequest() {
        for (Envelope area : Arrays.asList(null, new Envelope(),
                new Envelope(0, 0, 0, 1), new Envelope(0, 1, 0, 0))) {
            assertThrows(IllegalArgumentException.class, () -> uri(area));
        }
    }

    @Test
    void positiveInputSpansThatCollapseAtWirePrecisionAreRejected() {
        for (Envelope area : List.of(new Envelope(1.0000001, 1.0000002, 0, 1),
                new Envelope(0, 1, 1.0000001, 1.0000002),
                new Envelope(-0.0000001, 0.0000001, 0, 1))) {
            assertTrue(area.getWidth() > 0 && area.getHeight() > 0);
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> uri(area));
            assertTrue(failure.getMessage().contains("seis decimales"));
        }
        assertEquals("1.000001", query(uri(new Envelope(1, 1.000001, 0, 1))).get("east"));
    }

    @Test
    void signedZeroAndOrdinaryQuantizationRemainDeterministic() {
        Map<String, String> values = query(uri(new Envelope(-0.0000001, 0.0000021, -1.1234561, 1.7654321)));
        assertEquals("-0.000000", values.get("west"));
        assertEquals(Long.MIN_VALUE, Double.doubleToRawLongBits(Double.parseDouble(values.get("west"))));
        assertEquals("0.000002", values.get("east"));
        assertEquals("-1.123456", values.get("south"));
        assertEquals("1.765432", values.get("north"));
    }

    @Test
    void validRequestBuilderNeverChangesTheCallersGeometry() {
        Envelope area = new Envelope(-68.1234561, -67.7654321, -45.1234561, -44.7654321);
        Envelope original = new Envelope(area);
        URI first = uri(area);
        assertEquals(original, area);
        area.init(10, 11, 20, 21);
        assertEquals("-68.123456", query(first).get("west"));
        assertEquals("10.000000", query(uri(area)).get("west"));
    }

    @Test
    void inclusiveCoordinateLimitsArePreservedWithoutPromisingProviderCoverage() {
        Map<String, String> values = query(uri(new Envelope(-180, 180, -90, 90)));
        assertEquals("-180.000000", values.get("west"));
        assertEquals("180.000000", values.get("east"));
        assertEquals("-90.000000", values.get("south"));
        assertEquals("90.000000", values.get("north"));
    }

    @Test
    void concurrentIndependentRequestsDoNotShareNumberFormattingState() throws Exception {
        var executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int worker = 0; worker < 8; worker++) {
                int id = worker;
                tasks.add(executor.submit(() -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    for (int i = 0; i < 200; i++) {
                        OpenTopographyDataset dataset = OpenTopographyDataset.values()[id % 3];
                        String key = KEY + id;
                        Map<String, String> values = query(OpenTopographyDemService.buildDownloadUri(dataset,
                                new Envelope(id + 0.125, id + 0.625, -id - 0.875, -id - 0.375), key));
                        assertEquals(id + ".125000", values.get("west"));
                        assertEquals(id + ".625000", values.get("east"));
                        assertEquals(-id - 0.875, Double.parseDouble(values.get("south")));
                        assertEquals(-id - 0.375, Double.parseDouble(values.get("north")));
                        assertEquals(dataset.getApiCode(), values.get("demtype"));
                        assertEquals(key, values.get("API_Key"));
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) task.get(30, TimeUnit.SECONDS);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void invalidCoordinatesAuthenticationAndSpanOverflowHaveZeroNetworkAndFilesystemEffects() throws Exception {
        List<Envelope> invalid = Arrays.asList(null, new Envelope(), new Envelope(-181, 1, 0, 1),
                new Envelope(Double.NaN, 1, 0, 1), new Envelope(0, 0, 0, 1),
                new Envelope(1.0000001, 1.0000002, 0, 1),
                new Envelope(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1));
        int n = 0;
        for (Envelope area : invalid) {
            assertRejectedBeforeWrites(OpenTopographyDataset.NASADEM, area, null, KEY, "bounds-" + n++);
        }
        assertRejectedBeforeWrites(OpenTopographyDataset.NASADEM,
                new Envelope(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1), "EPSG:3857", KEY, "projected-overflow");
        assertRejectedBeforeWrites(null, new Envelope(0, 1, 0, 1), null, KEY, "dataset");
        for (String key : new String[] {null, "", " \t "}) {
            assertRejectedBeforeWrites(OpenTopographyDataset.NASADEM,
                    new Envelope(0, 1, 0, 1), null, key, "key-" + n++);
        }
    }

    @Test
    void rejectedPlanKeepsExistingRasterAndSidecarBytesUntouched() throws Exception {
        Path target = directory.resolve("existing.tif");
        Path sidecar = RasterSidecarSupport.sidecarFile(target.toFile()).toPath();
        byte[] rasterBytes = tiff();
        byte[] sidecarBytes = "existing metadata must survive".getBytes(StandardCharsets.UTF_8);
        Files.write(target, rasterBytes);
        Files.write(sidecar, sidecarBytes);
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> OpenTopographyDemService.download(
                OpenTopographyDataset.NASADEM, new Envelope(-181, 1, 0, 1), null, KEY, target.toFile(),
                request -> { calls.incrementAndGet(); throw new AssertionError("No HTTP for invalid plans"); }));
        assertEquals(0, calls.get());
        assertArrayEquals(rasterBytes, Files.readAllBytes(target));
        assertArrayEquals(sidecarBytes, Files.readAllBytes(sidecar));
    }

    @Test
    void actualMercatorReprojectionProducesTheIndependentOneDegreeRequest() throws Exception {
        // Independent EPSG:3857 controls for longitude/latitude 0..1 degrees.
        Envelope projected = new Envelope(0, 111319.49079327357, 0, 111325.1428663851);
        Envelope original = new Envelope(projected);
        Map<String, String> values = query(captureRequest(projected, "EPSG:3857"));
        assertEquals(0d, Double.parseDouble(values.get("west")), 0d);
        assertEquals(0d, Double.parseDouble(values.get("south")), 0d);
        assertEquals(1d, Double.parseDouble(values.get("east")), 0.000001d);
        assertEquals(1d, Double.parseDouble(values.get("north")), 0.000001d);
        assertEquals(original, projected);
    }

    @Test
    void invalidCrsCannotCreateADestinationOrReachTransport() {
        Path target = directory.resolve("not-created-crs").resolve("bad.tif");
        AtomicInteger calls = new AtomicInteger();
        assertThrows(Exception.class, () -> OpenTopographyDemService.download(OpenTopographyDataset.NASADEM,
                new Envelope(0, 1, 0, 1), "EPSG:NOT_A_REAL_CODE", KEY, target.toFile(), request -> {
                    calls.incrementAndGet();
                    throw new AssertionError("No HTTP for invalid CRS");
                }));
        assertEquals(0, calls.get());
        assertFalse(Files.exists(target.getParent()));
    }

    @Test
    void omittedOrBlankCrsRetainsTheDocumentedWgs84ApiConvention() throws Exception {
        Envelope area = new Envelope(-69, -68, -46, -45);
        assertEquals(uri(area), captureRequest(area, null));
        assertEquals(uri(area), captureRequest(area, "  "));
        assertEquals(uri(area), captureRequest(area, "EPSG:4326"));
    }

    @Test
    void mutationAfterSendCannotRepointTheReturnedExtentOrExistingSidecarWorkflow() throws Exception {
        Envelope source = new Envelope(-68.1234561, -67.7654321, -45.1234561, -44.7654321);
        AtomicReference<HttpRequest> sent = new AtomicReference<>();
        AtomicInteger closed = new AtomicInteger();
        byte[] image = tiff();
        Path target = directory.resolve("snapshot with spaces.tif");
        var result = OpenTopographyDemService.download(OpenTopographyDataset.NASADEM, source, null, KEY,
                target.toFile(), request -> {
                    sent.set(request);
                    source.init(10, 11, 20, 21);
                    return new Reply(request, tracked(image, closed));
                });
        Envelope effective = envelope(query(sent.get().uri()));
        assertEquals("-68.123456", query(sent.get().uri()).get("west"));
        assertEquals(effective, result.latLonEnvelope());
        assertEquals(effective, RasterSidecarSupport.read(target.toFile()).envelope());
        assertNotEquals(source, effective);
        assertArrayEquals(image, Files.readAllBytes(target));
        assertEquals(1, closed.get());
        BufferedImage reopened = ImageIO.read(target.toFile());
        assertNotNull(reopened);
        assertEquals(17, reopened.getRaster().getSample(0, 0, 0));
        reopened.flush();
        Files.delete(target);
    }

    @Test
    void returnedMetadataMatchesQuantizedWireValuesRatherThanUnroundedCallerCoordinates() throws Exception {
        Envelope source = new Envelope(-68.1234561, -67.7654321, -45.1234561, -44.7654321);
        Path target = directory.resolve("quantized.tif");
        byte[] image = tiff();
        var result = OpenTopographyDemService.download(OpenTopographyDataset.COP30, source, "EPSG:4326", KEY,
                target.toFile(), request -> new Reply(request, new ByteArrayInputStream(image)));
        Envelope effective = envelope(query(result.requestUri()));
        assertNotEquals(source, effective);
        assertEquals(effective, result.latLonEnvelope());
        assertEquals(effective, RasterSidecarSupport.read(target.toFile()).envelope());
        assertEquals("EPSG:4326", RasterSidecarSupport.read(target.toFile()).sourceCrs());
        assertArrayEquals(image, Files.readAllBytes(target));
    }

    private void assertRejectedBeforeWrites(OpenTopographyDataset dataset, Envelope area,
                                            String crs, String key, String label) {
        Path target = directory.resolve(label).resolve("not-created.tif");
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> OpenTopographyDemService.download(
                dataset, area, crs, key, target.toFile(), request -> {
                    calls.incrementAndGet();
                    throw new AssertionError("No HTTP for invalid plans");
                }));
        assertEquals(0, calls.get());
        assertFalse(Files.exists(target.getParent()));
    }

    private URI captureRequest(Envelope area, String crs) {
        AtomicReference<URI> captured = new AtomicReference<>();
        IOException result = assertThrows(IOException.class, () -> OpenTopographyDemService.download(
                OpenTopographyDataset.NASADEM, area, crs, KEY, directory.resolve("capture.tif").toFile(),
                request -> { captured.set(request.uri()); throw new IOException("Synthetic stop before body"); }));
        assertTrue(result.getMessage().contains("TRANSPORT"));
        assertNotNull(captured.get());
        assertFalse(Files.exists(directory.resolve("capture.tif")));
        return captured.get();
    }

    private static URI uri(Envelope area) {
        return OpenTopographyDemService.buildDownloadUri(OpenTopographyDataset.NASADEM, area, KEY);
    }

    private static Map<String, String> query(URI uri) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : uri.getRawQuery().split("&")) {
            String[] pair = part.split("=", 2);
            assertEquals(2, pair.length);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            assertNull(values.put(key, value), "Duplicate query key");
        }
        return values;
    }

    private static Envelope envelope(Map<String, String> values) {
        return new Envelope(Double.parseDouble(values.get("west")), Double.parseDouble(values.get("east")),
                Double.parseDouble(values.get("south")), Double.parseDouble(values.get("north")));
    }

    private static InputStream tracked(byte[] bytes, AtomicInteger closed) {
        return new ByteArrayInputStream(bytes) {
            @Override public void close() throws IOException {
                closed.incrementAndGet();
                super.close();
            }
        };
    }

    private static byte[] tiff() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        try {
            image.getRaster().setSample(0, 0, 0, 17);
            image.getRaster().setSample(1, 1, 0, 254);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            assertTrue(ImageIO.write(image, "tif", output));
            return output.toByteArray();
        } finally {
            image.flush();
        }
    }

    private record Reply(HttpRequest request, InputStream body) implements HttpResponse<InputStream> {
        @Override public int statusCode() { return 200; }
        @Override public Optional<HttpResponse<InputStream>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of("Content-Type", List.of("image/tiff")), (a, b) -> true); }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return request.uri(); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }
}
