package ar.com.catgis;

/**
 * Pure interaction-state/metrics holder for the pan-preview fast path.
 * No Layer, Graphics, or Swing references.
 *
 * <p>During an active pan, the map scene is captured once (snapshot) and
 * then drawn translated by cumulative pixel offsets — avoiding repeated
 * full scene re-renders on every drag event.</p>
 */
final class PanPreviewState {

    private boolean active;
    private int offsetX;
    private int offsetY;
    private long startedNanos;
    private long snapshotNanos;
    private int previewPaintCount;
    private long maxPreviewPaintNanos;

    PanPreviewState() {
    }

    /**
     * Begin a new pan-preview session. Resets offsets and metrics.
     *
     * @param snapshotNanos the time (in ns, from {@code System.nanoTime()})
     *                      when the scene snapshot was captured
     */
    void begin(long snapshotNanos) {
        this.active = true;
        this.offsetX = 0;
        this.offsetY = 0;
        this.startedNanos = snapshotNanos;
        this.snapshotNanos = snapshotNanos;
        this.previewPaintCount = 0;
        this.maxPreviewPaintNanos = 0L;
    }

    /**
     * Accumulate a pixel translation delta. Called once per drag event.
     */
    void shift(int dx, int dy) {
        if (!active) {
            return;
        }
        this.offsetX += dx;
        this.offsetY += dy;
    }

    /**
     * Record the wall-clock cost of one preview paint.
     *
     * @param elapsedNanos nanoseconds the preview paint took
     */
    void recordPreviewPaint(long elapsedNanos) {
        if (!active) {
            return;
        }
        previewPaintCount++;
        if (elapsedNanos > maxPreviewPaintNanos) {
            maxPreviewPaintNanos = elapsedNanos;
        }
    }

    /**
     * End the current session. Returns metrics for diagnostic logging.
     * After this call, {@link #isActive()} returns {@code false}.
     *
     * @return snapshot duration in milliseconds
     */
    long finish() {
        long snapshotMs = (snapshotNanos - startedNanos) / 1_000_000L;
        this.active = false;
        return snapshotMs;
    }

    /**
     * Hard reset — deactivate without returning metrics.
     */
    void reset() {
        this.active = false;
        this.offsetX = 0;
        this.offsetY = 0;
        this.startedNanos = 0L;
        this.snapshotNanos = 0L;
        this.previewPaintCount = 0;
        this.maxPreviewPaintNanos = 0L;
    }

    // --- accessors (used by MapPanel and tests) ---

    boolean isActive() {
        return active;
    }

    int getOffsetX() {
        return offsetX;
    }

    int getOffsetY() {
        return offsetY;
    }

    long getStartedNanos() {
        return startedNanos;
    }

    long getSnapshotNanos() {
        return snapshotNanos;
    }

    int getPreviewPaintCount() {
        return previewPaintCount;
    }

    long getMaxPreviewPaintNanos() {
        return maxPreviewPaintNanos;
    }
}