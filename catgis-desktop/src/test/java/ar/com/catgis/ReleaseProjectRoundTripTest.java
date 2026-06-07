package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseProjectRoundTripTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void savesAndReloadsProjectWithVectorLayerAndCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-release-project");
        ShapefileData data = ReleaseTestSupport.buildPointData(
                "project_smoke",
                "EPSG:4326",
                new Coordinate(-68.85, -32.89),
                "Pozo A",
                7
        );
        Path shp = tempDir.resolve("project_smoke.shp");
        Path projectFile = tempDir.resolve("Proyecto beta final.catgis");
        Layer layer = ReleaseTestSupport.buildVectorLayer("Pozos", shp, "EPSG:4326");

        ExportVectorLayerAction.saveLayerDataToFile(layer, data, shp.toFile(), null, false);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto beta final");
            CatgisDesktopApp.currentProject.setProjectCRS("EPSG:4326");
            CatgisDesktopApp.currentProject.setProjectFile(projectFile.toFile());
            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.mapPanel.showShapefile(layer, data);
            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertNotNull(CatgisDesktopApp.currentProject);
            assertEquals("EPSG:4326", CatgisDesktopApp.currentProject.getProjectCRS());
            assertEquals(1, CatgisDesktopApp.currentProject.getLayers().size());
            Layer loadedLayer = CatgisDesktopApp.currentProject.getLayers().get(0);
            assertEquals("Pozos", loadedLayer.getName());
            assertTrue("EPSG:4326".equalsIgnoreCase(loadedLayer.getSourceCRS())
                    || "CRS:84".equalsIgnoreCase(loadedLayer.getSourceCRS()));
            assertNotNull(CatgisDesktopApp.mapPanel.getShapefileData(loadedLayer));
        });
    }
}
