package ar.com.catgis;

import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectOpenReadBudgetContractTest {
    @TempDir Path directory;

    @AfterEach
    void cleanup() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void smallValidLegacyProjectRecordsRemainAccepted() throws Exception {
        ProjectOpenReadBudget budget = new ProjectOpenReadBudget(1000, 12, 3, 2, 3);
        for (String line : new String[]{
                "CATGIS_PROJECT", "PROJECT_CRS|EPSG:32719", "VIEW|0|0|1",
                "PROJECT_LAYER_GROUP|Catastro|true|true",
                "[VECTOR]|Pozos|/tmp/data.shp",
                "PROJECT_LAYOUT_ITEM|ok", "PROJECT_LEGEND_ITEM|ok"
        }) {
            budget.checkLine(line);
        }
    }

    @Test
    void rejectsOversizeLineTooManyLinesAndControlCharacters() throws Exception {
        ProjectOpenReadBudget shortLines = new ProjectOpenReadBudget(4, 10, 2, 2, 2);
        assertThrows(IOException.class, () -> shortLines.checkLine("12345"));
        ProjectOpenReadBudget fewLines = new ProjectOpenReadBudget(100, 2, 2, 2, 2);
        fewLines.checkLine("one");
        fewLines.checkLine("two");
        assertThrows(IOException.class, () -> fewLines.checkLine("three"));
        ProjectOpenReadBudget control = new ProjectOpenReadBudget();
        assertThrows(IOException.class, () -> control.checkLine("PROJECT_CRS|EPSG:32719" + (char) 0));
        assertThrows(IOException.class, () -> control.checkLine("metadata" + (char) 1));
    }

    @Test
    void rejectsCountsIndependentlyAndWithoutPartialAcceptance() throws Exception {
        ProjectOpenReadBudget layers = new ProjectOpenReadBudget(100, 20, 1, 20, 20);
        layers.checkLine("[VECTOR]|one|a.shp");
        assertThrows(IOException.class, () -> layers.checkLine("[VECTOR]|two|b.shp"));
        ProjectOpenReadBudget groups = new ProjectOpenReadBudget(100, 20, 20, 1, 20);
        groups.checkLine("PROJECT_LAYER_GROUP|one");
        assertThrows(IOException.class, () -> groups.checkLine("PROJECT_LAYER_GROUP|two"));
        ProjectOpenReadBudget catmap = new ProjectOpenReadBudget(100, 20, 20, 20, 1);
        catmap.checkLine("PROJECT_LAYOUT_ITEM|one");
        assertThrows(IOException.class, () -> catmap.checkLine("PROJECT_LEGEND_ITEM|two"));
    }

    @Test
    void refusesNonRegularAndOversizedProjectBeforeReaderOpens() throws Exception {
        Path project = directory.resolve("a.catgis");
        Files.writeString(project, "CATGIS_PROJECT\n", StandardCharsets.UTF_8);
        ProjectOpenReadBudget.verifyFile(project.toFile(), Files.size(project));
        assertThrows(IOException.class,
                () -> ProjectOpenReadBudget.verifyFile(project.toFile(), Files.size(project) - 1L));
        Path folder = Files.createDirectory(directory.resolve("folder.catgis"));
        assertThrows(IOException.class, () -> ProjectOpenReadBudget.openChecked(folder.toFile()));
        Path missing = directory.resolve("missing.catgis");
        assertThrows(IOException.class, () -> ProjectOpenReadBudget.openChecked(missing.toFile()));
    }

    @Test
    void invalidControlRecordPreservesPriorProjectIdentityBeforeAnyReplacement() throws Exception {
        Project before = new Project("Trabajo sin guardar");
        before.setProjectCRS("EPSG:32719");
        AppContext.setCurrentProject(before);
        Path poisoned = directory.resolve("broken.catgis");
        Files.writeString(poisoned,
                "CATGIS_PROJECT\nPROJECT_CRS|EPSG:4326\nPROJECT_META|STUDY_NAME|bad" + (char) 0 + "data\n",
                StandardCharsets.UTF_8);
        assertFalse(LoadProjectAction.loadProjectFile(poisoned.toFile(), false));
        assertSame(before, AppContext.project());
        assertEquals("EPSG:32719", AppContext.project().getProjectCRS());
        assertEquals(0, AppContext.project().getLayers().size());
    }
}
