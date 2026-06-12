package ar.com.catgis;

import ar.com.catgis.core.model.Project;

import java.awt.Component;

/**
 * Central runtime context for CATGIS Desktop.
 * All UI modules should obtain dependencies from here.
 * This replaces direct static coupling to CatgisDesktopApp globals.
 */
public final class AppContext {

    private static volatile AppContext instance = new AppContext();

    private volatile MapPanel mapPanel;
    private volatile LayersPanel layersPanel;
    private volatile Project currentProject;
    private volatile Component mainFrame;
    private volatile javax.swing.JLabel statusLabel;
    private volatile StatusBar statusBar;
    private volatile FloatingVectorEditToolbar floatingEditToolbar;
    private volatile CartographyToolbar cartographyToolbar;
    private volatile CatserverToolbar catserverToolbar;
    private volatile OnlineConnectionsToolbar onlineConnectionsToolbar;
    private volatile TopographyToolbar topographyToolbar;
    private volatile QuickStylePanel quickStylePanel;

    private AppContext() {}

    public static AppContext get() { return instance; }

    // ---- MapPanel ----
    public MapPanel getMapPanel() { return mapPanel; }
    public void setMapPanel(MapPanel mp) { this.mapPanel = mp; }

    // ---- Project ----
    public Project getProject() { return currentProject; }
    public void setProject(Project p) { this.currentProject = p; }

    // ---- Layers ----
    public LayersPanel getLayersPanel() { return layersPanel; }
    public void setLayersPanel(LayersPanel lp) { this.layersPanel = lp; }

    // ---- Main frame ----
    public Component getMainFrame() { return mainFrame; }
    public void setMainFrame(Component f) { this.mainFrame = f; }

    // ---- Status ----
    public javax.swing.JLabel getStatusLabel() { return statusLabel; }
    public void setStatusLabel(javax.swing.JLabel l) { this.statusLabel = l; }

    // ---- StatusBar ----
    public StatusBar getStatusBar() { return statusBar; }
    public void setStatusBar(StatusBar sb) { this.statusBar = sb; }

    // ---- Toolbars ----
    public FloatingVectorEditToolbar getFloatingEditToolbar() { return floatingEditToolbar; }
    public void setFloatingEditToolbar(FloatingVectorEditToolbar tb) { this.floatingEditToolbar = tb; }

    public CartographyToolbar getCartographyToolbar() { return cartographyToolbar; }
    public void setCartographyToolbar(CartographyToolbar tb) { this.cartographyToolbar = tb; }

    public CatserverToolbar getCatserverToolbar() { return catserverToolbar; }
    public void setCatserverToolbar(CatserverToolbar tb) { this.catserverToolbar = tb; }

    public OnlineConnectionsToolbar getOnlineConnectionsToolbar() { return onlineConnectionsToolbar; }
    public void setOnlineConnectionsToolbar(OnlineConnectionsToolbar tb) { this.onlineConnectionsToolbar = tb; }

    public TopographyToolbar getTopographyToolbar() { return topographyToolbar; }
    public void setTopographyToolbar(TopographyToolbar tb) { this.topographyToolbar = tb; }

    // ---- QuickStyle ----
    public QuickStylePanel getQuickStylePanel() { return quickStylePanel; }
    public void setQuickStylePanel(QuickStylePanel p) { this.quickStylePanel = p; }

    // ---- Convenience accessors ----
    public static MapPanel mapPanel() { return get().mapPanel; }
    public static Project project() { return get().currentProject; }
    public static void setCurrentProject(Project p) { get().setProject(p); }

    // ---- LayersPanel convenience accessors ----
    public static void addLayer(ar.com.catgis.core.model.Layer layer) {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.addLayer(layer);
    }
    public static void selectLayer(ar.com.catgis.core.model.Layer layer) {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.selectLayer(layer);
    }
    public static void refreshLayerList() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.refreshLayerList();
    }
    public static ar.com.catgis.core.model.Layer getSelectedLayer() {
        LayersPanel lp = get().layersPanel;
        return lp != null ? lp.getSelectedLayer() : null;
    }
    public static void clearLayers() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.clearLayers();
    }
    public static void repaintLayers() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.repaint();
    }
    public static void runSelectedProThematic() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.runSelectedProThematic();
    }
    public static void runSelectedProQa() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.runSelectedProQa();
    }
    public static void runSelectedLandsatQaMask(String mask) {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.runSelectedLandsatQaMask(mask);
    }
    public static void runSelectedProComparison() {
        LayersPanel lp = get().layersPanel;
        if (lp != null) lp.runSelectedProComparison();
    }

    public static void setStatusMessage(String message) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setMessage(message);
        }
    }

    public static void clearStatusCoordinates() {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.clearCoordinates();
        }
    }

    public static void setStatusScaleText(String text) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setScaleText(text);
        }
    }

    public static void setStatusScaleToolTip(String tooltip) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setScaleToolTip(tooltip);
        }
    }

    public static void forceStatusScaleText(String text) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.forceScaleText(text);
        }
    }

    public static void setStatusProjectCoordinates(String text) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setProjectCoordinates(text);
        }
    }

    public static void setStatusGeographicCoordinates(String text) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setGeographicCoordinates(text);
        }
    }

    public static void setStatusGeographicDms(String text) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setGeographicDms(text);
        }
    }
}
