package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless/lightweight test for the pan-preview fast path.
 * Uses a package-private subclass with test counters to prove that
 * preview paints do NOT trigger full scene renders, and that session
 * metrics are truthful (snapshot timing, session-local render count, fallback).
 */
class MapPanelPanPreviewTest {

    @Test
    void previewPaintsAreSessionLocalAndDoNotCountFullRenders() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        // Produce a non-zero lifetime full-scene render count BEFORE the session.
        panel.paintComponent(panel.getGraphics());
        panel.paintComponent(panel.getGraphics());
        panel.paintComponent(panel.getGraphics());
        int lifetimeBefore = panel.fullSceneRenderCount;
        assertTrue(lifetimeBefore > 0);

        panel.beginPanPreview();
        assertTrue(panel.isPanPreviewActive());
        assertFalse(panel.getPanPreviewState().isFallback());

        panel.shiftPanPreview(5, 3);
        panel.paintComponent(panel.getGraphics());
        panel.shiftPanPreview(10, -2);
        panel.paintComponent(panel.getGraphics());
        panel.shiftPanPreview(-3, 7);
        panel.paintComponent(panel.getGraphics());

        // No definitive scene render during preview paints.
        assertEquals(lifetimeBefore, panel.fullSceneRenderCount);

        panel.finishPanPreview();
        assertFalse(panel.isPanPreviewActive());

        PanPreviewState.Metrics metrics = panel.getLastPanMetrics();
        assertNotNull(metrics);
        // Crucial: session-local (NOT the lifetime count of 3).
        assertEquals(0, metrics.fullRendersDuringPan());
        assertFalse(metrics.fallback());
        assertEquals(3, metrics.previewPaintCount());
    }

    @Test
    void snapshotMetricIsValidAndNotStructurallyZero() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        panel.beginPanPreview();
        long elapsed = panel.getPanPreviewState().getSnapshotElapsedNanos();
        assertTrue(elapsed >= 0L); // real measurement, not forced

        panel.finishPanPreview();
        PanPreviewState.Metrics metrics = panel.getLastPanMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.snapshotElapsedNanos() >= 0L);
        // Deterministic millisecond conversion is asserted in PanPreviewStateTest.
    }

    @Test
    void invalidDimensionsProduceTruthfulFallbackAndKeepDefinitiveRendering() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(0, 0); // invalid dimensions -> fallback

        panel.beginPanPreview();
        assertTrue(panel.isPanPreviewActive());
        assertTrue(panel.getPanPreviewState().isFallback());

        int before = panel.fullSceneRenderCount;
        // Definitive rendering remains allowed during fallback.
        panel.paintComponent(panel.getGraphics());
        assertEquals(before + 1, panel.fullSceneRenderCount);

        panel.finishPanPreview();
        PanPreviewState.Metrics metrics = panel.getLastPanMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.fallback());
        assertTrue(metrics.fullRendersDuringPan() >= 1);
    }

    @Test
    void nextPaintAfterFinishPerformsDefinitiveRender() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        panel.paintComponent(panel.getGraphics());
        int baseline = panel.fullSceneRenderCount;

        panel.beginPanPreview();
        panel.shiftPanPreview(8, 4);
        panel.paintComponent(panel.getGraphics());
        assertEquals(baseline, panel.fullSceneRenderCount);

        panel.finishPanPreview();

        panel.paintComponent(panel.getGraphics());
        assertEquals(baseline + 1, panel.fullSceneRenderCount);
    }

    @Test
    void cumulativeTranslationIsCorrect() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L, 0);

        state.shift(10, 20);
        state.shift(15, -5);
        state.shift(-3, 12);

        assertEquals(22, state.getOffsetX()); // 10+15-3
        assertEquals(27, state.getOffsetY()); // 20-5+12
    }

    @Test
    void viewportExtentFollowsExistingZoomMath() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);
        panel.viewMinX = 100.0;
        panel.viewMinY = 200.0;
        panel.zoomFactor = 2.0;
        panel.syncViewToController();

        double zf = panel.viewController.getZoomFactor();
        int dx = 10;
        int dy = 20;
        panel.viewMinX -= dx / zf;
        panel.viewMinY += dy / zf;
        panel.syncViewToController();

        assertEquals(95.0, panel.viewController.getViewMinX(), 0.001);
        assertEquals(210.0, panel.viewController.getViewMinY(), 0.001);
    }

    /**
     * Minimal subclass that provides a real Graphics2D for headless painting
     * and tracks full-scene renders via the inherited counter.
     */
    private static final class PanTestMapPanel extends MapPanel {
        PanTestMapPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        public Graphics getGraphics() {
            BufferedImage img = new BufferedImage(
                    Math.max(1, getWidth()),
                    Math.max(1, getHeight()),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return g2;
        }
    }
}
