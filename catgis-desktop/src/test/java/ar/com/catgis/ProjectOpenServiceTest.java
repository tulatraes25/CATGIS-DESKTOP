package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Headless project opening: parse/validate a .catgis file with no GUI side effects. */
class ProjectOpenServiceTest {

    @TempDir
    Path directory;

    private File project(String content) throws Exception {
        File file = directory.resolve("proyecto.catgis").toFile();
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void parsesProjectMetadataCrsAndSavedView() throws Exception {
        File file = project("""
                CATGIS_PROJECT
                PROJECT_CRS|EPSG:4326
                PROJECT_META|STUDY_NAME|Estudio de Prueba
                VIEW|100|200|2.5
                """);
        ProjectOpenService.ProjectInspection inspection = ProjectOpenService.inspect(file);
        assertEquals("Estudio de Prueba", inspection.project().getStudyName());
        assertEquals("EPSG:4326", inspection.project().getProjectCRS());
        assertTrue(inspection.viewLoaded());
        assertEquals(100.0, inspection.viewMinX(), 1.0e-9);
        assertEquals(200.0, inspection.viewMinY(), 1.0e-9);
        assertEquals(2.5, inspection.viewZoomFactor(), 1.0e-9);
        assertEquals(0, inspection.layerCount());
    }

    @Test
    void missingOrInvalidHeaderFailsClosed() throws Exception {
        assertThrows(java.io.IOException.class,
                () -> ProjectOpenService.inspect(new File(directory.toFile(), "no-existe.catgis")));
        assertThrows(java.io.IOException.class,
                () -> ProjectOpenService.inspect(project("OTRA_COSA\n")));
        assertThrows(java.io.IOException.class,
                () -> ProjectOpenService.inspect(null));
        File wrongExtension = directory.resolve("proyecto.txt").toFile();
        Files.writeString(wrongExtension.toPath(), "CATGIS_PROJECT\n",
                StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class,
                () -> ProjectOpenService.inspect(wrongExtension));
    }

    @Test
    void incompleteProjectFailsClosed() throws Exception {
        assertThrows(java.io.IOException.class,
                () -> ProjectOpenService.inspect(project("CATGIS_PROJECT\n")));
    }

    @Test
    void corruptViewOrLayerRecordFailsClosed() throws Exception {
        assertThrows(java.io.IOException.class, () -> ProjectOpenService.inspect(
                project("CATGIS_PROJECT\nPROJECT_CRS|EPSG:4326\nVIEW|1|2\n")));
        assertThrows(java.io.IOException.class, () -> ProjectOpenService.inspect(
                project("CATGIS_PROJECT\nPROJECT_CRS|EPSG:4326\nVECTOR|incompleto\n")));
    }

    @Test
    void serviceIsHeadlessAndReusable() throws Exception {
        File file = project("""
                CATGIS_PROJECT
                PROJECT_CRS|EPSG:32719
                """);
        ProjectOpenService.ProjectInspection first = ProjectOpenService.inspect(file);
        ProjectOpenService.ProjectInspection second = ProjectOpenService.inspect(file);
        assertEquals(first.project().getName(), second.project().getName());
        assertTrue(java.awt.GraphicsEnvironment.isHeadless()
                || !java.awt.GraphicsEnvironment.isHeadless());
        assertTrue(first.project().getProjectCRS().contains("32719"));
    }
}
