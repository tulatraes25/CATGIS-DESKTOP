package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Real loopback HTTP and TIFF contracts; no changes to older regression tests. */
class WcsSpatialApiCompatibilityContractTest {

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String origin;
    private final AtomicInteger requests = new AtomicInteger();
    private final List<String> queries = new CopyOnWriteArrayList<>();

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void apiOmittedDefaultsDoNotPermitEmptyInteractiveForm() throws Exception {
        for (String absent : new String[] {null, "", " "}) {
            WcsSpatialPreflight.validateApi(absent, absent);
            WcsSpatialPreflight.validateApi("-70,-46,-69,-45", absent);
            WcsSpatialPreflight.validateApi(absent, "EPSG:4326");
            assertEquals("-180,-90,180,90", WcsSpatialPreflight.effectiveBbox(absent));
            assertEquals("EPSG:4326", WcsSpatialPreflight.effectiveCrs(absent));
            assertThrows(IOException.class, () -> WcsSpatialPreflight.validate(absent, "EPSG:4326"));
            assertThrows(IOException.class, () -> WcsSpatialPreflight.validate("-70,-46,-69,-45", absent));
        }
        assertThrows(IllegalArgumentException.class,
                () -> WcsDimensionsPreflight.validate("0", "-1"));
    }

    @Test
    void rejectsUnboundedSpansAndJavaOnlyNumberSyntaxWithoutReordering() throws Exception {
        for (String bbox : new String[] {"-1e308,0,1e308,1", "0,-1e308,1,1e308",
                "0x0p0,0,1,2", "0,0,1d,2", "0,0,1f,2", "0,0,1e309,2",
                "0,0,1,2,", "0,0,1,2&FORMAT=other", "0," + "1".repeat(1024) + ",2,3"}) {
            assertThrows(IOException.class,
                    () -> WcsSpatialPreflight.validateApi(bbox, "EPSG:3857"), bbox);
        }
        double[] bounds = WcsSpatialPreflight.parseBounds(" +1e0, 2.0, +3e0, 4.0 ");
        assertArrayEquals(new double[] {1, 2, 3, 4}, bounds);
        bounds[0] = 99;
        assertEquals(1, WcsSpatialPreflight.parseBounds("1,2,3,4")[0]);
    }

    @Test
    void invalidExplicitInputsContactNeitherVersionAndCreateNoFilesInBothApis() throws Exception {
        startServer(tiff(16), false);
        Path destination = tempDir.resolve("previous coverage.tif");
        byte[] previous = "previous valid user data".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, previous);
        String[][] invalid = {
                {"NaN,-46,-69,-45", "EPSG:4326"},
                {"-70,-46,-69,-45", "EPSG:4326&FORMAT=other"},
                {"-181,-46,-69,-45", null},
                {"-1e308,0,1e308,1", "EPSG:3857"},
                {"0,0,1,2&REQUEST=other", "EPSG:4326"}
        };
        for (String[] input : invalid) {
            assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                    origin, "sample", input[0], input[1], 16, 16, destination.toFile()));
            assertThrows(IOException.class, () -> WcsClient.downloadCoverage(
                    origin, "sample", input[0], input[1], 16, 16));
        }
        assertEquals(0, requests.get(), "Input rejection must precede both HTTP attempts.");
        assertArrayEquals(previous, Files.readAllBytes(destination));
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(List.of(destination), files.toList(),
                    "Invalid input must not create even a destination lease or staging file.");
        }
    }

    @Test
    void omittedFieldsKeepRealTiffFallbackAndLegacy512Dimensions() throws Exception {
        byte[] payload = tiff(512);
        startServer(payload, true);
        Path destination = tempDir.resolve("API defaults with spaces.tif");
        for (String absent : new String[] {null, " "}) {
            WcsClient.downloadCoverageToFile(origin, "sample", absent, absent,
                    0, -1, destination.toFile());
            assertArrayEquals(payload, Files.readAllBytes(destination));
            BufferedImage result = ImageIO.read(destination.toFile());
            assertNotNull(result);
            try {
                assertEquals(512, result.getWidth());
                assertEquals(512, result.getHeight());
                assertEquals(123, result.getRaster().getSample(5, 7, 0));
            } finally {
                result.flush();
            }
        }
        assertEquals(4, requests.get());
        for (String query : queries) {
            if (query.contains("VERSION=1.0.0")) {
                assertTrue(query.contains("BBOX=-180,-90,180,90"));
                assertTrue(query.contains("CRS=EPSG:4326"));
                assertTrue(query.contains("WIDTH=512&HEIGHT=512"));
            }
        }
        assertNoParts();
        Files.delete(destination); // File handles must already be released on Windows.
    }

    @Test
    void omittedFieldsDoNotTurnInvalidTiffIntoSuccessfulPublication() throws Exception {
        startServer(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, false);
        Path destination = tempDir.resolve("preserved.tif");
        Files.writeString(destination, "keep previous");
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "sample", null, null, 16, 16, destination.toFile()));
        assertEquals(2, requests.get(), "Both versions must reach raster validation.");
        assertEquals("keep previous", Files.readString(destination));
        assertNoParts();
    }

    @Test
    void explicitGeographyStillRejectsTiffWithoutRequestedGeoReference() throws Exception {
        startServer(tiff(16), false);
        Path destination = tempDir.resolve("geographic output.tif");
        Files.writeString(destination, "keep geography");
        assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                origin, "sample", "-70,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile()));
        assertEquals(2, requests.get());
        assertEquals("keep geography", Files.readString(destination));
        assertNoParts();
    }

    @Test
    void wcs20EscapesPositiveSignsAndWhitespaceWithoutChangingCoordinates() throws Exception {
        byte[] payload = tiff(16);
        startServer(payload, false);
        assertArrayEquals(payload, WcsClient.downloadCoverage(origin, "my coverage",
                " +1e0, +2e0, +3e0, +4e0 ", " EPSG:4326 ", 16, 16));
        assertEquals(1, requests.get());
        String query = queries.get(0);
        assertTrue(query.contains("SUBSET=x(+1e0,+3e0)"), query);
        assertTrue(query.contains("SUBSET=y(+2e0,+4e0)"), query);
        assertTrue(query.contains("COVERAGEID=my coverage"), query);
    }

    @Test
    void inMemoryCompatibilityApiAlsoKeepsOmittedDefaults() throws Exception {
        byte[] payload = tiff(16);
        startServer(payload, true);
        assertArrayEquals(payload, WcsClient.downloadCoverage(
                origin, "sample", null, null, 16, 16));
        assertEquals(2, requests.get());
        assertTrue(queries.get(1).contains("BBOX=-180,-90,180,90"));
        assertTrue(queries.get(1).contains("CRS=EPSG:4326"));
    }

    private void startServer(byte[] payload, boolean failVersion2) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        origin = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            String query = URLDecoder.decode(exchange.getRequestURI().getRawQuery(), StandardCharsets.UTF_8);
            queries.add(query);
            if (failVersion2 && query.contains("VERSION=2.0.1")) {
                respond(exchange, 503, new byte[] {1});
            } else {
                respond(exchange, 200, payload);
            }
        });
        server.start();
    }

    private static void respond(HttpExchange exchange, int status, byte[] payload) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", "image/tiff");
            exchange.sendResponseHeaders(status, payload.length);
            exchange.getResponseBody().write(payload);
        } finally {
            exchange.close();
        }
    }

    private static byte[] tiff(int size) throws IOException {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            image.getRaster().setSample(5, 7, 0, 123);
            assertTrue(ImageIO.write(image, "TIFF", bytes), "A real TIFF codec is required, never skip.");
            return bytes.toByteArray();
        } finally {
            image.flush();
        }
    }

    private void assertNoParts() throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }
}
