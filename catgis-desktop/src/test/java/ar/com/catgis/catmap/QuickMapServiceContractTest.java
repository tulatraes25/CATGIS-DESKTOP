package ar.com.catgis.catmap;

import ar.com.catgis.AppContext;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Quick map: single data files open without a previous project. */
class QuickMapServiceContractTest {

    @TempDir
    Path directory;

    @AfterEach
    void clearMapPanel() {
        AppContext.get().setMapPanel(null);
    }

    private File dataFile(String name, String content) throws Exception {
        File file = directory.resolve(name).toFile();
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void supportedExtensionsAreOpenable() throws Exception {
        for (String name : new String[]{"capa.shp", "capa.geojson", "capa.json", "capa.kml",
                "capa.kmz", "capa.gpkg", "capa.fgb", "capa.gpx", "capa.tif", "capa.tiff",
                "capa.png", "capa.jpg", "tabla.csv"}) {
            assertTrue(QuickMapService.canOpen(dataFile(name, "")),
                    "expected openable: " + name);
        }
        assertFalse(QuickMapService.canOpen(dataFile("nota.txt", "")));
        assertFalse(QuickMapService.canOpen(directory.resolve("falta.shp").toFile()));
        assertFalse(QuickMapService.canOpen(null));
    }

    @Test
    void unsupportedFilesAreRejectedWithoutSideEffects() throws Exception {
        File text = dataFile("nota.txt", "hola");
        assertFalse(QuickMapService.open(text, null, null));
    }

    @Test
    void csvWithExplicitCrsBecomesAPointLayer() throws Exception {
        File csv = dataFile("puntos.csv", "nombre,x,y\nA,10,20\nB,11,21\n");
        QuickMapService.CsvPointLayer parsed =
                QuickMapService.parseCsvPoints(csv, "EPSG:32719");

        assertNotNull(parsed.layer());
        assertEquals(2, parsed.layer().getFeatureCount());
        assertEquals("EPSG:32719", parsed.layer().getSourceCRS());
        assertEquals("puntos.csv", parsed.layer().getName());
        assertEquals(2, parsed.data().getFeatureCount());
    }

    @Test
    void csvWithLatLonColumnsInfersWgs84() throws Exception {
        File csv = dataFile("geo.csv", "sitio,lat,lon\nA,-34.6,-58.4\n");
        QuickMapService.CsvPointLayer parsed = QuickMapService.parseCsvPoints(csv, null);
        assertEquals("EPSG:4326", parsed.layer().getSourceCRS());
        assertEquals(1, parsed.layer().getFeatureCount());
    }

    @Test
    void csvWithoutCrsOrCoordinatesFailsClosed() throws Exception {
        File projected = dataFile("proyectado.csv", "este,norte\n1000,2000\n");
        assertThrows(java.io.IOException.class,
                () -> QuickMapService.parseCsvPoints(projected, null));

        File noCoordinates = dataFile("sinxy.csv", "solo,datos\n1,2\n");
        assertThrows(java.io.IOException.class,
                () -> QuickMapService.parseCsvPoints(noCoordinates, "EPSG:4326"));

        assertThrows(java.io.IOException.class,
                () -> QuickMapService.parseCsvPoints(directory.resolve("falta.csv").toFile(),
                        "EPSG:4326"));
        assertThrows(java.io.IOException.class,
                () -> QuickMapService.parseCsvPoints(null, "EPSG:4326"));
    }

    @Test
    void installingTheCsvLayerCreatesTheQuickProject() throws Exception {
        AppContext.get().setMapPanel(null);
        AppContext.setCurrentProject(new Project("CATMAP Standalone"));
        File csv = dataFile("mapa.csv", "nombre,x,y\nA,10,20\n");

        QuickMapService.CsvPointLayer parsed =
                QuickMapService.parseCsvPoints(csv, "EPSG:32719");
        QuickMapService.installCsvLayer(parsed);

        Project project = AppContext.project();
        assertEquals(1, project.getLayers().size());
        Layer layer = project.getLayers().get(0);
        assertEquals("mapa.csv", layer.getName());
        assertEquals("EPSG:32719", project.getProjectCRS());
    }
}
