package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Loader contract: untyped GeoJSON must keep real geometries, not null attributes. */
class GeoJsonLoaderContractTest {

    @TempDir
    Path directory;

    @Test
    void untypedLineStringFeatureKeepsGeometryAndEnvelope() throws Exception {
        Path file = directory.resolve("roads.geojson");
        Files.writeString(file, """
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","properties":{"name":"Ruta 39","highway":"secondary"},
                   "geometry":{"type":"LineString","coordinates":[[-67.6886,-45.7712],[-67.6879,-45.7714]]}}
                ]}
                """, StandardCharsets.UTF_8);

        ShapefileData data = GeoJsonLoader.load(file.toFile());

        assertEquals(1, data.getFeatureCount());
        Geometry geometry = data.getFeatures().get(0).getDefaultGeometry() instanceof Geometry g
                ? g : null;
        assertNotNull(geometry, "untyped GeoJSON lost its geometry");
        assertTrue(geometry instanceof LineString, geometry.getClass().getName());
        assertEquals(-67.6886, geometry.getCoordinates()[0].getX(), 1e-6);
        assertEquals(-45.7712, geometry.getCoordinates()[0].getY(), 1e-6);
        assertNotNull(data.getEnvelope());
        assertTrue(data.getEnvelope().getMinX() <= -67.6886
                && data.getEnvelope().getMaxX() >= -67.6879);
    }

    @Test
    void untypedPolygonFeatureKeepsGeometry() throws Exception {
        Path file = directory.resolve("urban.geojson");
        Files.writeString(file, """
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","properties":{"landuse":"residential"},
                   "geometry":{"type":"Polygon","coordinates":[[[-67.50,-45.87],[-67.49,-45.87],[-67.49,-45.86],[-67.50,-45.87]]]}}
                ]}
                """, StandardCharsets.UTF_8);

        ShapefileData data = GeoJsonLoader.load(file.toFile());

        Geometry geometry = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertNotNull(geometry, "untyped GeoJSON polygon lost its geometry");
        assertTrue(geometry instanceof Polygon, geometry.getClass().getName());
        assertEquals(0.00005, geometry.getArea(), 1e-9);
        assertTrue(data.getEnvelope().getWidth() > 0);
    }
}
