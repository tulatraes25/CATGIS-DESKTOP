package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopologyValidatorTest {

    private static final GeometryFactory GF = new GeometryFactory();
    private SimpleFeatureType polygonType;
    private SimpleFeatureType lineType;
    private SimpleFeatureType pointType;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder ptb = new SimpleFeatureTypeBuilder();
        ptb.setName("polygons"); ptb.add("the_geom", Polygon.class);
        polygonType = ptb.buildFeatureType();

        SimpleFeatureTypeBuilder ltb = new SimpleFeatureTypeBuilder();
        ltb.setName("lines"); ltb.add("the_geom", LineString.class);
        lineType = ltb.buildFeatureType();

        SimpleFeatureTypeBuilder ptgb = new SimpleFeatureTypeBuilder();
        ptgb.setName("points"); ptgb.add("the_geom", Point.class);
        pointType = ptgb.buildFeatureType();
    }

    @Test
    void validateNoGapsFindsNoIssuesForContiguousPolygons() throws Exception {
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validateNoGaps(polys, 0.001);
        assertTrue(result.valid());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void validateNoOverlapsFindsNoIssuesForNonOverlapping() throws Exception {
        List<SimpleFeature> polys = createNonOverlappingPolygons();
        var result = TopologyValidator.validateNoOverlaps(polys);
        assertTrue(result.valid());
    }

    @Test
    void validateNoOverlapsFindsOverlappingPolygons() throws Exception {
        List<SimpleFeature> polys = createOverlappingPolygons();
        var result = TopologyValidator.validateNoOverlaps(polys);
        assertFalse(result.valid());
        assertTrue(result.issues().size() > 0);
    }

    @Test
    void validateNoSelfIntersectionsPassesForValidGeometry() throws Exception {
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validateNoSelfIntersections(polys);
        assertTrue(result.valid());
    }

    @Test
    void validateLineConnectivityPassesForConnectedLines() throws Exception {
        // Create a closed line (ring) - no dangling endpoints
        List<SimpleFeature> lines = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        fb.add(GF.createLineString(new Coordinate[]{new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,10), new Coordinate(0,10), new Coordinate(0,0)}));
        lines.add(fb.buildFeature("ring"));
        var result = TopologyValidator.validateLineConnectivity(lines, 0.001);
        assertTrue(result.valid());
    }

    @Test
    void validatePointsInPolygonsPassesForContainedPoints() throws Exception {
        List<SimpleFeature> points = createPointsInsidePolygon();
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validatePointsInPolygons(points, polys);
        assertTrue(result.valid());
    }

    @Test
    void validatePointsInPolygonsFindsPointsOutside() throws Exception {
        List<SimpleFeature> points = createPointsOutsidePolygon();
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validatePointsInPolygons(points, polys);
        assertFalse(result.valid());
    }

    @Test
    void validateGeometryTypesPasses() throws Exception {
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validateGeometryTypes(polys, Polygon.class);
        assertTrue(result.valid());
    }

    @Test
    void validateGeometryTypesFails() throws Exception {
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validateGeometryTypes(polys, LineString.class);
        assertFalse(result.valid());
    }

    @Test
    void validateWithinDistancePasses() throws Exception {
        List<SimpleFeature> polys = createContiguousPolygons();
        var result = TopologyValidator.validateWithinDistance(polys, 100.0);
        assertTrue(result.valid());
    }

    private List<SimpleFeature> createContiguousPolygons() throws Exception {
        List<SimpleFeature> polys = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(polygonType);
        Coordinate[] ring1 = {new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,10), new Coordinate(0,10), new Coordinate(0,0)};
        fb.add(GF.createPolygon(ring1));
        polys.add(fb.buildFeature("p1"));
        Coordinate[] ring2 = {new Coordinate(10,0), new Coordinate(20,0), new Coordinate(20,10), new Coordinate(10,10), new Coordinate(10,0)};
        fb.add(GF.createPolygon(ring2));
        polys.add(fb.buildFeature("p2"));
        return polys;
    }

    private List<SimpleFeature> createNonOverlappingPolygons() throws Exception {
        return createContiguousPolygons();
    }

    private List<SimpleFeature> createOverlappingPolygons() throws Exception {
        List<SimpleFeature> polys = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(polygonType);
        Coordinate[] ring1 = {new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,10), new Coordinate(0,10), new Coordinate(0,0)};
        fb.add(GF.createPolygon(ring1));
        polys.add(fb.buildFeature("p1"));
        Coordinate[] ring2 = {new Coordinate(5,5), new Coordinate(15,5), new Coordinate(15,15), new Coordinate(5,15), new Coordinate(5,5)};
        fb.add(GF.createPolygon(ring2));
        polys.add(fb.buildFeature("p2"));
        return polys;
    }

    private List<SimpleFeature> createConnectedLines() throws Exception {
        List<SimpleFeature> lines = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        fb.add(GF.createLineString(new Coordinate[]{new Coordinate(0,0), new Coordinate(10,0)}));
        lines.add(fb.buildFeature("l1"));
        fb.add(GF.createLineString(new Coordinate[]{new Coordinate(10,0), new Coordinate(20,0)}));
        lines.add(fb.buildFeature("l2"));
        return lines;
    }

    private List<SimpleFeature> createPointsInsidePolygon() throws Exception {
        List<SimpleFeature> points = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        fb.add(GF.createPoint(new Coordinate(5, 5)));
        points.add(fb.buildFeature("pt1"));
        fb.add(GF.createPoint(new Coordinate(15, 5)));
        points.add(fb.buildFeature("pt2"));
        return points;
    }

    private List<SimpleFeature> createPointsOutsidePolygon() throws Exception {
        List<SimpleFeature> points = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        fb.add(GF.createPoint(new Coordinate(50, 50)));
        points.add(fb.buildFeature("pt1"));
        return points;
    }
}
