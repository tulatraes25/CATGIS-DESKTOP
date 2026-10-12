package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
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

class StacCollectionPaginationContractTest {

    private HttpServer server;
    private String origin;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void followsRelativeNextLinkWithReorderedKeysAndWhitespace() throws Exception {
        initServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/collections", exchange -> {
            requests.incrementAndGet();
            boolean second = "page=2".equals(exchange.getRequestURI().getRawQuery());
            String json = second
                    ? "{\"collections\":[{\"id\":\"second\",\"title\":\"Second\"}],\"links\":[]}"
                    : "{\"collections\":[{\"id\":\"first\",\"title\":\"First\"}],"
                            + "\"links\":[ { \"href\" : \"?page=2\", \"rel\" : \"next\" } ]}";
            reply(exchange, json);
        });
        server.start();

        List<StacClient.StacCollection> result = StacClient.getCollections(origin);
        assertEquals(2, requests.get());
        assertEquals(2, result.size());
        assertEquals("first", result.get(0).id());
        assertEquals("second", result.get(1).id());
    }

    @Test
    void rejectsCyclicPaginationRatherThanLoopingOrReturningPartialSuccess() throws Exception {
        initServer();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/collections", exchange -> {
            requests.incrementAndGet();
            reply(exchange, "{\"collections\":[{\"id\":\"one\"}],"
                    + "\"links\":[{\"rel\":\"next\",\"href\":\"/collections\"}]}");
        });
        server.start();

        IOException failure = assertThrows(IOException.class,
                () -> StacClient.getCollections(origin));
        assertTrue(failure.getMessage().contains("pagination"));
        assertEquals(1, requests.get());
    }

    @Test
    void rejectsNonHttpPaginationSchemesWithoutFetchingThem() throws Exception {
        initServer();
        server.createContext("/collections", exchange ->
                reply(exchange, "{\"collections\":[{\"id\":\"one\"}],"
                        + "\"links\":[{\"rel\":\"next\",\"href\":\"file:///etc/passwd\"}]}"));
        server.start();

        IOException failure = assertThrows(IOException.class,
                () -> StacClient.getCollections(origin));
        assertTrue(failure.getMessage().contains("HTTP(S)"));
    }

    private void initServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        origin = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    private static void reply(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
