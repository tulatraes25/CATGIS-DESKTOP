package ar.com.catgis;

import ar.com.catgis.core.geometry.SpatialIndex;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        List<Integer> featureIndices = new ArrayList<>();

        for (int i = 0; i < features.size(); i++) {
            Geometry geom = (Geometry) features.get(i).getDefaultGeometry();
            if (geom instanceof Polygon) { polygons.add(geom); featureIndices.add(i); }
            else if (geom instanceof MultiPolygon mp) {
                for (int j = 0; j < mp.getNumGeometries(); j++) {
                    if (mp.getGeometryN(j) instanceof Polygon) { polygons.add(mp.getGeometryN(j)); featureIndices.add(i); }
                }
            }
        }
        if (polygons.size() < 2) return new TopologyResult(true, issues);

        STRtree tree = new STRtree(10);
        for (int i = 0; i < polygons.size(); i++) tree.insert(polygons.get(i).getEnvelopeInternal(), i);
        try { tree.build(); } catch (Exception e) { CatgisLogger.warn("STRtree build failed in validateNoGaps", e); }

        Set<String> checked = new LinkedHashSet<>();
        for (int i = 0; i < polygons.size(); i++) {
            Geometry p1 = polygons.get(i);
            for (Object obj : tree.query(p1.getEnvelopeInternal())) {
                int j = (Integer) obj;
                if (i >= j) continue;
                String key = i < j ? i + "_" + j : j + "_" + i;
                if (checked.contains(key)) continue;
                checked.add(key);
                try {
                    Geometry p2 = polygons.get(j);
                    if (!p1.getEnvelopeInternal().intersects(p2.getEnvelopeInternal())) continue;
                    Geometry union = p1.union(p2);
                    Geometry gap = p1.symDifference(p2).difference(union);
                    if (gap != null && !gap.isEmpty() && gap.getArea() > tolerance) {
                        issues.add(new TopologyIssue("NO_GAPS", "Gap found between polygons " + featureIndices.get(i) + " and " + featureIndices.get(j), featureIndices.get(i), gap));
                    }
                } catch (Exception e) { CatgisLogger.warn("Topology check failed for pair " + i + "," + j, e); }
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
        List<Integer> featureIndices = new ArrayList<>();

        for (int i = 0; i < features.size(); i++) {
            Geometry geom = (Geometry) features.get(i).getDefaultGeometry();
            if (geom instanceof Polygon) { polygons.add(geom); featureIndices.add(i); }
            else if (geom instanceof MultiPolygon mp) {
                for (int j = 0; j < mp.getNumGeometries(); j++) {
                    if (mp.getGeometryN(j) instanceof Polygon) { polygons.add(mp.getGeometryN(j)); featureIndices.add(i); }
                }
            }
        }
        if (polygons.size() < 2) return new TopologyResult(true, issues);

        STRtree tree = new STRtree(10);
        for (int i = 0; i < polygons.size(); i++) tree.insert(polygons.get(i).getEnvelopeInternal(), i);
        try { tree.build(); } catch (Exception e) { CatgisLogger.warn("STRtree build failed in validateNoGaps", e); }

        Set<String> checked = new LinkedHashSet<>();
        for (int i = 0; i < polygons.size(); i++) {
            Geometry p1 = polygons.get(i);
            for (Object obj : tree.query(p1.getEnvelopeInternal())) {
                int j = (Integer) obj;
                if (i >= j) continue;
                String key = i < j ? i + "_" + j : j + "_" + i;
                if (checked.contains(key)) continue;
                checked.add(key);
                try {
                    Geometry p2 = polygons.get(j);
                    if (!p1.getEnvelopeInternal().intersects(p2.getEnvelopeInternal())) continue;
                    Geometry intersection = p1.intersection(p2);
                    if (intersection != null && !intersection.isEmpty() && intersection.getArea() > 0) {
                        issues.add(new TopologyIssue("NO_OVERLAPS", "Overlap between polygons " + featureIndices.get(i) + " and " + featureIndices.get(j), featureIndices.get(i), intersection));
                    }
                } catch (Exception e) { CatgisLogger.warn("Topology check failed for pair " + i + "," + j, e); }
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

    public static TopologyResult validateNoOverlapsWith(List<org.geotools.api.feature.simple.SimpleFeature> featuresA, List<org.geotools.api.feature.simple.SimpleFeature> featuresB) {
        List<TopologyIssue> issues = new ArrayList<>();
        for (int i = 0; i < featuresA.size(); i++) {
            Geometry ga = (Geometry) featuresA.get(i).getDefaultGeometry();
            if (ga == null) continue;
            for (int j = 0; j < featuresB.size(); j++) {
                Geometry gb = (Geometry) featuresB.get(j).getDefaultGeometry();
                if (gb == null) continue;
                try {
                    Geometry inter = ga.intersection(gb);
                    if (inter != null && !inter.isEmpty() && inter.getDimension() >= 1) {
                        issues.add(new TopologyIssue("MUST_NOT_OVERLAP_WITH", "Overlap between feature " + i + " and " + j, i, inter));
                    }
                } catch (Exception e) { CatgisLogger.warn("Topology check failed for pair " + i + "," + j, e); }
            }
        }
        return new TopologyResult(issues.isEmpty(), issues);
    }

    public static TopologyResult validateCoveredBy(List<org.geotools.api.feature.simple.SimpleFeature> featuresA, List<org.geotools.api.feature.simple.SimpleFeature> featuresB) {
        List<TopologyIssue> issues = new ArrayList<>();
        for (int i = 0; i < featuresA.size(); i++) {
            Geometry ga = (Geometry) featuresA.get(i).getDefaultGeometry();
            if (ga == null) continue;
            boolean covered = false;
            for (int j = 0; j < featuresB.size(); j++) {
                Geometry gb = (Geometry) featuresB.get(j).getDefaultGeometry();
                if (gb != null && gb.covers(ga)) { covered = true; break; }
            }
            if (!covered) issues.add(new TopologyIssue("MUST_BE_COVERED_BY", "Feature " + i + " not covered by layer B", i, ga));
        }
        return new TopologyResult(issues.isEmpty(), issues);
    }

    public static TopologyResult validateWithinDistance(List<org.geotools.api.feature.simple.SimpleFeature> features, double maxDistance) {
        List<TopologyIssue> issues = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            Geometry ga = (Geometry) features.get(i).getDefaultGeometry();
            if (ga == null) continue;
            for (int j = i + 1; j < features.size(); j++) {
                Geometry gb = (Geometry) features.get(j).getDefaultGeometry();
                if (gb == null) continue;
                if (ga.distance(gb) > maxDistance) {
                    issues.add(new TopologyIssue("MUST_BE_WITHIN_DISTANCE", "Features " + i + " and " + j + " exceed max distance " + maxDistance, i, ga));
                }
            }
        }
        return new TopologyResult(issues.isEmpty(), issues);
    }

    public static TopologyResult validateNotTouch(List<org.geotools.api.feature.simple.SimpleFeature> features, double tolerance) {
        List<TopologyIssue> issues = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            Geometry ga = (Geometry) features.get(i).getDefaultGeometry();
            if (ga == null) continue;
            for (int j = i + 1; j < features.size(); j++) {
                Geometry gb = (Geometry) features.get(j).getDefaultGeometry();
                if (gb == null) continue;
                if (ga.distance(gb) < tolerance) {
                    issues.add(new TopologyIssue("MUST_NOT_TOUCH", "Features " + i + " and " + j + " are too close (<" + tolerance + ")", i, ga));
                }
            }
        }
        return new TopologyResult(issues.isEmpty(), issues);
    }
}
