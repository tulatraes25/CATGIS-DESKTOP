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

class GeometryToolsTest {

    private static final GeometryFactory GF = new GeometryFactory();
    private SimpleFeatureType pointType;
    private SimpleFeatureType lineType;
    private SimpleFeatureType polygonType;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder ptb = new SimpleFeatureTypeBuilder();
        ptb.setName("points"); ptb.add("the_geom", Point.class);
        pointType = ptb.buildFeatureType();

        SimpleFeatureTypeBuilder ltb = new SimpleFeatureTypeBuilder();
        ltb.setName("lines"); ltb.add("the_geom", LineString.class);
        lineType = ltb.buildFeatureType();

        SimpleFeatureTypeBuilder ptgb = new SimpleFeatureTypeBuilder();
        ptgb.setName("polygons"); ptgb.add("the_geom", Polygon.class);
        polygonType = ptgb.buildFeatureType();
    }

    @Test
    void extractCentroidsReturnsPoints() throws Exception {
        List<SimpleFeature> polys = createPolygons();
        List<SimpleFeature> centroids = GeometryTools.extractCentroids(polys, pointType);
        assertEquals(polys.size(), centroids.size());
        for (SimpleFeature f : centroids) {
            assertTrue(f.getDefaultGeometry() instanceof Point);
        }
    }

    @Test
    void extractVerticesReturnsAllCoordinates() throws Exception {
        List<SimpleFeature> lines = createLines();
        List<SimpleFeature> vertices = GeometryTools.extractVertices(lines, pointType);
        assertTrue(vertices.size() > 0);
    }

    @Test
    void computeConvexHullReturnsGeometry() throws Exception {
        List<SimpleFeature> points = createPoints();
        Geometry hull = GeometryTools.computeConvexHull(points);
        assertNotNull(hull);
        assertTrue(hull.getArea() > 0);
    }

    @Test
    void computeBufferReturnsGeometry() throws Exception {
        List<SimpleFeature> points = createPoints();
        Geometry buffer = GeometryTools.computeBuffer(points, 1.0);
        assertNotNull(buffer);
        assertTrue(buffer.getArea() > 0, "Buffer should have positive area");
        assertTrue(buffer.getNumGeometries() > 0, "Buffer should have geometries");
    }

    @Test
    void simplifyReducesComplexity() {
        Coordinate[] coords = new Coordinate[100];
        for (int i = 0; i < 100; i++) {
            coords[i] = new Coordinate(i, Math.sin(i * 0.1));
        }
        LineString line = GF.createLineString(coords);
        Geometry simplified = GeometryTools.simplify(line, 1.0);
        assertNotNull(simplified);
        assertTrue(simplified.getCoordinates().length <= coords.length);
    }

    @Test
    void smoothReturnsGeometry() {
        Coordinate[] coords = {new Coordinate(0,0), new Coordinate(5,10), new Coordinate(10,0)};
        LineString line = GF.createLineString(coords);
        Geometry smoothed = GeometryTools.smooth(line, 1.0);
        assertNotNull(smoothed);
        assertTrue(smoothed instanceof LineString, "Smoothed line should remain a LineString");
        assertTrue(smoothed.getCoordinates().length >= coords.length,
                "Smoothing should not reduce coordinate count below original");
    }

    @Test
    void computeMinimumBoundingCircle() throws Exception {
        List<SimpleFeature> points = createPoints();
        Geometry circle = GeometryTools.computeMinimumBoundingGeometry(points, "circle");
        assertNotNull(circle);
        assertTrue(circle.getArea() > 0);
    }

    @Test
    void computeNearestDistance() throws Exception {
        List<SimpleFeature> points = createPoints();
        double dist = GeometryTools.computeNearestDistance(points);
        assertTrue(dist > 0);
    }

    private List<SimpleFeature> createPoints() throws Exception {
        List<SimpleFeature> pts = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        double[][] coords = {{0,0},{10,0},{10,10},{0,10},{5,5}};
        for (double[] c : coords) {
            fb.add(GF.createPoint(new Coordinate(c[0], c[1])));
            pts.add(fb.buildFeature("p" + pts.size()));
        }
        return pts;
    }

    private List<SimpleFeature> createPolygons() throws Exception {
        List<SimpleFeature> polys = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(polygonType);
        Coordinate[] ring1 = {new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,10), new Coordinate(0,10), new Coordinate(0,0)};
        fb.add(GF.createPolygon(ring1));
        polys.add(fb.buildFeature("poly1"));
        Coordinate[] ring2 = {new Coordinate(20,20), new Coordinate(30,20), new Coordinate(30,30), new Coordinate(20,30), new Coordinate(20,20)};
        fb.add(GF.createPolygon(ring2));
        polys.add(fb.buildFeature("poly2"));
        return polys;
    }

    private List<SimpleFeature> createLines() throws Exception {
        List<SimpleFeature> lines = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        fb.add(GF.createLineString(new Coordinate[]{new Coordinate(0,0), new Coordinate(10,5), new Coordinate(20,0)}));
        lines.add(fb.buildFeature("line1"));
        return lines;
    }

    @Test
    void invertedPolygon_simplePolygon() {
        Polygon poly = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0),
                new Coordinate(10, 10), new Coordinate(0, 10),
                new Coordinate(0, 0)
        });
        Geometry result = GeometryTools.invertedPolygon(poly, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getArea() > 0);
    }

    @Test
    void invertedPolygon_nullInput() {
        assertNull(GeometryTools.invertedPolygon(null, List.of()));
    }

    @Test
    void invertedPolygon_withExtraFeatures() {
        Polygon main = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0),
                new Coordinate(10, 10), new Coordinate(0, 10),
                new Coordinate(0, 0)
        });
        Polygon extra = GF.createPolygon(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(7, 3),
                new Coordinate(7, 7), new Coordinate(3, 7),
                new Coordinate(3, 3)
        });
        Geometry result = GeometryTools.invertedPolygon(main, List.of(extra));
        assertNotNull(result);
        // Inverted polygon area = envelope area - polygon area(s) — should be smaller
        assertTrue(result.getArea() < main.getArea());
    }
}
