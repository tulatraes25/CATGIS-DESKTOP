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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectOpenRecordIntegrityContractTest {
    @TempDir Path directory;

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void nonFiniteAndInvalidViewsMustPreserveCurrentDirtyProject() throws Exception {
        for (String view : new String[]{"VIEW|NaN|0|1",
                "VIEW|Infinity|0|1", "VIEW|0|0|Infinity",
                "VIEW|0|0|0", "VIEW|0|0|-3", "VIEW|0|0|invalid", "VIEW|0|0"}) {
            assertRejectedBeforeReplacingCurrentProject(view);
        }
        assertEquals(3, ProjectOpenRecordIntegrity.parseView("VIEW|-1.5|20.25|0.5").length);
    }

    @Test
    void incompleteDeclaredLayerCannotSilentlyDisappearFromLoadedProject() throws Exception {
        assertRejectedBeforeReplacingCurrentProject("[VECTOR]|Parcelas");
        assertRejectedBeforeReplacingCurrentProject("[RASTER]|DEM");
        assertRejectedBeforeReplacingCurrentProject("GPX|Rutas");
        assertThrows(IOException.class, () ->
                ProjectOpenRecordIntegrity.rejectMalformedLayerRecord("[VECTOR]|Parcelas"));
    }

    @Test
    void damagedLayoutLegendGroupMetadataAndCrsCannotSilentlyDisappear() throws Exception {
        assertRejectedBeforeReplacingCurrentProject("PROJECT_LAYOUT_ITEM|not-a-layout");
        assertRejectedBeforeReplacingCurrentProject("PROJECT_LEGEND_ITEM|not-a-legend");
        assertRejectedBeforeReplacingCurrentProject("PROJECT_LAYER_GROUP|");
        assertRejectedBeforeReplacingCurrentProject("PROJECT_META|STUDY_NAME");
        assertRejectedBeforeReplacingCurrentProject("PROJECT_CRS|");
    }

    private void assertRejectedBeforeReplacingCurrentProject(String corruptRecord) throws Exception {
        Project prior = new Project("Trabajo no guardado");
        prior.setProjectCRS("EPSG:32719");
        prior.setModified(true);
        AppContext.setCurrentProject(prior);
        Path candidate = directory.resolve("corrupt-" + Math.abs(corruptRecord.hashCode()) + ".catgis");
        Files.writeString(candidate,
                "CATGIS_PROJECT\nPROJECT_CRS|EPSG:4326\n" + corruptRecord + "\n",
                StandardCharsets.UTF_8);
        var physicalLines = Files.readAllLines(candidate, StandardCharsets.UTF_8);
        assertEquals(3, physicalLines.size(), "Fixture must use real physical newlines");
        assertEquals("CATGIS_PROJECT", physicalLines.get(0));
        assertEquals("PROJECT_CRS|EPSG:4326", physicalLines.get(1));
        assertEquals(corruptRecord, physicalLines.get(2));
        assertFalse(LoadProjectAction.loadProjectFile(candidate.toFile(), false),
                () -> "Malformed record accepted: " + corruptRecord);
        assertSame(prior, AppContext.project());
        assertEquals("EPSG:32719", prior.getProjectCRS());
        assertTrue(prior.isModified());
    }
}
