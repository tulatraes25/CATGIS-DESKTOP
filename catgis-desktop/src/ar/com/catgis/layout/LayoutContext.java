package ar.com.catgis.layout;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;

import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.AppContext;
import ar.com.catgis.MapPanel;

/**
 * Context for CATMAP layout operations.
 * Provides access to project and map without direct singleton coupling.
 * Can be populated from CatgisDesktopApp (when running inside CATGIS)
 * or from standalone data (when running CATMAP independently).
 */
public class LayoutContext {

    private Project project;
    private MapPanel mapPanel;
    private String projectFilePath;

    public LayoutContext() {}

    public LayoutContext(Project project) {
        this.project = project;
    }

    /**
     * Create context from CATGIS Desktop singletons (backward compatible).
     */
    public static LayoutContext fromCatgis() {
        LayoutContext ctx = new LayoutContext();
        ctx.project = AppContext.project();
        ctx.mapPanel = CatgisDesktopApp.mapPanel;
        return ctx;
    }

    /**
     * Create standalone context with a project loaded from file.
     */
    public static LayoutContext standalone(Project project, String projectFilePath) {
        LayoutContext ctx = new LayoutContext(project);
        ctx.projectFilePath = projectFilePath;
        return ctx;
    }

    public Project getProject() {
        return project != null ? project : AppContext.project();
    }

    public void setProject(Project project) { this.project = project; }

    public MapPanel getMapPanel() {
        return mapPanel != null ? mapPanel : CatgisDesktopApp.mapPanel;
    }

    public void setMapPanel(MapPanel mapPanel) { this.mapPanel = mapPanel; }

    public String getProjectFilePath() { return projectFilePath; }
    public void setProjectFilePath(String path) { this.projectFilePath = path; }

    public boolean hasProject() {
        return getProject() != null;
    }
}