package ar.com.catgis.climate;

import ar.com.catgis.CRSDefinitions;
import org.geotools.referencing.CRS;

/**
 * T3/#162: pure CF-conventions helpers for the NetCDF reader.
 *
 * <p>Handles {@code grid_mapping} CRS discovery, coordinate units
 * (degrees/radians, 0..360 longitudes) and CF packaging ({@code scale_factor},
 * {@code add_offset}, {@code _FillValue}, {@code valid_min/max}). The UCAR
 * fallback must never label a file as EPSG:4326 just because it is a NetCDF.</p>
 */
public final class NetCdfCfSupport {

    /** CF CRS resolution with explicit provenance. */
    public record CrsResolution(String crsCode, String provenance) {
        public boolean hasCrs() {
            return crsCode != null && !crsCode.isBlank();
        }
    }

    /** CF packing/NoData attributes (raw domain before scale/offset). */
    public record Packaging(double scaleFactor, double addOffset, Float fillValue,
                            Float validMin, Float validMax) {
        public boolean isNoData(float raw) {
            if (!Float.isFinite(raw)) {
                return true;
            }
            if (fillValue != null && raw == fillValue) {
                return true;
            }
            double decoded = decode(raw);
            if (!Double.isFinite(decoded)) {
                return true;
            }
            if (validMin != null && decoded < validMin) {
                return true;
            }
            return validMax != null && decoded > validMax;
        }

        public float decode(float raw) {
            return (float) (raw * scaleFactor + addOffset);
        }
    }

    private NetCdfCfSupport() {}

    /**
     * Resolves the CRS from CF attributes. Returns a blank code when the file
     * lacks a trustworthy CF mapping; callers must fail closed, never assume
     * WGS84.
     */
    public static CrsResolution resolveCrs(String gridMappingName, String epsgCode,
                                           String spatialRef, String xUnits, String yUnits) {
        String epsg = normalizeEpsg(epsgCode);
        if (!epsg.isBlank()) {
            return new CrsResolution(epsg, "CF epsg_code");
        }
        String wktCode = codeFromWkt(spatialRef);
        if (!wktCode.isBlank()) {
            return new CrsResolution(wktCode, "CF spatial_ref");
        }
        String mapping = gridMappingName == null ? "" : gridMappingName.trim().toLowerCase();
        if (!mapping.isBlank()) {
            if ("latitude_longitude".equals(mapping)) {
                return new CrsResolution("EPSG:4326", "CF latitude_longitude");
            }
            // A rotated/projected grid_mapping without epsg_code/spatial_ref is not
            // WGS84: fail closed instead of mislabeling degree-like coordinates.
            return new CrsResolution("", "grid_mapping CF " + mapping
                    + " sin epsg_code/spatial_ref decodable; no se asume WGS84");
        }
        boolean degrees = isDegreeUnits(xUnits) && isDegreeUnits(yUnits);
        if (degrees) {
            return new CrsResolution("EPSG:4326", "CF degree coordinates");
        }
        return new CrsResolution("", "sin grid_mapping CF con CRS; no se asume WGS84");
    }

    /** Converts a coordinate value to degrees when CF units say radians. */
    public static double toDegrees(double value, String units) {
        String normalized = units == null ? "" : units.trim().toLowerCase();
        if (normalized.contains("radian")) {
            return Math.toDegrees(value);
        }
        return value;
    }

    /** Normalizes a longitude to -180..180 handling 0..360 CF grids. */
    public static double normalizeLongitude(double degrees) {
        double lon = degrees;
        while (lon > 180.0) {
            lon -= 360.0;
        }
        while (lon < -180.0) {
            lon += 360.0;
        }
        return lon;
    }

    /** Builds packaging from parsed CF attributes ({@code null} when absent). */
    public static Packaging packaging(Double scaleFactor, Double addOffset, Float fillValue,
                                      Float validMin, Float validMax) {
        return new Packaging(
                scaleFactor != null ? scaleFactor : 1.0,
                addOffset != null ? addOffset : 0.0,
                fillValue, validMin, validMax);
    }

    private static String normalizeEpsg(String epsgCode) {
        if (epsgCode == null || epsgCode.isBlank()) {
            return "";
        }
        String digits = epsgCode.trim();
        if (digits.regionMatches(true, 0, "EPSG:", 0, 5)) {
            digits = digits.substring(5).trim();
        }
        if (!digits.matches("\\d{4,6}")) {
            return "";
        }
        try {
            CRSDefinitions.decode("EPSG:" + digits, true);
            return "EPSG:" + digits;
        } catch (Exception invalid) {
            return "";
        }
    }

    private static String codeFromWkt(String spatialRef) {
        if (spatialRef == null || spatialRef.isBlank()) {
            return "";
        }
        try {
            var crs = CRS.decode(spatialRef, true);
            String code = CRS.lookupIdentifier(crs, true);
            return code != null && code.regionMatches(true, 0, "EPSG:", 0, 5) ? code : "";
        } catch (Exception invalid) {
            return "";
        }
    }

    private static boolean isDegreeUnits(String units) {
        if (units == null || units.isBlank()) {
            return false;
        }
        String normalized = units.trim().toLowerCase();
        return normalized.contains("degree") || normalized.equals("degrees")
                || normalized.equals("degrees_east") || normalized.equals("degrees_north")
                || normalized.equals("degrees_west") || normalized.equals("degrees_south");
    }
}
