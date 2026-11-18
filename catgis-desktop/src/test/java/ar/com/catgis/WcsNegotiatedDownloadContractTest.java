package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#108: WCS downloads address DescribeCoverage-advertised native axes. */
class WcsNegotiatedDownloadContractTest {

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String origin;
    private final List<String> queries = new ArrayList<>();
    private volatile byte[] coveragePayload;
    private volatile String describeXml;
    private volatile int describeCount;
    private volatile int v2GetCoverageCount;
    private volatile int v1GetCoverageCount;

    @BeforeEach
    void setUp() throws Exception {
        WcsClient.clearAxisPlanCacheForTesting();
        coveragePayload = geoTiff(16, 16);
        describeXml = describeDocument("EPSG:4326", "lat lon");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/wcs", this::handle);
        server.start();
        origin = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
    }

    @AfterEach
    void tearDown() {
        WcsClient.clearAxisPlanCacheForTesting();
        if (server != null) {
            server.stop(0);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        synchronized (queries) {
            queries.add(query == null ? "" : query);
        }
        String lower = query == null ? "" : query.toUpperCase(java.util.Locale.ROOT);
        if (lower.contains("REQUEST=DESCRIBECOVERAGE")) {
            describeCount++;
            byte[] body = describeXml.getBytes(StandardCharsets.UTF_8);
            respond(exchange, 200, "text/xml", body);
            return;
        }
        if (lower.contains("VERSION=1.0.0")) {
            v1GetCoverageCount++;
            respond(exchange, 200, "image/tiff", coveragePayload);
            return;
        }
        v2GetCoverageCount++;
        respond(exchange, 200, "image/tiff", coveragePayload);
    }

    @Test
    void nativeAxisLabelsAreUsedAndDescribeIsCached() throws Exception {
        Path target = tempDir.resolve("dem-negociado.tif");
        WcsClient.downloadCoverageToFileNegotiated(origin, "dem",
                "-69.5,-45.5,-69.0,-45.0", "EPSG:4326", 16, 16, target.toFile());

        assertArrayEquals(coveragePayload, Files.readAllBytes(target));
        assertEquals(1, describeCount);
        assertEquals(1, v2GetCoverageCount);
        String getCoverage = queryContaining("REQUEST=GETCOVERAGE");
        assertTrue(getCoverage.contains("SUBSET=lon%28-69.5%2C-69.0%29"), getCoverage);
        assertTrue(getCoverage.contains("SUBSET=lat%28-45.5%2C-45.0%29"), getCoverage);
        assertTrue(getCoverage.contains("SCALESIZE=lon%2816%29"), getCoverage);
        assertTrue(getCoverage.contains("SCALESIZE=lat%2816%29"), getCoverage);
        String describe = queryContaining("REQUEST=DESCRIBECOVERAGE");
        assertTrue(describe.contains("COVERAGEID=dem"), describe);
        assertTrue(describe.contains("VERSION=2.0.1"), describe);

        // Second download reuses the cached DescribeCoverage plan.
        Path second = tempDir.resolve("dem-negociado-2.tif");
        WcsClient.downloadCoverageToFileNegotiated(origin, "dem",
                "-69.5,-45.5,-69.0,-45.0", "EPSG:4326", 16, 16, second.toFile());
        assertEquals(1, describeCount, "axis plan must be cached per service/coverage");
        assertEquals(2, v2GetCoverageCount);
        assertArrayEquals(coveragePayload, Files.readAllBytes(second));
    }

    @Test
    void ambiguousAxesFallBackToWcs10WithoutGuessingXY() throws Exception {
        describeXml = describeDocument("EPSG:4326", "i j");
        Path target = tempDir.resolve("dem-fallback.tif");
        WcsClient.downloadCoverageToFileNegotiated(origin, "dem",
                "-69.5,-45.5,-69.0,-45.0", "EPSG:4326", 16, 16, target.toFile());

        assertEquals(0, v2GetCoverageCount, "ambiguous axes must never issue a guessed SUBSET=x/y");
        assertEquals(1, v1GetCoverageCount);
        assertTrue(queryContaining("VERSION=1.0.0").contains("BBOX="));
        assertArrayEquals(coveragePayload, Files.readAllBytes(target));
    }

    @Test
    void describeFailureFallsBackToWcs10AndKeepsOutputOnTotalFailure() throws Exception {
        describeXml = "<not-a-describe/>";
        Path target = tempDir.resolve("dem-describe-fail.tif");
        WcsClient.downloadCoverageToFileNegotiated(origin, "dem",
                "-69.5,-45.5,-69.0,-45.0", "EPSG:4326", 16, 16, target.toFile());
        assertEquals(1, v1GetCoverageCount);
        assertEquals(0, v2GetCoverageCount);

        Files.writeString(target, "PREVIOUS", StandardCharsets.UTF_8);
        describeXml = "<broken";
        server.stop(0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/wcs", exchange -> respond(exchange, 500, "text/plain",
                "down".getBytes(StandardCharsets.UTF_8)));
        server.start();
        String failing = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFileNegotiated(
                failing, "dem", "-69.5,-45.5,-69.0,-45.0", "EPSG:4326", 16, 16,
                target.toFile()));
        assertEquals("PREVIOUS", Files.readString(target));
    }

    private String queryContaining(String marker) {
        synchronized (queries) {
            return queries.stream()
                    .filter(q -> q.toUpperCase(java.util.Locale.ROOT).contains(marker))
                    .findFirst().orElseThrow(() -> new AssertionError("missing query " + marker));
        }
    }

    private static void respond(HttpExchange exchange, int status, String contentType,
                                byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (var out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String describeDocument(String srs, String axisLabels) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <wcs:CoverageDescriptions xmlns:wcs="http://www.opengis.net/wcs/2.0"
                    xmlns:gml="http://www.opengis.net/gml/3.2">
                  <wcs:CoverageDescription gml:id="dem">
                    <gml:boundedBy>
                      <gml:Envelope srsName="%s" axisLabels="%s">
                        <gml:lowerCorner>-69.5 -45.5</gml:lowerCorner>
                        <gml:upperCorner>-69.0 -45.0</gml:upperCorner>
                      </gml:Envelope>
                    </gml:boundedBy>
                  </wcs:CoverageDescription>
                </wcs:CoverageDescriptions>
                """.formatted(srs, axisLabels);
    }

    private byte[] geoTiff(int width, int height) throws Exception {
        Path fixture = tempDir.resolve("neg-fixture-" + java.util.UUID.randomUUID() + ".tif");
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
                -69.5, -69.0, -45.5, -45.0, CRSDefinitions.decode("EPSG:4326", true));
        GridCoverage2D coverage = new GridCoverageFactory().create(
                "wcs-negotiated-fixture", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(fixture.toFile());
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }
        return Files.readAllBytes(fixture);
    }
}
