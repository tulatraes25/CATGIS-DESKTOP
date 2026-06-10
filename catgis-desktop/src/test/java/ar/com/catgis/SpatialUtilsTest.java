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

    private SimpleFeatureType pointType;
    private List<SimpleFeature> points;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("test_points");
        tb.add("the_geom", Point.class);
        pointType = tb.buildFeatureType();

        points = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        addPoint(fb, 0, 0);
        addPoint(fb, 10, 0);
        addPoint(fb, 10, 10);
        addPoint(fb, 0, 10);
        addPoint(fb, 5, 5);
    }

    private void addPoint(SimpleFeatureBuilder fb, double x, double y) {
        fb.add(GF.createPoint(new Coordinate(x, y)));
        points.add(fb.buildFeature("p" + points.size()));
    }

    private static final GeometryFactory GF = new GeometryFactory();

    @Test
    void concaveHullReturnsPolygon() {
        Geometry hull = SpatialUtils.concaveHull(points, 5.0);
        assertNotNull(hull, "Concave hull should not be null");
        assertTrue(hull.getArea() > 0, "Concave hull should have area");
    }

    @Test
    void boundingCircleReturnsGeometry() {
        Geometry circle = SpatialUtils.boundingCircle(points);
        assertNotNull(circle, "Bounding circle should not be null");
    }

    @Test
    void boundingDiameterReturnsGeometry() {
        Geometry diameter = SpatialUtils.boundingDiameter(points);
        assertNotNull(diameter, "Bounding diameter should not be null");
    }

    @Test
    void concaveHullReturnsNullForTooFewPoints() {
        List<SimpleFeature> twoPoints = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(pointType);
        fb.add(GF.createPoint(new Coordinate(0, 0)));
        twoPoints.add(fb.buildFeature("p0"));
        fb.add(GF.createPoint(new Coordinate(1, 1)));
        twoPoints.add(fb.buildFeature("p1"));
        assertNull(SpatialUtils.concaveHull(twoPoints, 5.0));
    }
}
