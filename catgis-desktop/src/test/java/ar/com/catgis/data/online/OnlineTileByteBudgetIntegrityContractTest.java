package ar.com.catgis.data.online;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineTileByteBudgetIntegrityContractTest {

    @Test
    void oversizedPhysicalBankCannotEscapeBudgetThroughUnderreportedLogicalSize() {
        byte[] bank = new byte[4096];
        // Deliberately expose only 16 logical elements of a larger physical bank.
        DataBufferByte data = new DataBufferByte(bank, 48);
        // RGB band order deliberately differs from standard TYPE_3BYTE_BGR.
        // This must exercise the TYPE_CUSTOM path without making every
        // ordinary Java2D tile untrackable via getBankData().
        PixelInterleavedSampleModel model = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_BYTE, 4, 4, 3, 12, new int[]{0, 1, 2});
        WritableRaster raster = Raster.createWritableRaster(model, data, new Point());
        ColorModel color = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(color, raster, false, null);

        assertEquals(BufferedImage.TYPE_CUSTOM, image.getType());
        assertEquals(48, data.getSize());
        assertEquals(4096L, OnlineTileCache.imageDataBytes(image),
                "The cache must account for the entire underlying Java byte array.");

        Map<String, BufferedImage> cache = synchronizedLru();
        OnlineTileCache.putMemoryTileWithBudget(cache, "oversized", image, 2048L);
        assertFalse(cache.containsKey("oversized"),
                "One image whose backing allocation exceeds its budget must be evicted.");
    }

    @Test
    void duplicateKeyReplacementAndExplicitRemoveMustNotLeaveStaleBudget() {
        Map<String, BufferedImage> cache = synchronizedLru();
        BufferedImage small = image(8); // 256 decoded bytes
        BufferedImage larger = image(16); // 1024 decoded bytes
        assertEquals(256L, OnlineTileCache.imageDataBytes(small));
        assertEquals(1024L, OnlineTileCache.imageDataBytes(larger));

        OnlineTileCache.putMemoryTileWithBudget(cache, "same", small, 1280L);
        OnlineTileCache.putMemoryTileWithBudget(cache, "other", small, 1280L);
        OnlineTileCache.putMemoryTileWithBudget(cache, "same", larger, 1280L);
        assertEquals(2, cache.size());
        assertEquals(1280L, summedBytes(cache));

        cache.remove("same");
        OnlineTileCache.putMemoryTileWithBudget(cache, "third", larger, 1280L);
        assertEquals(1280L, summedBytes(cache));
        assertTrue(cache.containsKey("other"));
        assertTrue(cache.containsKey("third"));
        assertFalse(cache.containsKey("same"));
    }

    @Test
    void budgetEvictionMustFollowTrueLeastRecentlyUsedOrder() {
        Map<String, BufferedImage> cache = synchronizedLru();
        BufferedImage tile = image(8);
        long budget = OnlineTileCache.imageDataBytes(tile) * 2;
        OnlineTileCache.putMemoryTileWithBudget(cache, "a", tile, budget);
        OnlineTileCache.putMemoryTileWithBudget(cache, "b", tile, budget);
        cache.get("a"); // a is recently used, b is oldest
        OnlineTileCache.putMemoryTileWithBudget(cache, "c", tile, budget);

        assertTrue(cache.containsKey("a"));
        assertFalse(cache.containsKey("b"));
        assertTrue(cache.containsKey("c"));
        assertEquals(budget, summedBytes(cache));
    }

    @Test
    void concurrentTileAdmissionsNeverExceedAggregatedPixelBudget() throws Exception {
        Map<String, BufferedImage> cache = synchronizedLru();
        long budget = 16L * 1024L;
        int workers = 6;
        int iterations = 100;
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            @SuppressWarnings("unchecked")
            Future<?>[] futures = new Future<?>[workers];
            for (int worker = 0; worker < workers; worker++) {
                final int workerId = worker;
                futures[worker] = executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new AssertionError("Concurrent workers did not start.");
                    }
                    for (int i = 0; i < iterations; i++) {
                        BufferedImage tile = image(i % 5 == 0 ? 32 : 16);
                        String key = "provider-" + workerId + "-tile-" + (i % 12);
                        OnlineTileCache.putMemoryTileWithBudget(cache, key, tile, budget);
                        cache.get(key);
                        if (i % 7 == 0) {
                            cache.remove(key);
                        }
                        synchronized (cache) {
                            long retained = summedBytes(cache);
                            if (retained > budget) {
                                throw new AssertionError("Pixel budget exceeded: " + retained);
                            }
                        }
                    }
                    return null;
                });
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
            assertTrue(summedBytes(cache) <= budget);
            assertTrue(cache.size() <= 16);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private static Map<String, BufferedImage> synchronizedLru() {
        return Collections.synchronizedMap(new LinkedHashMap<>(32, 0.75f, true));
    }

    private static BufferedImage image(int side) {
        return new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
    }

    private static long summedBytes(Map<String, BufferedImage> cache) {
        synchronized (cache) {
            long bytes = 0L;
            for (BufferedImage tile : cache.values()) {
                bytes += OnlineTileCache.imageDataBytes(tile);
            }
            return bytes;
        }
    }
}
