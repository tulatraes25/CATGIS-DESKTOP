package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapGeometryUtils {

    public static Coordinate[] collapseDuplicateLineCoordinates(Coordinate[] coords) {
        if (coords == null || coords.length == 0) {
            return null;
        }

        List<Coordinate> normalized = new ArrayList<>();
        for (Coordinate coord : coords) {
            if (coord == null) {
                continue;
            }
            if (normalized.isEmpty() || normalized.get(normalized.size() - 1).distance(coord) > 0.0000001) {
                normalized.add(new Coordinate(coord));
            }
        }

        if (normalized.size() < 2) {
            return null;
        }
        return normalized.toArray(new Coordinate[0]);
    }

    public static Coordinate[] normalizeRingCoordinates(Coordinate[] shell) {
        if (shell == null || shell.length < 4) {
            return null;
        }

        List<Coordinate> visible = new ArrayList<>();
        for (int i = 0; i < shell.length - 1; i++) {
            Coordinate coordinate = shell[i];
            if (coordinate == null) {
                continue;
            }
            if (visible.isEmpty() || visible.get(visible.size() - 1).distance(coordinate) > 0.0000001) {
                visible.add(new Coordinate(coordinate));
            }
        }

        if (visible.size() > 1 && visible.get(0).distance(visible.get(visible.size() - 1)) <= 0.0000001) {
            visible.remove(visible.size() - 1);
        }

        if (visible.size() < 3) {
            return null;
        }

        Coordinate[] normalized = new Coordinate[visible.size() + 1];
        for (int i = 0; i < visible.size(); i++) {
            normalized[i] = new Coordinate(visible.get(i));
        }
        normalized[normalized.length - 1] = new Coordinate(visible.get(0));
        return normalized;
    }

    public static Coordinate[] copyCoordinates(Coordinate[] coords) {
        Coordinate[] copies = new Coordinate[coords.length];
        for (int i = 0; i < coords.length; i++) {
            copies[i] = coords[i] != null ? new Coordinate(coords[i]) : null;
        }
        return copies;
    }

    public static Coordinate[] insertCoordinate(Coordinate[] coords, int insertIndex, Coordinate coordinate) {
        Coordinate[] out = new Coordinate[coords.length + 1];
        for (int i = 0, j = 0; i < out.length; i++) {
            if (i == insertIndex) {
                out[i] = new Coordinate(coordinate);
            } else {
                out[i] = new Coordinate(coords[j++]);
            }
        }
        return out;
    }

    public static Coordinate[] removeCoordinate(Coordinate[] coords, int removeIndex) {
        Coordinate[] out = new Coordinate[coords.length - 1];
        for (int i = 0, j = 0; i < coords.length; i++) {
            if (i == removeIndex) {
                continue;
            }
            out[j++] = new Coordinate(coords[i]);
        }
        return out;
    }

    public static Coordinate[] removeRingCoordinate(Coordinate[] shell, int removeVisibleIndex) {
        Coordinate[] visible = new Coordinate[shell.length - 1];
        for (int i = 0; i < visible.length; i++) {
            visible[i] = new Coordinate(shell[i]);
        }
        Coordinate[] reduced = removeCoordinate(visible, removeVisibleIndex);
        Coordinate[] closed = new Coordinate[reduced.length + 1];
        for (int i = 0; i < reduced.length; i++) {
            closed[i] = new Coordinate(reduced[i]);
        }
        closed[closed.length - 1] = new Coordinate(reduced[0]);
        return closed;
    }

    public static LinearRing[] copyInteriorRings(GeometryFactory factory, Polygon polygon) {
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            holes[i] = factory.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
        }
        return holes;
    }

    public static Polygon buildPolygonFromCoordinates(List<Coordinate> coordinates, GeometryFactory factory) {
        if (coordinates == null || coordinates.size() < 3) {
            return null;
        }
        Coordinate[] shell = new Coordinate[coordinates.size() + 1];
        for (int i = 0; i < coordinates.size(); i++) {
            shell[i] = new Coordinate(coordinates.get(i));
        }
        shell[shell.length - 1] = new Coordinate(coordinates.get(0));
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    public static Geometry normalizePolygonalGeometry(Geometry geometry, GeometryFactory factory) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return geometry;
        }
        List<Polygon> polygons = collectPolygons(geometry);
        if (polygons.isEmpty()) {
            return null;
        }
        return assemblePolygons(polygons, factory);
    }

    public static Geometry assemblePolygons(List<Polygon> polygons, GeometryFactory factory) {
        if (polygons == null || polygons.isEmpty()) {
            return null;
        }
        if (polygons.size() == 1) {
            return polygons.get(0);
        }
        return factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    public static List<Polygon> collectPolygons(Geometry geometry) {
        List<Polygon> polygons = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return polygons;
        }
        if (geometry instanceof Polygon) {
            polygons.add((Polygon) geometry);
            return polygons;
        }
        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                polygons.add((Polygon) multi.getGeometryN(i));
            }
            return polygons;
        }
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            polygons.addAll(collectPolygons(geometry.getGeometryN(i)));
        }
        return polygons;
    }

    public static List<Geometry> collectGeometryParts(Geometry geometry) {
        List<Geometry> parts = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return parts;
        }
        if (geometry instanceof GeometryCollection collection
                && !(geometry instanceof MultiLineString)
                && !(geometry instanceof MultiPolygon)
                && !(geometry instanceof MultiPoint)) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                Geometry part = collection.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.addAll(collectGeometryParts(part));
                }
            }
            return parts;
        }
        if (geometry instanceof MultiPoint multiPoint) {
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                Geometry part = multiPoint.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.add((Geometry) part.copy());
                }
            }
            return parts;
        }
        if (geometry instanceof MultiLineString multiLine) {
            for (int i = 0; i < multiLine.getNumGeometries(); i++) {
                Geometry part = multiLine.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.add((Geometry) part.copy());
                }
            }
            return parts;
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Geometry part = multiPolygon.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.add((Geometry) part.copy());
                }
            }
            return parts;
        }

        parts.add((Geometry) geometry.copy());
        return parts;
    }

    public static void offsetGeometryForPaste(Geometry geometry) {
        if (geometry == null) {
            return;
        }
        Envelope env = geometry.getEnvelopeInternal();
        double dx = Math.max(1.0, Math.max(env.getWidth(), 1.0) * 0.03);
        double dy = Math.max(1.0, Math.max(env.getHeight(), 1.0) * 0.03);
        for (Coordinate coordinate : geometry.getCoordinates()) {
            if (coordinate != null) {
                coordinate.x += dx;
                coordinate.y += dy;
            }
        }
        geometry.geometryChanged();
    }

    public static Geometry translateGeometry(Geometry geometry, double dx, double dy) {
        if (geometry == null) {
            return null;
        }
        Geometry translated = (Geometry) geometry.copy();
        for (Coordinate coordinate : translated.getCoordinates()) {
            if (coordinate != null) {
                coordinate.x += dx;
                coordinate.y += dy;
            }
        }
        translated.geometryChanged();
        return translated;
    }

    public static String buildNextFeatureId(List<SimpleFeature> features) {
        long maxSuffix = 0L;
        if (features != null) {
            for (SimpleFeature feature : features) {
                if (feature == null || feature.getID() == null) {
                    continue;
                }
                String digits = feature.getID().replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    try {
                        maxSuffix = Math.max(maxSuffix, Long.parseLong(digits));
                    } catch (Exception ignored) { CatgisLogger.warn("MapGeometryUtils: operation failed", ignored); }
                }
            }
        }
        return "catgis." + (maxSuffix + 1);
    }

    public static Geometry extractFeatureGeometryCopy(SimpleFeature feature) {
        if (feature == null) {
            return null;
        }
        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return null;
        }
        return ((Geometry) geomObj).copy();
    }

    public static String formatDistance(double meters) {
        if (meters >= 1000.0) {
            return String.format(Locale.US, "%.3f km", meters / 1000.0);
        }
        return String.format(Locale.US, "%.2f m", meters);
    }

    public static String formatArea(double squareMeters) {
        if (squareMeters >= 10000.0) {
            return String.format(Locale.US, "%.3f ha", squareMeters / 10000.0);
        }
        return String.format(Locale.US, "%.2f m\u00B2", squareMeters);
    }

    public static String getBaseName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    public static Coordinate[] reverseCoordinates(Coordinate[] coordinates) {
        if (coordinates == null) return null;
        Coordinate[] reversed = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[coordinates.length - 1 - i];
            reversed[i] = c != null ? new Coordinate(c) : null;
        }
        return reversed;
    }

    public static Coordinate[] cloneCoordinates(Coordinate[] coordinates) {
        if (coordinates == null) return null;
        Coordinate[] clones = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            clones[i] = coordinates[i] != null ? new Coordinate(coordinates[i]) : null;
        }
        return clones;
    }

    public static Coordinate computeCircumcenter(Coordinate a, Coordinate b, Coordinate c) {
        if (a == null || b == null || c == null) return null;
        double d = (2.0 * ((a.x * (b.y - c.y)) + (b.x * (c.y - a.y)) + (c.x * (a.y - b.y))));
        if (Math.abs(d) < 0.0000001) return null;
        double ax2ay2 = (a.x * a.x) + (a.y * a.y);
        double bx2by2 = (b.x * b.x) + (b.y * b.y);
        double cx2cy2 = (c.x * c.x) + (c.y * c.y);
        double ux = ((ax2ay2 * (b.y - c.y)) + (bx2by2 * (c.y - a.y)) + (cx2cy2 * (a.y - b.y))) / d;
        double uy = ((ax2ay2 * (c.x - b.x)) + (bx2by2 * (a.x - c.x)) + (cx2cy2 * (b.x - a.x))) / d;
        return new Coordinate(ux, uy);
    }

    public static List<Coordinate> buildRectangleCoordinates(List<Coordinate> coordinates) {
        List<Coordinate> rectangle = new ArrayList<>();
        if (coordinates == null || coordinates.size() < 2) return rectangle;
        Coordinate first = coordinates.get(0);
        Coordinate opposite = coordinates.get(coordinates.size() - 1);
        if (first == null || opposite == null) return rectangle;
        if (Math.abs(first.x - opposite.x) < 0.0000001 || Math.abs(first.y - opposite.y) < 0.0000001) return rectangle;
        rectangle.add(new Coordinate(first.x, first.y));
        rectangle.add(new Coordinate(opposite.x, first.y));
        rectangle.add(new Coordinate(opposite.x, opposite.y));
        rectangle.add(new Coordinate(first.x, opposite.y));
        rectangle.add(new Coordinate(first.x, first.y));
        return rectangle;
    }

    public static Geometry buildCirclePolygon(Coordinate center, double radius, int segments) {
        if (center == null || !(radius > 0.0)) return null;
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] shell = new Coordinate[segments + 1];
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2.0 * i) / segments;
            shell[i] = new Coordinate(center.x + (Math.cos(angle) * radius), center.y + (Math.sin(angle) * radius));
        }
        shell[segments] = new Coordinate(shell[0]);
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    public static Geometry buildCircleFromTwoPoints(List<Coordinate> coordinates, int segments) {
        if (coordinates == null || coordinates.size() < 2) return null;
        Coordinate center = coordinates.get(0);
        Coordinate radiusPoint = coordinates.get(coordinates.size() - 1);
        if (center == null || radiusPoint == null) return null;
        double radius = center.distance(radiusPoint);
        if (!(radius > 0.0)) return null;
        return buildCirclePolygon(center, radius, segments);
    }

    public static Geometry buildCircleFromThreePoints(List<Coordinate> coordinates, int segments) {
        if (coordinates == null || coordinates.size() < 3) return null;
        Coordinate center = computeCircumcenter(coordinates.get(0), coordinates.get(1), coordinates.get(2));
        if (center == null) return null;
        double radius = center.distance(coordinates.get(0));
        if (!(radius > 0.0)) return null;
        return buildCirclePolygon(center, radius, segments);
    }

    public static Coordinate[] extractContinuableLineCoordinates(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) return null;
        if (geometry instanceof org.locationtech.jts.geom.LineString lineString) return cloneCoordinates(lineString.getCoordinates());
        return null;
    }
}
