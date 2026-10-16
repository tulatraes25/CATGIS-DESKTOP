package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.List;
import java.util.Locale;

public final class MapMeasurementUtils {

    private MapMeasurementUtils() {}

    public static Geometry buildLineInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            if (coordinates == null || coordinates.size() < 2) return null;
            GeometryFactory gf = new GeometryFactory();
            LineString line = gf.createLineString(coordinates.toArray(new Coordinate[0]));
            return reprojectToMetric(line, sourceCRSCode);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo construir geometria de medicion lineal", ex);
            return null;
        }
    }

    public static Geometry buildPolygonInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            Geometry polygon = DrawFeatureBuilder.buildPolygon(coordinates);
            if (polygon == null) return null;
            return reprojectToMetric(polygon, sourceCRSCode);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo construir geometria de medicion de area", ex);
            return null;
        }
    }

    public static Geometry reprojectToMetric(Geometry geometry, String sourceCRSCode) {
        try {
            if (geometry == null || geometry.isEmpty()) return geometry;
            if (sourceCRSCode == null || sourceCRSCode.isBlank()) return null;
            String sourceCode = sourceCRSCode.trim();
            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            String targetCode = chooseMetricCRS(geometry, sourceCode, sourceCRS);
            if (targetCode == null || targetCode.isBlank()) return null;
            if (sourceCode.equalsIgnoreCase(targetCode)) return geometry.copy();
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            Geometry transformed = JTS.transform(geometry, transform);
            return isFiniteGeometry(transformed) ? transformed : null;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo reproyectar geometria para medicion", ex);
            return null;
        }
    }

    private static boolean isFiniteGeometry(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) return geometry != null;
        for (Coordinate c : geometry.getCoordinates()) {
            if (c == null || !Double.isFinite(c.x) || !Double.isFinite(c.y)) return false;
        }
        return true;
    }

    public static String chooseMetricCRS(String sourceCRSCode) {
        if (sourceCRSCode == null || sourceCRSCode.isBlank()) return "";
        try {
            String sourceCode = sourceCRSCode.trim();
            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            return isProjectedMetric(sourceCRS) ? sourceCode : "";
        } catch (Exception ex) {
            return "";
        }
    }

    private static String chooseMetricCRS(
            Geometry geometry, String sourceCode, CoordinateReferenceSystem sourceCRS) throws Exception {
        if (isProjectedMetric(sourceCRS)) {
            return sourceCode;
        }
        if (geometry == null || geometry.isEmpty()) {
            return "";
        }

        Geometry geographic = geometry;
        CoordinateReferenceSystem wgs84 = CRSDefinitions.decode("EPSG:4326", true);
        if (!CRS.equalsIgnoreMetadata(sourceCRS, wgs84)) {
            MathTransform toWgs84 = CRS.findMathTransform(sourceCRS, wgs84, true);
            geographic = JTS.transform(geometry, toWgs84);
        }
        Coordinate centroid = geographic.getCentroid().getCoordinate();
        if (centroid == null || !Double.isFinite(centroid.x) || !Double.isFinite(centroid.y)
                || centroid.y < -80d || centroid.y > 84d) {
            return "";
        }
        int zone = (int) Math.floor((centroid.x + 180d) / 6d) + 1;
        zone = Math.max(1, Math.min(60, zone));
        return "EPSG:" + ((centroid.y >= 0d ? 32600 : 32700) + zone);
    }

    private static boolean isProjectedMetric(CoordinateReferenceSystem crs) {
        if (!(crs instanceof ProjectedCRS)) return false;
        try {
            for (int i = 0; i < crs.getCoordinateSystem().getDimension(); i++) {
                String unit = String.valueOf(crs.getCoordinateSystem().getAxis(i).getUnit())
                        .toLowerCase(Locale.ROOT);
                if (!(unit.contains("metre") || unit.contains("meter")
                        || unit.equals("m") || unit.contains("9001"))) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
