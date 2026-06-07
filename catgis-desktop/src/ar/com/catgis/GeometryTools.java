package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometry extraction and conversion tools for CATGIS.
 * Based on Kosmo's geometry plugin functionality.
 */
public final class GeometryTools {

    private GeometryTools() {}

    /**
     * Extract all points from line features.
     */
    public static List<SimpleFeature> extractPointsFromLines(List<SimpleFeature> lineFeatures, SimpleFeatureType pointType) {
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(pointType);
        for (SimpleFeature feature : lineFeatures) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            extractPointsFromGeometry(geom, builder, result);
        }
        return result;
    }

    private static void extractPointsFromGeometry(Geometry geom, SimpleFeatureBuilder builder, List<SimpleFeature> result) {
        if (geom instanceof Point p) {
            builder.add(p);
            result.add(builder.buildFeature(null));
        } else if (geom instanceof GeometryCollection gc) {
            for (int i = 0; i < gc.getNumGeometries(); i++) {
                extractPointsFromGeometry(gc.getGeometryN(i), builder, result);
            }
        }
    }

    /**
     * Extract boundary lines from polygon features.
     */
    public static List<SimpleFeature> extractLinesFromPolygons(List<SimpleFeature> polygonFeatures, SimpleFeatureType lineType) {
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(lineType);
        for (SimpleFeature feature : polygonFeatures) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            if (geom instanceof Polygon poly) {
                builder.add(poly.getExteriorRing());
                result.add(builder.buildFeature(null));
                for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                    builder.add(poly.getInteriorRingN(i));
                    result.add(builder.buildFeature(null));
                }
            } else if (geom instanceof MultiPolygon mp) {
                for (int i = 0; i < mp.getNumGeometries(); i++) {
                    if (mp.getGeometryN(i) instanceof Polygon poly) {
                        builder.add(poly.getExteriorRing());
                        result.add(builder.buildFeature(null));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Extract centroids from polygon features.
     */
    public static List<SimpleFeature> extractCentroids(List<SimpleFeature> polygonFeatures, SimpleFeatureType pointType) {
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(pointType);
        for (SimpleFeature feature : polygonFeatures) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            Point centroid = geom.getCentroid();
            if (centroid != null && !centroid.isEmpty()) {
                builder.add(centroid);
                result.add(builder.buildFeature(null));
            }
        }
        return result;
    }

    /**
     * Extract all vertices as points.
     */
    public static List<SimpleFeature> extractVertices(List<SimpleFeature> features, SimpleFeatureType pointType) {
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(pointType);
        for (SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            for (Coordinate c : geom.getCoordinates()) {
                GeometryFactory gf = new GeometryFactory();
                builder.add(gf.createPoint(c));
                result.add(builder.buildFeature(null));
            }
        }
        return result;
    }

    /**
     * Compute convex hull of a layer.
     */
    public static Geometry computeConvexHull(List<SimpleFeature> features) {
        List<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom != null && !geom.isEmpty()) {
                geometries.add(geom);
            }
        }
        if (geometries.isEmpty()) return null;
        GeometryFactory gf = new GeometryFactory();
        GeometryCollection gc = gf.createGeometryCollection(geometries.toArray(new Geometry[0]));
        return gc.convexHull();
    }

    /**
     * Compute buffer for all features in a layer.
     */
    public static Geometry computeBuffer(List<SimpleFeature> features, double distance) {
        List<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom != null && !geom.isEmpty()) {
                geometries.add(geom.buffer(distance));
            }
        }
        if (geometries.isEmpty()) return null;
        GeometryFactory gf = new GeometryFactory();
        return gf.createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    /**
     * Compute intersection of two layers.
     */
    public static List<SimpleFeature> computeIntersection(
            List<SimpleFeature> layerA, List<SimpleFeature> layerB,
            SimpleFeatureType resultType) {
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(resultType);
        for (SimpleFeature fa : layerA) {
            Geometry geomA = (Geometry) fa.getDefaultGeometry();
            if (geomA == null) continue;
            for (SimpleFeature fb : layerB) {
                Geometry geomB = (Geometry) fb.getDefaultGeometry();
                if (geomB == null) continue;
                try {
                    Geometry intersection = geomA.intersection(geomB);
                    if (intersection != null && !intersection.isEmpty() && intersection.getArea() > 0) {
                        builder.add(intersection);
                        result.add(builder.buildFeature(null));
                    }
                } catch (Exception ignored) {}
            }
        }
        return result;
    }

    /**
     * Simplify geometry using Douglas-Peucker algorithm.
     */
    public static Geometry simplify(Geometry geometry, double tolerance) {
        if (geometry == null) return null;
        return org.locationtech.jts.simplify.DouglasPeuckerSimplifier.simplify(geometry, tolerance);
    }

    public static Geometry smooth(Geometry geometry, double tolerance) {
        if (geometry == null) return null;
        return org.locationtech.jts.simplify.TopologyPreservingSimplifier.simplify(geometry, tolerance);
    }

    public static List<SimpleFeature> computeVoronoi(List<SimpleFeature> pointFeatures, Geometry envelope, SimpleFeatureType polygonType) {
        List<SimpleFeature> result = new ArrayList<>();
        if (pointFeatures == null || pointFeatures.isEmpty()) return result;
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> coords = new ArrayList<>();
        for (SimpleFeature f : pointFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g instanceof Point) coords.add(g.getCoordinate());
            else coords.add(g.getCentroid().getCoordinate());
        }
        if (coords.size() < 2) return result;
        try {
            org.locationtech.jts.triangulate.VoronoiDiagramBuilder voronoi = new org.locationtech.jts.triangulate.VoronoiDiagramBuilder();
            voronoi.setSites(coords);
            voronoi.setClipEnvelope(envelope != null ? envelope.getEnvelopeInternal() : null);
            Geometry diagram = voronoi.getDiagram(gf);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(polygonType);
            for (int i = 0; i < diagram.getNumGeometries(); i++) {
                Geometry cell = diagram.getGeometryN(i);
                if (cell instanceof Polygon && !cell.isEmpty()) {
                    builder.add(cell);
                    result.add(builder.buildFeature(null));
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static List<SimpleFeature> computeDelaunay(List<SimpleFeature> pointFeatures, SimpleFeatureType lineType) {
        List<SimpleFeature> result = new ArrayList<>();
        if (pointFeatures == null || pointFeatures.isEmpty()) return result;
        List<Coordinate> coords = new ArrayList<>();
        for (SimpleFeature f : pointFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g instanceof Point) coords.add(g.getCoordinate());
            else coords.add(g.getCentroid().getCoordinate());
        }
        if (coords.size() < 3) return result;
        try {
            org.locationtech.jts.triangulate.DelaunayTriangulationBuilder delaunay = new org.locationtech.jts.triangulate.DelaunayTriangulationBuilder();
            delaunay.setSites(coords);
            Geometry tri = delaunay.getTriangles(new GeometryFactory());
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(lineType);
            for (int i = 0; i < tri.getNumGeometries(); i++) {
                Geometry triangle = tri.getGeometryN(i);
                if (triangle instanceof Polygon) {
                    builder.add(triangle.getBoundary());
                    result.add(builder.buildFeature(null));
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static List<SimpleFeature> polygonsToLines(List<SimpleFeature> polygonFeatures, SimpleFeatureType lineType) {
        return extractLinesFromPolygons(polygonFeatures, lineType);
    }

    public static List<SimpleFeature> linesToPolygons(List<SimpleFeature> lineFeatures, SimpleFeatureType polygonType) {
        List<SimpleFeature> result = new ArrayList<>();
        org.locationtech.jts.operation.polygonize.Polygonizer polygonizer = new org.locationtech.jts.operation.polygonize.Polygonizer();
        for (SimpleFeature f : lineFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) polygonizer.add(g);
        }
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(polygonType);
        for (Object obj : polygonizer.getPolygons()) {
            if (obj instanceof Polygon poly && !poly.isEmpty()) {
                builder.add(poly);
                result.add(builder.buildFeature(null));
            }
        }
        return result;
    }

    public static Geometry computeMinimumBoundingGeometry(List<SimpleFeature> features, String type) {
        List<Geometry> geoms = new ArrayList<>();
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        if (geoms.isEmpty()) return null;
        try {
            Geometry union = new GeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0]));
            return switch (type) {
                case "circle" -> new org.locationtech.jts.algorithm.MinimumBoundingCircle(union).getCircle();
                case "diameter" -> new org.locationtech.jts.algorithm.MinimumDiameter(union).getDiameter();
                case "rectangle" -> org.locationtech.jts.algorithm.MinimumAreaRectangle.getMinimumRectangle(union);
                default -> union.getEnvelope();
            };
        } catch (Exception e) {
            return new GeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0])).getEnvelope();
        }
    }

    public static double computeNearestDistance(List<SimpleFeature> features) {
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < features.size(); i++) {
            Geometry gi = (Geometry) features.get(i).getDefaultGeometry();
            if (gi == null) continue;
            for (int j = i + 1; j < features.size(); j++) {
                Geometry gj = (Geometry) features.get(j).getDefaultGeometry();
                if (gj == null) continue;
                double dist = gi.distance(gj);
                if (dist < minDist) minDist = dist;
            }
        }
        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }
}
