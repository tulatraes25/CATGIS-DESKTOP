package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineWmsDiskCacheEdtContractTest {

    @Test
    void readsExistingPngOffEdtAndRepaintsBeforeReturningCachedImage() throws Exception {
        // Unique request identity prevents another test or run from warming memory.
        String url = "http://127.0.0.1:1/cached-wms-edt-" + UUID.randomUUID();
        String key = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-1").digest(url.getBytes(StandardCharsets.UTF_8)));
        Path file = Path.of(System.getProperty("java.io.tmpdir"),
                "catgis-online-wms", key + ".png");
        Files.createDirectories(file.getParent());

        BufferedImage sample = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        sample.setRGB(2, 1, Color.MAGENTA.getRGB());
        assertTrue(ImageIO.write(sample, "png", file.toFile()));

        CountDownLatch loaded = new CountDownLatch(1);
        AtomicBoolean callbackOnEdt = new AtomicBoolean();
        AtomicReference<BufferedImage> first = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> first.set(OnlineWmsImageCache.getImage(
                    url, "image/png", () -> {
                        callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
                        loaded.countDown();
                    })));

            assertNull(first.get(), "A disk hit must not decode images synchronously on map paint.");
            assertTrue(loaded.await(10, TimeUnit.SECONDS),
                    "Off-EDT cache decode must schedule a repaint.");
            assertTrue(callbackOnEdt.get(), "The repaint callback must run on the EDT.");

            AtomicReference<BufferedImage> warm = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() ->
                    warm.set(OnlineWmsImageCache.getImage(url, "image/png", null)));
            assertNotNull(warm.get());
            assertEquals(Color.MAGENTA.getRGB(), warm.get().getRGB(2, 1));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
