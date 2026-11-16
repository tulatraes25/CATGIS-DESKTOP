package ar.com.catgis.catmap.workspace;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T16: workspace rows and bounded jobs/results model. */
class DataWorkspaceModelContractTest {

    private static Project project(Layer... layers) {
        Project project = new Project("Prueba");
        for (Layer layer : layers) {
            project.getLayers().add(layer);
        }
        return project;
    }

    @Test
    void rowsReflectProjectLayersAndSymbology() {
        Layer roads = new Layer("Rutas", "/tmp/rutas.shp", "shapefile");
        roads.setFeatureCount(120);
        roads.setSourceCRS("EPSG:3857");
        roads.setVisible(true);
        Layer dem = new Layer("DEM", "/tmp/dem.tif", "raster");
        dem.setVisible(false);
        List<DataWorkspaceModel.LayerRow> rows =
                DataWorkspaceModel.rows(project(roads, dem));
        assertEquals(2, rows.size());
        assertEquals("Rutas", rows.get(0).name());
        assertEquals("Vectorial", rows.get(0).kind());
        assertEquals("EPSG:3857", rows.get(0).crs());
        assertEquals(120, rows.get(0).featureCount());
        assertTrue(rows.get(0).describe().contains("120 entidades"));
        assertEquals("Raster", rows.get(1).kind());
        assertEquals(Color.class, rows.get(0).symbol().getClass());
        assertTrue(rows.get(1).describe().contains("oculta"));
        assertEquals(rows.get(0).symbol(), DataWorkspaceModel.symbolOf("rutas"));
        assertNotEquals(DataWorkspaceModel.symbolOf("Rutas"),
                DataWorkspaceModel.symbolOf("Otro"));
    }

    @Test
    void nullProjectHasNoRows() {
        assertTrue(DataWorkspaceModel.rows(null).isEmpty());
    }

    @Test
    void jobsAreBoundedAndDescribed() {
        DataWorkspaceModel model = new DataWorkspaceModel();
        model.addJob("Curvas de nivel", "OK", "generadas");
        model.addJob(null, null, null);
        assertEquals(2, model.jobCount());
        assertTrue(model.jobs().get(0).describe().contains("Curvas de nivel"));
        assertEquals("Trabajo", model.jobs().get(1).kind());
        assertEquals("INFO", model.jobs().get(1).status());
        for (int i = 0; i < DataWorkspaceModel.MAX_JOBS + 10; i++) {
            model.addJob("Job " + i, "OK", "detalle");
        }
        assertEquals(DataWorkspaceModel.MAX_JOBS, model.jobCount());
        model.clearJobs();
        assertEquals(0, model.jobCount());
    }
}
