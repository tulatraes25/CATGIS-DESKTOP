package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T1/#146: canonical project save/reopen roundtrip for every persisted layer type
 * keeps identity, path, source CRS, feature count and source name. Remote layers
 * are serialized without credentials and deferred to the staging phase (#144).
 */
class ProjectLayerRoundtripByTypeContractTest {

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
        MapPanel mapPanel = new MapPanel();
        CatgisDesktopApp.mapPanel = mapPanel;
        AppContext.get().setMapPanel(mapPanel);
    }

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
        AppContext.get().setProject(null);
        AppContext.get().setMapPanel(null);
        CatgisDesktopApp.mapPanel = null;
    }

    private Path source(String fileName) throws Exception {
        Path path = directory.resolve(fileName);
        Files.writeString(path, "fixture", StandardCharsets.UTF_8);
        return path;
    }

    @Test
    void roundtripKeepsMetadataForEveryLayerType() throws Exception {
        Project project = new Project("Roundtrip");
        project.setProjectCRS("EPSG:32719");
        List<Layer> expected = new ArrayList<>();
        expected.add(layer("GPX capa", "GPX", source("puntos.gpx")));
        expected.add(layer("GeoPackage capa", "GEOPACKAGE", source("datos.gpkg")));
        expected.add(layer("SpatiaLite capa", "SPATIALITE", source("datos.sqlite")));
        expected.add(layer("Vector capa", "VECTOR", source("capa.shp")));
        PostgisLayer postgis = new PostgisLayer("PostGIS capa");
        postgis.setHost("localhost");
        postgis.setPort(5432);
        postgis.setDatabaseName("catastro");
        postgis.setSchemaName("public");
        postgis.setTypeName("parcelas");
        postgis.setSourceCRS("EPSG:32719");
        postgis.setFeatureCount(1234);
        postgis.setSourceName("origen | Ñandú");
        expected.add(postgis);
        OnlineWfsLayer wfs = new OnlineWfsLayer("WFS capa");
        wfs.setServiceUrl("https://example.test/wfs?typename=catastro:parcelas");
        wfs.setTypeName("catastro:parcelas");
        wfs.setSourceCRS("EPSG:32719");
        wfs.setFeatureCount(1234);
        wfs.setSourceName("origen | Ñandú");
        expected.add(wfs);
        for (Layer layer : expected) {
            project.addLayer(layer);
        }
        AppContext.setCurrentProject(project);
        AppContext.get().setProject(project);

        File file = directory.resolve("roundtrip.catgis").toFile();
        Method save = Class.forName("ar.com.catgis.SaveProjectAction")
                .getDeclaredMethod("saveProjectToFile", File.class, boolean.class);
        save.setAccessible(true);
        assertTrue((boolean) save.invoke(null, file, false));


        Project reopened = ProjectOpenService.inspect(file).project();
        assertNotNull(reopened);
        assertEquals(expected.size(), reopened.getLayers().size());
        Map<String, Layer> byName = new LinkedHashMap<>();
        for (Layer layer : reopened.getLayers()) {
            byName.put(layer.getName(), layer);
        }

        for (Layer original : expected) {
            Layer copy = byName.get(original.getName());
            assertNotNull(copy, "layer not reopened: " + original.getName());
            assertEquals(original.getType().replace("[", "").replace("]", ""),
                    copy.getType().replace("[", "").replace("]", ""),
                    "type mismatch for " + original.getName());
            assertEquals(original.getPath(), copy.getPath(),
                    "path mismatch for " + original.getName());
            assertEquals(original.getSourceCRS(), copy.getSourceCRS(),
                    "source CRS mismatch for " + original.getName());
            assertEquals(original.getFeatureCount(), copy.getFeatureCount(),
                    "feature count mismatch for " + original.getName());
            assertEquals(original.getSourceName(), copy.getSourceName(),
                    "source name mismatch for " + original.getName());
        }
    }

    private static Layer layer(String name, String type, Path path) {
        Layer layer = new Layer(name, path.toString(), type);
        layer.setSourceCRS("EPSG:32719");
        layer.setFeatureCount(1234);
        layer.setSourceName("origen | Ñandú");
        return layer;
    }
}
