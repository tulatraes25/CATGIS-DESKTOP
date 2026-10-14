package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A0.2 characterization: real MapPanel controller + mouse handlers must
 * preserve navigation history without relying on display-server timing.
 */
class MapPanelNavigationHistoryContractTest {

    @Test
    void resizedPanelZoomUsesActualCenterAndRoundTripsInHistory() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = new MapPanel();
            panel.setSize(320, 200);
            panel.getViewController().restoreView(10d, 20d, 2d);
            panel.syncViewFromController();

            panel.zoomIn();
            assertEquals(2.4d, panel.getZoomFactor(), 0.000000001d);
            assertEquals(90d, panel.getViewMinX()
                    + 320d / (2d * panel.getZoomFactor()), 0.000000001d);
            assertEquals(70d, panel.getViewMinY()
                    + 200d / (2d * panel.getZoomFactor()), 0.000000001d);
            double zoomedX = panel.getViewMinX();
            double zoomedY = panel.getViewMinY();

            assertTrue(panel.canZoomPrevious());
            panel.zoomPrevious();
            assertEquals(10d, panel.getViewMinX(), 0.000000001d);
            assertEquals(20d, panel.getViewMinY(), 0.000000001d);
            assertEquals(2d, panel.getZoomFactor(), 0.000000001d);
            assertFalse(panel.canZoomPrevious(),
                    "Duplicate pre-zoom samples must not create phantom states.");
            assertTrue(panel.canZoomNext());

            panel.zoomNext();
            assertEquals(zoomedX, panel.getViewMinX(), 0.000000001d);
            assertEquals(zoomedY, panel.getViewMinY(), 0.000000001d);
            assertEquals(2.4d, panel.getZoomFactor(), 0.000000001d);
            assertFalse(panel.canZoomNext());
        });
    }

    @Test
    void zoomAfterPreviousDiscardsForwardBranchAndDoesNotAddDuplicateStates()
            throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = new MapPanel();
            panel.setSize(300, 180);
            panel.zoomIn();
            panel.zoomIn();
            assertTrue(panel.canZoomPrevious());
            panel.zoomPrevious();
            assertTrue(panel.canZoomNext());

            panel.zoomOut();
            assertFalse(panel.canZoomNext(),
                    "New zoom must replace the abandoned redo branch.");
            panel.zoomPrevious();
            assertEquals(1.2d, panel.getZoomFactor(), 0.000000001d);
        });
    }

    @Test
    void wheelAnchorRemainsStableAfterPanelResize() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = new MapPanel();
            panel.setSize(180, 160);
            panel.getViewController().restoreView(-25d, 30d, 2d);
            panel.syncViewFromController();
            // Cursor at (30, 45) maps to x=-10 and y=87.5.
            panel.handleZoom(-1, 30, 45);
            assertEquals(-10d, panel.getViewMinX() + 30d / panel.getZoomFactor(),
                    0.000000001d);
            assertEquals(87.5d, panel.getViewMinY()
                    + (160d - 45d) / panel.getZoomFactor(), 0.000000001d);
            panel.zoomPrevious();
            assertEquals(-25d, panel.getViewMinX(), 0.000000001d);
            assertEquals(30d, panel.getViewMinY(), 0.000000001d);
        });
    }

    @Test
    void actualMoveDragRecordsOldAndNewViewsForUndoRedo() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = new MapPanel();
            panel.setSize(360, 240);
            panel.getViewController().restoreView(30d, -20d, 2d);
            panel.syncViewFromController();
            MouseHandler input = new MouseHandler(panel);

            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    100, 70, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    130, 82, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    130, 82, MouseEvent.BUTTON1, 0));

            assertEquals(15d, panel.getViewMinX(), 0.000000001d);
            assertEquals(-14d, panel.getViewMinY(), 0.000000001d);
            assertTrue(panel.canZoomPrevious(), "Pan end must be recorded.");
            panel.zoomPrevious();
            assertEquals(30d, panel.getViewMinX(), 0.000000001d);
            assertEquals(-20d, panel.getViewMinY(), 0.000000001d);
            panel.zoomNext();
            assertEquals(15d, panel.getViewMinX(), 0.000000001d);
            assertEquals(-14d, panel.getViewMinY(), 0.000000001d);
        });
    }

    @Test
    void stationaryClickNeverCreatesGhostPanHistory() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = new MapPanel();
            panel.setSize(300, 200);
            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    42, 57, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    42, 57, MouseEvent.BUTTON1, 0));
            assertFalse(panel.canZoomPrevious());
            assertFalse(panel.canZoomNext());
        });
    }

    private static MouseEvent mouse(MapPanel panel, int type, int x, int y,
                                    int button, int modifiers) {
        return new MouseEvent(panel, type, 1L, modifiers, x, y, 1, false, button);
    }
}
