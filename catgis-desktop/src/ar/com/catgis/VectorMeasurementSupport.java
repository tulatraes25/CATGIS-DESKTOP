package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class VectorMeasurementSupport {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private VectorMeasurementSupport() {
    }

    static double resolveAreaSquareMeters(Layer layer, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return 0d;
        }

        try {
            Geometry metricGeometry = toMetricGeometry(layer, geometry);
            if (metricGeometry != null && !metricGeometry.isEmpty()) {
                return Math.abs(metricGeometry.getArea());
            }
        } catch (Exception ignored) { CatgisLogger.warn("VectorMeasurementSupport: operation failed", ignored); }

        return Math.abs(geometry.getArea());
    }

    static double resolveLengthMeters(Layer layer, Geometry geometry) {
        return resolveLengthMeters(layer, geometry, false);
    }

    static double resolvePerimeterMeters(Layer layer, Geometry geometry) {
        return resolveLengthMeters(layer, geometry, true);
    }

    private static double resolveLengthMeters(Layer layer, Geometry geometry, boolean perimeterMode) {
        if (geometry == null || geometry.isEmpty()) {
            return 0d;
        }

        String sourceCode = resolveSourceCode(layer);
        try {
            CoordinateReferenceSystem sourceCrs = decode(sourceCode);
            if (sourceCrs == null) {
                return Math.abs(geometry.getLength());
            }

            Geometry workingGeometry = perimeterMode ? extractPerimeterGeometry(geometry) : geometry;
            if (workingGeometry == null || workingGeometry.isEmpty()) {
                return 0d;
            }

            if (!isProjectedMetric(sourceCrs)) {
                double orthodromic = measureOrthodromicLength(workingGeometry, sourceCode);
                if (Double.isFinite(orthodromic) && orthodromic > 0d) {
                    return orthodromic;
                }
            }

            Geometry metricGeometry = toMetricGeometry(layer, workingGeometry);
            if (metricGeometry != null && !metricGeometry.isEmpty()) {
                return Math.abs(metricGeometry.getLength());
            }
        } catch (Exception ignored) { CatgisLogger.warn("VectorMeasurementSupport: operation failed", ignored); }

        return Math.abs(geometry.getLength());
    }

    private static Geometry toMetricGeometry(Layer layer, Geometry geometry) throws Exception {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }

        String sourceCode = resolveSourceCode(layer);
        CoordinateReferenceSystem sourceCrs = decode(sourceCode);
        if (sourceCrs == null) {
            return geometry;
        }

        if (isProjectedMetric(sourceCrs)) {
            return geometry;
        }

        String metricCode = resolveMetricCrs(layer, geometry, sourceCode, sourceCrs);
        if (metricCode == null || metricCode.isBlank() || metricCode.equalsIgnoreCase(sourceCode)) {
            return geometry;
        }

        CoordinateReferenceSystem metricCrs = decode(metricCode);
        if (metricCrs == null) {
            return geometry;
        }

        MathTransform transform = CRS.findMathTransform(sourceCrs, metricCrs, true);
        return JTS.transform(geometry, transform);
    }

    private static double measureOrthodromicLength(Geometry geometry, String sourceCode) {
        try {
            CoordinateReferenceSystem sourceCrs = decode(sourceCode);
            if (sourceCrs == null) {
                return Double.NaN;
            }

            Geometry geographic = geometry;
            if (!(CRS.getHorizontalCRS(sourceCrs) == null) && !isGeographicLonLat(sourceCrs)) {
                CoordinateReferenceSystem wgs84 = decode("EPSG:4326");
                if (wgs84 != null) {
                    MathTransform toWgs84 = CRS.findMathTransform(sourceCrs, wgs84, true);
                    geographic = JTS.transform(geometry, toWgs84);
                }
            }

            Geometry lineal = extractPerimeterGeometry(geographic);
            if (lineal == null || lineal.isEmpty()) {
                return Double.NaN;
            }

            List<LineString> lines = new ArrayList<>();
            collectLineStrings(lineal, lines);
            if (lines.isEmpty()) {
                return Double.NaN;
            }

            double total = 0d;
            org.geotools.referencing.GeodeticCalculator calculator =
                    new org.geotools.referencing.GeodeticCalculator(DefaultGeographicCRSHolder.get());

            for (LineString line : lines) {
                Coordinate[] coordinates = line.getCoordinates();
                for (int i = 1; i < coordinates.length; i++) {
                    Coordinate a = coordinates[i - 1];
                    Coordinate b = coordinates[i];
                    if (a == null || b == null) {
                        continue;
                    }
                    calculator.setStartingGeographicPoint(a.x, a.y);
                    calculator.setDestinationGeographicPoint(b.x, b.y);
                    total += Math.abs(calculator.getOrthodromicDistance());
                }
            }

            return total;
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private static Geometry extractPerimeterGeometry(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return geometry.getBoundary();
        }
        return geometry;
    }

    private static void collectLineStrings(Geometry geometry, List<LineString> target) {
        if (geometry == null || geometry.isEmpty() || target == null) {
            return;
        }
        if (geometry instanceof LineString lineString) {
            target.add(lineString);
            return;
        }
        if (geometry instanceof MultiLineString multiLineString) {
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                collectLineStrings(multiLineString.getGeometryN(i), target);
            }
            return;
        }
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            collectLineStrings(geometry.getGeometryN(i), target);
        }
    }

    private static String resolveMetricCrs(Layer layer,
                                           Geometry geometry,
                                           String sourceCode,
                                           CoordinateReferenceSystem sourceCrs) {
        String projectCode = CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
        if (projectCode != null && !projectCode.isBlank()) {
            CoordinateReferenceSystem projectCrs = decode(projectCode);
            if (isProjectedMetric(projectCrs)) {
                return projectCode;
            }
        }

        Coordinate lonLat = toLonLatCentroid(geometry, sourceCode, sourceCrs);
        if (lonLat != null) {
            String utm = utmCodeFor(lonLat.x, lonLat.y);
            if (!utm.isBlank()) {
                return utm;
            }
        }

        if (isProjectedMetric(sourceCrs)) {
            return sourceCode;
        }
        return "EPSG:3857";
    }

    private static Coordinate toLonLatCentroid(Geometry geometry,
                                               String sourceCode,
                                               CoordinateReferenceSystem sourceCrs) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }

        Point centroid = geometry.getCentroid();
        if (centroid == null || centroid.isEmpty()) {
            return null;
        }

        try {
            if (isGeographicLonLat(sourceCrs)) {
                return centroid.getCoordinate();
            }

            CoordinateReferenceSystem wgs84 = decode("EPSG:4326");
            if (wgs84 == null || sourceCode == null || sourceCode.isBlank()) {
                return centroid.getCoordinate();
            }
            MathTransform transform = CRS.findMathTransform(sourceCrs, wgs84, true);
            Point geographic = (Point) JTS.transform(centroid, transform);
            return geographic.getCoordinate();
        } catch (Exception ex) {
            return centroid.getCoordinate();
        }
    }

    private static String utmCodeFor(double lon, double lat) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat)) {
            return "";
        }
        if (lat < -80d || lat > 84d) {
            return "EPSG:3857";
        }
        int zone = (int) Math.floor((lon + 180d) / 6d) + 1;
        zone = Math.max(1, Math.min(60, zone));
        int epsgBase = lat >= 0d ? 32600 : 32700;
        return String.format(Locale.US, "EPSG:%d", epsgBase + zone);
    }

    private static boolean isGeographicLonLat(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return false;
        }
        try {
            return !(CRS.getHorizontalCRS(crs) instanceof ProjectedCRS) && !(crs instanceof ProjectedCRS);
        } catch (Exception ex) {
            return !(crs instanceof ProjectedCRS);
        }
    }

    private static boolean isProjectedMetric(CoordinateReferenceSystem crs) {
        if (!(crs instanceof ProjectedCRS)) {
            return false;
        }
        try {
            String unit0 = String.valueOf(crs.getCoordinateSystem().getAxis(0).getUnit()).toLowerCase(Locale.ROOT);
            String unit1 = String.valueOf(crs.getCoordinateSystem().getAxis(1).getUnit()).toLowerCase(Locale.ROOT);
            return looksMetric(unit0) && looksMetric(unit1);
        } catch (Exception ex) {
            return true;
        }
    }

    private static boolean looksMetric(String unitText) {
        if (unitText == null) {
            return false;
        }
        String normalized = unitText.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("metre")
                || normalized.contains("meter")
                || normalized.equals("m")
                || normalized.contains("9001");
    }

    private static String resolveSourceCode(Layer layer) {
        if (layer != null && layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()) {
            return CRSDefinitions.normalizeCode(layer.getSourceCRS());
        }
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            return CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS());
        }
        return "";
    }

    private static CoordinateReferenceSystem decode(String code) {
        try {
            String normalized = CRSDefinitions.normalizeCode(code);
            if (normalized == null || normalized.isBlank()) {
                return null;
            }
            return CRSDefinitions.decode(normalized, true);
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class DefaultGeographicCRSHolder {
        private static CoordinateReferenceSystem get() {
            return org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
        }
    }
}
