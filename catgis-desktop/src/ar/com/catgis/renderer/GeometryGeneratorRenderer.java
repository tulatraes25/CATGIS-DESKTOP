package ar.com.catgis.renderer;

import org.locationtech.jts.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * On-the-fly geometry generation from expressions.
 * Applies geometric transformations to features during rendering
 * without modifying the source data. Similar to QGIS Geometry Generator.
 */
public final class GeometryGeneratorRenderer {

    private GeometryGeneratorRenderer() {}

    private static final GeometryFactory GF = new GeometryFactory();

    // ─── Symbology generators ───────────────────────────────────────

    /**
     * Generate a buffer polygon around each point.
     */
    public static Geometry pointToBuffer(Geometry geom, double distanceMeters) {
        if (geom == null || !(geom instanceof Point)) return geom;
        return geom.buffer(distanceMeters);
    }

    /**
     * Generate a centroid point from each polygon.
     */
    public static Geometry polygonToCentroid(Geometry geom) {
        if (geom == null || geom.isEmpty()) return geom;
        return geom.getCentroid();
    }

    /**
     * Generate the oriented bounding box of a geometry.
     */
    public static Geometry orientedBoundingBox(Geometry geom) {
        if (geom == null || geom.isEmpty()) return null;
        return geom.getEnvelope();
    }

    /**
     * Generate the convex hull of a geometry.
     */
    public static Geometry convexHull(Geometry geom) {
        if (geom == null) return null;
        return geom.convexHull();
    }

    /**
     * Offset a line by a given distance (parallel line).
     */
    public static Geometry offsetLine(Geometry geom, double distance) {
        if (geom == null || !(geom instanceof LineString)) return geom;
        LineString line = (LineString) geom;
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) return geom;

        List<Coordinate> offsetCoords = new ArrayList<>();
        for (int i = 0; i < coords.length; i++) {
            // Compute perpendicular offset for each vertex
            double dx, dy;
            if (i == 0) {
                dx = coords[1].x - coords[0].x;
                dy = coords[1].y - coords[0].y;
            } else if (i == coords.length - 1) {
                dx = coords[i].x - coords[i - 1].x;
                dy = coords[i].y - coords[i - 1].y;
            } else {
                dx = coords[i + 1].x - coords[i - 1].x;
                dy = coords[i + 1].y - coords[i - 1].y;
            }
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 0) {
                double nx = -dy / len;
                double ny = dx / len;
                offsetCoords.add(new Coordinate(
                        coords[i].x + nx * distance,
                        coords[i].y + ny * distance
                ));
            } else {
                offsetCoords.add(coords[i].copy());
            }
        }
        return GF.createLineString(offsetCoords.toArray(new Coordinate[0]));
    }

    /**
     * Generate a regular grid of points within a polygon.
     */
    public static Geometry polygonToGrid(Geometry geom, double cellSize) {
        if (geom == null || geom.isEmpty()) return geom;
        Envelope env = geom.getEnvelopeInternal();
        List<Coordinate> points = new ArrayList<>();
        for (double x = env.getMinX() + cellSize / 2; x <= env.getMaxX(); x += cellSize) {
            for (double y = env.getMinY() + cellSize / 2; y <= env.getMaxY(); y += cellSize) {
                Point p = GF.createPoint(new Coordinate(x, y));
                if (geom.contains(p)) {
                    points.add(p.getCoordinate());
                }
            }
        }
        return GF.createMultiPointFromCoords(points.toArray(new Coordinate[0]));
    }

    /**
     * Generate a simplified version of a line (Douglas-Peucker).
     */
    public static Geometry simplifyLine(Geometry geom, double tolerance) {
        if (geom == null) return null;
        org.locationtech.jts.simplify.DouglasPeuckerSimplifier simplifier =
                new org.locationtech.jts.simplify.DouglasPeuckerSimplifier(geom);
        simplifier.setDistanceTolerance(tolerance);
        return simplifier.getResultGeometry();
    }

    /**
     * Generate a circle polygon from a point and radius.
     */
    public static Geometry pointToCircle(Geometry geom, double radius, int segments) {
        if (geom == null || !(geom instanceof Point)) return geom;
        Point center = (Point) geom;
        Coordinate[] coords = new Coordinate[segments + 1];
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            coords[i] = new Coordinate(
                    center.getX() + radius * Math.cos(angle),
                    center.getY() + radius * Math.sin(angle)
            );
        }
        coords[segments] = coords[0].copy();
        return GF.createPolygon(coords);
    }
}
