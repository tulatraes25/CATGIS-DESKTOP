package ar.com.catgis;

import ar.com.catgis.core.geometry.PolygonCoverageAudit;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.io.WKTReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Valid MultiPolygons can intersect in area plus a separate edge or point. */
class PolygonMixedIntersectionContractTest {

    @Test
    void overlappingAreaSurvivesASeparateSharedEdge() throws Exception {
        verifyMixedIntersection("((6 0,8 0,8 2,6 2,6 0))", "LineString");
    }

    @Test
    void overlappingAreaSurvivesASeparateSharedPoint() throws Exception {
        verifyMixedIntersection("((6 2,8 2,8 4,6 4,6 2))", "Point");
    }

    private static void verifyMixedIntersection(String secondComponent, String contactType) throws Exception {
        WKTReader reader = new WKTReader();
        Geometry a = reader.read("MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)),((4 0,6 0,6 2,4 2,4 0)))");
        Geometry b = reader.read("MULTIPOLYGON(((1 1,3 1,3 3,1 3,1 1))," + secondComponent + ")");
        a.setSRID(32719);
        b.setSRID(32719);
        String beforeA = a.toText();
        String beforeB = b.toText();
        assertTrue(a.isValid());
        assertTrue(b.isValid());
        Geometry raw = a.intersection(b);
        assertEquals("GeometryCollection", raw.getGeometryType(), "Exercise JTS mixed output, not just a Polygon.");
        assertEquals(1.0, raw.getArea(), 1e-12);
        boolean foundContact = false;
        for (int i = 0; i < raw.getNumGeometries(); i++) {
            foundContact |= contactType.equals(raw.getGeometryN(i).getGeometryType());
        }
        assertTrue(foundContact);
        Geometry expected = reader.read("POLYGON((1 1,2 1,2 2,1 2,1 1))");
        for (List<Geometry> inputs : List.of(List.of(a, b), List.of(b, a))) {
            var overlaps = PolygonCoverageAudit.overlaps(inputs);
            assertEquals(1, overlaps.size());
            Geometry actual = overlaps.get(0).geometry();
            assertTrue(actual instanceof Polygonal);
            assertTrue(actual.equalsTopo(expected), "Only real overlapping area belongs in the issue geometry.");
            assertEquals(1.0, actual.getArea(), 1e-12);
            assertEquals(32719, actual.getSRID());
        }
        var report = TopologyValidator.validateNoOverlaps(List.of(feature("A", a), feature("B", b)));
        assertFalse(report.valid());
        assertEquals(TopologyValidator.EvaluationStatus.INVALID, report.status(),
                "A genuine overlap must be reported, not downgraded to NOT_EVALUATED.");
        assertEquals(1, report.issues().size());
        assertTrue(report.issues().get(0).geometry().equalsTopo(expected));
        assertEquals(beforeA, a.toText());
        assertEquals(beforeB, b.toText());
        assertEquals(32719, a.getSRID());
        assertEquals(32719, b.getSRID());
    }

    private static SimpleFeature feature(String id, Geometry geometry) {
        SimpleFeatureTypeBuilder type = new SimpleFeatureTypeBuilder();
        type.setName("mixed-intersection");
        type.add("geom", Geometry.class);
        SimpleFeatureBuilder feature = new SimpleFeatureBuilder(type.buildFeatureType());
        feature.add(geometry);
        return feature.buildFeature(id);
    }
}
