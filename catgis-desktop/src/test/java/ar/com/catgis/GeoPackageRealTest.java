package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GeoPackageRealTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir
    Path tempDir;

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
                    "Skipped: GDAL >= 3.13 GPKG format incompatible");
            }
        } catch (Exception e) {
            if (e instanceof org.opentest4j.TestAbortedException) throw (RuntimeException) e;
        }
    }

    private String discoverFirstSpatialTable(File gpkg) throws Exception {
        SpatiaLiteConnectionInfo info = new SpatiaLiteConnectionInfo();
        info.setFilePath(gpkg.getAbsolutePath());
        var types = SpatiaLiteLoader.listFeatureTypes(info);
        assertFalse(types.isEmpty(), "expected >= 1 spatial table");
        return types.get(0).getTableName();
    }

    @Test void loadGpkgWithPolygonReturnsShapefileData() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File gpkg = tempDir.resolve("test.gpkg").toFile();
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));
        var layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName(discoverFirstSpatialTable(gpkg));
        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertNotNull(data); assertEquals(1, data.getFeatures().size());
    }

    @Test void loadGpkgPreservesGeometryType() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File gpkg = tempDir.resolve("points.gpkg").toFile();
        generateGpkg(gpkg, pointGeoJson("Punto A", 5, 5));
        var layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName(discoverFirstSpatialTable(gpkg));
        var g = (org.locationtech.jts.geom.Geometry) SpatiaLiteLoader.loadLayerData(layer).getFeatures().get(0).getDefaultGeometry();
        assertEquals("Point", g.getGeometryType());
    }

    @Test void loadGpkgPreservesAttributes() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File gpkg = tempDir.resolve("attribs.gpkg").toFile();
        generateGpkg(gpkg, "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Zona Urbana\", \"area_ha\": 150.0 },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[0,0],[10,0],[10,10],[0,10],[0,0]]] } },"
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"Parque Industrial\", \"area_ha\": 320.0 },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[20,20],[30,20],[30,30],[20,30],[20,20]]] } } ] }");
        var layer = new SpatiaLiteLayer("test", gpkg.getAbsolutePath());
        layer.setTableName(discoverFirstSpatialTable(gpkg));
        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertEquals(2, data.getFeatures().size());
        assertEquals("Zona Urbana", data.getFeatures().get(0).getAttribute("nombre"));
    }

    @Test void validateFileAcceptsGpkg() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File gpkg = tempDir.resolve("valid.gpkg").toFile();
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));
        assertTrue(SpatiaLiteLoader.validateFile(gpkg).isValid());
    }

    @Test void listFeatureTypesFindsTable() throws Exception {
        if (!ogr2ogrAvailable()) return; assumeGdalCompatible();
        File gpkg = tempDir.resolve("multi.gpkg").toFile();
        generateGpkg(gpkg, polygonGeoJson("Zona A", 0, 0, 10, 10));
        var info = new SpatiaLiteConnectionInfo(); info.setFilePath(gpkg.getAbsolutePath());
        assertTrue(SpatiaLiteLoader.listFeatureTypes(info).size() >= 1);
    }

    private void generateGpkg(File output, String geoJson) throws Exception {
        File input = tempDir.resolve("input.geojson").toFile();
        Files.writeString(input.toPath(), geoJson, StandardCharsets.UTF_8);
        Process p = new ProcessBuilder(OGR2OGR, "-f", "GPKG", output.getAbsolutePath(),
                input.getAbsolutePath(), "-nln", "test").redirectErrorStream(true).start();
        assertEquals(0, p.waitFor(), "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes()));
    }

    private String polygonGeoJson(String name, double x1, double y1, double x2, double y2) {
        return "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"" + name + "\" },"
            + "  \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[["
            + x1 + "," + y1 + "],[" + x2 + "," + y1 + "],[" + x2 + "," + y2 + "],[" + x1 + "," + y2 + "],[" + x1 + "," + y1 + "]]] } } ] }";
    }

    private String pointGeoJson(String name, double x, double y) {
        return "{ \"type\": \"FeatureCollection\", \"features\": ["
            + "{ \"type\": \"Feature\", \"properties\": { \"nombre\": \"" + name + "\" },"
            + "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [" + x + "," + y + "] } } ] }";
    }
}
