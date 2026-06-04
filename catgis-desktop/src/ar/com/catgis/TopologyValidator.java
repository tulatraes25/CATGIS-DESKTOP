package ar.com.catgis;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic topology validation rules for CATGIS.
 * Based on Kosmo's topology validation system.
 */
public final class TopologyValidator {

    private TopologyValidator() {}

    /**
     * Result of a topology validation.
     */
    public record TopologyResult(
            boolean valid,
            List<TopologyIssue> issues
    ) {}

    /**
     * A topology issue found during validation.
     */
    public record TopologyIssue(
            String rule,
            String message,
            int featureIndex,
            org.locationtech.jts.geom.Geometry geometry
    ) {}

    /**
     * Validate that polygons have no gaps between them.
     */
    public static TopologyResult validateNoGaps(List<org.geotools.api.feature.simple.SimpleFeature> features, double tolerance) {
        List<TopologyIssue> issues = new ArrayList<>();
        List<Geometry> polygons = new ArrayList<>();

        for (org.geotools.api.feature.simple.SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom instanceof Polygon) {
                polygons.add(geom);
            } else if (geom instanceof MultiPolygon mp) {
                for (int i = 0; i < mp.getNumGeometries(); i++) {
                    if (mp.getGeometryN(i) instanceof Polygon) {
                        polygons.add(mp.getGeometryN(i));
                    }
                }
            }
        }

        // Check each polygon for gaps by buffering and comparing
        for (int i = 0; i < polygons.size(); i++) {
            Geometry poly1 = polygons.get(i);
            for (int j = i + 1; j < polygons.size(); j++) {
                Geometry poly2 = polygons.get(j);
                try {
                    Geometry union = poly1.union(poly2);
                    Geometry gap = poly1.symDifference(poly2).difference(union);
                    if (gap != null && !gap.isEmpty() && gap.getArea() > tolerance) {
                        issues.add(new TopologyIssue(
                                "NO_GAPS",
                                "Gap found between polygons " + i + " and " + j,
                                i, gap));
                    }
                } catch (Exception ignored) {}
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }

    /**
     * Validate that polygons have no overlaps.
     */
    public static TopologyResult validateNoOverlaps(List<org.geotools.api.feature.simple.SimpleFeature> features) {
        List<TopologyIssue> issues = new ArrayList<>();
        List<Geometry> polygons = new ArrayList<>();

        for (org.geotools.api.feature.simple.SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom instanceof Polygon) {
                polygons.add(geom);
            } else if (geom instanceof MultiPolygon mp) {
                for (int i = 0; i < mp.getNumGeometries(); i++) {
                    if (mp.getGeometryN(i) instanceof Polygon) {
                        polygons.add(mp.getGeometryN(i));
                    }
                }
            }
        }

        for (int i = 0; i < polygons.size(); i++) {
            for (int j = i + 1; j < polygons.size(); j++) {
                try {
                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));
                    if (intersection != null && !intersection.isEmpty() && intersection.getArea() > 0) {
                        issues.add(new TopologyIssue(
                                "NO_OVERLAPS",
                                "Overlap between polygons " + i + " and " + j,
                                i, intersection));
                    }
                } catch (Exception ignored) {}
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }

    /**
     * Validate that polygons have no self-intersections.
     */
    public static TopologyResult validateNoSelfIntersections(List<org.geotools.api.feature.simple.SimpleFeature> features) {
        List<TopologyIssue> issues = new ArrayList<>();

        for (int i = 0; i < features.size(); i++) {
            Geometry geom = (Geometry) features.get(i).getDefaultGeometry();
            if (geom == null) continue;

            if (!geom.isValid()) {
                issues.add(new TopologyIssue(
                        "NO_SELF_INTERSECTION",
                        "Invalid geometry at feature " + i,
                        i, geom));
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }

    /**
     * Validate that lines are connected (no dangles).
     */
    public static TopologyResult validateLineConnectivity(List<org.geotools.api.feature.simple.SimpleFeature> features, double tolerance) {
        List<TopologyIssue> issues = new ArrayList<>();
        List<Coordinate> endpoints = new ArrayList<>();

        for (org.geotools.api.feature.simple.SimpleFeature feature : features) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom instanceof LineString ls) {
                Coordinate[] coords = ls.getCoordinates();
                if (coords.length > 0) {
                    endpoints.add(coords[0]);
                    endpoints.add(coords[coords.length - 1]);
                }
            }
        }

        // Check for isolated endpoints
        for (int i = 0; i < endpoints.size(); i++) {
            boolean connected = false;
            for (int j = 0; j < endpoints.size(); j++) {
                if (i == j) continue;
                if (endpoints.get(i).distance(endpoints.get(j)) < tolerance) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                issues.add(new TopologyIssue(
                        "NO_DANGLES",
                        "Dangle found at endpoint " + i,
                        i / 2, new GeometryFactory().createPoint(endpoints.get(i))));
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }

    /**
     * Validate that points are within polygons.
     */
    public static TopologyResult validatePointsInPolygons(
            List<org.geotools.api.feature.simple.SimpleFeature> pointFeatures,
            List<org.geotools.api.feature.simple.SimpleFeature> polygonFeatures) {

        List<TopologyIssue> issues = new ArrayList<>();

        for (int i = 0; i < pointFeatures.size(); i++) {
            Geometry pointGeom = (Geometry) pointFeatures.get(i).getDefaultGeometry();
            if (!(pointGeom instanceof Point)) continue;

            boolean insideAny = false;
            for (org.geotools.api.feature.simple.SimpleFeature polyFeature : polygonFeatures) {
                Geometry polyGeom = (Geometry) polyFeature.getDefaultGeometry();
                if (polyGeom != null && polyGeom.contains(pointGeom)) {
                    insideAny = true;
                    break;
                }
            }

            if (!insideAny) {
                issues.add(new TopologyIssue(
                        "POINTS_IN_POLYGONS",
                        "Point " + i + " is not inside any polygon",
                        i, pointGeom));
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }

    /**
     * Validate that features have correct geometry types.
     */
    public static TopologyResult validateGeometryTypes(
            List<org.geotools.api.feature.simple.SimpleFeature> features,
            Class<?> expectedType) {

        List<TopologyIssue> issues = new ArrayList<>();

        for (int i = 0; i < features.size(); i++) {
            Geometry geom = (Geometry) features.get(i).getDefaultGeometry();
            if (geom != null && !expectedType.isInstance(geom)) {
                issues.add(new TopologyIssue(
                        "VALIDATE_GEOMETRY_TYPE",
                        "Feature " + i + " has wrong geometry type: " + geom.getGeometryType(),
                        i, geom));
            }
        }

        return new TopologyResult(issues.isEmpty(), issues);
    }
}
