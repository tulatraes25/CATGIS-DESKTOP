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

    public static void setStatusMessage(String message) {
        StatusBar sb = get().statusBar;
        if (sb != null) {
            sb.setMessage(message);
        }
    }
}
