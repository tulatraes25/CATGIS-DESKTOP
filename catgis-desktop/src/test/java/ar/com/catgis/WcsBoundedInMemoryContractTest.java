package ar.com.catgis;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WcsBoundedInMemoryContractTest {

    @Test
    void declaredOversizeMustFailWithoutReadingEvenOneByte() {
        InputStream forbidden = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("Declared oversized response must not read HTTP body.");
            }
        };
        IOException failure = assertThrows(IOException.class,
                () -> WcsClient.readBoundedCoverageBytes(
                        forbidden, WcsClient.MAX_IN_MEMORY_COVERAGE_BYTES + 1L));
        assertTrue(failure.getMessage().contains("32 MiB"));
    }

    @Test
    void unknownLengthStreamMustBeStoppedAtHardLimitWithoutHugeFixture() {
        InputStream generated = new InputStream() {
            private long remaining = WcsClient.MAX_IN_MEMORY_COVERAGE_BYTES + 8192L;

            @Override
            public int read() {
                if (remaining <= 0) {
                    return -1;
                }
                remaining--;
                return 0x35;
            }

            @Override
            public int read(byte[] target, int offset, int length) {
                if (remaining <= 0) {
                    return -1;
                }
                int count = (int) Math.min(remaining, length);
                Arrays.fill(target, offset, offset + count, (byte) 0x35);
                remaining -= count;
                return count;
            }
        };
        IOException failure = assertThrows(IOException.class,
                () -> WcsClient.readBoundedCoverageBytes(generated, -1));
        assertTrue(failure.getMessage().contains("32 MiB"));
    }

    @Test
    void truncatedAndEmptyContentLengthCannotBeReturnedAsCoverage() {
        assertThrows(IOException.class, () -> WcsClient.readBoundedCoverageBytes(
                new ByteArrayInputStream(new byte[]{1, 2}), 3L));
        assertThrows(IOException.class, () -> WcsClient.readBoundedCoverageBytes(
                new ByteArrayInputStream(new byte[0]), -1L));
    }

    @Test
    void validSmallResponseRemainsByteExact() throws Exception {
        byte[] payload = new byte[]{1, 3, 7, 11, 17};
        assertArrayEquals(payload, WcsClient.readBoundedCoverageBytes(
                new ByteArrayInputStream(payload), payload.length));
    }

    @Test
    void oversizedV2HeaderFallsBackToSmallV1ResponseWithoutBodyAllocation() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] valid = new byte[]{73, 73, 42, 0, 8, 0, 0, 0, 1};
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                exchange.getResponseHeaders().set("Content-Type", "image/tiff");
                exchange.sendResponseHeaders(200,
                        WcsClient.MAX_IN_MEMORY_COVERAGE_BYTES + 1L);
                exchange.close();
            } else {
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, valid.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(valid);
                }
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
            byte[] actual = WcsClient.downloadCoverage(
                    url, "dem", "-70,-46,-69,-45", "EPSG:4326", 16, 16);
            assertArrayEquals(valid, actual);
            assertEquals(2, requests.get(), "Oversize v2 must not stop fallback to valid v1.");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void successfulSmallHttpCoverageStillUsesV2WithoutUnnecessaryFallback() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] valid = new byte[]{1, 2, 3, 4};
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/wcs", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, valid.length);
            try (var body = exchange.getResponseBody()) {
                body.write(valid);
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
            assertArrayEquals(valid, WcsClient.downloadCoverage(
                    url, "dem", null, null, 16, 16));
            assertEquals(1, requests.get());
        } finally {
            server.stop(0);
        }
    }
}
