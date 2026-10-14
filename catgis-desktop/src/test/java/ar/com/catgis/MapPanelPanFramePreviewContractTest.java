package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapPanelPanFramePreviewContractTest {

    private static final class Fixture extends MapPanel {
        int fullScenePaints;
        int liveOverlayPaints;
        Color worldColor = Color.RED;
        boolean allowPreview = true;

        @Override protected boolean isPanPreviewSceneEligible() {
            return allowPreview;
        }

        @Override protected void paintWorldScene(Graphics2D g2) {
            fullScenePaints++;
            g2.setColor(worldColor);
            int x = (int) Math.round((60d - viewMinX) * zoomFactor);
            int y = (int) Math.round(getHeight() - (40d - viewMinY) * zoomFactor);
            g2.fillRect(x, y, 5, 5);
        }

        @Override protected void paintScreenOverlays(Graphics2D g2) {
            liveOverlayPaints++;
            g2.setColor(Color.BLUE);
            g2.fillRect(5, 5, 5, 5);
        }
    }

    private static BufferedImage render(Fixture map) {
        BufferedImage img = new BufferedImage(map.getWidth(), map.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            map.paintComponent(g2);
        } finally {
            g2.dispose();
        }
        return img;
    }

    @Test
    void worldPixelMovesButScreenDecorationStaysFixed() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            panel.beginPanSceneSession();
            BufferedImage first = render(panel);
            assertEquals(Color.RED.getRGB(), first.getRGB(60, 50));
            assertEquals(Color.BLUE.getRGB(), first.getRGB(5, 5));
            assertEquals(1, panel.fullScenePaints);

            panel.viewMinX = -12d;
            panel.syncViewToController();
            panel.requestPanSceneRepaint();
            BufferedImage moved = render(panel);
            assertEquals(Color.RED.getRGB(), moved.getRGB(72, 50));
            assertEquals(Color.BLUE.getRGB(), moved.getRGB(5, 5));
            assertEquals(1, panel.fullScenePaints,
                    "The second world view must reuse translated pixels.");
            assertEquals(2, panel.liveOverlayPaints,
                    "Fixed screen overlays redraw instead of moving.");
            panel.endPanSceneSession();
            BufferedImage after = render(panel);
            assertEquals(2, panel.fullScenePaints,
                    "Release forces a definitive world repaint.");
            assertEquals(Color.RED.getRGB(), after.getRGB(72, 50));
        });
    }

    @Test
    void externalLayerRepaintInvalidatesCachedSceneImmediately() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            panel.beginPanSceneSession();
            render(panel);
            render(panel);
            assertEquals(1, panel.fullScenePaints);
            panel.worldColor = Color.GREEN;
            panel.repaint(); // style, layer or CRS change outside navigation
            BufferedImage newStyle = render(panel);
            assertEquals(2, panel.fullScenePaints);
            assertEquals(Color.GREEN.getRGB(), newStyle.getRGB(60, 50),
                    "External style repaint cannot show stale red snapshot.");
            panel.endPanSceneSession();
        });
    }

    @Test
    void overlongOffsetTriggersBoundedRecaptureWithoutStalePixels() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            panel.beginPanSceneSession();
            render(panel);
            panel.viewMinX = -45; // past w/4 threshold
            panel.syncViewToController();
            panel.requestPanSceneRepaint();
            BufferedImage shifted = render(panel);
            assertEquals(2, panel.fullScenePaints);
            assertEquals(Color.RED.getRGB(), shifted.getRGB(105, 50));
            panel.endPanSceneSession();
        });
    }

    @Test
    void exportNeverUsesTranslatedScreenSnapshot() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            panel.beginPanSceneSession();
            render(panel);
            panel.viewMinX = -12;
            panel.syncViewToController();
            panel.requestPanSceneRepaint();
            render(panel);
            assertEquals(1, panel.fullScenePaints);
            BufferedImage exported = panel.renderMapViewImage(
                    0d, 0d, 1d, 120, 90, false);
            assertEquals(Color.RED.getRGB(), exported.getRGB(60, 50));
            assertEquals(2, panel.fullScenePaints,
                    "CATMAP/export should render full scene at requested view.");
            render(panel);
            assertEquals(3, panel.fullScenePaints,
                    "Export must invalidate the prior interactive preview.");
            panel.endPanSceneSession();
        });
    }

    @Test
    void actualMouseHandlerPanUsesPreviewThenRepaintsOnRelease() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    25, 35, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    37, 35, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            BufferedImage first = render(panel);
            assertEquals(Color.RED.getRGB(), first.getRGB(72, 50));
            assertEquals(1, panel.fullScenePaints);

            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    43, 35, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            BufferedImage reused = render(panel);
            assertEquals(Color.RED.getRGB(), reused.getRGB(78, 50));
            assertEquals(Color.BLUE.getRGB(), reused.getRGB(5, 5));
            assertEquals(1, panel.fullScenePaints);
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    43, 35, MouseEvent.BUTTON1, 0));
            render(panel);
            assertEquals(2, panel.fullScenePaints);
            assertTrue(panel.canZoomPrevious());
        });
    }

    @Test
    void actualMiddleButtonPanUsesPreviewAndEndsCleanly() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    25, 35, MouseEvent.BUTTON2, InputEvent.BUTTON2_DOWN_MASK));
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    37, 35, MouseEvent.NOBUTTON, InputEvent.BUTTON2_DOWN_MASK));
            render(panel);
            assertEquals(1, panel.fullScenePaints);
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    43, 35, MouseEvent.NOBUTTON, InputEvent.BUTTON2_DOWN_MASK));
            render(panel);
            assertEquals(1, panel.fullScenePaints);
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    43, 35, MouseEvent.BUTTON2, 0));
            render(panel);
            assertEquals(2, panel.fullScenePaints);
            assertTrue(panel.canZoomPrevious());
        });
    }

    private static MouseEvent mouse(Fixture p, int event, int x, int y,
                                    int button, int mods) {
        return new MouseEvent(p, event, 123L, mods, x, y, 1, false, button);
    }

    @Test
    void memoryLimitAndFallbackRemainTruthful() throws Exception {
        assertTrue(MapPanScenePreview.withinBudget(120, 90));
        assertFalse(MapPanScenePreview.withinBudget(5000, 5000));
        assertFalse(MapPanScenePreview.withinBudget(Integer.MAX_VALUE,
                Integer.MAX_VALUE));
        assertFalse(MapPanScenePreview.withinBudget(0, 90));
        ReleaseTestSupport.runOnEdt(() -> {
            Fixture panel = new Fixture();
            panel.setSize(120, 90);
            panel.allowPreview = false; // e.g. active drawing/edit
            panel.beginPanSceneSession();
            render(panel);
            render(panel);
            assertEquals(2, panel.fullScenePaints);
            panel.endPanSceneSession();
        });
    }
}
