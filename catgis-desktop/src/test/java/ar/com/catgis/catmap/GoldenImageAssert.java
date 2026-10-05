package ar.com.catgis.catmap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pixel-level golden image comparison with tolerance for anti-aliasing variance.
 * <p>
 * Compares two images pixel by pixel. A per-pixel delta of up to
 * {@code maxChannelDelta} (default 8) per ARGB channel is considered
 * "close enough" to account for JDK/OS font rendering differences.
 * If more than {@code tolerancePercent} of pixels differ, the assertion
 * fails with the mismatch percentage.
 */
public final class GoldenImageAssert {

    private GoldenImageAssert() {}

    /**
     * Compare two images with default tolerance (1% pixel mismatch, channel delta 8).
     */
    public static void assertMatches(BufferedImage expected, BufferedImage actual) {
        assertMatches(expected, actual, 1.0, 8);
    }

    /**
     * @param tolerancePercent max percentage of pixels allowed to differ (0.0–100.0)
     * @param maxChannelDelta  max per-channel ARGB difference treated as "same" (1–255)
     */
    public static void assertMatches(BufferedImage expected, BufferedImage actual,
                                      double tolerancePercent, int maxChannelDelta) {
        assertNotNull(expected, "expected image is null");
        assertNotNull(actual, "actual image is null");
        assertEquals(expected.getWidth(), actual.getWidth(), "width mismatch");
        assertEquals(expected.getHeight(), actual.getHeight(), "height mismatch");

        long mismatched = 0;
        long total = (long) expected.getWidth() * expected.getHeight();

        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                if (!pixelsSimilar(expected.getRGB(x, y), actual.getRGB(x, y), maxChannelDelta)) {
                    mismatched++;
                }
            }
        }

        double percent = 100.0 * mismatched / total;
        assertTrue(percent <= tolerancePercent,
                String.format("Image mismatch: %.2f%% pixels differ (tolerance %.2f%%, delta=%d)",
                        percent, tolerancePercent, maxChannelDelta));
    }

    private static boolean pixelsSimilar(int rgb1, int rgb2, int maxDelta) {
        if (rgb1 == rgb2) return true;
        int da = Math.abs(((rgb1 >> 24) & 0xFF) - ((rgb2 >> 24) & 0xFF));
        int dr = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF));
        int dg = Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF));
        int db = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
        return da <= maxDelta && dr <= maxDelta && dg <= maxDelta && db <= maxDelta;
    }

    /**
     * Save an image to a file (for generating golden references).
     */
    public static void saveGoldenImage(BufferedImage image, Path path) throws IOException {
        File parent = path.getParent().toFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        ImageIO.write(image, "png", path.toFile());
    }

    /**
     * Load a golden image from the classpath or file system.
     */
    public static BufferedImage loadGoldenImage(String classpathResource) throws IOException {
        var stream = GoldenImageAssert.class.getClassLoader()
                .getResourceAsStream(classpathResource);
        if (stream == null) {
            throw new IOException("Golden image not found: " + classpathResource);
        }
        return ImageIO.read(stream);
    }
}
