package ar.com.catgis.data.online;

import ar.com.catgis.OnlineServiceType;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineTileDiskCacheEdtContractTest {

    @Test
    void xyzTileDiskHitMustReturnAsynchronouslyAndRepaintOnEdt() throws Exception {
        String id = "catgis-edt-tile-" + UUID.randomUUID();
        OnlineRasterSource source = new OnlineRasterSource(
                id, "Local tile", "CI", OnlineServiceType.XYZ,
                "http://127.0.0.1:1/tiles/{z}/{x}/{y}.png",
                0, 20, 256, "", "", false, "EPSG:3857");

        String appData = System.getenv("APPDATA");
        Path root = appData != null
                ? Path.of(appData, "CATGIS", "online-tile-cache")
                : Path.of(System.getProperty("java.io.tmpdir"), "catgis-online-tiles");
        Path sourceDir = root.resolve(id);
        Path tileFile = sourceDir.resolve("3").resolve("2").resolve("1.png");
        Files.createDirectories(tileFile.getParent());

        BufferedImage sample = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        sample.setRGB(2, 1, Color.ORANGE.getRGB());
        assertTrue(ImageIO.write(sample, "png", tileFile.toFile()));

        CountDownLatch repaint = new CountDownLatch(1);
        AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
        AtomicReference<BufferedImage> first = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> first.set(OnlineTileCache.getTile(
                    source, 3, 2, 1, () -> {
                        callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
                        repaint.countDown();
                    })));

            assertNull(first.get(), "Disk reads during painting must not block EDT.");
            assertTrue(repaint.await(10, TimeUnit.SECONDS),
                    "Worker should decode the cached tile and post an EDT repaint.");
            assertTrue(callbackOnEdt.get());

            AtomicReference<BufferedImage> warm = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() ->
                    warm.set(OnlineTileCache.getTile(source, 3, 2, 1, null)));
            assertNotNull(warm.get());
            assertEquals(Color.ORANGE.getRGB(), warm.get().getRGB(2, 1));
        } finally {
            Files.deleteIfExists(tileFile);
            Files.deleteIfExists(tileFile.getParent());
            Files.deleteIfExists(tileFile.getParent().getParent());
            Files.deleteIfExists(sourceDir);
        }
    }
}
