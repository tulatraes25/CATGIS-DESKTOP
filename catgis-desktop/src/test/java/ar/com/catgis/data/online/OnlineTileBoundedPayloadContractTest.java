package ar.com.catgis.data.online;

import ar.com.catgis.OnlineServiceType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineTileBoundedPayloadContractTest {

    @TempDir
    Path temporary;

    @Test
    void boundedInputRejectsDeclaredAndUnknownLengthOverflowAndTruncation() {
        byte[] oversized = new byte[OnlineTileCache.MAX_TILE_BYTES + 1];
        assertThrows(IOException.class, () -> OnlineTileCache.readLimitedTileBytes(
                new ByteArrayInputStream(oversized), -1L));
        assertThrows(IOException.class, () -> OnlineTileCache.readLimitedTileBytes(
                new ByteArrayInputStream(new byte[0]), OnlineTileCache.MAX_TILE_BYTES + 1L));
        assertThrows(IOException.class, () -> OnlineTileCache.readLimitedTileBytes(
                new ByteArrayInputStream(new byte[]{1, 2}), 3L));
    }

    @Test
    void imageReaderRejectsCompressedDimensionBombBeforeRasterDecode() throws Exception {
        BufferedImage tall = new BufferedImage(
                OnlineTileCache.MAX_TILE_DIMENSION + 1, 2, BufferedImage.TYPE_INT_RGB);
        byte[] png = png(tall);
        assertTrue(png.length < OnlineTileCache.MAX_TILE_BYTES,
                "The attack image must be small in compressed form.");
        assertThrows(IOException.class, () -> OnlineTileCache.decodeTileBytes(png));

        Path cached = temporary.resolve("oversized-dimensions.png");
        Files.write(cached, png);
        assertThrows(IOException.class, () -> OnlineTileCache.readCachedTile(cached.toFile()));
    }

    @Test
    void diskCacheRefusesFilesLargerThanTheCompressedByteLimit() throws Exception {
        Path file = temporary.resolve("huge-cache.png");
        Files.write(file, new byte[OnlineTileCache.MAX_TILE_BYTES + 1]);
        assertThrows(IOException.class, () -> OnlineTileCache.readCachedTile(file.toFile()));
    }

    @Test
    void tinyValidPngStillDecodesPixelExactlyFromNetworkAndDiskBytes() throws Exception {
        BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        source.setRGB(2, 1, Color.MAGENTA.getRGB());
        byte[] png = png(source);
        assertEquals(Color.MAGENTA.getRGB(),
                OnlineTileCache.decodeTileBytes(png).getRGB(2, 1));
        Path cached = temporary.resolve("legitimate.png");
        Files.write(cached, png);
        assertEquals(Color.MAGENTA.getRGB(),
                OnlineTileCache.readCachedTile(cached.toFile()).getRGB(2, 1));
        assertEquals(png.length, OnlineTileCache.readLimitedTileBytes(
                new ByteArrayInputStream(png), png.length).length);
    }

    @Test
    void chunkedOversizedHttpTileCannotPoisonMemoryOrDiskCache() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tiles", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, 0); // Chunked: no Content-Length.
            try (var output = exchange.getResponseBody()) {
                byte[] chunk = new byte[8192];
                int chunks = (OnlineTileCache.MAX_TILE_BYTES / chunk.length) + 2;
                for (int i = 0; i < chunks; i++) {
                    output.write(chunk);
                }
            } catch (IOException expectedAfterClientRejectsOversizedBody) {
                // The bounded client is allowed to close the stream early.
            }
        });
        server.start();

        String id = "oversized-chunked-" + UUID.randomUUID();
        OnlineRasterSource source = new OnlineRasterSource(
                id, "Huge tile", "CI", OnlineServiceType.XYZ,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/tiles/{z}/{x}/{y}.png",
                0, 20, 256, "", "", false, "EPSG:3857");
        Path cached = cachePath(source);
        try {
            SwingUtilities.invokeAndWait(() ->
                    assertNull(OnlineTileCache.getTile(source, 3, 2, 1, null)));
            String failure = awaitFailure(id);
            assertTrue(failure.contains("exceeds maximum encoded size"), failure);
            assertEquals(1, requests.get());
            assertFalse(Files.exists(cached), "Rejected HTTP bytes must never become a disk tile.");
        } finally {
            server.stop(0);
            deleteOwnCacheDirectories(cached);
        }
    }

    @Test
    void tinyHttpPngStillRepaintsAndCachesWithoutSecondRequest() throws Exception {
        byte[] png = png(sample(Color.BLUE));
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tiles", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, png.length);
            try (var output = exchange.getResponseBody()) {
                output.write(png);
            }
        });
        server.start();
        String id = "bounded-valid-" + UUID.randomUUID();
        OnlineRasterSource source = new OnlineRasterSource(
                id, "Valid tile", "CI", OnlineServiceType.XYZ,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/tiles/{z}/{x}/{y}.png",
                0, 20, 256, "", "", false, "EPSG:3857");
        Path cached = cachePath(source);
        try {
            CountDownLatch repaint = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(() ->
                    assertNull(OnlineTileCache.getTile(source, 3, 2, 1, repaint::countDown)));
            assertTrue(repaint.await(10, TimeUnit.SECONDS), "Loaded tile must notify map painter.");
            AtomicReference<BufferedImage> warm = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() ->
                    warm.set(OnlineTileCache.getTile(source, 3, 2, 1, null)));
            assertNotNull(warm.get());
            assertEquals(Color.BLUE.getRGB(), warm.get().getRGB(1, 1));
            assertEquals(1, requests.get());
            assertTrue(Files.isRegularFile(cached));
        } finally {
            server.stop(0);
            deleteOwnCacheDirectories(cached);
        }
    }

    @Test
    void diskNeighborPrefetchWarmsMemoryFromWorkerWithBoundedDecode() throws Exception {
        String id = "bounded-prefetch-" + UUID.randomUUID();
        OnlineRasterSource source = new OnlineRasterSource(
                id, "Cached prefetch", "CI", OnlineServiceType.XYZ,
                "http://127.0.0.1:1/tiles/{z}/{x}/{y}.png",
                0, 20, 256, "", "", false, "EPSG:3857");
        BufferedImage tile = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < tile.getHeight(); y++) {
            for (int x = 0; x < tile.getWidth(); x++) {
                tile.setRGB(x, y, new Color((x * 7) % 256, (y * 11) % 256,
                        (x * y * 3) % 256).getRGB());
            }
        }
        Path cached = cachePath(source);
        Files.createDirectories(cached.getParent());
        byte[] validPng = png(tile);
        assertTrue(validPng.length > 100, "Prefetch only loads nontrivial cached tiles.");
        Files.write(cached, validPng);
        try {
            SwingUtilities.invokeAndWait(() -> OnlineTileCache.prefetchNeighbors(source, 3, 1, 1));
            Field field = OnlineTileCache.class.getDeclaredField("MEMORY_CACHE");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, BufferedImage> memory = (Map<String, BufferedImage>) field.get(null);
            String cacheKey = id + "_3_2_1";
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!memory.containsKey(cacheKey) && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            assertNotNull(memory.get(cacheKey), "Background prefetch must warm the expected tile.");
            assertEquals(tile.getRGB(7, 9), memory.get(cacheKey).getRGB(7, 9));
        } finally {
            deleteOwnCacheDirectories(cached);
        }
    }

    @Test
    void prefetchPendingMustNeverBlockForegroundTileRepaint() throws Exception {
        byte[] png = png(sample(Color.GREEN));
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tiles", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, png.length);
            try (var output = exchange.getResponseBody()) {
                output.write(png);
            }
        });
        server.start();
        String id = "prefetch-foreground-" + UUID.randomUUID();
        OnlineRasterSource source = new OnlineRasterSource(
                id, "Foreground while prefetch pending", "CI", OnlineServiceType.XYZ,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/tiles/{z}/{x}/{y}.png",
                0, 20, 256, "", "", false, "EPSG:3857");
        Path cached = cachePath(source);

        Field field = OnlineTileCache.class.getDeclaredField("PREFETCH_PENDING");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> prefetch = (Set<String>) field.get(null);
        Method keyBuilder = OnlineTileCache.class.getDeclaredMethod(
                "buildKey", OnlineRasterSource.class, int.class, int.class, int.class);
        keyBuilder.setAccessible(true);
        String key = (String) keyBuilder.invoke(null, source, 3, 2, 1);
        assertTrue(prefetch.add(key));
        try {
            CountDownLatch repaint = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(() ->
                    assertNull(OnlineTileCache.getTile(source, 3, 2, 1, repaint::countDown)));
            assertTrue(repaint.await(10, TimeUnit.SECONDS),
                    "Foreground tile must repaint even if prefetch owns the same key.");
            AtomicReference<BufferedImage> displayed = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() ->
                    displayed.set(OnlineTileCache.getTile(source, 3, 2, 1, null)));
            assertNotNull(displayed.get());
            assertEquals(Color.GREEN.getRGB(), displayed.get().getRGB(1, 1));
            assertEquals(1, requests.get());
        } finally {
            prefetch.remove(key);
            server.stop(0);
            deleteOwnCacheDirectories(cached);
        }
    }

    @Test
    void decodedMemoryCacheHasRealByteBudgetAndEvictsLeastRecentlyUsed() {
        Map<String, BufferedImage> cache = new LinkedHashMap<>(8, 0.75f, true);
        BufferedImage a = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        BufferedImage b = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        BufferedImage c = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        BufferedImage d = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        assertEquals(64L, OnlineTileCache.imageDataBytes(a));
        long budget = OnlineTileCache.imageDataBytes(a) * 2;

        OnlineTileCache.putMemoryTileWithBudget(cache, "a", a, budget);
        OnlineTileCache.putMemoryTileWithBudget(cache, "b", b, budget);
        OnlineTileCache.putMemoryTileWithBudget(cache, "c", c, budget);
        assertFalse(cache.containsKey("a"), "Oldest decoded image must be evicted by bytes.");
        assertTrue(cache.containsKey("b"));
        assertTrue(cache.containsKey("c"));

        cache.get("b"); // Access b, now c is least recently used.
        OnlineTileCache.putMemoryTileWithBudget(cache, "d", d, budget);
        assertFalse(cache.containsKey("c"));
        assertTrue(cache.containsKey("b"));
        assertTrue(cache.containsKey("d"));
        assertEquals(2, cache.size());

        OnlineTileCache.putMemoryTileWithBudget(cache, "huge", d,
                OnlineTileCache.imageDataBytes(d) - 1);
        assertFalse(cache.containsKey("huge"),
                "An entry larger than its own budget must not remain cached.");
        assertTrue(cache.isEmpty(), "Budget below one image must evict every entry.");
    }

    private static String awaitFailure(String sourceId) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        String message = "";
        while (message.isBlank() && System.nanoTime() < deadline) {
            message = OnlineTileCache.getRecentSourceFailure(sourceId, 120000L);
            if (message.isBlank()) {
                Thread.sleep(10);
            }
        }
        assertFalse(message.isBlank(), "Expected asynchronous tile-load failure.");
        return message;
    }

    private static BufferedImage sample(Color color) {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, color.getRGB());
        return image;
    }

    private static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", bytes)) {
            throw new IOException("PNG writer unavailable.");
        }
        return bytes.toByteArray();
    }

    private static Path cachePath(OnlineRasterSource source) {
        String appData = System.getenv("APPDATA");
        Path root = appData != null
                ? Path.of(appData, "CATGIS", "online-tile-cache")
                : Path.of(System.getProperty("java.io.tmpdir"), "catgis-online-tiles");
        return root.resolve(source.getId()).resolve("3").resolve("2").resolve("1.png");
    }

    private static void deleteOwnCacheDirectories(Path tile) throws IOException {
        Files.deleteIfExists(tile);
        Path parent = tile.getParent();
        for (int i = 0; i < 3; i++) {
            Files.deleteIfExists(parent);
            parent = parent.getParent();
        }
    }
}
