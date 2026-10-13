package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WcsNamespaceCapabilitiesContractTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void prefixedWcs201IsParsedWithoutWrongLegacyFallback() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        openServer(exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <wcs:Capabilities xmlns:wcs="http://www.opengis.net/wcs/2.0"
                                      xmlns:ows="http://www.opengis.net/ows/2.0">
                      <wcs:Contents>
                        <wcs:CoverageSummary>
                          <wcs:CoverageId>dem_2026</wcs:CoverageId>
                          <ows:Title>DEM regional</ows:Title>
                          <ows:Abstract>Relieve provincial</ows:Abstract>
                          <wcs:SupportedFormat>image/tiff</wcs:SupportedFormat>
                          <wcs:SupportedFormat>image/png</wcs:SupportedFormat>
                        </wcs:CoverageSummary>
                      </wcs:Contents>
                    </wcs:Capabilities>
                    """);
        });
        List<WcsClient.WcsCoverage> results = WcsClient.getCoverages(url());
        assertEquals(1, requests.get(),
                "WCS 2.0.1 success must not silently fall back to WCS 1.0.0.");
        assertEquals(1, results.size());
        assertEquals("dem_2026", results.get(0).name());
        assertEquals("DEM regional", results.get(0).title());
        assertEquals("Relieve provincial", results.get(0).description());
        assertEquals(List.of("image/tiff", "image/png"), results.get(0).formats());
    }

    @Test
    void genuineWcs100CoverageOfferingBriefIsRecognizedAfterV2Failure() throws Exception {
        AtomicInteger v2 = new AtomicInteger();
        AtomicInteger v1 = new AtomicInteger();
        openServer(exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && query.contains("VERSION=2.0.1")) {
                v2.incrementAndGet();
                respond(exchange, 503, "Temporarily unavailable");
            } else {
                v1.incrementAndGet();
                respond(exchange, 200, """
                        <?xml version="1.0"?>
                        <wcs:WCS_Capabilities xmlns:wcs="http://www.opengis.net/wcs"
                                              xmlns:ows="http://www.opengis.net/ows">
                          <wcs:ContentMetadata>
                            <wcs:CoverageOfferingBrief>
                              <wcs:name>raster_topo</wcs:name>
                              <wcs:label>Topografía</wcs:label>
                              <wcs:description>Cobertura costera</wcs:description>
                            </wcs:CoverageOfferingBrief>
                          </wcs:ContentMetadata>
                        </wcs:WCS_Capabilities>
                        """);
            }
        });
        List<WcsClient.WcsCoverage> results = WcsClient.getCoverages(url());
        assertEquals(1, v2.get());
        assertEquals(1, v1.get());
        assertEquals(1, results.size());
        assertEquals("raster_topo", results.get(0).name());
        assertEquals("Topografía", results.get(0).title());
        assertEquals("Cobertura costera", results.get(0).description());
    }

    @Test
    void malformedCapabilitiesCannotMasqueradeAsAnEmptyCatalogue() throws Exception {
        openServer(exchange -> respond(exchange, 200,
                "<wcs:Capabilities><wcs:CoverageSummary>broken"));
        assertThrows(Exception.class, () -> WcsClient.getCoverages(url()));
    }

    @Test
    void doctypeAndExternalEntityDeclarationsAreRejected() throws Exception {
        String injected = """
                <?xml version="1.0"?>
                <!DOCTYPE wcs:Capabilities [<!ENTITY secret SYSTEM "file:///etc/passwd">]>
                <wcs:Capabilities xmlns:wcs="http://www.opengis.net/wcs/2.0">
                  <wcs:Contents>
                    <wcs:CoverageSummary>
                      <wcs:CoverageId>&secret;</wcs:CoverageId>
                    </wcs:CoverageSummary>
                  </wcs:Contents>
                </wcs:Capabilities>
                """;
        openServer(exchange -> respond(exchange, 200, injected));
        assertThrows(Exception.class, () -> WcsClient.getCoverages(url()));
    }

    @Test
    void oversizedAdvertisedContentLengthIsRejectedWithoutReadingBody() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        openServer(exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, 4L * 1024L * 1024L + 10L);
            exchange.close();
        });
        assertThrows(Exception.class, () -> WcsClient.getCoverages(url()));
        assertEquals(2, requests.get(), "Both WCS versions must fail boundedly.");
    }

    @Test
    void oversizedChunkedResponseStopsAfterBoundedRead() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        openServer(exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, 0);
            try (var output = exchange.getResponseBody()) {
                byte[] chunk = new byte[8192];
                int chunks = 4 * 1024 * 1024 / chunk.length + 2;
                for (int i = 0; i < chunks; i++) {
                    output.write(chunk);
                }
            } catch (IOException closedByBoundedClient) {
                // Closing after the cap is expected and must not hang test servers.
            }
        });
        assertThrows(Exception.class, () -> WcsClient.getCoverages(url()));
        assertEquals(2, requests.get());
    }

    private void openServer(HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/wcs", handler);
        server.start();
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/wcs";
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/xml");
        exchange.sendResponseHeaders(status, payload.length);
        try (var output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }
}
