package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.*;

class CrsMeasurementEnvelopeFailClosedContractTest {

    @AfterEach
    void cleanup() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void mapMeasurementRejectsUnknownAndInvalidSourceCrs() {
        LineString line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 1)});
        assertNull(MapMeasurementUtils.reprojectToMetric(line, ""));
        assertNull(MapMeasurementUtils.reprojectToMetric(line, "EPSG:NOT_REAL"));
        assertEquals("", MapMeasurementUtils.chooseMetricCRS(""));
    }

    @Test
    void geographicMeasurementStillProducesFiniteMetricGeometry() {
        LineString line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                new Coordinate[]{new Coordinate(-68.85, -32.89), new Coordinate(-68.85, -32.88)});
        Geometry metric = MapMeasurementUtils.reprojectToMetric(line, "EPSG:4326");
        assertNotNull(metric);
        assertTrue(Double.isFinite(metric.getLength()));
        assertTrue(metric.getLength() > 100d);
    }

    @Test
    void vectorMeasurementDoesNotInheritProjectCrsWhenLayerCrsIsUnknown() {
        Project project = new Project("metric-project");
        project.setProjectCRS("EPSG:32719");
        AppContext.setCurrentProject(project);

        Layer layer = new Layer("unknown", "", "VECTOR");
        layer.setSourceCRS("");
        LineString line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                new Coordinate[]{new Coordinate(500000, 6200000), new Coordinate(500100, 6200000)});

        assertTrue(Double.isNaN(VectorMeasurementSupport.resolveLengthMeters(layer, line)));
    }

    @Test
    void arbitraryProjectedMetricCrsIsNotForcedThroughWebMercator() {
        LineString line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                new Coordinate[]{new Coordinate(4321000, 3210000), new Coordinate(4321100, 3210000)});
        Geometry metric = MapMeasurementUtils.reprojectToMetric(line, "EPSG:3035");
        assertNotNull(metric);
        assertEquals(100d, metric.getLength(), 1e-6);
        assertEquals("EPSG:3035", MapMeasurementUtils.chooseMetricCRS("EPSG:3035"));
    }

    @Test
    void knownMetricLayerKeepsMetricLength() {
        Layer layer = new Layer("utm", "", "VECTOR");
        layer.setSourceCRS("EPSG:32719");
        LineString line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                new Coordinate[]{new Coordinate(500000, 6200000), new Coordinate(500100, 6200000)});
        assertEquals(100d, VectorMeasurementSupport.resolveLengthMeters(layer, line), 1e-9);
    }

    @Test
    void fieldMetricGuardRejectsUnavailableMeasurementInsteadOfConvertingItToZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GeometryMetricExpressionSupport.requireFinite(Double.NaN, "$area_m2"));
        assertTrue(ex.getMessage().contains("CRS"));
        assertEquals(12.5,
                GeometryMetricExpressionSupport.requireFinite(12.5, "$length_m"), 0d);
    }

    @Test
    void envelopeTransformMatchesGeoToolsForArbitraryProjectedCrs() throws Exception {
        MapPanel panel = new MapPanel();
        MapUtilities utilities = new MapUtilities(panel);
        Envelope input = new Envelope(4321000, 4322000, 3210000, 3211000);
        Envelope actual = utilities.reprojectEnvelopeIfNeeded(input, "EPSG:3035", "EPSG:4326");
        var source = CRSDefinitions.decode("EPSG:3035", true);
        var target = CRSDefinitions.decode("EPSG:4326", true);
        var transform = org.geotools.referencing.CRS.findMathTransform(source, target, true);
        Envelope expected = org.geotools.geometry.jts.JTS.transform(input, null, transform, 10);
        assertNotNull(actual);
        assertEquals(expected.getMinX(), actual.getMinX(), 1e-8);
        assertEquals(expected.getMaxY(), actual.getMaxY(), 1e-8);
    }

    @Test
    void envelopeTransformReturnsNullForUnknownOrInvalidCrsInsteadOfOriginalBox() {
        MapPanel panel = new MapPanel();
        MapUtilities utilities = new MapUtilities(panel);
        Envelope input = new Envelope(1, 2, 3, 4);

        assertNull(utilities.reprojectEnvelopeIfNeeded(input, "", "EPSG:4326"));
        assertNull(utilities.reprojectEnvelopeIfNeeded(input, "EPSG:NOT_REAL", "EPSG:4326"));
        Envelope identity = utilities.reprojectEnvelopeIfNeeded(input, "EPSG:4326", "EPSG:4326");
        assertNotSame(input, identity);
        assertEquals(input, identity);
    }

    @Test
    void onlineEnvelopeProjectionRequiresEveryCornerAndDeclaredCrs() {
        MapPanel panel = new MapPanel();
        Envelope input = new Envelope(-68.9, -68.8, -32.9, -32.8);
        assertNull(OnlineLayerRenderer.projectEnvelopeToCrs(panel, input, "", "EPSG:3857"));
        Envelope projected = OnlineLayerRenderer.projectEnvelopeToCrs(
                panel, input, "EPSG:4326", "EPSG:3857");
        assertNotNull(projected);
        assertTrue(Double.isFinite(projected.getMinX()));
        assertTrue(Double.isFinite(projected.getMaxY()));
    }
}
