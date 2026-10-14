package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A0.2 tranche B: pure pan never performs coordinate reprojection/snap work
 * on each pointer step; finishing the pan refreshes it exactly once.
 * Editing and selection still execute the original interactive pipeline.
 */
class MapPanelPurePanCoordinateDeferralTest {

    private static final class CountingMapPanel extends MapPanel {
        private int coordinateUpdates;
        private int snapUpdates;

        @Override
        void updateStatusCoordinates(int x, int y) {
            coordinateUpdates++;
        }

        @Override
        void updateHoverAndSnap(int x, int y) {
            snapUpdates++;
        }
    }

    @Test
    void leftMovePanSkipsRepeatedStatusAndSnapUntilMouseUp() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            CountingMapPanel panel = new CountingMapPanel();
            panel.setSize(300, 200);
            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    50, 60, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK));
            for (int i = 0; i < 6; i++) {
                input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                        60 + i * 3, 70 + i * 2, MouseEvent.NOBUTTON,
                        InputEvent.BUTTON1_DOWN_MASK));
            }
            assertEquals(0, panel.coordinateUpdates);
            assertEquals(0, panel.snapUpdates);
            assertTrue(panel.canZoomPrevious() == false,
                    "History is committed only when the drag ends.");
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    75, 80, MouseEvent.BUTTON1, 0));
            assertEquals(1, panel.coordinateUpdates);
            assertEquals(1, panel.snapUpdates);
            assertTrue(panel.canZoomPrevious());
            assertFalse(panel.canZoomNext());
        });
    }

    @Test
    void middlePanDefersWorkAndRestoresItOnRelease() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            CountingMapPanel panel = new CountingMapPanel();
            panel.setSize(400, 240);
            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    110, 120, MouseEvent.BUTTON2, InputEvent.BUTTON2_DOWN_MASK));
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    130, 122, MouseEvent.NOBUTTON, InputEvent.BUTTON2_DOWN_MASK));
            assertEquals(0, panel.coordinateUpdates);
            assertEquals(0, panel.snapUpdates);
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    130, 122, MouseEvent.BUTTON2, 0));
            assertEquals(1, panel.coordinateUpdates);
            assertEquals(1, panel.snapUpdates);
            assertTrue(panel.canZoomPrevious(),
                    "The middle drag must retain its original viewport history.");
        });
    }

    @Test
    void selectionDragKeepsLiveCoordinateAndSnapUpdates() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            CountingMapPanel panel = new CountingMapPanel();
            panel.setSize(200, 150);
            panel.currentTool = "SELECT";
            panel.selectionBoxActive = true;
            panel.selectionBoxStartX = 20;
            panel.selectionBoxStartY = 30;
            MouseHandler input = new MouseHandler(panel);
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    45, 60, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            assertEquals(1, panel.coordinateUpdates);
            assertEquals(1, panel.snapUpdates);
            assertTrue(panel.selectionBoxDragging);
        });
    }

    @Test
    void editingModeDoesNotActivatePurePanOptimization() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            CountingMapPanel panel = new CountingMapPanel();
            panel.setSize(200, 150);
            panel.dragging = true;
            panel.featureEditMode = true;
            panel.currentTool = "MOVE";
            MouseHandler input = new MouseHandler(panel);
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    45, 60, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            assertEquals(1, panel.coordinateUpdates);
            assertEquals(1, panel.snapUpdates);
        });
    }

    @Test
    void ordinaryHoverRetainsCoordinateAndSnapFeedback() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            CountingMapPanel panel = new CountingMapPanel();
            MouseHandler input = new MouseHandler(panel);
            input.mouseMoved(mouse(panel, MouseEvent.MOUSE_MOVED,
                    18, 26, MouseEvent.NOBUTTON, 0));
            assertEquals(1, panel.coordinateUpdates);
            assertEquals(1, panel.snapUpdates);
        });
    }

    private static MouseEvent mouse(MapPanel p, int id, int x, int y,
                                    int button, int modifiers) {
        return new MouseEvent(p, id, 1L, modifiers, x, y, 1, false, button);
    }
}
