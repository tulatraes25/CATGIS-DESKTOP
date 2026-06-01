package ar.com.catgis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppContextTest {

    @Test
    public void testSingleton() {
        AppContext ctx1 = AppContext.get();
        AppContext ctx2 = AppContext.get();
        assertSame(ctx1, ctx2);
    }

    @Test
    public void testMapPanelGetterSetter() {
        try {
            MapPanel mp = new MapPanel();
            AppContext.get().setMapPanel(mp);
            assertSame(mp, AppContext.get().getMapPanel());
            AppContext.get().setMapPanel(null);
        } catch (Exception e) {
            // OK - may fail in headless env
        }
    }

    @Test
    public void testProjectGetterSetter() {
        Project p = new Project("Test");
        AppContext.get().setProject(p);
        assertSame(p, AppContext.get().getProject());
        AppContext.get().setProject(null); // cleanup
    }

    @Test
    public void testLayersPanelGetterSetter() {
        try {
            LayersPanel lp = new LayersPanel();
            AppContext.get().setLayersPanel(lp);
            assertSame(lp, AppContext.get().getLayersPanel());
            AppContext.get().setLayersPanel(null);
        } catch (Exception e) { /* headless */ }
    }

    @Test
    public void testMainFrameSetter() {
        try {
            javax.swing.JFrame frame = new javax.swing.JFrame();
            AppContext.get().setMainFrame(frame);
            assertSame(frame, AppContext.get().getMainFrame());
            AppContext.get().setMainFrame(null);
            frame.dispose();
        } catch (java.awt.HeadlessException e) {
            // OK - headless CI environment
        }
    }

    @Test
    public void testStaticConvenienceMethods() {
        MapPanel mp = new MapPanel();
        AppContext.get().setMapPanel(mp);
        assertSame(mp, AppContext.mapPanel());
        Project p = new Project("Test");
        AppContext.get().setProject(p);
        assertSame(p, AppContext.project());
        AppContext.get().setMapPanel(null);
        AppContext.get().setProject(null);
    }

    @Test
    public void testSyncFromLegacy() {
        CatgisDesktopApp.mapPanel = null;
        CatgisDesktopApp.currentProject = null;
        AppContext.get().setMapPanel(null);
        AppContext.get().setProject(null);
        AppContext.get().syncFromLegacy();
        assertNull(AppContext.get().getMapPanel());
        assertNull(AppContext.get().getProject());
    }
}
