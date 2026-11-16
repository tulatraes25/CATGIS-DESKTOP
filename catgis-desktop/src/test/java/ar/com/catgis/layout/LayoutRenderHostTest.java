package ar.com.catgis.layout;

import ar.com.catgis.AppContext;
import ar.com.catgis.MapPanel;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Layout elements read the project/map through an injectable host instead of the
 * global AppContext; the default adapter must preserve legacy behaviour.
 */
class LayoutRenderHostTest {

    private static final LayoutRenderContext PREVIEW =
            new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 96, 297, 210);

    private Project previousProject;

    @BeforeEach
    void rememberProject() {
        previousProject = AppContext.project();
    }

    @AfterEach
    void restore() {
        LayoutRenderHosts.reset();
        AppContext.setCurrentProject(previousProject);
    }

    @Test
    void defaultHostDelegatesToTheLegacyCatgisContext() {
        Project project = new Project("Proyecto CATGIS");
        project.setProjectCRS("EPSG:32719");
        AppContext.setCurrentProject(project);
        LayoutRenderHosts.reset();

        assertEquals(project, LayoutRenderHosts.get().project());
        assertEquals("Proyecto CATGIS / EPSG:32719",
                LayoutLabel.resolveExpression("{project} / {crs}", PREVIEW));
    }

    @Test
    void installedHostSuppliesLabelProjectInfo() {
        AppContext.setCurrentProject(new Project("Proyecto legacy"));
        CountingHost host = new CountingHost(new Project("Proyecto Inyectado"));
        LayoutRenderHosts.install(host);

        assertEquals("Proyecto Inyectado",
                LayoutLabel.resolveExpression("{project}", PREVIEW));
        assertTrue(host.projectCalls >= 1, "label must ask the host for the project");
    }

    @Test
    void installedHostIsUsedByViewportExtentResolution() {
        CountingHost host = new CountingHost(new Project("Vacio"));
        LayoutRenderHosts.install(host);

        MapFrameViewport viewport = new MapFrameViewport();
        assertFalse(viewport.fitFromProjectLayers());
        assertTrue(host.projectCalls >= 1, "viewport must ask the host for the project");
    }

    @Test
    void nullHostIsRejected() {
        assertThrows(NullPointerException.class, () -> LayoutRenderHosts.install(null));
    }

    @Test
    void resetRestoresTheDefaultHost() {
        LayoutRenderHosts.install(new CountingHost(new Project("Inyectado")));
        LayoutRenderHosts.reset();
        AppContext.setCurrentProject(new Project("Proyecto legacy"));

        assertEquals("Proyecto legacy", LayoutLabel.resolveExpression("{project}", PREVIEW));
    }

    private static final class CountingHost implements LayoutRenderHost {
        private final Project project;
        private int projectCalls;

        CountingHost(Project project) {
            this.project = project;
        }

        @Override
        public Project project() {
            projectCalls++;
            return project;
        }

        @Override
        public MapPanel mapPanel() {
            return null;
        }
    }
}
