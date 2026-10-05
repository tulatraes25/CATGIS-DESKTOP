package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
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
            String sourceCode = (sourceCRSCode != null && !sourceCRSCode.isBlank()) ? sourceCRSCode : "EPSG:4326";
            String targetCode = chooseMetricCRS(sourceCode);
            if (sourceCode.equalsIgnoreCase(targetCode)) return geometry;
            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo reproyectar geometria para medicion", ex);
            return geometry;
        }
    }

    public static String chooseMetricCRS(String sourceCRSCode) {
        if (sourceCRSCode == null || sourceCRSCode.isBlank()) return "EPSG:3857";
        String code = sourceCRSCode.trim().toUpperCase(Locale.ROOT);
        if (code.equals("EPSG:4326")) return "EPSG:3857";
        if (code.startsWith("EPSG:327") || code.startsWith("EPSG:326")) return code;
        if (code.startsWith("EPSG:221") || code.startsWith("EPSG:534") || code.startsWith("EPSG:248")) return code;
        return "EPSG:3857";
    }
}
