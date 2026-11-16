package ar.com.catgis.layout;

import java.awt.Font;

/**
 * Single typographic metric for layout rendering: sizes stored in the layout
 * model are points, and every renderer converts them to device pixels with the
 * current DPI. Preview and export therefore share physical text sizes.
 */
public final class RenderMetrics {

    private RenderMetrics() {}

    public static final double POINTS_PER_INCH = 72.0;

    /** Convert a point size to device pixels at {@code dpi}. */
    public static double pointsToPixels(double points, double dpi) {
        if (!Double.isFinite(points) || points <= 0 || !Double.isFinite(dpi) || dpi <= 0) {
            return 1.0;
        }
        return points * dpi / POINTS_PER_INCH;
    }

    /** Convert a font (size in points) to a device-pixel font for {@code dpi}. */
    public static Font scaledFont(Font font, double dpi) {
        if (font == null) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }
        float pixels = (float) Math.max(1.0, pointsToPixels(font.getSize2D(), dpi));
        return font.deriveFont(pixels);
    }

    /** Physical size of a device-pixel font back in points. */
    public static double pixelsToPoints(double pixels, double dpi) {
        if (!Double.isFinite(pixels) || pixels <= 0 || !Double.isFinite(dpi) || dpi <= 0) {
            return 0.0;
        }
        return pixels * POINTS_PER_INCH / dpi;
    }
}
