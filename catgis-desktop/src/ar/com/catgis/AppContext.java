package ar.com.catgis;

import java.awt.Component;

/**
 * Central runtime context for CATGIS Desktop.
 * Reduces static coupling to CatgisDesktopApp globals.
 * All UI modules should obtain dependencies from here.
 */
public final class AppContext {

    private static volatile AppContext instance = new AppContext();

    private volatile MapPanel mapPanel;
    private volatile LayersPanel layersPanel;
    private volatile Project currentProject;
    private volatile Component mainFrame;
    private volatile javax.swing.JLabel statusLabel;

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

    // ---- Convenience accessors (backward compat) ----
    public static MapPanel mapPanel() { return get().mapPanel; }
    public static Project project() { return get().currentProject; }

    // ---- Sync with legacy CatgisDesktopApp ----
    public void syncFromLegacy() {
        if (CatgisDesktopApp.mapPanel != null) this.mapPanel = CatgisDesktopApp.mapPanel;
        if (CatgisDesktopApp.currentProject != null) this.currentProject = CatgisDesktopApp.currentProject;
    }

    public void syncToLegacy() {
        if (this.mapPanel != null) CatgisDesktopApp.mapPanel = this.mapPanel;
        if (this.currentProject != null) CatgisDesktopApp.currentProject = this.currentProject;
    }
}
