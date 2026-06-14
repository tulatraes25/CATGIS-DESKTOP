package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
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
 * Round-trip tests for GeoPackage loading via SpatiaLiteLoader.
 * Generates valid .gpkg files via ogr2ogr (OSGeo4W / GDAL) from GeoJSON.
 * <p>
 * Requires: GDAL ogr2ogr at C:\OSGeo4W64\bin\ogr2ogr.exe.
 * Tests skip silently if GDAL is not installed.
 */
class GeoPackageRealTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir
    Path tempDir;

    private static boolean ogr2ogrAvailable() {
        return new File(OGR2OGR).exists();
    }

    @Test
    void loadGpkgWithPolygonReturnsShapefileData() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File gpkg = tempDir.resolve("test.gpkg").toFile();
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName("test");

        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertNotNull(data);
        assertEquals(1, data.getFeatures().size());
    }

    @Test
    void loadGpkgPreservesGeometryType() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File gpkg = tempDir.resolve("points.gpkg").toFile();
        generateGpkg(gpkg, pointGeoJson("Punto A", 5, 5));

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName("test");

        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertEquals("Point", g.getGeometryType());
    }

    @Test
    void loadGpkgPreservesAttributes() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File gpkg = tempDir.resolve("attribs.gpkg").toFile();
        generateGpkg(gpkg,
                "{ \"type\": \"FeatureCollection\", \"features\": ["
                        + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona Urbana\", \"area_ha\": 150.0 },"
                        + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                        + "[0,0],[10,0],[10,10],[0,10],[0,0]]] } },"
                        + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Parque Industrial\", \"area_ha\": 320.0 },"
                        + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                        + "[20,20],[30,20],[30,30],[20,30],[20,20]]] } } ] }");

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName("test");

        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertEquals(2, data.getFeatures().size());
        SimpleFeature f0 = data.getFeatures().get(0);
        assertEquals("Zona Urbana", f0.getAttribute("nombre"));
        assertEquals(150.0, (Double) f0.getAttribute("area_ha"), 0.01);
    }

    @Test
    void validateFileAcceptsGpkg() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File gpkg = tempDir.resolve("valid.gpkg").toFile();
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));

        ValidationResult vr = SpatiaLiteLoader.validateFile(gpkg);
        assertTrue(vr.isValid(), vr.message());
    }

    @Test
    void listFeatureTypesFindsTable() throws Exception {
        if (!ogr2ogrAvailable()) return;

        File gpkg = tempDir.resolve("multi.gpkg").toFile();
        // Generate with 2 layers by writing separate GPKGs, then merging... 
        // Simpler: single GPKG with 1 layer
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));

        SpatiaLiteConnectionInfo info = new SpatiaLiteConnectionInfo();
        info.setFilePath(gpkg.getAbsolutePath());

        java.util.List<SpatiaLiteFeatureTypeInfo> types =
                SpatiaLiteLoader.listFeatureTypes(info);
        assertNotNull(types);
        assertTrue(types.size() >= 1, "expected at least 1 table, got " + types.size());
    }

    // ---- Helpers ----

    private void generateGpkg(File output, String geoJson) throws Exception {
        File input = tempDir.resolve("input.geojson").toFile();
        Files.writeString(input.toPath(), geoJson, StandardCharsets.UTF_8);

        Process p = new ProcessBuilder(OGR2OGR, "-f", "GPKG", output.getAbsolutePath(),
                input.getAbsolutePath(), "-nln", "test")
                .redirectErrorStream(true).start();
        int exit = p.waitFor();
        assertEquals(0, exit, "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes()));
    }

    private String polygonGeoJson(String name, double x1, double y1, double x2, double y2) {
        return "{ \"type\": \"FeatureCollection\", \"features\": ["
                + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"" + name + "\" },"
                + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [["
                + "[" + x1 + "," + y1 + "],[" + x2 + "," + y1 + "],"
                + "[" + x2 + "," + y2 + "],[" + x1 + "," + y2 + "],"
                + "[" + x1 + "," + y1 + "]]] } } ] }";
    }

    private String pointGeoJson(String name, double x, double y) {
        return "{ \"type\": \"FeatureCollection\", \"features\": ["
                + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"" + name + "\" },"
                + "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [" + x + "," + y + "] } } ] }";
    }
}
