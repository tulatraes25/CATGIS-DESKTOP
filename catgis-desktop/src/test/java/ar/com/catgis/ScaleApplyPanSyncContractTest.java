package ar.com.catgis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * REQ-AR-5 contract: applying a view scale from the status bar must leave the
 * legacy {@code MapPanel} view mirror (viewMinX/viewMinY/zoomFactor) in sync
 * with {@code MapViewController}. Panning copies that mirror back into the
 * controller, so a stale mirror silently reverts the scale the user applied.
 */
class ScaleApplyPanSyncContractTest {

    private static final double EPSILON = 1e-9d;

    @BeforeEach
    void setUp() {
        ReleaseTestSupport.initializeAppContext("scale-pan-contract");
        AppContext.project().setProjectCRS("EPSG:22182");
    }

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void scaleFromStatusBarStaysSynchronizedWithTheViewMirror() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = loadedPanel();
            double currentDenominator = panel.getCurrentScaleDenominator();
            assertTrue(currentDenominator > 0d,
                    "El panel debe exponer una escala valida antes de aplicar una nueva.");
            double targetDenominator = Math.round(currentDenominator / 2d);
            double expectedZoom = 2d * currentDenominator / targetDenominator;

            panel.applyRequestedScale("1:" + (long) targetDenominator);

            assertEquals(expectedZoom, panel.getViewController().getZoomFactor(),
                    expectedZoom * 1e-6d,
                    "La barra de estado debe acercar la vista al reducir el denominador.");
            assertEquals(panel.getViewController().getViewMinX(), panel.viewMinX, EPSILON,
                    "El espejo legacy debe seguir al controlador tras aplicar la escala.");
            assertEquals(expectedZoom, panel.zoomFactor, EPSILON,
                    "El factor de zoom legacy debe seguir al controlador tras aplicar la escala.");
        });
    }

    @Test
    void panAfterApplyingScaleMustNotRevertTheAppliedScale() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            MapPanel panel = loadedPanel();
            double currentDenominator = panel.getCurrentScaleDenominator();
            assertTrue(currentDenominator > 0d, "El panel debe exponer una escala valida.");
            double targetDenominator = Math.round(currentDenominator / 2d);
            panel.applyRequestedScale("1:" + (long) targetDenominator);
            double appliedZoom = panel.getZoomFactor();
            assertTrue(appliedZoom > 2d, "La escala aplicada debe ser mas cercana que el zoom inicial.");

            MouseHandler input = new MouseHandler(panel);
            input.mousePressed(mouse(panel, MouseEvent.MOUSE_PRESSED,
                    160, 100, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseDragged(mouse(panel, MouseEvent.MOUSE_DRAGGED,
                    190, 112, MouseEvent.NOBUTTON, InputEvent.BUTTON1_DOWN_MASK));
            input.mouseReleased(mouse(panel, MouseEvent.MOUSE_RELEASED,
                    190, 112, MouseEvent.BUTTON1, 0));

            assertEquals(appliedZoom, panel.getZoomFactor(), EPSILON,
                    "El paneo no debe revertir el zoom fijado desde la barra de estado.");
            assertEquals(panel.getViewController().getZoomFactor(), panel.zoomFactor, EPSILON,
                    "Espejo y controlador deben quedar consistentes despues del paneo.");
        });
    }

    private static MapPanel loadedPanel() {
        MapPanel panel = new MapPanel();
        panel.setSize(320, 200);
        // The real navigation paths use the displayed dimensions, and the
        // scale denominator is only defined for a view with content.
        panel.syncViewToController();
        panel.getViewController().setHasLoadedContent(() -> true);
        panel.getViewController().restoreView(10d, 20d, 2d);
        panel.syncViewFromController();
        return panel;
    }

    private static MouseEvent mouse(MapPanel panel, int type, int x, int y,
                                    int button, int modifiers) {
        return new MouseEvent(panel, type, 1L, modifiers, x, y, 1, false, button);
    }
}
