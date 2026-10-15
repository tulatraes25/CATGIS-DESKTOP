package ar.com.catgis;

import ar.com.catgis.core.geometry.PolygonCoverageAudit;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic topology validation rules for CATGIS.
 * Based on Kosmo's topology validation system.
 */
public final class TopologyValidator {

    private TopologyValidator() {}

    public enum EvaluationStatus { VALID, INVALID, NOT_EVALUATED }

    /** Result of a rule evaluation, not certification of every topology rule. */
    public record TopologyResult(boolean valid, List<TopologyIssue> issues) {
        public EvaluationStatus status() {
            if (issues.stream().anyMatch(issue -> "NOT_EVALUATED".equals(issue.rule()))) {
                return EvaluationStatus.NOT_EVALUATED;
            }
            return valid ? EvaluationStatus.VALID : EvaluationStatus.INVALID;
        }
    }

    /** A coverage gap has featureIndex=-1: it is not owned by one source feature. */
    public record TopologyIssue(
            String rule,
            String message,
            int featureIndex,
            org.locationtech.jts.geom.Geometry geometry
    ) {}

    /**
     * Enclosed uncovered regions only. The tolerance is an AREA in squared
     * input-coordinate units, not a distance or an implicit square-metre value.
     * Exterior space requires an explicit study area via validateCoverageWithin.
     */
    public static TopologyResult validateNoGaps(List<SimpleFeature> features, double tolerance) {
        try {
            validateAreaTolerance(tolerance);
            Geometry gaps = PolygonCoverageAudit.enclosedGaps(polygonInputs(features));
            return gapResult(gaps, tolerance, "NO_GAPS");
        } catch (RuntimeException failure) {
            return notEvaluated("NO_GAPS", failure);
        }
    }

    /** Backend contract for completeness inside an explicit study-area feature. */
    public static TopologyResult validateCoverageWithin(List<SimpleFeature> features,
                                                        SimpleFeature studyArea, double tolerance) {
        try {
            validateAreaTolerance(tolerance);
            List<SimpleFeature> combined = new ArrayList<>(features);
            combined.add(studyArea);
            // Include the boundary in schema CRS checking; never invent or reproject it.
            List<Geometry> all = polygonInputs(combined);
            Geometry gaps = PolygonCoverageAudit.uncoveredWithin(
                    all.subList(0, all.size() - 1), all.get(all.size() - 1));
            return gapResult(gaps, tolerance, "COVERAGE_WITHIN_STUDY_AREA");
        } catch (RuntimeException failure) {
            return notEvaluated("COVERAGE_WITHIN_STUDY_AREA", failure);
        }
    }

    /** Validate area overlaps; empty/invalid inputs cannot produce a clean report. */
    public static TopologyResult validateNoOverlaps(List<SimpleFeature> features) {
        try {
            List<SimpleFeature> source = List.copyOf(features);
            List<PolygonCoverageAudit.Overlap> overlaps = PolygonCoverageAudit.overlaps(polygonInputs(source));
            List<TopologyIssue> issues = new ArrayList<>();
            for (PolygonCoverageAudit.Overlap overlap : overlaps) {
                issues.add(new TopologyIssue("NO_OVERLAPS",
                        "Solapamiento entre " + source.get(overlap.firstIndex()).getID()
                                + " y " + source.get(overlap.secondIndex()).getID(),
                        overlap.firstIndex(), overlap.geometry()));
            }
            return new TopologyResult(issues.isEmpty(), List.copyOf(issues));
        } catch (RuntimeException failure) {
            return notEvaluated("NO_OVERLAPS", failure);
        }
    }

    private static List<Geometry> polygonInputs(List<SimpleFeature> features) {
        List<SimpleFeature> source = List.copyOf(features);
        List<Geometry> geometries = new ArrayList<>(source.size());
        CoordinateReferenceSystem expected = null;
        boolean first = true;
        for (SimpleFeature feature : source) {
            if (Thread.currentThread().isInterrupted()) {
                throw new java.util.concurrent.CancellationException("Auditoria cancelada.");
            }
            CoordinateReferenceSystem current = feature.getFeatureType().getCoordinateReferenceSystem();
            if (!first && ((expected == null) != (current == null)
                    || (expected != null && !CRS.equalsIgnoreMetadata(expected, current)))) {
                throw new IllegalArgumentException("CRS de capa incompatibles; revise " + feature.getID());
            }
            Object value = feature.getDefaultGeometry();
            if (!(value instanceof Geometry geometry)) {
                throw new IllegalArgumentException("Sin geometria evaluable: " + feature.getID());
            }
            geometries.add(geometry);
            expected = first ? current : expected;
            first = false;
        }
        return geometries;
    }

    private static void validateAreaTolerance(double tolerance) {
        if (!Double.isFinite(tolerance) || tolerance < 0) {
            throw new IllegalArgumentException("La tolerancia de area debe ser finita y no negativa.");
        }
    }

    private static TopologyResult gapResult(Geometry missing, double tolerance, String rule) {
        List<TopologyIssue> issues = new ArrayList<>();
        for (int i = 0; i < missing.getNumGeometries(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new java.util.concurrent.CancellationException("Auditoria cancelada.");
            }
            Geometry gap = missing.getGeometryN(i);
            if (!gap.isEmpty() && gap.getArea() > tolerance) {
                issues.add(new TopologyIssue(rule,
                        "Area sin cobertura: " + gap.getArea() + " unidades de coordenadas al cuadrado.",
                        -1, gap.copy()));
            }
        }
        return new TopologyResult(issues.isEmpty(), List.copyOf(issues));
    }

    private static TopologyResult notEvaluated(String rule, RuntimeException failure) {
        String detail = failure.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = failure.getClass().getSimpleName();
        }
        if (detail.length() > 512) {
            detail = detail.substring(0, 512);
        }
        return new TopologyResult(false, List.of(new TopologyIssue(
                "NOT_EVALUATED", rule + " no evaluado: " + detail, -1, null)));
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
