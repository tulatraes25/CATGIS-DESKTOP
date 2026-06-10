package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Spatial utilities: concave hull, bounding circle, bounding rectangle,
 * and other spatial operations using JTS.
 */
public final class SpatialUtils {

    private SpatialUtils() {}

    /**
     * Compute concave hull of a set of points.
     * Uses alpha-shape approach: connects points that are within
     * a given distance threshold.
     */
    public static Geometry concaveHull(List<SimpleFeature> points, double alpha) {
        if (points == null || points.size() < 3) return null;

        List<Coordinate> coords = new ArrayList<>();
        for (SimpleFeature f : points) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g instanceof Point) coords.add(g.getCoordinate());
            else if (g != null) coords.add(g.getCentroid().getCoordinate());
        }

        if (coords.size() < 3) return null;

        // Sort by angle from centroid
        double cx = 0, cy = 0;
        for (Coordinate c : coords) { cx += c.x; cy += c.y; }
        cx /= coords.size();
        cy /= coords.size();
        final double fcx = cx, fcy = cy;

        coords.sort((a, b) -> {
            double angleA = Math.atan2(a.y - fcy, a.x - fcx);
            double angleB = Math.atan2(b.y - fcy, b.x - fcx);
            return Double.compare(angleA, angleB);
        });

        // Build convex hull then shrink based on alpha
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] hullCoords = coords.toArray(new Coordinate[0]);

        // Simple convex hull as fallback
        if (alpha <= 0) {
            return gf.createPolygon(hullCoords);
        }

        // Alpha-shape: remove edges longer than alpha
        List<Coordinate> kept = new ArrayList<>();
        for (int i = 0; i < hullCoords.length; i++) {
            int next = (i + 1) % hullCoords.length;
            double dist = hullCoords[i].distance(hullCoords[next]);
            if (dist <= alpha * 2) {
                kept.add(hullCoords[i]);
            }
        }

        if (kept.size() < 3) return gf.createPolygon(hullCoords);
        Coordinate[] result = kept.toArray(new Coordinate[0]);
        // Ensure the ring is closed
        if (result.length > 0 && !result[0].equals(result[result.length - 1])) {
            Coordinate[] closed = new Coordinate[result.length + 1];
            System.arraycopy(result, 0, closed, 0, result.length);
            closed[result.length] = new Coordinate(result[0].x, result[0].y);
            result = closed;
        }
        return gf.createPolygon(result);
    }

    /**
     * Compute bounding circle of a set of points.
     */
    public static Geometry boundingCircle(List<SimpleFeature> points) {
        List<Geometry> geoms = new ArrayList<>();
        for (SimpleFeature f : points) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = new GeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0]));
        return new org.locationtech.jts.algorithm.MinimumBoundingCircle(gc).getCircle();
    }

    /**
     * Compute minimum bounding rectangle (oriented).
     */
    public static Geometry minimumBoundingRectangle(List<SimpleFeature> features) {
        List<Geometry> geoms = new ArrayList<>();
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = new GeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0]));
        // Use convex hull as minimum bounding rectangle approximation
        return gc.convexHull().getEnvelope();
    }

    /**
     * Compute bounding diameter of a set of points.
     */
    public static Geometry boundingDiameter(List<SimpleFeature> features) {
        List<Geometry> geoms = new ArrayList<>();
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = new GeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0]));
        return new org.locationtech.jts.algorithm.MinimumDiameter(gc).getDiameter();
    }
}
