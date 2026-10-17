package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prevent failed project opens from mutating the file-chooser preferences. */
class ProjectOpenChooserCommitBoundaryContractTest {

    @Test
    void openAndSaveDirectoryHintsAreOnlyUpdatedAfterSuccessfulLoad() throws Exception {
        String source = Files.readString(Path.of("src", "ar", "com", "catgis",
                "LoadProjectAction.java"), StandardCharsets.UTF_8);
        int action = source.indexOf("public static void loadProject()");
        int guarded = source.indexOf("if (loadProjectFile(file)) {", action);
        int nextMethod = source.indexOf("public static boolean loadProjectFile(File file)", action);
        int openHint = source.indexOf("FileChooserSupport.rememberSelection(\"project-open\", chooser)", action);
        int saveHint = source.indexOf("FileChooserSupport.rememberSelection(\"project-save\", chooser)", action);
        assertTrue(action >= 0 && guarded > action && nextMethod > guarded);
        assertTrue(openHint > guarded && openHint < nextMethod);
        assertTrue(saveHint > guarded && saveHint < nextMethod);
        assertFalse(source.substring(action, guarded).contains("rememberSelection("));
        assertTrue(source.substring(guarded, nextMethod).contains("if (loadProjectFile(file)) {"));
    }
}
