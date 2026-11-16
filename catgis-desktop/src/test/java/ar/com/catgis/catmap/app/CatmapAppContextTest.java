package ar.com.catgis.catmap.app;

import ar.com.catgis.AppContext;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Standalone bootstrap context installs the shared runtime in one place. */
class CatmapAppContextTest {

    @Test
    void installPublishesTheProjectAndRuntimeSlots() {
        Project project = new Project("CATMAP de prueba");
        CatmapAppContext context = new CatmapAppContext(project, null, null, null);
        context.install();

        assertSame(project, context.project());
        assertSame(project, AppContext.project());
        assertNull(CatgisDesktopApp.mapPanel);
        assertNull(CatgisDesktopApp.layersPanel);
        assertNull(CatgisDesktopApp.statusBar);
        assertEquals("CATMAP de prueba", AppContext.project().getName());
    }

    @Test
    void projectIsMandatory() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> new CatmapAppContext(null, null, null, null));
    }
}
