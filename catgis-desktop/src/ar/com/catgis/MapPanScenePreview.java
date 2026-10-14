package ar.com.catgis;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * One bounded screenshot of world layers per active pan; never caches pins,
 * selections, dialogs, attribution or fixed map decorations.
 * Invalidated by any regular Swing repaint (layer, style, CRS, resize).
 */
final class MapPanScenePreview {
    static final long MAX_FRAME_BYTES = 32L * 1024L * 1024L;
    private static final int MAX_BLANK_EDGE_PX = 96;
    record Metrics(int captures, int reuses, int fallbacks,
                   int invalidations, long captureNanos) {}

    private BufferedImage frame;
    private boolean active;
    private int width;
    private int height;
    private double originalMinX;
    private double originalMinY;
    private double originalZoom;
    private int captures;
    private int reuses;
    private int fallbacks;
    private int invalidations;
    private long captureNanos;
    private long revision;

    boolean isActive() {
        return active;
    }

    void begin() {
        frame = null;
        active = true;
        captures = 0;
        reuses = 0;
        fallbacks = 0;
        invalidations = 0;
        captureNanos = 0;
    }

    Metrics end() {
        Metrics metrics = new Metrics(captures, reuses, fallbacks,
                invalidations, captureNanos);
        active = false;
        frame = null;
        return metrics;
    }

    void invalidate() {
        revision++;
        if (frame != null) {
            invalidations++;
        }
        frame = null;
    }

    static boolean withinBudget(int w, int h) {
        return w > 0 && h > 0
                && (long) w * h <= MAX_FRAME_BYTES / 4L;
    }

    void paintOrCapture(Graphics2D target, int w, int h, double minX,
                        double minY, double zoom, Consumer<Graphics2D> worldPainter) {
        if (!active || !withinBudget(w, h) || !Double.isFinite(zoom)
                || zoom <= 0 || !Double.isFinite(minX) || !Double.isFinite(minY)) {
            fallbacks++;
            invalidate();
            worldPainter.accept(target);
            return;
        }
        if (frame != null && w == width && h == height
                && Double.compare(zoom, originalZoom) == 0) {
            double x = (originalMinX - minX) * zoom;
            double y = (minY - originalMinY) * zoom;
            long limitX = Math.min(MAX_BLANK_EDGE_PX, w / 4);
            long limitY = Math.min(MAX_BLANK_EDGE_PX, h / 4);
            if (Double.isFinite(x) && Double.isFinite(y)
                    && Math.abs(x) <= limitX && Math.abs(y) <= limitY) {
                target.drawImage(frame, (int) Math.round(x),
                        (int) Math.round(y), null);
                reuses++;
                return;
            }
        }
        // Large pan offsets trigger a fresh full scene, limiting transient
        // exposed background strips rather than stretching stale imagery.
        invalidate();
        long version = revision;
        long start = System.nanoTime();
        BufferedImage snapshot = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D capture = snapshot.createGraphics();
        try {
            capture.setRenderingHints(target.getRenderingHints());
            worldPainter.accept(capture);
        } finally {
            capture.dispose();
        }
        captureNanos += System.nanoTime() - start;
        captures++;
        // A layer or style repaint during rendering must invalidate the frame.
        if (revision == version) {
            frame = snapshot;
            width = w;
            height = h;
            originalMinX = minX;
            originalMinY = minY;
            originalZoom = zoom;
        }
        target.drawImage(snapshot, 0, 0, null);
    }
}
