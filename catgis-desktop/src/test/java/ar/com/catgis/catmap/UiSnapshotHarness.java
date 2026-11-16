package ar.com.catgis.catmap;

import ar.com.catgis.ModuleRegistry;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * UI snapshot harness for T32 (runtime verification preparation).
 * <p>
 * Builds the real CATMAP standalone window and paints it into a PNG. It must run
 * on a display (Actions + Xvfb); on headless environments it reports SKIP.
 * <strong>No runtime claim is made from this harness</strong> until A0.7/F3 are
 * certified; it only produces evidence artefacts for review.
 */
public final class UiSnapshotHarness {

    private UiSnapshotHarness() {}

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("UI_SNAPSHOT_SKIP headless");
            return;
        }
        Path output = Path.of(args != null && args.length > 0 && !args[0].isBlank()
                ? args[0] : "build/ui-snapshots");
        Files.createDirectories(output);
        BufferedImage[] snapshot = new BufferedImage[1];
        JFrame[] window = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                ModuleRegistry.initializeDefaults();
                ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
                Method create = Main.class.getDeclaredMethod("createMainWindow");
                create.setAccessible(true);
                JFrame frame = (JFrame) create.invoke(null);
                frame.setSize(1280, 800);
                frame.setVisible(true);
                frame.validate();
            } catch (InvocationTargetException failure) {
                throw new IllegalStateException("CATMAP window could not be created",
                        failure.getCause());
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("CATMAP window factory unavailable", failure);
            }
        });
        // Allow Swing timers (layout preview) to paint once before capturing.
        Thread.sleep(1200);
        SwingUtilities.invokeAndWait(() -> {
            for (java.awt.Window candidate : java.awt.Window.getWindows()) {
                if (candidate instanceof JFrame frame && frame.isShowing()) {
                    BufferedImage image = new BufferedImage(Math.max(1, frame.getWidth()),
                            Math.max(1, frame.getHeight()), BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D graphics = image.createGraphics();
                    try {
                        frame.paint(graphics);
                    } finally {
                        graphics.dispose();
                    }
                    snapshot[0] = image;
                    window[0] = frame;
                }
            }
        });
        if (snapshot[0] == null) {
            System.out.println("UI_SNAPSHOT_SKIP no-window");
            return;
        }
        Path file = output.resolve("catmap-standalone.png");
        javax.imageio.ImageIO.write(snapshot[0], "png", file.toFile());
        System.out.println("UI_SNAPSHOT_OK " + file.toAbsolutePath());
        if (window[0] != null) {
            SwingUtilities.invokeLater(window[0]::dispose);
        }
        System.exit(0);
    }

    /** Headless environments cannot capture the UI (Actions uses xvfb-run). */
    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    /** True when the output artefact exists and is a readable PNG. */
    public static boolean isSnapshot(File file) {
        if (file == null || !file.isFile() || file.length() == 0) {
            return false;
        }
        try {
            var image = javax.imageio.ImageIO.read(file);
            return image != null && image.getWidth() > 100 && image.getHeight() > 100;
        } catch (Exception invalid) {
            return false;
        }
    }
}
