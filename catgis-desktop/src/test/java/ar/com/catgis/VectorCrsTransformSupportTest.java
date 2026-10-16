package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorCrsTransformSupportTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @AfterEach
    void cleanup() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void arbitraryEpsg3035MatchesDirectGeoToolsTransform() throws Exception {
        Point source = GEOMETRY_FACTORY.createPoint(new Coordinate(4321000.0, 3210000.0));

        Geometry actual = VectorCrsTransformSupport.reprojectGeometry(
                source, "EPSG:3035", "EPSG:4326");

        CoordinateReferenceSystem src = CRS.decode("EPSG:3035", true);
        CoordinateReferenceSystem dst = CRS.decode("EPSG:4326", true);
        MathTransform mt = CRS.findMathTransform(src, dst, true);
        Geometry expected = JTS.transform(source, mt);

        assertEquals(expected.getCoordinate().x, actual.getCoordinate().x, 1e-9);
        assertEquals(expected.getCoordinate().y, actual.getCoordinate().y, 1e-9);
    }

    @Test
    void wktSourceIsParsedRatherThanFedToAuthorityDecoder() throws Exception {
        CoordinateReferenceSystem epsg3035 = CRS.decode("EPSG:3035", true);
        String wkt = epsg3035.toWKT();
        Point source = GEOMETRY_FACTORY.createPoint(new Coordinate(4321000.0, 3210000.0));

        Geometry actual = VectorCrsTransformSupport.reprojectGeometry(
                source, wkt, "EPSG:4326");

        assertTrue(Double.isFinite(actual.getCoordinate().x));
        assertTrue(Double.isFinite(actual.getCoordinate().y));
        assertTrue(Math.abs(actual.getCoordinate().x) <= 180.0);
        assertTrue(Math.abs(actual.getCoordinate().y) <= 90.0);
    }

    @Test
    void topographyUsesSchemaCrsWhenLayerCrsIsBlank() throws Exception {
        ReleaseTestSupport.initializeAppContext("CRS strict");
        AppContext.project().setProjectCRS("EPSG:4326");

        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem laea = CRS.decode("EPSG:3035", true);
        MathTransform to3035 = CRS.findMathTransform(wgs84, laea, true);
        Point lonLat = GEOMETRY_FACTORY.createPoint(new Coordinate(10.0, 52.0));
        Point projected = (Point) JTS.transform(lonLat, to3035);

        ShapefileData data = ReleaseTestSupport.buildPointData(
                "epsg3035", "EPSG:3035", projected.getCoordinate(), "A", 1);
        Layer layer = ReleaseTestSupport.buildVectorLayer(
                "epsg3035", Path.of("epsg3035.geojson"), "");

        ShapefileData result =
                TopographyWorkflowSupport.projectVectorDataToCurrentProject(layer, data);

        Coordinate coordinate = result.getFeatures().get(0).getDefaultGeometry() instanceof Point p
                ? p.getCoordinate() : null;
        assertEquals(10.0, coordinate.x, 1e-6);
        assertEquals(52.0, coordinate.y, 1e-6);
        assertEquals("EPSG:4326", layer.getSourceCRS());
    }

    @Test
    void blankCadSourceIsNotInventedAsWgs84() throws Exception {
        ReleaseTestSupport.initializeAppContext("CAD CRS truth");
        AppContext.project().setProjectCRS("EPSG:4326");

        ShapefileData data = ReleaseTestSupport.buildPointData(
                "cad", "EPSG:4326", new Coordinate(1.0, 2.0), "A", 1);
        Layer layer = ReleaseTestSupport.buildVectorLayer(
                "cad", Path.of("cad.dxf"), "");

        TopographyWorkflowSupport.projectVectorDataToCurrentProject(layer, data);

        assertTrue(layer.getSourceCRS() == null || layer.getSourceCRS().isBlank());
    }

    @Test
    void invalidSourceFailsBeforeLayerCrsIsRelabeled() throws Exception {
        ReleaseTestSupport.initializeAppContext("CRS strict failure");
        AppContext.project().setProjectCRS("EPSG:4326");

        ShapefileData data = ReleaseTestSupport.buildPointData(
                "source", "EPSG:4326", new Coordinate(1000.0, 2000.0), "A", 1);
        Layer layer = ReleaseTestSupport.buildVectorLayer(
                "source", Path.of("source.geojson"), "EPSG:999999");
        String before = layer.getSourceCRS();

        assertThrows(Exception.class,
                () -> TopographyWorkflowSupport.projectVectorDataToCurrentProject(layer, data));

        assertEquals(before, layer.getSourceCRS());
        Point original = (Point) data.getFeatures().get(0).getDefaultGeometry();
        assertEquals(1000.0, original.getX(), 0.0);
        assertEquals(2000.0, original.getY(), 0.0);
    }
}
