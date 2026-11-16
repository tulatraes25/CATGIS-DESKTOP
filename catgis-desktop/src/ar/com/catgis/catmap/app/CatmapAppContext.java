package ar.com.catgis.catmap.app;

import ar.com.catgis.AppContext;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.LayersPanel;
import ar.com.catgis.MapPanel;
import ar.com.catgis.StatusBar;
import ar.com.catgis.core.model.Project;

import java.util.Objects;

/**
 * Standalone CATMAP bootstrap context. CATMAP can run without the full CATGIS
 * desktop shell, but the shared layout code expects an AppContext, a map panel,
 * a layers panel and a status bar. This class centralizes that wiring instead of
 * letting {@code Main} fabricate {@link CatgisDesktopApp} statics line by line.
 */
public final class CatmapAppContext {

    private final Project project;
    private final MapPanel mapPanel;
    private final LayersPanel layersPanel;
    private final StatusBar statusBar;

    public CatmapAppContext(Project project, MapPanel mapPanel,
                            LayersPanel layersPanel, StatusBar statusBar) {
        this.project = Objects.requireNonNull(project, "CATMAP project");
        this.mapPanel = mapPanel;
        this.layersPanel = layersPanel;
        this.statusBar = statusBar;
    }

    /** Default standalone context: fresh project and the shared runtime panels. */
    public static CatmapAppContext standalone() {
        return new CatmapAppContext(new Project("CATMAP Standalone"),
                new MapPanel(), new LayersPanel(), new StatusBar());
    }

    /** Install this context as the process-wide CATGIS/CATMAP runtime. */
    public void install() {
        AppContext.setCurrentProject(project);
        AppContext.get().setProject(project);
        CatgisDesktopApp.mapPanel = mapPanel;
        CatgisDesktopApp.layersPanel = layersPanel;
        CatgisDesktopApp.statusBar = statusBar;
    }

    public Project project() {
        return project;
    }

    public MapPanel mapPanel() {
        return mapPanel;
    }

    public LayersPanel layersPanel() {
        return layersPanel;
    }

    public StatusBar statusBar() {
        return statusBar;
    }
}
