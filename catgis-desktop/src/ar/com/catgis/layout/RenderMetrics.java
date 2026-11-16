package ar.com.catgis.layout;

import java.awt.Font;

/**
 * Single typographic metric for layout rendering. Layout font sizes are stored
 * as <em>baseline pixels</em> at 96 DPI (the historical CATMAP preview metric, so
 * existing templates keep their exact look); every renderer converts them to
 * device pixels with the current DPI. Preview and export therefore share the
 * same physical text size, and the checked-in golden baselines stay valid.
 */
public final class RenderMetrics {

    private RenderMetrics() {}

    /** Baseline DPI in which layout font sizes are expressed. */
    public static final double BASELINE_DPI = 96.0;

    /** Convert a baseline pixel size to device pixels at {@code dpi}. */
    public static double pointsToPixels(double points, double dpi) {
        if (!Double.isFinite(points) || points <= 0 || !Double.isFinite(dpi) || dpi <= 0) {
            return 1.0;
        }
        return points * dpi / BASELINE_DPI;
    }

    /** Convert a font to a device-pixel font for {@code dpi}. */
    public static Font scaledFont(Font font, double dpi) {
        if (font == null) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }
        float pixels = (float) Math.max(1.0, pointsToPixels(font.getSize2D(), dpi));
        return font.deriveFont(pixels);
    }

    /** Physical baseline-pixel size back from device pixels at {@code dpi}. */
    public static double pixelsToPoints(double pixels, double dpi) {
        if (!Double.isFinite(pixels) || pixels <= 0 || !Double.isFinite(dpi) || dpi <= 0) {
            return 0.0;
        }
        return pixels * BASELINE_DPI / dpi;
    }
}