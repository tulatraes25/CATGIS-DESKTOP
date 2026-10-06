package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanPreviewStateTest {

    @Test
    void beginResetsOffsetsAndMarksActive() {
        PanPreviewState state = new PanPreviewState();
        state.begin(1000L);

        assertTrue(state.isActive());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(1000L, state.getStartedNanos());
    }

    @Test
    void shiftsAccumulateExactly() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L);

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
    void shiftWhileInactiveDoesNotCorruptActiveSession() {
        PanPreviewState first = new PanPreviewState();
        first.begin(0L);
        first.shift(100, 200);

        // A separate instance that was never begun — shift is a no-op
        PanPreviewState dormant = new PanPreviewState();
        dormant.shift(999, 999);
        assertEquals(0, dormant.getOffsetX());
        assertEquals(0, dormant.getOffsetY());

        // The active session is unaffected
        assertEquals(100, first.getOffsetX());
        assertEquals(200, first.getOffsetY());
    }

    @Test
    void previewPaintMetricAccumulation() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L);

        state.recordPreviewPaint(5_000_000L);  // 5 ms
        state.recordPreviewPaint(12_000_000L); // 12 ms
        state.recordPreviewPaint(3_000_000L);  // 3 ms

        assertEquals(3, state.getPreviewPaintCount());
        assertEquals(12_000_000L, state.getMaxPreviewPaintNanos());
    }

    @Test
    void finishResetsActivityAndReturnsMetrics() {
        PanPreviewState state = new PanPreviewState();
        state.begin(100_000_000L); // 100 us
        state.shift(7, 13);
        state.recordPreviewPaint(1_000_000L);

        long snapshotMs = state.finish();

        assertFalse(state.isActive());
        // snapshotMs = (snapshotNanos - startedNanos) / 1_000_000
        // snapshotNanos was set to startedNanos in begin(), so result = 0
        assertEquals(0L, snapshotMs);
    }

    @Test
    void newSessionDoesNotInheritOldOffsets() {
        PanPreviewState state = new PanPreviewState();

        // First session
        state.begin(0L);
        state.shift(50, 100);
        state.finish();

        assertFalse(state.isActive());

        // Second session — must start clean
        state.begin(1_000_000_000L);
        assertTrue(state.isActive());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(0, state.getPreviewPaintCount());
        assertEquals(0L, state.getMaxPreviewPaintNanos());
    }

    @Test
    void resetClearsAllState() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L);
        state.shift(10, 20);
        state.recordPreviewPaint(5_000_000L);

        state.reset();

        assertFalse(state.isActive());
        assertEquals(0, state.getOffsetX());
        assertEquals(0, state.getOffsetY());
        assertEquals(0, state.getPreviewPaintCount());
        assertEquals(0L, state.getMaxPreviewPaintNanos());
    }
}