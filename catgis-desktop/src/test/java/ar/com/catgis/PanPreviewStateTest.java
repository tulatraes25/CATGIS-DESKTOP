package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanPreviewStateTest {

    @Test
    void beginResetsOffsetsAndMarksActive() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L, 0);

        assertTrue(state.isActive());
        assertFalse(state.isFallback());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(0L, state.getSnapshotElapsedNanos());
        assertEquals(0, state.getFullSceneRenderBaseline());
    }

    @Test
    void snapshotDurationIsReportedDeterministically() {
        PanPreviewState state = new PanPreviewState();
        state.begin(12_000_000L, 0); // injected 12 ms

        PanPreviewState.Metrics metrics = state.finish(0);

        assertEquals(12_000_000L, metrics.snapshotElapsedNanos());
        assertEquals(12L, metrics.snapshotElapsedNanos() / 1_000_000L);
    }

    @Test
    void shiftsAccumulateExactly() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L, 0);

        state.shift(10, 20);
        assertEquals(10, state.getOffsetX());
        assertEquals(20, state.getOffsetY());

        state.shift(-5, 15);
        assertEquals(5, state.getOffsetX());
        assertEquals(35, state.getOffsetY());

        state.shift(0, -40);
        assertEquals(5, state.getOffsetX());
        assertEquals(-5, state.getOffsetY());
    }

    @Test
    void shiftWhileInactiveDoesNotCorruptAnything() {
        PanPreviewState first = new PanPreviewState();
        first.begin(0L, 0);
        first.shift(100, 200);

        PanPreviewState dormant = new PanPreviewState();
        dormant.shift(999, 999); // never begun -> no-op
        assertEquals(0, dormant.getOffsetX());
        assertEquals(0, dormant.getOffsetY());

        assertEquals(100, first.getOffsetX());
        assertEquals(200, first.getOffsetY());
    }

    @Test
    void previewPaintMetricAccumulation() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L, 0);

        state.recordPreviewPaint(5_000_000L);  // 5 ms
        state.recordPreviewPaint(12_000_000L); // 12 ms
        state.recordPreviewPaint(3_000_000L);  // 3 ms

        PanPreviewState.Metrics metrics = state.finish(0);
        assertEquals(3, metrics.previewPaintCount());
        assertEquals(12_000_000L, metrics.maxPreviewPaintNanos());
    }

    @Test
    void fullRendersDuringPanIsSessionLocal() {
        // lifetime counter was already 7 before the session; no render during pan -> 0
        PanPreviewState clean = new PanPreviewState();
        clean.begin(1_000_000L, 7);
        PanPreviewState.Metrics cleanMetrics = clean.finish(7);
        assertEquals(0, cleanMetrics.fullRendersDuringPan());

        // lifetime 7 -> 8 (one unexpected definitive render during pan) -> 1
        PanPreviewState regressed = new PanPreviewState();
        regressed.begin(1_000_000L, 7);
        PanPreviewState.Metrics regressedMetrics = regressed.finish(8);
        assertEquals(1, regressedMetrics.fullRendersDuringPan());
    }

    @Test
    void cachedSessionReportsFallbackFalse() {
        PanPreviewState state = new PanPreviewState();
        state.begin(1_000_000L, 0);
        PanPreviewState.Metrics metrics = state.finish(0);
        assertFalse(metrics.fallback());
    }

    @Test
    void fallbackSessionReportsFallbackTrue() {
        PanPreviewState state = new PanPreviewState();
        state.beginFallback(0L, 3);
        assertTrue(state.isFallback());
        state.shift(10, 10); // ignored in fallback
        assertEquals(0, state.getOffsetX());
        PanPreviewState.Metrics metrics = state.finish(4);
        assertTrue(metrics.fallback());
        assertEquals(1, metrics.fullRendersDuringPan());
    }

    @Test
    void fallbackSessionRetainsMeasuredSnapshotAttemptDuration() {
        PanPreviewState state = new PanPreviewState();
        state.beginFallback(7_000_000L, 3);

        PanPreviewState.Metrics metrics = state.finish(3);

        assertTrue(metrics.fallback());
        assertEquals(7_000_000L, metrics.snapshotElapsedNanos());
        assertEquals(7L, metrics.snapshotElapsedNanos() / 1_000_000L);
        assertEquals(0, metrics.fullRendersDuringPan());
    }

    @Test
    void finishResetsActivityAndReturnsMetrics() {
        PanPreviewState state = new PanPreviewState();
        state.begin(100_000_000L, 2);
        state.shift(7, 13);
        state.recordPreviewPaint(1_000_000L);

        PanPreviewState.Metrics metrics = state.finish(2);

        assertFalse(state.isActive());
        assertEquals(100_000_000L, metrics.snapshotElapsedNanos());
        assertEquals(1, metrics.previewPaintCount());
        assertEquals(0, metrics.fullRendersDuringPan());
        assertFalse(metrics.fallback());
    }

    @Test
    void newSessionDoesNotInheritOldState() {
        PanPreviewState state = new PanPreviewState();

        state.begin(50_000_000L, 4);
        state.shift(50, 100);
        state.recordPreviewPaint(9_000_000L);
        state.finish(4);

        assertFalse(state.isActive());

        state.begin(0L, 0);
        assertTrue(state.isActive());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(0, state.getPreviewPaintCount());
        assertEquals(0L, state.getMaxPreviewPaintNanos());
        assertEquals(0L, state.getSnapshotElapsedNanos());
        assertEquals(0, state.getFullSceneRenderBaseline());
    }

    @Test
    void resetClearsAllState() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L, 5);
        state.shift(10, 20);
        state.recordPreviewPaint(5_000_000L);

        state.reset();

        assertFalse(state.isActive());
        assertFalse(state.isFallback());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(0, state.getPreviewPaintCount());
        assertEquals(0L, state.getMaxPreviewPaintNanos());
        assertEquals(0L, state.getSnapshotElapsedNanos());
        assertEquals(0, state.getFullSceneRenderBaseline());
    }
}
