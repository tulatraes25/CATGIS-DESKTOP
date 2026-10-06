package ar.com.catgis;

/**
 * Pure interaction-state/metrics holder for the pan-preview fast path.
 * No Layer, Graphics, or Swing references. No global/static performance state.
 *
 * <p>During an active pan, the map scene is captured once (snapshot) and
 * then drawn translated by cumulative pixel offsets — avoiding repeated
 * full scene re-renders on every drag event.</p>
 */
final class PanPreviewState {

    /**
     * Immutable session metrics produced by {@link #finish(int)}.
     *
     * @param snapshotElapsedNanos   real wall-clock duration of the one-time snapshot
     * @param previewPaintCount      number of preview paints performed during the session
     * @param maxPreviewPaintNanos   slowest preview paint duration
     * @param fullRendersDuringPan   definitive scene renders between snapshot completion and release (session-local)
     * @param fallback               true when the preview optimization was unavailable for this session
     */
    record Metrics(
            long snapshotElapsedNanos,
            int previewPaintCount,
            long maxPreviewPaintNanos,
            int fullRendersDuringPan,
            boolean fallback) {
    }

    private boolean active;
    private boolean fallback;
    private int offsetX;
    private int offsetY;
    private long snapshotElapsedNanos;
    private int previewPaintCount;
    private long maxPreviewPaintNanos;
    private int fullSceneRenderBaseline;

    PanPreviewState() {
    }

    /**
     * Begin a cached preview session.
     *
     * @param snapshotElapsedNanos     measured wall-clock duration of the snapshot capture
     * @param fullSceneRenderBaseline  panel lifetime definitive-render count captured at snapshot completion
     */
    void begin(long snapshotElapsedNanos, int fullSceneRenderBaseline) {
        this.active = true;
        this.fallback = false;
        this.offsetX = 0;
        this.offsetY = 0;
        this.snapshotElapsedNanos = Math.max(0L, snapshotElapsedNanos);
        this.fullSceneRenderBaseline = fullSceneRenderBaseline;
        this.previewPaintCount = 0;
        this.maxPreviewPaintNanos = 0L;
    }

    /**
     * Begin a fallback session: the preview optimization is unavailable
     * (invalid dimensions or snapshot failure). Translation is disabled and
     * normal definitive rendering remains allowed.
     */
    void beginFallback(long snapshotElapsedNanos, int fullSceneRenderBaseline) {
        begin(snapshotElapsedNanos, fullSceneRenderBaseline);
        this.fallback = true;
    }

    /**
     * Accumulate a pixel translation delta. Called once per drag event.
     * No-op when inactive or in a fallback session.
     */
    void shift(int dx, int dy) {
        if (!active || fallback) {
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
        if (!active || fallback) {
            return;
        }
        previewPaintCount++;
        if (elapsedNanos > maxPreviewPaintNanos) {
            maxPreviewPaintNanos = elapsedNanos;
        }
    }

    /**
     * End the current session and return session-local metrics.
     * After this call, {@link #isActive()} returns {@code false}.
     *
     * @param lifetimeFullSceneRenders the panel's current lifetime definitive-render count
     * @return immutable {@link Metrics}
     */
    Metrics finish(int lifetimeFullSceneRenders) {
        int delta = lifetimeFullSceneRenders - fullSceneRenderBaseline;
        if (delta < 0) {
            delta = 0;
        }
        Metrics metrics = new Metrics(snapshotElapsedNanos, previewPaintCount, maxPreviewPaintNanos, delta, fallback);
        this.active = false;
        return metrics;
    }

    /**
     * Hard reset — deactivate without returning metrics.
     */
    void reset() {
        this.active = false;
        this.fallback = false;
        this.offsetX = 0;
        this.offsetY = 0;
        this.snapshotElapsedNanos = 0L;
        this.previewPaintCount = 0;
        this.maxPreviewPaintNanos = 0L;
        this.fullSceneRenderBaseline = 0;
    }

    // --- accessors (used by MapPanel and tests) ---

    boolean isActive() {
        return active;
    }

    boolean isFallback() {
        return fallback;
    }

    int getOffsetX() {
        return offsetX;
    }

    int getOffsetY() {
        return offsetY;
    }

    long getSnapshotElapsedNanos() {
        return snapshotElapsedNanos;
    }

    int getPreviewPaintCount() {
        return previewPaintCount;
    }

    long getMaxPreviewPaintNanos() {
        return maxPreviewPaintNanos;
    }

    int getFullSceneRenderBaseline() {
        return fullSceneRenderBaseline;
    }
}
