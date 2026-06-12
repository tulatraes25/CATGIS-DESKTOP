package ar.com.catgis.service;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
// QuickStylePanel is in the flat package
import ar.com.catgis.AppContext;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.service.EventBus.Event;
import ar.com.catgis.service.EventBus.EventType;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Initializes the Event Bus wiring between CATGIS components.
 * <p>
 * Called once at application startup. Subscribe/unsubscribe pairs
 * ensure no listener leaks when components are swapped.
 * </p>
 */
public final class EventBusInitializer {

    private EventBusInitializer() {}

    /** Wire all event bus connections. */
    public static void init() {
        // ── Layer selection → QuickStylePanel ──
        EventBus.on(EventType.LAYER_SELECTED, (Event e) -> {
            Layer layer = e.getData();
            SwingUtilities.invokeLater(() -> {
                if (CatgisDesktopApp.quickStylePanel != null && CatgisDesktopApp.quickStylePanel.isVisible()) {
                    CatgisDesktopApp.quickStylePanel.setLayer(layer);
                }
            });
        });

        // ── Project changes → title bar ──
        EventBus.on(EventType.PROJECT_CHANGED, (Event e) -> {
            SwingUtilities.invokeLater(CatgisDesktopApp::refreshProjectHeader);
        });

        // ── Dirty state → title bar ──
        EventBus.on(EventType.PROJECT_DIRTY_STATE_CHANGED, (Event e) -> {
            SwingUtilities.invokeLater(CatgisDesktopApp::updateWindowTitle);
        });

        // ── Status messages → status bar ──
        EventBus.on(EventType.STATUS_MESSAGE, (Event e) -> {
            String msg = e.getData();
            SwingUtilities.invokeLater(() -> {
                if (CatgisDesktopApp.statusBar != null) {
                    AppContext.setStatusMessage(msg);
                }
            });
        });

        // ── Layer style changes → repaint map ──
        EventBus.on(EventType.LAYER_STYLE_CHANGED, (Event e) -> {
            SwingUtilities.invokeLater(() -> {
                if (CatgisDesktopApp.mapPanel != null) {
                    CatgisDesktopApp.mapPanel.repaint();
                }
                if (CatgisDesktopApp.layersPanel != null) {
                    AppContext.repaintLayers();
                }
            });
        });

        // ── Toolbar sync ──
        EventBus.on(EventType.TOOLBAR_SYNC, (Event e) -> {
            SwingUtilities.invokeLater(CatgisDesktopApp::syncFloatingVectorEditToolbar);
        });
    }
}
