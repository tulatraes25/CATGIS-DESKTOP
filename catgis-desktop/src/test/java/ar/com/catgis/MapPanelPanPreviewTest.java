package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless/lightweight test for the pan-preview fast path.
 * Uses a package-private subclass with test counters to prove that
 * preview paints do NOT trigger full scene renders.
 */
class MapPanelPanPreviewTest {

    @Test
    void snapshotCausesAtMostOneFullSceneRender() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        int before = panel.fullSceneRenderCount;
        panel.beginPanPreview();
        // The snapshot captures the scene once during beginPanPreview
        // (not through paintComponent, so the counter only increments
        // in the definitive paintComponent path)
        int after = panel.fullSceneRenderCount;

        // Snapshot is done inside beginPanPreview directly via renderScrollableScene,
        // not through paintComponent — so the counter should be unchanged
        assertEquals(before, after);

        panel.finishPanPreview();
    }

    @Test
    void previewPaintsDoNotIncrementFullSceneRenderCounter() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        // Initial definitive render
        panel.paintComponent(panel.getGraphics());
        int baseline = panel.fullSceneRenderCount;
        assertEquals(1, baseline);

        // Begin pan preview (snapshot captured)
        panel.beginPanPreview();
        assertTrue(panel.isPanPreviewActive());

        // Simulate multiple drag events and preview repaints
        panel.shiftPanPreview(5, 3);
        panel.paintComponent(panel.getGraphics());
        panel.shiftPanPreview(10, -2);
        panel.paintComponent(panel.getGraphics());
        panel.shiftPanPreview(-3, 7);
        panel.paintComponent(panel.getGraphics());

        // The counter must NOT have increased during preview paints
        assertEquals(baseline, panel.fullSceneRenderCount);

        // Finish preview
        panel.finishPanPreview();
        assertFalse(panel.isPanPreviewActive());
    }

    @Test
    void nextPaintAfterFinishPerformsDefinitiveRender() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);

        // Initial definitive render
        panel.paintComponent(panel.getGraphics());
        int baseline = panel.fullSceneRenderCount;

        // Pan preview session
        panel.beginPanPreview();
        panel.shiftPanPreview(8, 4);
        panel.paintComponent(panel.getGraphics());
        assertEquals(baseline, panel.fullSceneRenderCount);

        // Finish
        panel.finishPanPreview();

        // Next paint should be definitive
        panel.paintComponent(panel.getGraphics());
        assertEquals(baseline + 1, panel.fullSceneRenderCount);
    }

    @Test
    void cumulativeTranslationIsCorrect() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L);

        state.shift(10, 20);
        state.shift(15, -5);
        state.shift(-3, 12);

        // Cumulative: dx=10+15-3=22, dy=20-5+12=27
        assertEquals(22, state.getOffsetX());
        assertEquals(27, state.getOffsetY());

        state.finish();
    }

    @Test
    void viewportExtentFollowsExistingZoomMath() {
        PanTestMapPanel panel = new PanTestMapPanel();
        panel.setSize(200, 150);
        // Set a known viewport
        panel.viewMinX = 100.0;
        panel.viewMinY = 200.0;
        panel.zoomFactor = 2.0;
        panel.syncViewToController();

        // Simulate a left-button pan of dx=10, dy=20 pixels
        // Using the same formula as MouseHandler:
        //   viewMinX -= dx / zoomFactor => 100 - 10/2 = 95.0
        //   viewMinY += dy / zoomFactor => 200 + 20/2 = 210.0
        double zf = panel.viewController.getZoomFactor();
        int dx = 10;
        int dy = 20;
        panel.viewMinX -= dx / zf;
        panel.viewMinY += dy / zf;
        panel.syncViewToController();

        assertEquals(95.0, panel.viewController.getViewMinX(), 0.001);
        assertEquals(210.0, panel.viewController.getViewMinY(), 0.001);
    }

    @Test
    void panPreviewStateAccumulatesAcrossMultipleShifts() {
        PanPreviewState state = new PanPreviewState();
        state.begin(0L);

        // Simulate 5 drag events
        state.shift(2, 3);
        state.shift(4, 1);
        state.shift(-1, -2);
        state.shift(6, 0);
        state.shift(0, 5);

        assertEquals(11, state.getOffsetX()); // 2+4-1+6+0
        assertEquals(7, state.getOffsetY());  // 3+1-2+0+5

        state.finish();
    }

    /**
     * Minimal subclass that provides a real Graphics2D for headless painting
     * and tracks full-scene renders via the inherited counter.
     */
    private static final class PanTestMapPanel extends MapPanel {
        PanTestMapPanel() {
            setBackground(Color.WHITE);
        }

        /**
         * Provide a real Graphics2D from a small BufferedImage for headless testing.
         */
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