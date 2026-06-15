package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpatiaLiteRealTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir Path tempDir;
    private static boolean ogr2ogrAvailable() { return new File(OGR2OGR).exists(); }

    private void assumeGdalCompatible() {
        try {
            Process p = new ProcessBuilder(OGR2OGR, "--version").redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes()); p.waitFor();
            int d1 = out.indexOf('.'), d2 = out.indexOf('.', d1+1);
            if (d1 > 0 && d2 > 0) {
                int major = Integer.parseInt(out.substring(d1-1,d1).trim());
                int minor = Integer.parseInt(out.substring(d1+1,d2));
                Assumptions.assumeTrue(major < 3 || (major == 3 && minor < 13),
                    "Skipped: GDAL >= 3.13 SpatiaLite format incompatible");
            }
        } catch (Exception e) {
            if (e instanceof org.opentest4j.TestAbortedException) throw (RuntimeException) e;
        }
    }

    private String discoverTable(File sqliteDb) throws Exception {
        var info = new SpatiaLiteConnectionInfo(); info.setFilePath(sqliteDb.getAbsolutePath());
        var types = SpatiaLiteLoader.listFeatureTypes(info);
        assertFalse(types.isEmpty(), "expected >= 1 spatial table");
        return types.get(0).getTableName();
    }

    @Test void loadSpatiaLiteWithPolygonReturnsShapefileData() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File output = tempDir.resolve("output.sqlite").toFile();
        generateSpatiaLite(output, "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona A\" },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[0,0],[10,0],[10,10],[0,10],[0,0]]] } } ] }");
        var layer = new SpatiaLiteLayer("test", output.getAbsolutePath());
        layer.setTableName(discoverTable(output));
        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertNotNull(data); assertEquals(1, data.getFeatures().size());
    }

    @Test void loadSpatiaLitePreservesAttributes() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File output = tempDir.resolve("multi.sqlite").toFile();
        generateSpatiaLite(output, "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona Urbana\", \"area_ha\": 150.0 },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[0,0],[10,0],[10,10],[0,10],[0,0]]] } },"
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Parque Industrial\", \"area_ha\": 320.0 },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[20,20],[30,20],[30,30],[20,30],[20,20]]] } } ] }");
        var layer = new SpatiaLiteLayer("test", output.getAbsolutePath());
        layer.setTableName(discoverTable(output));
        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertEquals(2, data.getFeatures().size());
        assertEquals("Zona Urbana", data.getFeatures().get(0).getAttribute("nombre"));
    }

    @Test void validateFileAcceptsSpatiaLite() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File output = tempDir.resolve("pt.sqlite").toFile();
        generateSpatiaLite(output, "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": {},"
            + "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [5,5] } } ] }");
        assertTrue(SpatiaLiteLoader.validateFile(output).isValid());
    }

    private void generateSpatiaLite(File output, String geoJson) throws Exception {
        File input = writeGeoJson("input.geojson", geoJson);
        Process p = new ProcessBuilder(OGR2OGR, "-f", "SQLite", output.getAbsolutePath(),
                input.getAbsolutePath(), "-dsco", "SPATIALITE=YES").redirectErrorStream(true).start();
        assertEquals(0, p.waitFor(), "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes()));
    }

    private File writeGeoJson(String name, String content) throws Exception {
        File f = tempDir.resolve(name).toFile();
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }
}
