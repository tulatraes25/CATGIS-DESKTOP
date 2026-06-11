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

class SpatialUtilsTest {

    private static final GeometryFactory GF = new GeometryFactory();
    private SimpleFeatureType pointType;
    private SimpleFeatureType polyType;
    private SimpleFeatureType lineType;
    private List<SimpleFeature> points;
    private List<SimpleFeature> polygons;
    private List<SimpleFeature> lines;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder ptb = new SimpleFeatureTypeBuilder();
        ptb.setName("test_points");
        ptb.add("the_geom", Point.class);
        pointType = ptb.buildFeatureType();

        SimpleFeatureTypeBuilder potb = new SimpleFeatureTypeBuilder();
        potb.setName("test_polys");
        potb.add("the_geom", Polygon.class);
        polyType = potb.buildFeatureType();

        SimpleFeatureTypeBuilder ltb = new SimpleFeatureTypeBuilder();
        ltb.setName("test_lines");
        ltb.add("the_geom", LineString.class);
        lineType = ltb.buildFeatureType();

        points = new ArrayList<>();
        SimpleFeatureBuilder pfb = new SimpleFeatureBuilder(pointType);
        addPoint(pfb, 0, 0);
        addPoint(pfb, 10, 0);
        addPoint(pfb, 10, 10);
        addPoint(pfb, 0, 10);
        addPoint(pfb, 5, 5);

        polygons = new ArrayList<>();
        SimpleFeatureBuilder polfb = new SimpleFeatureBuilder(polyType);
        addPolygon(polfb, 0, 0, 5, 0, 5, 5, 0, 5);
        addPolygon(polfb, 3, 3, 8, 3, 8, 8, 3, 8);

        lines = new ArrayList<>();
        SimpleFeatureBuilder lfb = new SimpleFeatureBuilder(lineType);
        addLine(lfb, 0, 0, 10, 0);
        addLine(lfb, 10, 0, 10, 10);
    }

    private void addPoint(SimpleFeatureBuilder fb, double x, double y) {
        fb.add(GF.createPoint(new Coordinate(x, y)));
        points.add(fb.buildFeature("p" + points.size()));
    }

    private void addPolygon(SimpleFeatureBuilder fb, double... coords) {
        Coordinate[] ring = new Coordinate[coords.length / 2 + 1];
        for (int i = 0; i < coords.length; i += 2)
            ring[i / 2] = new Coordinate(coords[i], coords[i + 1]);
        ring[ring.length - 1] = new Coordinate(ring[0].x, ring[0].y);
        fb.add(GF.createPolygon(ring));
        polygons.add(fb.buildFeature("poly" + polygons.size()));
    }

    private void addLine(SimpleFeatureBuilder fb, double x1, double y1, double x2, double y2) {
        fb.add(GF.createLineString(new Coordinate[]{new Coordinate(x1, y1), new Coordinate(x2, y2)}));
        lines.add(fb.buildFeature("line" + lines.size()));
    }

    @Test void concaveHullReturnsPolygon() {
        Geometry hull = SpatialUtils.concaveHull(points, 5.0);
        assertNotNull(hull);
        assertTrue(hull.getArea() > 0);
    }

    @Test void boundingCircleReturnsGeometry() {
        assertNotNull(SpatialUtils.boundingCircle(points));
    }

    @Test void boundingDiameterReturnsGeometry() {
        Geometry diam = SpatialUtils.boundingDiameter(points);
        assertNotNull(diam);
        assertTrue(diam.getLength() > 0);
    }

    @Test void concaveHullNullForTooFew() {
        List<SimpleFeature> two = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        fb.add(GF.createPoint(new Coordinate(0, 0)));
        two.add(fb.buildFeature("a"));
        fb.add(GF.createPoint(new Coordinate(1, 1)));
        two.add(fb.buildFeature("b"));
        assertNull(SpatialUtils.concaveHull(two, 5.0));
    }

    @Test void convexHullReturnsPolygon() {
        Geometry hull = SpatialUtils.convexHull(points);
        assertNotNull(hull);
        assertTrue(hull instanceof Polygon);
    }

    @Test void convexHullArea() {
        double area = SpatialUtils.convexHullArea(points);
        assertTrue(area > 0);
    }

    @Test void mergeAllReturnsUnion() {
        Geometry merged = SpatialUtils.mergeAll(polygons);
        assertNotNull(merged);
        assertTrue(merged.getArea() > 0);
    }

    @Test void intersectionOfOverlappingPolygons() {
        Geometry a = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 0), new Coordinate(5, 5),
                new Coordinate(0, 5), new Coordinate(0, 0)});
        Geometry b = GF.createPolygon(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(8, 3), new Coordinate(8, 8),
                new Coordinate(3, 8), new Coordinate(3, 3)});
        Geometry ix = SpatialUtils.intersection(a, b);
        assertNotNull(ix);
        assertTrue(ix.getArea() > 0);
        assertEquals(4.0, ix.getArea(), 0.01);
    }

    @Test void differenceOfOverlappingPolygons() {
        Geometry a = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 0), new Coordinate(5, 5),
                new Coordinate(0, 5), new Coordinate(0, 0)});
        Geometry b = GF.createPolygon(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(8, 3), new Coordinate(8, 8),
                new Coordinate(3, 8), new Coordinate(3, 3)});
        Geometry diff = SpatialUtils.difference(a, b);
        assertNotNull(diff);
        assertTrue(diff.getArea() > 0);
    }

    @Test void symDifference() {
        Geometry a = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 0), new Coordinate(5, 5),
                new Coordinate(0, 5), new Coordinate(0, 0)});
        Geometry b = GF.createPolygon(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(8, 3), new Coordinate(8, 8),
                new Coordinate(3, 8), new Coordinate(3, 3)});
        Geometry sd = SpatialUtils.symDifference(a, b);
        assertNotNull(sd);
        assertTrue(sd.getArea() > 0);
    }

    @Test void pointInPolygonTrue() {
        Polygon poly = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10),
                new Coordinate(0, 10), new Coordinate(0, 0)});
        assertTrue(SpatialUtils.pointInPolygon(new Coordinate(5, 5), poly));
    }

    @Test void pointInPolygonFalse() {
        Polygon poly = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10),
                new Coordinate(0, 10), new Coordinate(0, 0)});
        assertFalse(SpatialUtils.pointInPolygon(new Coordinate(15, 15), poly));
    }

    @Test void nearestPointOnGeometry() {
        Geometry line = GF.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 0), new Coordinate(10, 0)});
        Coordinate nearest = SpatialUtils.nearestPointOnGeometry(new Coordinate(5, 5), line);
        assertNotNull(nearest);
        assertEquals(5.0, nearest.x, 0.1);
        assertEquals(0.0, nearest.y, 0.1);
    }

    @Test void nearestDistance() {
        Geometry line = GF.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 0), new Coordinate(10, 0)});
        double dist = SpatialUtils.nearestDistance(new Coordinate(5, 5), line);
        assertEquals(5.0, dist, 0.1);
    }

    @Test void simplifyReducesPoints() {
        Geometry line = GF.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0.1), new Coordinate(2, 0),
                new Coordinate(3, 0.1), new Coordinate(4, 0), new Coordinate(5, 0)});
        Geometry simplified = SpatialUtils.simplify(line, 0.5);
        assertNotNull(simplified);
        assertTrue(simplified.getNumPoints() <= line.getNumPoints());
    }

    @Test void bufferIncreasesArea() {
        Polygon poly = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10),
                new Coordinate(0, 10), new Coordinate(0, 0)});
        Geometry buffered = SpatialUtils.buffer(poly, 1);
        assertNotNull(buffered);
        assertTrue(buffered.getArea() > poly.getArea());
    }

    @Test void centroidReturnsPoint() {
        Polygon poly = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10),
                new Coordinate(0, 10), new Coordinate(0, 0)});
        Geometry centroid = SpatialUtils.centroid(poly);
        assertNotNull(centroid);
        assertTrue(centroid instanceof Point);
        assertEquals(5.0, ((Point) centroid).getX(), 0.01);
        assertEquals(5.0, ((Point) centroid).getY(), 0.01);
    }

    @Test void totalArea() {
        double area = SpatialUtils.totalArea(polygons);
        assertTrue(area > 0);
    }

    @Test void totalLength() {
        double len = SpatialUtils.totalLength(lines);
        assertEquals(20.0, len, 0.01);
    }

    @Test void envelopeReturnsRectangle() {
        Geometry env = SpatialUtils.envelope(points);
        assertNotNull(env);
        assertTrue(env.getArea() > 0);
    }

    @Test void collectToMultiPoint() {
        Geometry mp = SpatialUtils.collectToMultiPoint(points);
        assertNotNull(mp);
        assertTrue(mp instanceof MultiPoint);
        assertEquals(5, mp.getNumGeometries());
    }

    @Test void voronoiDiagramReturnsGeometry() {
        Geometry voronoi = SpatialUtils.voronoiDiagram(points, 1);
        assertNotNull(voronoi);
    }

    @Test void delaunayTriangulationReturnsGeometry() {
        Geometry delaunay = SpatialUtils.delaunayTriangulation(points);
        assertNotNull(delaunay);
        assertTrue(delaunay.getNumGeometries() > 0);
    }

    @Test void orientedMinimumBoundingBox() {
        Geometry box = SpatialUtils.orientedMinimumBoundingBox((Geometry) points.get(0).getDefaultGeometry());
        assertNotNull(box);
    }

    @Test void nullInputsReturnNull() {
        assertNull(SpatialUtils.convexHull(null));
        assertNull(SpatialUtils.mergeAll(null));
        assertNull(SpatialUtils.intersection(null, GF.createPoint(new Coordinate(0, 0))));
        assertNull(SpatialUtils.difference(null, GF.createPoint(new Coordinate(0, 0))));
        assertNull(SpatialUtils.symDifference(null, GF.createPoint(new Coordinate(0, 0))));
        assertNull(SpatialUtils.simplify(null, 1));
        assertNull(SpatialUtils.buffer(null, 1));
        assertNull(SpatialUtils.centroid(null));
        assertNull(SpatialUtils.envelope(null));
        assertNull(SpatialUtils.collectToMultiPoint(null));
        assertNull(SpatialUtils.voronoiDiagram(null, 1));
        assertNull(SpatialUtils.delaunayTriangulation(null));
        assertNull(SpatialUtils.orientedMinimumBoundingBox(null));
        assertEquals(0, SpatialUtils.totalArea(null), 0.001);
        assertEquals(0, SpatialUtils.totalLength(null), 0.001);
    }
}
