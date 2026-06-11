package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spatial utilities: concave hull, bounding circle, bounding rectangle,
 * Voronoi, Delaunay, and other spatial operations using JTS.
 */
public final class SpatialUtils {

    private static final GeometryFactory GF = new GeometryFactory();

    private SpatialUtils() {}

    public static Geometry concaveHull(List<SimpleFeature> points, double alpha) {
        if (points == null || points.size() < 3) return null;
        List<Coordinate> coords = new ArrayList<>();
        for (SimpleFeature f : points) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g instanceof Point) coords.add(g.getCoordinate());
            else if (g != null) coords.add(g.getCentroid().getCoordinate());
        }
        if (coords.size() < 3) return null;
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
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] hullCoords = coords.toArray(new Coordinate[0]);
        if (alpha <= 0) return gf.createPolygon(hullCoords);
        List<Coordinate> kept = new ArrayList<>();
        for (int i = 0; i < hullCoords.length; i++) {
            int next = (i + 1) % hullCoords.length;
            double dist = hullCoords[i].distance(hullCoords[next]);
            if (dist <= alpha * 2) kept.add(hullCoords[i]);
        }
        if (kept.size() < 3) return gf.createPolygon(hullCoords);
        Coordinate[] result = kept.toArray(new Coordinate[0]);
        if (result.length > 0 && !result[0].equals(result[result.length - 1])) {
            Coordinate[] closed = new Coordinate[result.length + 1];
            System.arraycopy(result, 0, closed, 0, result.length);
            closed[result.length] = new Coordinate(result[0].x, result[0].y);
            result = closed;
        }
        return gf.createPolygon(result);
    }

    public static Geometry boundingCircle(List<SimpleFeature> points) {
        List<Geometry> geoms = extractGeometries(points);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        return new org.locationtech.jts.algorithm.MinimumBoundingCircle(gc).getCircle();
    }

    public static Geometry minimumBoundingRectangle(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        return gc.convexHull().getEnvelope();
    }

    public static Geometry boundingDiameter(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        return new org.locationtech.jts.algorithm.MinimumDiameter(gc).getDiameter();
    }

    public static Geometry convexHull(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        return gc.convexHull();
    }

    public static Geometry mergeAll(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        return GF.createGeometryCollection(geoms.toArray(new Geometry[0])).union();
    }

    public static Geometry intersection(Geometry a, Geometry b) {
        if (a == null || b == null) return null;
        return a.intersection(b);
    }

    public static Geometry difference(Geometry a, Geometry b) {
        if (a == null || b == null) return null;
        return a.difference(b);
    }

    public static Geometry symDifference(Geometry a, Geometry b) {
        if (a == null || b == null) return null;
        return a.symDifference(b);
    }

    public static boolean pointInPolygon(Coordinate point, Polygon polygon) {
        if (point == null || polygon == null) return false;
        return polygon.contains(GF.createPoint(point));
    }

    public static Coordinate nearestPointOnGeometry(Coordinate target, Geometry geometry) {
        if (target == null || geometry == null || geometry.isEmpty()) return null;
        Coordinate[] nearest = org.locationtech.jts.operation.distance.DistanceOp.nearestPoints(GF.createPoint(target), geometry);
        return nearest != null && nearest.length >= 2 ? nearest[1] : null;
    }

    public static double nearestDistance(Coordinate target, Geometry geometry) {
        if (target == null || geometry == null || geometry.isEmpty()) return Double.MAX_VALUE;
        return geometry.distance(GF.createPoint(target));
    }

    public static Geometry simplify(Geometry geometry, double tolerance) {
        if (geometry == null) return null;
        return org.locationtech.jts.simplify.DouglasPeuckerSimplifier.simplify(geometry, tolerance);
    }

    public static Geometry buffer(Geometry geometry, double distance) {
        if (geometry == null) return null;
        return geometry.buffer(distance);
    }

    public static Geometry bufferWithSegments(Geometry geometry, double distance, int segments) {
        if (geometry == null) return null;
        return geometry.buffer(distance, Math.max(1, segments));
    }

    public static Geometry centroid(Geometry geometry) {
        if (geometry == null) return null;
        return geometry.getCentroid();
    }

    public static Geometry collectToMultiPoint(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        List<Point> points = new ArrayList<>();
        for (Geometry g : geoms) {
            if (g instanceof Point) points.add((Point) g);
            else points.add(g.getCentroid());
        }
        return GF.createMultiPoint(points.toArray(new Point[0]));
    }

    public static double totalArea(List<SimpleFeature> features) {
        if (features == null) return 0;
        double total = 0;
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) total += g.getArea();
        }
        return total;
    }

    public static double totalLength(List<SimpleFeature> features) {
        if (features == null) return 0;
        double total = 0;
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) total += g.getLength();
        }
        return total;
    }

    public static Geometry envelope(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        return GF.toGeometry(gc.getEnvelopeInternal());
    }

    public static Geometry voronoiDiagram(List<SimpleFeature> features, double bufferSize) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        VoronoiDiagramBuilder vdB = new VoronoiDiagramBuilder();
        vdB.setSites(gc);
        Envelope clipEnv = new Envelope(gc.getEnvelopeInternal());
        clipEnv.expandBy(bufferSize);
        vdB.setClipEnvelope(clipEnv);
        return vdB.getDiagram(GF);
    }

    public static Geometry delaunayTriangulation(List<SimpleFeature> features) {
        List<Geometry> geoms = extractGeometries(features);
        if (geoms.isEmpty()) return null;
        GeometryCollection gc = GF.createGeometryCollection(geoms.toArray(new Geometry[0]));
        DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
        dtb.setSites(gc);
        return dtb.getTriangles(GF);
    }

    public static Geometry orientedMinimumBoundingBox(Geometry geometry) {
        if (geometry == null) return null;
        return new org.locationtech.jts.algorithm.MinimumDiameter(geometry).getMinimumRectangle();
    }

    public static double convexHullArea(List<SimpleFeature> features) {
        Geometry hull = convexHull(features);
        return hull != null ? hull.getArea() : 0;
    }

    private static List<Geometry> extractGeometries(List<SimpleFeature> features) {
        List<Geometry> geoms = new ArrayList<>();
        if (features == null) return geoms;
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        return geoms;
    }
}
