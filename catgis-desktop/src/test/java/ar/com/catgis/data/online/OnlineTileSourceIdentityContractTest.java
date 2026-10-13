package ar.com.catgis.data.online;

import ar.com.catgis.OnlineServiceType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineTileSourceIdentityContractTest {

    @Test
    void sameIdDifferentProviderUrlsNeverShareMemoryOrDiskImages() throws Exception {
        byte[] red = png(Color.RED);
        byte[] blue = png(Color.BLUE);
        AtomicInteger redRequests = new AtomicInteger();
        AtomicInteger blueRequests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/red", exchange -> {
            redRequests.incrementAndGet();
            respondPng(exchange, red);
        });
        server.createContext("/blue", exchange -> {
            blueRequests.incrementAndGet();
            respondPng(exchange, blue);
        });
        server.start();

        String id = "source-reused-" + UUID.randomUUID();
        String origin = "http://127.0.0.1:" + server.getAddress().getPort();
        OnlineRasterSource oldProvider = source(id, origin + "/red/{z}/{x}/{y}.png", 256, "EPSG:3857");
        OnlineRasterSource newProvider = source(id, origin + "/blue/{z}/{x}/{y}.png", 256, "EPSG:3857");
        String redKey = OnlineTileCache.buildKey(oldProvider, 3, 2, 1);
        String blueKey = OnlineTileCache.buildKey(newProvider, 3, 2, 1);
        Path redFile = OnlineTileCache.resolveTileFile(oldProvider, 3, 2, 1).toPath();
        Path blueFile = OnlineTileCache.resolveTileFile(newProvider, 3, 2, 1).toPath();

        try {
            assertNotEquals(redKey, blueKey, "Provider URL is part of memory-cache identity.");
            assertNotEquals(redFile, blueFile, "Provider URL is part of disk-cache identity.");

            assertEquals(Color.RED.getRGB(), awaitTile(oldProvider).getRGB(1, 1));
            assertEquals(Color.BLUE.getRGB(), awaitTile(newProvider).getRGB(1, 1));
            assertEquals(1, redRequests.get());
            assertEquals(1, blueRequests.get());
            assertTrue(Files.isRegularFile(redFile));
            assertTrue(Files.isRegularFile(blueFile));

            // Simulate eviction of RAM entries. Each provider must recover its OWN
            // persisted image without contacting the server again.
            Field cacheField = OnlineTileCache.class.getDeclaredField("MEMORY_CACHE");
            cacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, BufferedImage> memory = (Map<String, BufferedImage>) cacheField.get(null);
            memory.remove(redKey);
            memory.remove(blueKey);

            assertEquals(Color.RED.getRGB(), awaitTile(oldProvider).getRGB(1, 1));
            assertEquals(Color.BLUE.getRGB(), awaitTile(newProvider).getRGB(1, 1));
            assertEquals(1, redRequests.get(), "Disk cache must avoid another HTTP request.");
            assertEquals(1, blueRequests.get(), "Disk cache must avoid another HTTP request.");
        } finally {
            server.stop(0);
            deleteOwnTile(redFile);
            deleteOwnTile(blueFile);
        }
    }

    @Test
    void sourceIdentityIncludesTileGeometryAndNeverUsesRawIdsAsPaths() {
        String id = "../opaque/path";
        String withToken = "https://example.invalid/tiles/{z}/{x}/{y}.png?token=secret-123";
        OnlineRasterSource original = source(id, withToken, 256, "EPSG:3857");
        OnlineRasterSource tileSizeChanged = source(id, withToken, 512, "EPSG:3857");
        OnlineRasterSource crsChanged = source(id, withToken, 256, "EPSG:4326");

        String namespace = OnlineTileCache.cacheNamespace(original);
        assertTrue(namespace.matches("v2-[0-9a-f]{32}"));
        assertFalse(namespace.contains("secret-123"));
        assertFalse(namespace.contains("opaque"));
        assertNotEquals(namespace, OnlineTileCache.cacheNamespace(tileSizeChanged));
        assertNotEquals(namespace, OnlineTileCache.cacheNamespace(crsChanged));
        assertEquals(namespace, OnlineTileCache.cacheNamespace(original),
                "The same configuration must have a reproducible disk namespace.");
        assertEquals(namespace, OnlineTileCache.resolveTileFile(original, 3, 2, 1)
                .toPath().getParent().getParent().getParent().getFileName().toString());
    }

    @Test
    void sanitizedLegacyIdCollisionsMustNotShareDiskDirectories() {
        String sameUrl = "https://example.invalid/tiles/{z}/{x}/{y}.png";
        OnlineRasterSource slash = source("same/name", sameUrl, 256, "EPSG:3857");
        OnlineRasterSource question = source("same?name", sameUrl, 256, "EPSG:3857");

        assertNotEquals(OnlineTileCache.cacheNamespace(slash),
                OnlineTileCache.cacheNamespace(question));
        assertNotEquals(OnlineTileCache.resolveTileFile(slash, 3, 2, 1),
                OnlineTileCache.resolveTileFile(question, 3, 2, 1),
                "Legacy sanitization made both IDs 'same_name' and reused the disk tile.");
    }

    private static OnlineRasterSource source(String id, String url, int size, String crs) {
        return new OnlineRasterSource(id, "Local identity test", "CI", OnlineServiceType.XYZ,
                url, 0, 20, size, "", "", false, crs);
    }

    private static BufferedImage awaitTile(OnlineRasterSource source) throws Exception {
        CountDownLatch repainted = new CountDownLatch(1);
        AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
        AtomicReference<BufferedImage> first = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> first.set(
                OnlineTileCache.getTile(source, 3, 2, 1, () -> {
                    callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
                    repainted.countDown();
                })));
        assertNull(first.get(), "A cold memory cache must not synchronously read disk or HTTP.");
        assertTrue(repainted.await(10, TimeUnit.SECONDS), "Image load must request repaint.");
        assertTrue(callbackOnEdt.get(), "Repaint must execute on Swing EDT.");

        // The repaint is posted before the worker's finally clears PENDING.
        // Wait for the real worker-complete boundary before evicting RAM.
        Field pendingField = OnlineTileCache.class.getDeclaredField("PENDING");
        pendingField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> pending = (Set<String>) pendingField.get(null);
        String key = OnlineTileCache.buildKey(source, 3, 2, 1);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (pending.contains(key) && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        assertFalse(pending.contains(key), "Worker must finish before next cache read.");

        AtomicReference<BufferedImage> warm = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> warm.set(
                OnlineTileCache.getTile(source, 3, 2, 1, null)));
        assertNotNull(warm.get());
        return warm.get();
    }

    private static byte[] png(Color color) throws IOException {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, color.getRGB());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", bytes)) {
            throw new IOException("PNG writer missing from test JRE.");
        }
        return bytes.toByteArray();
    }

    private static void respondPng(com.sun.net.httpserver.HttpExchange exchange, byte[] bytes)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void deleteOwnTile(Path file) throws IOException {
        Files.deleteIfExists(file);
        Path parent = file.getParent();
        for (int i = 0; i < 3; i++) {
            Files.deleteIfExists(parent);
            parent = parent.getParent();
        }
    }
}
