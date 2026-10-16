package ar.com.catgis;

import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectStateAuthorityContractTest {

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
        CatgisDesktopApp.currentProject = null;
    }

    @Test
    void appContextProjectWinsOverStaleLegacyForUnsavedDetection() {
        Project stale = new Project("stale");
        stale.setModified(false);
        CatgisDesktopApp.currentProject = stale;

        Project live = new Project("live");
        live.setModified(true);
        AppContext.setCurrentProject(live);

        assertSame(live, CatgisDesktopApp.getProject());
        assertTrue(CatgisDesktopApp.hasUnsavedProjectChanges());
    }

    @Test
    void appContextOnlyDirtyAndCleanTransitionsDriveUnsavedProtection() {
        Project live = new Project("live");
        AppContext.setCurrentProject(live);

        AppContext.get().markProjectDirty();
        assertTrue(CatgisDesktopApp.hasUnsavedProjectChanges());

        CatgisDesktopApp.markProjectClean();
        assertFalse(CatgisDesktopApp.hasUnsavedProjectChanges());
        assertFalse(live.isModified());
    }

    @Test
    void legacySetProjectKeepsCompatibilityMirrorAndContextIdentical() {
        Project project = new Project("compat");

        CatgisDesktopApp.setProject(project);

        assertSame(project, CatgisDesktopApp.currentProject);
        assertSame(project, AppContext.project());
        assertSame(project, CatgisDesktopApp.getProject());
    }

    @Test
    void staleDirtyLegacyCannotCreateFalseUnsavedWarningForCleanLiveProject() {
        Project stale = new Project("stale");
        stale.setModified(true);
        CatgisDesktopApp.currentProject = stale;

        Project live = new Project("live");
        live.setModified(false);
        AppContext.setCurrentProject(live);

        assertSame(live, CatgisDesktopApp.getProject());
        assertFalse(CatgisDesktopApp.hasUnsavedProjectChanges());
    }
}
