package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Locale;

final class CoordinateTransformSupport {

    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;
    private static final double WEB_MERCATOR_MAX_LAT = 85.0511287798066;

    private CoordinateTransformSupport() {
    }

    static double[] transformPoint(double x, double y, String sourceCode, String targetCode) {
        String source = CRSDefinitions.normalizeCode(sourceCode);
        String target = CRSDefinitions.normalizeCode(targetCode);
        if (source.isBlank() || target.isBlank()) {
            return null;
        }
        if (source.equalsIgnoreCase(target)) {
            return new double[]{x, y};
        }

        GeographicPoint geographic = toGeographic(x, y, source);
        if (geographic == null) {
            return null;
        }
        return fromGeographic(geographic.lon(), geographic.lat(), target);
    }

    static Envelope reprojectEnvelope(Envelope envelope, String sourceCode, String targetCode) {
        if (envelope == null || envelope.isNull()) {
            return envelope;
        }

        Envelope result = null;
        double centerX = (envelope.getMinX() + envelope.getMaxX()) / 2.0;
        double centerY = (envelope.getMinY() + envelope.getMaxY()) / 2.0;
        double[][] points = new double[][]{
                {envelope.getMinX(), envelope.getMinY()},
                {envelope.getMinX(), envelope.getMaxY()},
                {envelope.getMaxX(), envelope.getMinY()},
                {envelope.getMaxX(), envelope.getMaxY()},
                {centerX, centerY}
        };

        for (double[] point : points) {
            double[] transformed = transformPoint(point[0], point[1], sourceCode, targetCode);
            if (transformed == null || transformed.length < 2
                    || Double.isNaN(transformed[0]) || Double.isNaN(transformed[1])
                    || Double.isInfinite(transformed[0]) || Double.isInfinite(transformed[1])) {
                continue;
            }
            if (result == null) {
                result = new Envelope(transformed[0], transformed[0], transformed[1], transformed[1]);
            } else {
                result.expandToInclude(transformed[0], transformed[1]);
            }
        }
        return result;
    }

    static Geometry reprojectGeometry(Geometry geometry, String sourceCode, String targetCode) {
        if (geometry == null) {
            return null;
        }
        if (geometry.isEmpty()) {
            return geometry.copy();
        }

        GeometryFactory factory = geometry.getFactory() != null ? geometry.getFactory() : new GeometryFactory();
        if (geometry instanceof Point point) {
            Coordinate coordinate = reprojectCoordinate(point.getCoordinate(), sourceCode, targetCode);
            return coordinate != null ? factory.createPoint(coordinate) : null;
        }
        if (geometry instanceof LinearRing ring) {
            Coordinate[] coordinates = reprojectCoordinates(ring.getCoordinates(), sourceCode, targetCode);
            return coordinates != null ? factory.createLinearRing(coordinates) : null;
        }
        if (geometry instanceof LineString line) {
            Coordinate[] coordinates = reprojectCoordinates(line.getCoordinates(), sourceCode, targetCode);
            return coordinates != null ? factory.createLineString(coordinates) : null;
        }
        if (geometry instanceof Polygon polygon) {
            LinearRing shell = (LinearRing) reprojectGeometry(polygon.getExteriorRing(), sourceCode, targetCode);
            if (shell == null) {
                return null;
            }
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holes[i] = (LinearRing) reprojectGeometry(polygon.getInteriorRingN(i), sourceCode, targetCode);
                if (holes[i] == null) {
                    return null;
                }
            }
            return factory.createPolygon(shell, holes);
        }
        if (geometry instanceof MultiPoint multiPoint) {
            Point[] points = new Point[multiPoint.getNumGeometries()];
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                points[i] = (Point) reprojectGeometry(multiPoint.getGeometryN(i), sourceCode, targetCode);
                if (points[i] == null) {
                    return null;
                }
            }
            return factory.createMultiPoint(points);
        }
        if (geometry instanceof MultiLineString multiLine) {
            LineString[] lines = new LineString[multiLine.getNumGeometries()];
            for (int i = 0; i < multiLine.getNumGeometries(); i++) {
                lines[i] = (LineString) reprojectGeometry(multiLine.getGeometryN(i), sourceCode, targetCode);
                if (lines[i] == null) {
                    return null;
                }
            }
            return factory.createMultiLineString(lines);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                polygons[i] = (Polygon) reprojectGeometry(multiPolygon.getGeometryN(i), sourceCode, targetCode);
                if (polygons[i] == null) {
                    return null;
                }
            }
            return factory.createMultiPolygon(polygons);
        }
        if (geometry instanceof GeometryCollection collection) {
            Geometry[] geometries = new Geometry[collection.getNumGeometries()];
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                geometries[i] = reprojectGeometry(collection.getGeometryN(i), sourceCode, targetCode);
                if (geometries[i] == null) {
                    return null;
                }
            }
            return factory.createGeometryCollection(geometries);
        }
        return null;
    }

    private static Coordinate[] reprojectCoordinates(Coordinate[] source, String sourceCode, String targetCode) {
        if (source == null) {
            return null;
        }
        Coordinate[] transformed = new Coordinate[source.length];
        for (int i = 0; i < source.length; i++) {
            transformed[i] = reprojectCoordinate(source[i], sourceCode, targetCode);
            if (transformed[i] == null) {
                return null;
            }
        }
        return transformed;
    }

    private static Coordinate reprojectCoordinate(Coordinate coordinate, String sourceCode, String targetCode) {
        if (coordinate == null) {
            return null;
        }
        double[] transformed = transformPoint(coordinate.x, coordinate.y, sourceCode, targetCode);
        if (transformed == null || transformed.length < 2) {
            return null;
        }
        Coordinate result = new Coordinate(transformed[0], transformed[1]);
        if (!Double.isNaN(coordinate.getZ())) {
            result.setZ(coordinate.getZ());
        }
        return result;
    }

    private static GeographicPoint toGeographic(double x, double y, String sourceCode) {
        String normalized = CRSDefinitions.normalizeCode(sourceCode);
        if (isGeographic(normalized)) {
            return new GeographicPoint(x, y);
        }
        if ("EPSG:3857".equalsIgnoreCase(normalized)) {
            return webMercatorToGeographic(x, y);
        }
        if ("EPSG:4087".equalsIgnoreCase(normalized)) {
            return equidistantCylindricalToGeographic(x, y, Ellipsoid.WGS84);
        }

        ProjectedDefinition definition = resolveProjectedDefinition(normalized);
        if (definition == null) {
            return null;
        }

        if (definition.type() == ProjectionType.WEB_MERCATOR) {
            return webMercatorToGeographic(x, y);
        }
        if (definition.type() == ProjectionType.EQUIDISTANT_CYLINDRICAL) {
            return equidistantCylindricalToGeographic(x, y, definition.ellipsoid());
        }
        return inverseTransverseMercator(x, y, definition);
    }

    private static double[] fromGeographic(double lon, double lat, String targetCode) {
        String normalized = CRSDefinitions.normalizeCode(targetCode);
        if (isGeographic(normalized)) {
            return new double[]{lon, lat};
        }
        if ("EPSG:3857".equalsIgnoreCase(normalized)) {
            return geographicToWebMercator(lon, lat);
        }
        if ("EPSG:4087".equalsIgnoreCase(normalized)) {
            return geographicToEquidistantCylindrical(lon, lat, Ellipsoid.WGS84);
        }

        ProjectedDefinition definition = resolveProjectedDefinition(normalized);
        if (definition == null) {
            return null;
        }

        if (definition.type() == ProjectionType.WEB_MERCATOR) {
            return geographicToWebMercator(lon, lat);
        }
        if (definition.type() == ProjectionType.EQUIDISTANT_CYLINDRICAL) {
            return geographicToEquidistantCylindrical(lon, lat, definition.ellipsoid());
        }
        return forwardTransverseMercator(lon, lat, definition);
    }

    private static boolean isGeographic(String code) {
        return "EPSG:4326".equalsIgnoreCase(code)
                || "EPSG:84".equalsIgnoreCase(code)
                || "CRS:84".equalsIgnoreCase(code)
                || "EPSG:4258".equalsIgnoreCase(code)
                || "EPSG:4269".equalsIgnoreCase(code)
                || "EPSG:4674".equalsIgnoreCase(code)
                || "EPSG:4190".equalsIgnoreCase(code)
                || "EPSG:4221".equalsIgnoreCase(code)
                || "EPSG:4490".equalsIgnoreCase(code);
    }

    private static ProjectedDefinition resolveProjectedDefinition(String code) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized.isBlank()) {
            return null;
        }
        if ("EPSG:3857".equalsIgnoreCase(normalized)) {
            return new ProjectedDefinition(ProjectionType.WEB_MERCATOR, Ellipsoid.WGS84, 0.0, 1.0, 0.0, 0.0);
        }
        if ("EPSG:3395".equalsIgnoreCase(normalized)) {
            return new ProjectedDefinition(ProjectionType.TRANSVERSE_MERCATOR, Ellipsoid.WGS84, 0.0, 1.0, 0.0, 0.0);
        }
        if ("EPSG:4087".equalsIgnoreCase(normalized)) {
            return new ProjectedDefinition(ProjectionType.EQUIDISTANT_CYLINDRICAL, Ellipsoid.WGS84, 0.0, 1.0, 0.0, 0.0);
        }
        if (normalized.matches("EPSG:326\\d\\d")) {
            int zone = Integer.parseInt(normalized.substring(normalized.length() - 2));
            return new ProjectedDefinition(ProjectionType.TRANSVERSE_MERCATOR, Ellipsoid.WGS84, -183.0 + zone * 6.0, 0.9996, 500000.0, 0.0);
        }
        if (normalized.matches("EPSG:327\\d\\d")) {
            int zone = Integer.parseInt(normalized.substring(normalized.length() - 2));
            return new ProjectedDefinition(ProjectionType.TRANSVERSE_MERCATOR, Ellipsoid.WGS84, -183.0 + zone * 6.0, 0.9996, 500000.0, 10000000.0);
        }
        if (normalized.matches("EPSG:2218[1-7]")) {
            int zone = Integer.parseInt(normalized.substring(normalized.length() - 1));
            return buildArgentinaDefinition(zone, Ellipsoid.WGS84);
        }
        if (normalized.matches("EPSG:534[3-9]")) {
            int zone = Integer.parseInt(normalized.substring(normalized.length() - 1)) - 2;
            return buildArgentinaDefinition(zone, Ellipsoid.GRS80);
        }
        if (normalized.matches("EPSG:2219[1-7]")) {
            int zone = Integer.parseInt(normalized.substring(normalized.length() - 1));
            return buildArgentinaDefinition(zone, Ellipsoid.INTERNATIONAL_1924);
        }
        return null;
    }

    private static ProjectedDefinition buildArgentinaDefinition(int zone, Ellipsoid ellipsoid) {
        double centralMeridian = -75.0 + zone * 3.0;
        double falseEasting = 500000.0 + zone * 1000000.0;
        return new ProjectedDefinition(
                ProjectionType.TRANSVERSE_MERCATOR,
                ellipsoid,
                centralMeridian,
                1.0,
                falseEasting,
                10000000.0
        );
    }

    private static double[] geographicToWebMercator(double lon, double lat) {
        double clampedLat = Math.max(-WEB_MERCATOR_MAX_LAT, Math.min(WEB_MERCATOR_MAX_LAT, lat));
        double x = 6378137.0 * Math.toRadians(lon);
        double y = 6378137.0 * Math.log(Math.tan(Math.PI / 4.0 + Math.toRadians(clampedLat) / 2.0));
        return new double[]{x, y};
    }

    private static GeographicPoint webMercatorToGeographic(double x, double y) {
        double lon = Math.toDegrees(x / 6378137.0);
        double lat = Math.toDegrees(2.0 * Math.atan(Math.exp(y / 6378137.0)) - Math.PI / 2.0);
        return new GeographicPoint(lon, lat);
    }

    private static double[] geographicToEquidistantCylindrical(double lon, double lat, Ellipsoid ellipsoid) {
        double x = ellipsoid.a() * lon * DEG_TO_RAD;
        double y = ellipsoid.a() * lat * DEG_TO_RAD;
        return new double[]{x, y};
    }

    private static GeographicPoint equidistantCylindricalToGeographic(double x, double y, Ellipsoid ellipsoid) {
        double lon = x / ellipsoid.a() * RAD_TO_DEG;
        double lat = y / ellipsoid.a() * RAD_TO_DEG;
        return new GeographicPoint(lon, lat);
    }

    private static double[] forwardTransverseMercator(double lon, double lat, ProjectedDefinition definition) {
        Ellipsoid ellipsoid = definition.ellipsoid();
        double a = ellipsoid.a();
        double f = 1.0 / ellipsoid.inverseFlattening();
        double e2 = 2.0 * f - f * f;
        double ep2 = e2 / (1.0 - e2);
        double latRad = lat * DEG_TO_RAD;
        double lonRad = lon * DEG_TO_RAD;
        double lon0 = definition.centralMeridianDeg() * DEG_TO_RAD;

        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double tanLat = Math.tan(latRad);
        double n = a / Math.sqrt(1.0 - e2 * sinLat * sinLat);
        double t = tanLat * tanLat;
        double c = ep2 * cosLat * cosLat;
        double aTerm = cosLat * (lonRad - lon0);
        double m = meridionalArc(latRad, a, e2);

        double x = definition.falseEasting()
                + definition.scaleFactor() * n * (aTerm
                + (1.0 - t + c) * Math.pow(aTerm, 3) / 6.0
                + (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * ep2) * Math.pow(aTerm, 5) / 120.0);

        double y = definition.falseNorthing()
                + definition.scaleFactor() * (m
                + n * tanLat * (aTerm * aTerm / 2.0
                + (5.0 - t + 9.0 * c + 4.0 * c * c) * Math.pow(aTerm, 4) / 24.0
                + (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * ep2) * Math.pow(aTerm, 6) / 720.0));

        return new double[]{x, y};
    }

    private static GeographicPoint inverseTransverseMercator(double x, double y, ProjectedDefinition definition) {
        Ellipsoid ellipsoid = definition.ellipsoid();
        double a = ellipsoid.a();
        double f = 1.0 / ellipsoid.inverseFlattening();
        double e2 = 2.0 * f - f * f;
        double ep2 = e2 / (1.0 - e2);

        double xAdj = x - definition.falseEasting();
        double yAdj = y - definition.falseNorthing();
        double m = yAdj / definition.scaleFactor();
        double mu = m / (a * (1.0 - e2 / 4.0 - 3.0 * Math.pow(e2, 2) / 64.0 - 5.0 * Math.pow(e2, 3) / 256.0));
        double e1 = (1.0 - Math.sqrt(1.0 - e2)) / (1.0 + Math.sqrt(1.0 - e2));

        double phi1 = mu
                + (3.0 * e1 / 2.0 - 27.0 * Math.pow(e1, 3) / 32.0) * Math.sin(2.0 * mu)
                + (21.0 * Math.pow(e1, 2) / 16.0 - 55.0 * Math.pow(e1, 4) / 32.0) * Math.sin(4.0 * mu)
                + (151.0 * Math.pow(e1, 3) / 96.0) * Math.sin(6.0 * mu)
                + (1097.0 * Math.pow(e1, 4) / 512.0) * Math.sin(8.0 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);
        double n1 = a / Math.sqrt(1.0 - e2 * sinPhi1 * sinPhi1);
        double t1 = tanPhi1 * tanPhi1;
        double c1 = ep2 * cosPhi1 * cosPhi1;
        double r1 = a * (1.0 - e2) / Math.pow(1.0 - e2 * sinPhi1 * sinPhi1, 1.5);
        double d = xAdj / (n1 * definition.scaleFactor());

        double lat = phi1 - (n1 * tanPhi1 / r1) * (
                d * d / 2.0
                        - (5.0 + 3.0 * t1 + 10.0 * c1 - 4.0 * c1 * c1 - 9.0 * ep2) * Math.pow(d, 4) / 24.0
                        + (61.0 + 90.0 * t1 + 298.0 * c1 + 45.0 * t1 * t1 - 252.0 * ep2 - 3.0 * c1 * c1) * Math.pow(d, 6) / 720.0
        );

        double lon = definition.centralMeridianDeg() * DEG_TO_RAD + (
                d
                        - (1.0 + 2.0 * t1 + c1) * Math.pow(d, 3) / 6.0
                        + (5.0 - 2.0 * c1 + 28.0 * t1 - 3.0 * c1 * c1 + 8.0 * ep2 + 24.0 * t1 * t1) * Math.pow(d, 5) / 120.0
        ) / cosPhi1;

        return new GeographicPoint(lon * RAD_TO_DEG, lat * RAD_TO_DEG);
    }

    private static double meridionalArc(double latRad, double a, double e2) {
        return a * (
                (1.0 - e2 / 4.0 - 3.0 * Math.pow(e2, 2) / 64.0 - 5.0 * Math.pow(e2, 3) / 256.0) * latRad
                        - (3.0 * e2 / 8.0 + 3.0 * Math.pow(e2, 2) / 32.0 + 45.0 * Math.pow(e2, 3) / 1024.0) * Math.sin(2.0 * latRad)
                        + (15.0 * Math.pow(e2, 2) / 256.0 + 45.0 * Math.pow(e2, 3) / 1024.0) * Math.sin(4.0 * latRad)
                        - (35.0 * Math.pow(e2, 3) / 3072.0) * Math.sin(6.0 * latRad)
        );
    }

    private enum ProjectionType {
        WEB_MERCATOR,
        TRANSVERSE_MERCATOR,
        EQUIDISTANT_CYLINDRICAL
    }

    private record GeographicPoint(double lon, double lat) {
    }

    private record ProjectedDefinition(
            ProjectionType type,
            Ellipsoid ellipsoid,
            double centralMeridianDeg,
            double scaleFactor,
            double falseEasting,
            double falseNorthing
    ) {
    }

    private record Ellipsoid(double a, double inverseFlattening) {
        private static final Ellipsoid WGS84 = new Ellipsoid(6378137.0, 298.257223563);
        private static final Ellipsoid GRS80 = new Ellipsoid(6378137.0, 298.257222101);
        private static final Ellipsoid INTERNATIONAL_1924 = new Ellipsoid(6378388.0, 297.0);
    }
}
