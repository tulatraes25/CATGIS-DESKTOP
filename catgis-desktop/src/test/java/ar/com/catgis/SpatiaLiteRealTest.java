package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for SpatiaLiteLoader with valid .sqlite files
 * generated via ogr2ogr (OSGeo4W / GDAL) with SPATIALITE=YES.
 * <p>
 * Requires: GDAL ogr2ogr at C:\OSGeo4W64\bin\ogr2ogr.exe.
 * Tests skip silently if GDAL is not installed.
 */
@Disabled("GDAL >= 3.13 SQLite/SpatiaLite output changed; table naming differs. Pending fixture update.")
class SpatiaLiteRealTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir
    Path tempDir;

    private static boolean ogr2ogrAvailable() {
        return new File(OGR2OGR).exists();
    }

    @Test
    void loadSpatiaLiteWithPolygonReturnsShapefileData() throws Exception {
        if (!ogr2ogrAvailable()) return;

        String geoJson = "{ \"type\": \"FeatureCollection\", \"features\": ["
                + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona A\", \"area_ha\": 150.0 },"
                + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                + "[0,0],[10,0],[10,10],[0,10],[0,0]]] } } ] }";

        File input = writeGeoJson("zonas.geojson", geoJson);
        File output = tempDir.resolve("output.sqlite").toFile();
        Process p = new ProcessBuilder(OGR2OGR, "-f", "SQLite", output.getAbsolutePath(),
                input.getAbsolutePath(), "-dsco", "SPATIALITE=YES")
                .redirectErrorStream(true).start();
        assertEquals(0, p.waitFor(),
                "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes()));

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", output.getAbsolutePath());
        layer.setTableName("zonas");

        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertNotNull(data);
        assertEquals(1, data.getFeatures().size());
        assertFalse(data.getEnvelope().isNull());

        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertNotNull(g);
        assertEquals("Polygon", g.getGeometryType());
    }

    @Test
    void loadSpatiaLitePreservesAttributes() throws Exception {
        if (!ogr2ogrAvailable()) return;

        String geoJson = "{ \"type\": \"FeatureCollection\", \"features\": ["
                + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona Urbana\", \"area_ha\": 150.0 },"
                + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                + "[0,0],[10,0],[10,10],[0,10],[0,0]]] } },"
                + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Parque Industrial\", \"area_ha\": 320.0 },"
                + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                + "[20,20],[30,20],[30,30],[20,30],[20,20]]] } } ] }";

        File input = writeGeoJson("multi.geojson", geoJson);
        File output = tempDir.resolve("multi.sqlite").toFile();
        new ProcessBuilder(OGR2OGR, "-f", "SQLite", output.getAbsolutePath(),
                input.getAbsolutePath(), "-dsco", "SPATIALITE=YES")
                .redirectErrorStream(true).start().waitFor();

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", output.getAbsolutePath());
        layer.setTableName("multi");

        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertEquals(2, data.getFeatures().size());

        assertEquals("Zona Urbana",
                data.getFeatures().get(0).getAttribute("nombre"));
        assertEquals(150.0, (Double) data.getFeatures().get(0).getAttribute("area_ha"), 0.01);
        assertEquals(320.0, (Double) data.getFeatures().get(1).getAttribute("area_ha"), 0.01);
    }

    @Test
    void validateFileAcceptsSpatiaLite() throws Exception {
        if (!ogr2ogrAvailable()) return;

        String geoJson = "{ \"type\": \"FeatureCollection\", \"features\": ["
                + "{ \"type\": \"Feature\", \"properties\": {},"
                + "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [5,5] } } ] }";

        File input = writeGeoJson("pt.geojson", geoJson);
        File output = tempDir.resolve("pt.sqlite").toFile();
        new ProcessBuilder(OGR2OGR, "-f", "SQLite", output.getAbsolutePath(),
                input.getAbsolutePath(), "-dsco", "SPATIALITE=YES")
                .redirectErrorStream(true).start().waitFor();

        ValidationResult vr = SpatiaLiteLoader.validateFile(output);
        assertTrue(vr.isValid(), vr.message());
    }

    private File writeGeoJson(String name, String content) throws Exception {
        File f = tempDir.resolve(name).toFile();
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }
}
