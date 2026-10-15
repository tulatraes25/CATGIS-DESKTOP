package ar.com.catgis;

import ar.com.catgis.core.geometry.PolygonCoverageAudit;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.*;

/** Scientific oracles use genuine JTS geometry and genuine GeoTools features. */
class PolygonCoverageAuditContractTest {
    private static final String L_BOTTOM = "POLYGON((0 0,2 0,2 1,0 1,0 0))";
    private static final String L_TOP = "POLYGON((0 1,1 1,1 2,0 2,0 1))";
    private static final String SQUARE = "POLYGON((0 0,2 0,2 2,0 2,0 0))";

    @Test
    void concaveLDoesNotReportItsExteriorCornerAsAnEnclosedGap() throws Exception {
        List<Geometry> pieces = geometries(L_BOTTOM, L_TOP);
        assertTrue(PolygonCoverageAudit.enclosedGaps(pieces).isEmpty());
        assertTrue(TopologyValidator.validateNoGaps(features(pieces), 0).valid());
        Geometry missing = PolygonCoverageAudit.uncoveredWithin(pieces, wkt(SQUARE));
        assertEquals(1.0, missing.getArea(), 1e-12);
        assertTrue(missing.equalsTopo(wkt("POLYGON((1 1,2 1,2 2,1 2,1 1))")));
    }

    @Test
    void sameCoverageIsCompleteInsideItsActualLShapedStudyArea() throws Exception {
        List<Geometry> pieces = geometries(L_BOTTOM, L_TOP);
        Geometry area = wkt("POLYGON((0 0,2 0,2 1,1 1,1 2,0 2,0 0))");
        assertTrue(PolygonCoverageAudit.uncoveredWithin(pieces, area).isEmpty());
        assertTrue(TopologyValidator.validateCoverageWithin(features(pieces), feature("AOI", area, null), 0).valid());
    }

    @Test
    void fourPolygonsEnclosingAHoleReturnTheExactMissingPolygon() throws Exception {
        Geometry missing = PolygonCoverageAudit.enclosedGaps(frame());
        assertEquals(1.0, missing.getArea(), 1e-12);
        assertTrue(missing.equalsTopo(wkt("POLYGON((1 1,2 1,2 2,1 2,1 1))")));
        var report = TopologyValidator.validateNoGaps(features(frame()), 0);
        assertEquals(TopologyValidator.EvaluationStatus.INVALID, report.status());
        assertEquals(-1, report.issues().get(0).featureIndex(), "A gap is not falsely assigned to one feature.");
        assertEquals(1.0, report.issues().get(0).geometry().getArea(), 1e-12);
    }

    @Test
    void islandsInsideAHoleAreSubtractedAndSourceHolesAreRespected() throws Exception {
        Geometry outer = wkt("POLYGON((0 0,10 0,10 10,0 10,0 0),(2 2,2 8,8 8,8 2,2 2))");
        Geometry island = wkt("POLYGON((4 4,6 4,6 6,4 6,4 4))");
        Geometry missing = PolygonCoverageAudit.enclosedGaps(List.of(outer, island));
        assertEquals(32.0, missing.getArea(), 1e-12);
        // Shared boundary is legitimate: zero overlap AREA, not empty intersection.
        assertEquals(0.0, missing.intersection(island).getArea(), 1e-12);
        assertFalse(missing.covers(island.getInteriorPoint()));
        Geometry filledHole = wkt("POLYGON((2 2,8 2,8 8,2 8,2 2))");
        assertTrue(PolygonCoverageAudit.enclosedGaps(List.of(outer, filledHole)).isEmpty());
    }

    @Test
    void disjointRegionsAreNotGapsWithoutAnExplicitStudyBoundary() throws Exception {
        List<Geometry> pieces = geometries("POLYGON((0 0,1 0,1 1,0 1,0 0))",
                "POLYGON((3 0,4 0,4 1,3 1,3 0))");
        assertTrue(PolygonCoverageAudit.enclosedGaps(pieces).isEmpty());
        Geometry missing = PolygonCoverageAudit.uncoveredWithin(pieces,
                wkt("POLYGON((0 0,4 0,4 1,0 1,0 0))"));
        assertEquals(2.0, missing.getArea(), 1e-12);
    }

    @Test
    void studyBoundaryHolesAndCoverageOutsideTheBoundaryAreNotReported() throws Exception {
        Geometry aoi = wkt("POLYGON((0 0,10 0,10 10,0 10,0 0),(2 2,2 8,8 8,8 2,2 2))");
        Geometry covering = wkt("POLYGON((-1 -1,11 -1,11 11,-1 11,-1 -1))");
        assertTrue(PolygonCoverageAudit.uncoveredWithin(List.of(covering), aoi).isEmpty());
        Geometry missing = PolygonCoverageAudit.uncoveredWithin(List.of(), aoi);
        assertEquals(64.0, missing.getArea(), 1e-12);
        assertTrue(missing.equalsTopo(aoi));
        assertNotSame(aoi, missing);
    }

    @Test
    void multipolygonAndInputPermutationDoNotChangeMissingArea() throws Exception {
        List<Geometry> pieces = frame();
        Polygon[] polygons = pieces.stream().map(g -> (Polygon) g).toArray(Polygon[]::new);
        Geometry multi = new GeometryFactory().createMultiPolygon(new Polygon[] {polygons[0], polygons[1]});
        List<Geometry> grouped = List.of(multi, polygons[2], polygons[3]);
        Geometry expected = PolygonCoverageAudit.enclosedGaps(pieces);
        assertTrue(PolygonCoverageAudit.enclosedGaps(grouped).equalsTopo(expected));
        Collections.reverse(pieces);
        assertTrue(PolygonCoverageAudit.enclosedGaps(pieces).equalsTopo(expected));
    }

    @Test
    void translationPreservesAreaAndScalingChangesItByTheSquaredScale() throws Exception {
        List<Geometry> pieces = frame();
        List<Geometry> moved = new ArrayList<>();
        List<Geometry> scaled = new ArrayList<>();
        for (Geometry geometry : pieces) {
            moved.add(AffineTransformation.translationInstance(500000, 4500000).transform(geometry));
            scaled.add(AffineTransformation.scaleInstance(3, 3).transform(geometry));
        }
        assertEquals(1.0, PolygonCoverageAudit.enclosedGaps(moved).getArea(), 1e-8);
        assertEquals(9.0, PolygonCoverageAudit.enclosedGaps(scaled).getArea(), 1e-12);
    }

    @Test
    void areaToleranceIsNotSilentlyInterpretedAsLinearDistance() throws Exception {
        List<SimpleFeature> source = features(frame());
        assertFalse(TopologyValidator.validateNoGaps(source, 0.99).valid());
        assertTrue(TopologyValidator.validateNoGaps(source, 1.0).valid());
        for (double tolerance : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertNotEvaluated(TopologyValidator.validateNoGaps(source, tolerance));
            assertNotEvaluated(TopologyValidator.validateCoverageWithin(source, feature("AOI", wkt(SQUARE), null), tolerance));
        }
    }

    @Test
    void overlapReportsActualAreaAndSharedEdgesDoNotCount() throws Exception {
        Geometry a = wkt(SQUARE);
        Geometry b = wkt("POLYGON((1 1,3 1,3 3,1 3,1 1))");
        var result = PolygonCoverageAudit.overlaps(List.of(a, b));
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).firstIndex());
        assertEquals(1, result.get(0).secondIndex());
        assertEquals(1.0, result.get(0).geometry().getArea(), 1e-12);
        assertTrue(PolygonCoverageAudit.overlaps(geometries(L_BOTTOM, L_TOP)).isEmpty());
        var report = TopologyValidator.validateNoOverlaps(features(List.of(a, b)));
        assertEquals(TopologyValidator.EvaluationStatus.INVALID, report.status());
        assertTrue(report.issues().get(0).message().contains("f0"));
        assertTrue(report.issues().get(0).message().contains("f1"));
    }

    @Test
    void invalidNullEmptyOrWrongDimensionNeverProducesASuccessfulAudit() throws Exception {
        List<Geometry> bad = Arrays.asList(null, new GeometryFactory().createPolygon(),
                wkt("LINESTRING(0 0,1 1)"), wkt("POLYGON((0 0,2 2,0 2,2 0,0 0))"));
        for (Geometry geometry : bad) {
            List<Geometry> input = Arrays.asList(wkt(SQUARE), geometry);
            assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.enclosedGaps(input));
            assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.overlaps(input));
            assertNotEvaluated(TopologyValidator.validateNoGaps(features(input), 0));
            assertNotEvaluated(TopologyValidator.validateNoOverlaps(features(input)));
        }
        assertNotEvaluated(TopologyValidator.validateNoGaps(List.of(), 0));
        assertNotEvaluated(TopologyValidator.validateNoOverlaps(null));
    }

    @Test
    void incompatibleSridOrDeclaredLayerCrsCannotProduceCleanResults() throws Exception {
        Geometry a = wkt(SQUARE);
        Geometry b = wkt(L_TOP);
        a.setSRID(4326);
        b.setSRID(3857);
        assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.enclosedGaps(List.of(a, b)));
        assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.uncoveredWithin(List.of(a), b));
        CoordinateReferenceSystem geographic = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem projected = CRS.decode("EPSG:3857", true);
        List<SimpleFeature> mismatched = List.of(feature("degrees", wkt(SQUARE), geographic),
                feature("metres", wkt(L_TOP), projected));
        assertNotEvaluated(TopologyValidator.validateNoGaps(mismatched, 0));
        assertNotEvaluated(TopologyValidator.validateCoverageWithin(List.of(mismatched.get(0)), mismatched.get(1), 0));
        assertNotEvaluated(TopologyValidator.validateNoOverlaps(List.of(mismatched.get(0), feature("unknown", wkt(SQUARE), null))));
    }

    @Test
    void interruptedAuditIsExplicitAndDoesNotConsumeThreadInterrupt() throws Exception {
        List<Geometry> source = frame();
        List<SimpleFeature> layer = features(source);
        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, () -> PolygonCoverageAudit.enclosedGaps(source));
            assertNotEvaluated(TopologyValidator.validateNoGaps(layer, 0));
            assertNotEvaluated(TopologyValidator.validateNoOverlaps(layer));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void resultMutationNeverAltersSourceGeometryAndInputLimitFailsBeforeUnion() throws Exception {
        List<Geometry> source = frame();
        List<String> before = source.stream().map(Geometry::toText).toList();
        Geometry result = PolygonCoverageAudit.enclosedGaps(source);
        result.apply((org.locationtech.jts.geom.CoordinateFilter) coordinate -> coordinate.x += 100);
        result.geometryChanged();
        assertEquals(before, source.stream().map(Geometry::toText).toList());
        List<Geometry> oversized = Collections.nCopies(100001, source.get(0));
        assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.enclosedGaps(oversized));
    }

    @Test
    void resultingGapAndOverlapKeepSridEvenWhenFactoryDefaultsToZero() throws Exception {
        List<Geometry> source = frame();
        source.forEach(geometry -> geometry.setSRID(32719));
        Geometry result = PolygonCoverageAudit.enclosedGaps(source);
        assertEquals(32719, result.getSRID());
        Geometry aoi = wkt(SQUARE);
        aoi.setSRID(32719);
        assertEquals(32719, PolygonCoverageAudit.uncoveredWithin(source, aoi).getSRID());
        Geometry duplicate = source.get(0).copy();
        var overlap = PolygonCoverageAudit.overlaps(List.of(source.get(0), duplicate));
        assertEquals(32719, overlap.get(0).geometry().getSRID());
    }

    @Test
    void nonfiniteCoordinatesAndMixedPrecisionAreRejectedWithoutRepair() throws Exception {
        Geometry ordinary = wkt(SQUARE);
        Geometry fixed = new GeometryFactory(new PrecisionModel(1000), 0)
                .createPolygon(ordinary.getCoordinates());
        assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.enclosedGaps(List.of(ordinary, fixed)));
        Geometry nonfinite = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(1, 0),
                new Coordinate(Double.POSITIVE_INFINITY, 1), new Coordinate(0, 0)});
        assertThrows(IllegalArgumentException.class, () -> PolygonCoverageAudit.enclosedGaps(List.of(nonfinite)));
    }

    private static List<Geometry> frame() throws Exception {
        return geometries("POLYGON((0 0,3 0,3 1,0 1,0 0))",
                "POLYGON((0 2,3 2,3 3,0 3,0 2))",
                "POLYGON((0 1,1 1,1 2,0 2,0 1))",
                "POLYGON((2 1,3 1,3 2,2 2,2 1))");
    }

    private static List<Geometry> geometries(String... text) throws Exception {
        List<Geometry> result = new ArrayList<>();
        for (String value : text) {
            result.add(wkt(value));
        }
        return result;
    }

    private static Geometry wkt(String value) throws Exception {
        return new WKTReader().read(value);
    }

    private static List<SimpleFeature> features(List<Geometry> values) {
        List<SimpleFeature> features = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            features.add(feature("f" + i, values.get(i), null));
        }
        return features;
    }

    private static SimpleFeature feature(String id, Geometry geometry, CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder type = new SimpleFeatureTypeBuilder();
        type.setName("audit");
        if (crs != null) {
            type.setCRS(crs);
        }
        type.add("geom", Geometry.class);
        SimpleFeatureType schema = type.buildFeatureType();
        SimpleFeatureBuilder feature = new SimpleFeatureBuilder(schema);
        feature.add(geometry);
        return feature.buildFeature(id);
    }

    private static void assertNotEvaluated(TopologyValidator.TopologyResult result) {
        assertFalse(result.valid());
        assertEquals(TopologyValidator.EvaluationStatus.NOT_EVALUATED, result.status());
        assertEquals(1, result.issues().size(), "No partial success/defect list should survive a failed audit.");
        assertEquals("NOT_EVALUATED", result.issues().get(0).rule());
    }
}
