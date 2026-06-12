package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spatial utilities: concave hull, bounding circle, bounding rectangle,
 * Voronoi, Delaunay, and other spatial operations using JTS.
 */
public final class SpatialUtils {

    private static final GeometryFactory GF = new GeometryFactory();

    private SpatialUtils() {}

    /**
     * Compute concave hull using Delaunay-based alpha shape.
     * Alpha controls the level of detail: smaller = tighter, larger = closer to convex hull.
     * Uses JTS DelaunayTriangulationBuilder and filters edges by circumradius.
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

        // Remove duplicates for Delaunay
        coords = new ArrayList<>(new java.util.LinkedHashSet<>(coords));
        if (coords.size() < 3) return null;

        GeometryFactory gf = new GeometryFactory();

        // Build Delaunay triangulation
        org.locationtech.jts.triangulate.DelaunayTriangulationBuilder dtb =
                new org.locationtech.jts.triangulate.DelaunayTriangulationBuilder();
        dtb.setSites(coords);
        dtb.setTolerance(0);
        GeometryCollection triangles = (GeometryCollection) dtb.getTriangles(gf);
        if (triangles == null || triangles.getNumGeometries() == 0) {
            // Fallback: convex hull
            return gf.createMultiPointFromCoords(coords.toArray(new Coordinate[0])).convexHull();
        }

        // Collect boundary edges with their circumradii
        Map<String, Double> edgeRadii = new java.util.HashMap<>();
        for (int i = 0; i < triangles.getNumGeometries(); i++) {
            Polygon tri = (Polygon) triangles.getGeometryN(i);
            Coordinate[] verts = tri.getExteriorRing().getCoordinates(); // 4 coords (closed)
            double circumradius = circumradius(verts[0], verts[1], verts[2]);

            for (int j = 0; j < 3; j++) {
                Coordinate a = verts[j];
                Coordinate b = verts[j + 1];
                String key = edgeKey(a, b);
                // Keep the smallest circumradius for each edge
                edgeRadii.merge(key, circumradius, Math::min);
            }
        }

        // Filter edges: keep edges where circumradius <= alpha
        List<org.locationtech.jts.geom.LineSegment> keptEdges = new ArrayList<>();
        for (var entry : edgeRadii.entrySet()) {
            if (entry.getValue() <= alpha || alpha <= 0) {
                String[] parts = entry.getKey().split(":");
                Coordinate a = new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                Coordinate b = new Coordinate(Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                keptEdges.add(new org.locationtech.jts.geom.LineSegment(a, b));
            }
        }

        // Dissolve edges into polygon boundary
        List<Geometry> edgeLines = new ArrayList<>();
        for (var seg : keptEdges) {
            edgeLines.add(gf.createLineString(new Coordinate[]{seg.p0, seg.p1}));
        }
        if (edgeLines.isEmpty()) {
            return gf.createMultiPointFromCoords(coords.toArray(new Coordinate[0])).convexHull();
        }

        Geometry merged = gf.createMultiLineString(edgeLines.toArray(new LineString[0])).union();
        if (merged instanceof Polygon p) return p;

        // Try to polygonize
        org.locationtech.jts.operation.polygonize.Polygonizer polygonizer =
                new org.locationtech.jts.operation.polygonize.Polygonizer();
        polygonizer.add(merged);
        @SuppressWarnings("unchecked")
        java.util.Collection<Polygon> polygons = polygonizer.getPolygons();
        if (!polygons.isEmpty()) return polygons.iterator().next();
        return merged.convexHull();
    }

    private static double circumradius(Coordinate a, Coordinate b, Coordinate c) {
        double ab = a.distance(b);
        double bc = b.distance(c);
        double ca = c.distance(a);
        if (ab < 1e-10 || bc < 1e-10 || ca < 1e-10) return Double.MAX_VALUE;
        double s = (ab + bc + ca) / 2;
        double area = Math.sqrt(Math.max(0, s * (s - ab) * (s - bc) * (s - ca)));
        if (area < 1e-10) return Double.MAX_VALUE;
        return (ab * bc * ca) / (4 * area);
    }

    private static String edgeKey(Coordinate a, Coordinate b) {
        if (a.compareTo(b) < 0) {
            return a.x + ":" + a.y + ":" + b.x + ":" + b.y;
        }
        return b.x + ":" + b.y + ":" + a.x + ":" + a.y;
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
