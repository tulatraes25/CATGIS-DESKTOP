package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for FlatGeobufLoader with valid .fgb files
 * generated via ogr2ogr (OSGeo4W / GDAL).
 * <p>
 * Requires: GDAL ogr2ogr at C:\OSGeo4W64\bin\ogr2ogr.exe.
 * Tests skip silently if GDAL is not installed.
 */
@Disabled("GDAL >= 3.13 writes FlatGeobuf in official spec format incompatible with org.wololo:flatgeobuf:3.26.2 reader. Requires library update.")
class FlatGeobufRealTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir
    Path tempDir;

    private static boolean ogr2ogrAvailable() {
        return new File(OGR2OGR).exists();
    }

    @Test
    void loadSinglePolygonRoundtrip() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File geojson = tempDir.resolve("test.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": ["
                        + "{ \"type\": \"Feature\", \"properties\": { \"name\": \"Zona A\" },"
                        + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                        + "[0,0],[10,0],[10,10],[0,10],[0,0]]] } } ] }",
                StandardCharsets.UTF_8);

        File fgb = tempDir.resolve("output.fgb").toFile();
        Process p = new ProcessBuilder(OGR2OGR, "-f", "FlatGeobuf", fgb.getAbsolutePath(),
                geojson.getAbsolutePath()).redirectErrorStream(true).start();
        int exit = p.waitFor();
        assertEquals(0, exit, "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes()));

        ShapefileData data = FlatGeobufLoader.load(fgb);
        assertNotNull(data);
        assertEquals(1, data.getFeatures().size());
        assertFalse(data.getEnvelope().isNull());

        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertNotNull(g);
        assertEquals("Polygon", g.getGeometryType());
        Coordinate[] coords = g.getCoordinates();
        assertEquals(5, coords.length);
        assertEquals(0.0, coords[0].x, 0.01);
        assertEquals(10.0, coords[2].x, 0.01);
    }

    @Test
    void validateFileAcceptsValidFgb() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File geojson = tempDir.resolve("v.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": ["
                        + "{ \"type\": \"Feature\", \"properties\": {},"
                        + "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [5,5] } } ] }",
                StandardCharsets.UTF_8);

        File fgb = tempDir.resolve("valid.fgb").toFile();
        new ProcessBuilder(OGR2OGR, "-f", "FlatGeobuf", fgb.getAbsolutePath(),
                geojson.getAbsolutePath()).redirectErrorStream(true).start().waitFor();

        ValidationResult vr = FlatGeobufLoader.validateFile(fgb);
        assertTrue(vr.isValid(), vr.message());
        assertTrue(vr.message().contains("FlatGeobuf"));
    }

    @Test
    void loadTwoPolygonsRoundtrip() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File geojson = tempDir.resolve("two.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": ["
                        + "{ \"type\": \"Feature\", \"properties\": { \"id\": 1 },"
                        + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                        + "[0,0],[10,0],[10,10],[0,10],[0,0]]] } },"
                        + "{ \"type\": \"Feature\", \"properties\": { \"id\": 2 },"
                        + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                        + "[20,20],[30,20],[30,30],[20,30],[20,20]]] } } ] }",
                StandardCharsets.UTF_8);

        File fgb = tempDir.resolve("two.fgb").toFile();
        new ProcessBuilder(OGR2OGR, "-f", "FlatGeobuf", fgb.getAbsolutePath(),
                geojson.getAbsolutePath()).redirectErrorStream(true).start().waitFor();

        ShapefileData data = FlatGeobufLoader.load(fgb);
        assertEquals(2, data.getFeatures().size());
    }

    @Test
    void loadEmptyFeaturesFile() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File geojson = tempDir.resolve("empty.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": [] }",
                StandardCharsets.UTF_8);

        File fgb = tempDir.resolve("empty.fgb").toFile();
        new ProcessBuilder(OGR2OGR, "-f", "FlatGeobuf", fgb.getAbsolutePath(),
                geojson.getAbsolutePath()).redirectErrorStream(true).start().waitFor();

        ShapefileData data = FlatGeobufLoader.load(fgb);
        assertNotNull(data);
        assertEquals(0, data.getFeatures().size());
    }
}
