package ar.com.catgis;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;

/**
 * Approximate geometry checks that are more robust than exact JTS predicates
 * for real-world data with tiny numerical errors. Uses area tolerance instead
 * of exact topology for polygon operations.
 * Adapted from KOSMO ApproxJTSCheckers.
 */
public final class ApproxGeometryChecks {

    public static final double AREA_TOLERANCE = 1e-7;

    private ApproxGeometryChecks() {}

    public static boolean contains(Geometry a, Geometry b) {
        if (a == null || b == null) return false;
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return safeDifferenceArea(b, a) < AREA_TOLERANCE;
        }
        return a.contains(b);
    }

    public static boolean crosses(Geometry a, Geometry b) {
        if (a == null || b == null) return false;
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return safeIntersectionArea(b, a) > AREA_TOLERANCE;
        }
        return a.crosses(b);
    }

    public static boolean notOverlapOfAreas(Geometry a, Geometry b) {
        if (a == null || b == null) return true;
        try {
            return safeIntersectionArea(a, b) < AREA_TOLERANCE;
        } catch (TopologyException e) {
            return !a.overlaps(b);
        }
    }

    public static boolean within(Geometry a, Geometry b) {
        if (a == null || b == null) return false;
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return safeDifferenceArea(a, b) < AREA_TOLERANCE;
        }
        return a.within(b);
    }

    public static boolean coversArea(Geometry a, Geometry b) {
        if (a == null || b == null) return false;
        if (a.getDimension() < 2 || b.getDimension() < 2) return a.covers(b);
        return safeDifferenceArea(b, a) < AREA_TOLERANCE;
    }

    private static double safeIntersectionArea(Geometry a, Geometry b) {
        try {
            if (!a.getEnvelopeInternal().intersects(b.getEnvelopeInternal())) return 0;
            Geometry intersection = a.intersection(b);
            return intersection != null ? intersection.getArea() : 0;
        } catch (TopologyException e) {
            return 0;
        }
    }

    private static double safeDifferenceArea(Geometry a, Geometry b) {
        try {
            Geometry difference = a.difference(b);
            return difference != null ? difference.getArea() : a.getArea();
        } catch (TopologyException e) {
            return a.getArea();
        }
    }
}
