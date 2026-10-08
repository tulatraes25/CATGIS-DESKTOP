package ar.com.catgis;

import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreProjectPersistenceContractTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void projectDefaultsAndNormalizesCrs() {
        Project project = new Project("Contrato");
        assertEquals("EPSG:4326", project.getProjectCRS());

        project.setProjectCRS(" epsg:22182 ");
        assertEquals("EPSG:22182", project.getProjectCRS());

        project.setProjectCRS("   ");
        assertEquals("EPSG:4326", project.getProjectCRS());
    }

    @Test
    void minimalProjectRoundTripPreservesCoreState() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-core-project-contract");
        Path projectFile = tempDir.resolve("contrato.catgis");

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Contrato original");
            AppContext.project().setProjectCRS("EPSG:22182");
            AppContext.project().setStudyName("Estudio contractual");
            AppContext.project().setCompanyName("CATGIS");
            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Otro");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertEquals("contrato", AppContext.project().getName());
            assertEquals("EPSG:22182", AppContext.project().getProjectCRS());
            assertEquals("Estudio contractual", AppContext.project().getStudyName());
            assertEquals("CATGIS", AppContext.project().getCompanyName());
            assertEquals(projectFile.toFile().getCanonicalFile(),
                    AppContext.project().getProjectFile().getCanonicalFile());
        });
    }

    @Test
    void invalidHeaderDoesNotReplaceCurrentProject() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-invalid-project");
        Path projectFile = tempDir.resolve("invalid.catgis");
        Files.writeString(projectFile, "NOT_A_CATGIS_PROJECT\n", StandardCharsets.UTF_8);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto vivo");
            Project current = AppContext.project();

            assertFalse(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertSame(current, AppContext.project());
            assertEquals("Proyecto vivo", AppContext.project().getName());
        });
    }

    @Test
    void headerOnlyTruncatedProjectDoesNotReplaceCurrentProject() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-truncated-project");
        Path projectFile = tempDir.resolve("truncated.catgis");
        Files.writeString(projectFile, "CATGIS_PROJECT\n", StandardCharsets.UTF_8);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto vivo");
            Project current = AppContext.project();

            assertFalse(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertSame(current, AppContext.project());
            assertEquals("Proyecto vivo", AppContext.project().getName());
        });
    }

    @Test
    void failedSaveDoesNotRepointProjectFileOrRenameProject() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-failed-save");
        Path original = tempDir.resolve("original.catgis");
        Path directoryTarget = Files.createDirectory(tempDir.resolve("not-a-file.catgis"));

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto estable");
            AppContext.project().setProjectFile(original.toFile());
            File previousFile = AppContext.project().getProjectFile();
            String previousName = AppContext.project().getName();

            assertFalse(SaveProjectAction.saveProjectToFile(directoryTarget.toFile(), false));
            assertSame(previousFile, AppContext.project().getProjectFile());
            assertEquals(previousName, AppContext.project().getName());
        });
    }
}
