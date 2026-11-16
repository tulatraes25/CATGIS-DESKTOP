package ar.com.catgis.catmap;

import org.locationtech.jts.geom.Envelope;

/**
 * Deterministic atlas framing: centre the feature, add a percentage buffer and
 * expand to the aspect ratio of the map frame so nothing is stretched.
 */
public final class AtlasFraming {

    private static final double MIN_WORLD_SPAN = 1.0e-9;

    private AtlasFraming() {}

    /** World-coordinate envelope for one atlas page. */
    public record Framing(double minX, double minY, double maxX, double maxY) {
        public double width() {
            return maxX - minX;
        }

        public double height() {
            return maxY - minY;
        }

        public double centerX() {
            return (minX + maxX) / 2.0;
        }

        public double centerY() {
            return (minY + maxY) / 2.0;
        }

        /** World units per rendered pixel for a frame of {@code frameWidthPx}. */
        public double worldPerPixel(int frameWidthPx) {
            return width() / frameWidthPx;
        }
    }

    /**
     * Frame a feature envelope for a map frame of the given pixel size.
     *
     * @param featureEnvelope feature bounds in world coordinates
     * @param frameWidthPx    map frame width in pixels (must be positive)
     * @param frameHeightPx   map frame height in pixels (must be positive)
     * @param bufferPercent   extra margin on each side, 0-100
     */
    public static Framing forFeature(Envelope featureEnvelope,
                                     int frameWidthPx,
                                     int frameHeightPx,
                                     double bufferPercent) {
        if (featureEnvelope == null || featureEnvelope.isNull()) {
            throw new IllegalArgumentException("CATMAP atlas: feature extent is required.");
        }
        if (frameWidthPx <= 0 || frameHeightPx <= 0) {
            throw new IllegalArgumentException("CATMAP atlas: frame pixel size must be positive.");
        }
        if (!Double.isFinite(bufferPercent) || bufferPercent < 0 || bufferPercent > 100) {
            throw new IllegalArgumentException("CATMAP atlas: buffer must be between 0 and 100.");
        }
        double width = featureEnvelope.getWidth();
        double height = featureEnvelope.getHeight();
        double minSpan = Math.max(Math.max(width, height) * 0.01, MIN_WORLD_SPAN);
        width = Math.max(width, minSpan);
        height = Math.max(height, minSpan);

        double bufferFactor = 1.0 + (bufferPercent * 2.0 / 100.0);
        width *= bufferFactor;
        height *= bufferFactor;

        double frameAspect = (double) frameHeightPx / (double) frameWidthPx;
        double aspect = height / width;
        if (aspect < frameAspect) {
            height = width * frameAspect;
        } else {
            width = height / frameAspect;
        }

        double centerX = featureEnvelope.centre().x;
        double centerY = featureEnvelope.centre().y;
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;
        return new Framing(centerX - halfWidth, centerY - halfHeight,
                centerX + halfWidth, centerY + halfHeight);
    }
}
