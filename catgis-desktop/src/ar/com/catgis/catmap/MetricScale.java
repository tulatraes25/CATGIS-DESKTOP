package ar.com.catgis.catmap;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.climate.GeographicMetrics;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.GeographicCRS;

/**
 * Metric scale of an analytical raster: converts CRS cell sizes to metres so
 * morphology/volume results are expressed in SI units instead of raw degrees or
 * feet. Geographic CRS use WGS84 ellipsoid metrics at the raster's latitude.
 */
public final class MetricScale {

    /** Metres per CRS unit along each axis. */
    public record GridScale(double metersPerUnitX, double metersPerUnitY) {
        public GridScale {
            if (!Double.isFinite(metersPerUnitX) || metersPerUnitX <= 0
                    || !Double.isFinite(metersPerUnitY) || metersPerUnitY <= 0) {
                throw new IllegalArgumentException("Invalid metric scale for raster.");
            }
        }

        public double cellAreaM2(double cellSizeX, double cellSizeY) {
            return Math.abs(cellSizeX) * metersPerUnitX
                    * Math.abs(cellSizeY) * metersPerUnitY;
        }

        public double lengthMeters(double deltaX, double deltaY) {
            return Math.hypot(deltaX * metersPerUnitX, deltaY * metersPerUnitY);
        }

        public double minCellSizeM(double cellSizeX, double cellSizeY) {
            return Math.min(Math.abs(cellSizeX) * metersPerUnitX,
                    Math.abs(cellSizeY) * metersPerUnitY);
        }
    }

    private MetricScale() {}

    /** Scale for a raster, using its latitude when the CRS is geographic. */
    public static GridScale forRaster(AnalysisRaster raster) {
        if (raster == null) {
            throw new IllegalArgumentException("Metric scale requires a raster.");
        }
        double centerLatitude = (raster.minY() + raster.maxY()) / 2.0;
        return forCrs(raster.crsCode(), centerLatitude);
    }

    /**
     * Scale for a CRS. Projected CRS use their declared axis units; geographic
     * CRS use WGS84 metres per degree at {@code centerLatitudeDegrees}.
     */
    public static GridScale forCrs(String crsCode, double centerLatitudeDegrees) {
        if (crsCode == null || crsCode.isBlank()) {
            throw new IllegalArgumentException("Metric scale requires an explicit CRS.");
        }
        try {
            CoordinateReferenceSystem crs =
                    CRSDefinitions.decode(CRSDefinitions.normalizeCode(crsCode), true);
            if (crs instanceof GeographicCRS) {
                double latitude = Math.max(-89.0, Math.min(89.0, centerLatitudeDegrees));
                return new GridScale(
                        GeographicMetrics.metersPerDegreeLongitude(latitude),
                        GeographicMetrics.metersPerDegreeLatitude(latitude));
            }
            double axisX = metresPerUnit(crs, 0);
            double axisY = metresPerUnit(crs, 1);
            return new GridScale(axisX, axisY);
        } catch (IllegalArgumentException failClosed) {
            throw failClosed;
        } catch (Exception unsupported) {
            throw new IllegalArgumentException(
                    "CRS without metric conversion for analytical results: " + crsCode,
                    unsupported);
        }
    }

    private static double metresPerUnit(CoordinateReferenceSystem crs, int axisIndex) {
        int axisCount = crs.getCoordinateSystem().getDimension();
        int index = Math.min(axisIndex, axisCount - 1);
        var unit = crs.getCoordinateSystem().getAxis(index).getUnit()
                .asType(javax.measure.quantity.Length.class);
        double metres = unit.getConverterTo(org.geotools.measure.Units.METRE).convert(1.0);
        if (!Double.isFinite(metres) || metres <= 0) {
            throw new IllegalArgumentException("CRS axis unit is not convertible to metres.");
        }
        return metres;
    }
}
