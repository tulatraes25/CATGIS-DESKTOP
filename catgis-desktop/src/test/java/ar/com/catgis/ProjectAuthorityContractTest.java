package ar.com.catgis;

import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T1/#153: AppContext is the single authoritative current-project state. */
class ProjectAuthorityContractTest {

    @Test
    void appContextProjectIsAuthoritativeForDirtyProtection() {
        Project previous = AppContext.project();
        try {
            AppContext.setCurrentProject(null);
            assertFalse(CatgisDesktopApp.hasUnsavedProjectChanges(),
                    "no project means nothing to save");

            Project dirty = new Project("autoridad");
            dirty.setModified(true);
            AppContext.setCurrentProject(dirty);
            assertSame(dirty, CatgisDesktopApp.getProject(),
                    "CatgisDesktopApp.getProject must read AppContext");
            assertTrue(CatgisDesktopApp.hasUnsavedProjectChanges(),
                    "dirty AppContext project must trigger unsaved-change protection");

            dirty.setModified(false);
            assertFalse(CatgisDesktopApp.hasUnsavedProjectChanges());

            Project viaLegacySetter = new Project("sincronizado");
            CatgisDesktopApp.setProject(viaLegacySetter);
            assertSame(viaLegacySetter, AppContext.project(),
                    "legacy setProject must synchronize AppContext, never diverge");
        } finally {
            AppContext.setCurrentProject(previous);
        }
    }
}
