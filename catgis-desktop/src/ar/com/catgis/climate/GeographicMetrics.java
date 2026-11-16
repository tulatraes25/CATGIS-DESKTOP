package ar.com.catgis.climate;

/**
 * WGS84 geographic metrics using public ellipsoid formulas (no magic
 * metres-per-degree constant). Used by the risk engine to convert between
 * metres and degrees in a latitude-aware way.
 */
public final class GeographicMetrics {

    /** WGS84 semi-major axis (metres). */
    public static final double WGS84_A = 6378137.0;
    /** WGS84 flattening. */
    public static final double WGS84_F = 1.0 / 298.257223563;
    /** WGS84 first eccentricity squared. */
    public static final double WGS84_E2 = WGS84_F * (2.0 - WGS84_F);

    private GeographicMetrics() {}

    /** Meridian radius of curvature (metres) at the given latitude. */
    public static double meridianRadius(double latitudeDegrees) {
        double sin = Math.sin(Math.toRadians(clampLatitude(latitudeDegrees)));
        double denominator = Math.pow(1.0 - WGS84_E2 * sin * sin, 1.5);
        return WGS84_A * (1.0 - WGS84_E2) / denominator;
    }

    /** Prime-vertical radius of curvature (metres) at the given latitude. */
    public static double primeVerticalRadius(double latitudeDegrees) {
        double sin = Math.sin(Math.toRadians(clampLatitude(latitudeDegrees)));
        double denominator = Math.sqrt(1.0 - WGS84_E2 * sin * sin);
        return WGS84_A / denominator;
    }

    /** Metres per degree of latitude (meridian arc). */
    public static double metersPerDegreeLatitude(double latitudeDegrees) {
        return Math.toRadians(1.0) * meridianRadius(latitudeDegrees);
    }

    /** Metres per degree of longitude along the parallel at this latitude. */
    public static double metersPerDegreeLongitude(double latitudeDegrees) {
        double latitude = clampLatitude(latitudeDegrees);
        return Math.toRadians(1.0) * primeVerticalRadius(latitude)
                * Math.cos(Math.toRadians(latitude));
    }

    public static double degreesPerMeterLatitude(double latitudeDegrees) {
        return 1.0 / metersPerDegreeLatitude(latitudeDegrees);
    }

    public static double degreesPerMeterLongitude(double latitudeDegrees) {
        return 1.0 / metersPerDegreeLongitude(latitudeDegrees);
    }

    private static double clampLatitude(double latitudeDegrees) {
        return Math.max(-89.999, Math.min(89.999, latitudeDegrees));
    }
}
