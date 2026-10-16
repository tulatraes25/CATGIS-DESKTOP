package ar.com.catgis;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Synthetic credentials and in-process transport, real Log4j events and actual TIFF bytes. */
@Isolated("Temporarily attaches an in-memory appender to the shared CATGIS logger")
class OpenTopographyDiagnosticsContractTest {
    private static final String KEY = "TEST_ONLY_NOT_A_REAL_KEY+&/\u00f1";
    private static final Envelope AREA = new Envelope(-69, -68, -46, -45);
    @TempDir Path directory;

    @Test
    void requestKeepsAuthenticationWhileRealLogsAndResultRenderingOmitIt() throws Exception {
        byte[] image = tiff();
        TrackingStream stream = new TrackingStream(image);
        Path target = directory.resolve("real raster with spaces.tif");
        AtomicReference<HttpRequest> sent = new AtomicReference<>();
        try (LogCapture logs = new LogCapture()) {
            var result = run(target, request -> {
                sent.set(request);
                return reply(request, 200, "image/tiff; trace=" + encodedKey(), stream);
            });
            assertEquals(KEY, queryValue(sent.get().uri(), "API_Key"));
            assertEquals("NASADEM", queryValue(sent.get().uri(), "demtype"));
            assertEquals("GET", sent.get().method());
            assertEquals(KEY, queryValue(result.requestUri(), "API_Key"));
            assertEquals(target.toFile(), result.file());
            assertEquals(AREA, result.latLonEnvelope());
            assertArrayEquals(image, Files.readAllBytes(target));
            BufferedImage reopened = ImageIO.read(target.toFile());
            assertNotNull(reopened);
            try {
                assertEquals(17, reopened.getRaster().getSample(0, 0, 0));
                assertEquals(254, reopened.getRaster().getSample(1, 1, 0));
            } finally {
                reopened.flush();
            }
            assertEquals("EPSG:4326", RasterSidecarSupport.read(target.toFile()).sourceCrs());
            assertTrue(logs.text().contains("OpenTopography response status=200"));
            assertTrue(logs.text().contains("download finished"));
            assertSafe(logs.text());
            assertSafe(result.toString());
            assertFalse(result.toString().contains(target.toString()));
        }
        assertEquals(1, stream.closeCalls);
        Files.delete(target);
    }

    @Test
    void rejectedHttpResponseIsNeverReadAndPreviousBytesStayIntact() throws Exception {
        for (int status : new int[] {401, 403, 503}) {
            Path target = previous("http-" + status + ".tif");
            TrackingStream stream = forbiddenBody();
            try (LogCapture logs = new LogCapture()) {
                IOException error = assertThrows(IOException.class,
                        () -> run(target, request -> reply(request, status, "text/plain", stream)));
                assertTrue(error.getMessage().contains("HTTP_" + status));
                assertSafeException(error);
                assertSafe(logs.text());
                assertFalse(logs.text().contains("download finished"));
            }
            assertEquals(0, stream.readCalls);
            assertEquals(1, stream.closeCalls);
            assertEquals("PREVIOUS", Files.readString(target));
            assertFalse(Files.exists(RasterSidecarSupport.sidecarFile(target.toFile()).toPath()));
        }
    }

    @Test
    void textResponseWithSuccessStatusCannotReflectItsBodyOrHeader() throws Exception {
        Path target = previous("text.tif");
        TrackingStream stream = forbiddenBody();
        try (LogCapture logs = new LogCapture()) {
            IOException error = assertThrows(IOException.class,
                    () -> run(target, request -> reply(request, 200,
                            "application/json; trace=" + encodedKey(), stream)));
            assertTrue(error.getMessage().contains("CONTENT_TYPE"));
            assertSafeException(error);
            assertSafe(logs.text());
        }
        assertEquals(0, stream.readCalls);
        assertEquals(1, stream.closeCalls);
        assertEquals("PREVIOUS", Files.readString(target));
    }

    @Test
    void transportIOExceptionDoesNotRetainRemoteCauseOrSuppressedText() throws Exception {
        try (LogCapture logs = new LogCapture()) {
            IOException remote = new IOException(KEY, new IllegalStateException(encodedKey()));
            remote.addSuppressed(new IOException(KEY));
            IOException result = assertThrows(IOException.class,
                    () -> run(directory.resolve("transport.tif"), request -> { throw remote; }));
            assertTrue(result.getMessage().contains("TRANSPORT"));
            assertSafeException(result);
            assertSafe(logs.text());
        }
    }

    @Test
    void uncheckedTransportFailureIsAlsoSafeForCallerStackTraceLogging() throws Exception {
        try (LogCapture logs = new LogCapture()) {
            IOException result = assertThrows(IOException.class,
                    () -> run(directory.resolve("unchecked.tif"), request -> {
                        throw new IllegalArgumentException(request.uri().toString());
                    }));
            assertSafeException(result);
            CatgisLogger.error("Expected synthetic transport failure", result);
            assertSafe(logs.text());
        }
    }

    @Test
    void interruptedSendPreservesCancellationWithoutRetainingTheOriginalMessage() throws Exception {
        try (LogCapture logs = new LogCapture()) {
            try {
                InterruptedIOException result = assertThrows(InterruptedIOException.class,
                        () -> run(directory.resolve("interrupted.tif"), request -> {
                            throw new InterruptedException(KEY);
                        }));
                assertTrue(Thread.currentThread().isInterrupted());
                assertSafeException(result);
                assertSafe(logs.text());
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void readAndSuppressedCloseFailuresReleaseHandleAndExposeOnlyLocalCode() throws Exception {
        TrackingStream stream = new TrackingStream(tiff());
        stream.readProblem = new IOException(KEY);
        stream.closeProblem = new IOException(encodedKey());
        try (LogCapture logs = new LogCapture()) {
            IOException result = assertThrows(IOException.class,
                    () -> run(directory.resolve("read.tif"),
                            request -> reply(request, 200, "image/tiff", stream)));
            assertTrue(result.getMessage().contains("COPY"));
            assertSafeException(result);
            assertSafe(logs.text());
            assertFalse(logs.text().contains("download finished"));
        }
        assertEquals(1, stream.closeCalls);
    }

    @Test
    void interruptedBodyReadClosesTheResponseAndRestoresTheInterruptFlag() throws Exception {
        TrackingStream stream = new TrackingStream(tiff());
        stream.readProblem = new InterruptedIOException(KEY);
        try {
            InterruptedIOException result = assertThrows(InterruptedIOException.class,
                    () -> run(directory.resolve("read-cancel.tif"),
                            request -> reply(request, 200, "image/tiff", stream)));
            assertTrue(Thread.currentThread().isInterrupted());
            assertSafeException(result);
        } finally {
            Thread.interrupted();
        }
        assertEquals(1, stream.closeCalls);
    }

    @Test
    void failedSuccessfulBodyCloseCannotReturnASuccessfulDownload() throws Exception {
        TrackingStream stream = new TrackingStream(tiff());
        stream.closeProblem = new IOException(KEY);
        try (LogCapture logs = new LogCapture()) {
            IOException result = assertThrows(IOException.class,
                    () -> run(directory.resolve("close.tif"),
                            request -> reply(request, 200, "image/tiff", stream)));
            assertTrue(result.getMessage().contains("CLOSE"));
            assertSafeException(result);
            assertSafe(logs.text());
            assertFalse(logs.text().contains("download finished"));
        }
        assertEquals(1, stream.closeCalls);
    }

    @Test
    void rejectedResponseCloseFailureCannotLeakAsASuppressedException() throws Exception {
        TrackingStream stream = forbiddenBody();
        stream.closeProblem = new IOException(KEY);
        IOException result = assertThrows(IOException.class,
                () -> run(directory.resolve("reject-close.tif"),
                        request -> reply(request, 503, "text/plain", stream)));
        assertSafeException(result);
        assertEquals(1, stream.closeCalls);
        assertEquals(0, stream.readCalls);
    }

    @Test
    void timeoutsRemainDistinctFromUserCancellationAndDoNotInterruptCaller() throws Exception {
        for (IOException timeout : new IOException[] {
                new java.net.http.HttpTimeoutException(KEY), new java.net.SocketTimeoutException(KEY)}) {
            IOException result = assertThrows(IOException.class,
                    () -> run(directory.resolve("timeout.tif"), request -> { throw timeout; }));
            assertTrue(result.getMessage().contains("TIMEOUT"));
            assertFalse(Thread.currentThread().isInterrupted());
            assertSafeException(result);
        }
    }

    @Test
    void preexistingInterruptionDoesNotCreateDirectoriesOrInvokeTransport() throws Exception {
        Path target = directory.resolve("not created").resolve("cancel.tif");
        AtomicInteger calls = new AtomicInteger();
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedIOException.class, () -> run(target, request -> {
                calls.incrementAndGet();
                throw new AssertionError("Must not send after cancellation");
            }));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertEquals(0, calls.get());
        assertFalse(Files.exists(target.getParent()));
    }

    @Test
    void interruptionAfterResponseClosesUnreadBodyAndPreservesOldFile() throws Exception {
        TrackingStream stream = forbiddenBody();
        Path target = previous("late-cancel.tif");
        try {
            assertThrows(InterruptedIOException.class, () -> run(target, request -> {
                Thread.currentThread().interrupt();
                return reply(request, 200, "image/tiff", stream);
            }));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertEquals(1, stream.closeCalls);
        assertEquals(0, stream.readCalls);
        assertEquals("PREVIOUS", Files.readString(target));
    }

    @Test
    void recordStringNeverRendersSensitiveAccessorOrPrivateFilePath() {
        URI uri = OpenTopographyDemService.buildDownloadUri(OpenTopographyDataset.COP30, AREA, KEY);
        var value = new OpenTopographyDemService.FileDownloadResult(
                directory.resolve(KEY.replace('/', '_')).toFile(), AREA, uri);
        assertEquals(uri, value.requestUri());
        assertSafe(value.toString());
        assertFalse(value.toString().contains(directory.toString()));
        assertFalse(value.toString().contains("API_Key"));
    }

    @Test
    void missingBodyDoesNotBecomeAnEmptySuccessfulRaster() throws Exception {
        Path target = previous("missing-body.tif");
        IOException result = assertThrows(IOException.class,
                () -> run(target, request -> reply(request, 200, "image/tiff", null)));
        assertTrue(result.getMessage().contains("BODY"));
        assertSafeException(result);
        assertEquals("PREVIOUS", Files.readString(target));
    }

    @Test
    void failureReadingResponseHeadersStillClosesTheBodyAndDoesNotEchoIt() throws Exception {
        TrackingStream stream = forbiddenBody();
        IOException result = assertThrows(IOException.class,
                () -> run(directory.resolve("headers.tif"), request ->
                        new Reply(request, 200, null, stream, true)));
        assertSafeException(result);
        assertEquals(1, stream.closeCalls);
        assertEquals(0, stream.readCalls);
    }

    private OpenTopographyDemService.FileDownloadResult run(Path target,
            OpenTopographyDemService.RequestSender sender) throws Exception {
        return OpenTopographyDemService.download(OpenTopographyDataset.NASADEM,
                AREA, null, " " + KEY + " ", target.toFile(), sender);
    }

    private Path previous(String name) throws IOException {
        return Files.writeString(directory.resolve(name), "PREVIOUS");
    }

    private static String encodedKey() {
        return URLEncoder.encode(KEY, StandardCharsets.UTF_8);
    }

    private static void assertSafe(String value) {
        assertFalse(value.contains(KEY), "Synthetic credential must not appear in diagnostics");
        assertFalse(value.contains(encodedKey()), "Encoded synthetic credential must not appear");
        assertFalse(value.contains("TEST_ONLY_NOT_A_REAL_KEY"));
    }

    private static void assertSafeException(Throwable error) {
        assertNull(error.getCause(), "Do not attach untrusted exception chains");
        assertEquals(0, error.getSuppressed().length, "Do not retain untrusted close failures");
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        assertSafe(trace.toString());
    }

    private static String queryValue(URI uri, String name) {
        for (String entry : uri.getRawQuery().split("&")) {
            String[] pair = entry.split("=", 2);
            if (URLDecoder.decode(pair[0], StandardCharsets.UTF_8).equals(name)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("Missing request parameter: " + name);
    }

    private static byte[] tiff() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setSample(0, 0, 0, 17);
        image.getRaster().setSample(1, 1, 0, 254);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            assertTrue(ImageIO.write(image, "TIFF", out), "A real TIFF encoder is required");
            return out.toByteArray();
        } finally {
            image.flush();
        }
    }

    private static Reply reply(HttpRequest request, int status, String type, InputStream stream) {
        return new Reply(request, status,
                HttpHeaders.of(Map.of("Content-Type", java.util.List.of(type)), (a, b) -> true),
                stream, false);
    }

    private record Reply(HttpRequest request, int statusCode, HttpHeaders declaredHeaders,
                         InputStream body, boolean failHeaders) implements HttpResponse<InputStream> {
        @Override public HttpHeaders headers() {
            if (failHeaders) { throw new IllegalStateException(KEY); }
            return declaredHeaders;
        }
        @Override public Optional<HttpResponse<InputStream>> previousResponse() { return Optional.empty(); }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return request.uri(); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }

    private static TrackingStream forbiddenBody() {
        TrackingStream value = new TrackingStream(KEY.getBytes(StandardCharsets.UTF_8));
        value.forbidRead = true;
        return value;
    }

    private static final class TrackingStream extends InputStream {
        private final byte[] data;
        private int offset;
        int readCalls;
        int closeCalls;
        boolean forbidRead;
        IOException readProblem;
        IOException closeProblem;
        TrackingStream(byte[] data) { this.data = data; }
        @Override public int read() throws IOException {
            byte[] one = new byte[1];
            return read(one, 0, 1) < 0 ? -1 : one[0] & 255;
        }
        @Override public int read(byte[] bytes, int start, int length) throws IOException {
            readCalls++;
            if (forbidRead) { throw new AssertionError("Rejected response body must never be read"); }
            if (readProblem != null) { throw readProblem; }
            if (length == 0) { return 0; }
            if (offset == data.length) { return -1; }
            int size = Math.min(length, data.length - offset);
            System.arraycopy(data, offset, bytes, start, size);
            offset += size;
            return size;
        }
        @Override public void close() throws IOException {
            closeCalls++;
            if (closeProblem != null) { throw closeProblem; }
        }
    }

    /** Core is already runtimeOnly. Reflect only in this capture helper so its
     * real events are tested without changing production or build dependencies. */
    private static final class LogCapture implements AutoCloseable {
        private final StringWriter writer = new StringWriter();
        private final Object logger = LogManager.getLogger("ar.com.catgis");
        private final Level previousLevel = ((org.apache.logging.log4j.Logger) logger).getLevel();
        private final Class<?> appenderType = Class.forName("org.apache.logging.log4j.core.Appender");
        private final Object appender;
        LogCapture() throws Exception {
            Class<?> writerType = Class.forName("org.apache.logging.log4j.core.appender.WriterAppender");
            appender = writerType.getMethod("createAppender",
                    Class.forName("org.apache.logging.log4j.core.StringLayout"),
                    Class.forName("org.apache.logging.log4j.core.Filter"),
                    Writer.class, String.class, boolean.class, boolean.class)
                    .invoke(null, null, null, writer, "catgis-synthetic-diagnostics", false, false);
            assertNotNull(appender, "Real Log4j appender must be available, never skip");
            appender.getClass().getMethod("start").invoke(appender);
            logger.getClass().getMethod("addAppender", appenderType).invoke(logger, appender);
            logger.getClass().getMethod("setLevel", Level.class).invoke(logger, Level.INFO);
            CatgisLogger.info("capture-canary");
            assertTrue(writer.toString().contains("capture-canary"), "A silent logger is not evidence");
            writer.getBuffer().setLength(0);
        }
        String text() { return writer.toString(); }
        @Override public void close() throws Exception {
            try {
                logger.getClass().getMethod("removeAppender", appenderType).invoke(logger, appender);
                logger.getClass().getMethod("setLevel", Level.class).invoke(logger, previousLevel);
            } finally {
                appender.getClass().getMethod("stop").invoke(appender);
            }
        }
    }
}
