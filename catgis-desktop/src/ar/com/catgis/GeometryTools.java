package ar.com.catgis;

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
}
