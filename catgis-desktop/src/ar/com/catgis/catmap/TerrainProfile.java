package ar.com.catgis.catmap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Terrain profile along a line: sampled elevations (bilinear), slope per sample,
 * cumulative ascent/descent, hypsometric curve and a 3D profile line.
 */
public final class TerrainProfile {

    /** One sampled point along the profile. */
    public record ProfileSample(double distanceM, double x, double y,
                                double elevationM, double slopeDegrees) {}

    /** Point of the hypsometric curve (relative height vs relative area). */
    public record HypsometricPoint(double relativeHeight, double relativeArea) {}

    /** Full profile result. */
    public record Result(List<ProfileSample> samples, List<HypsometricPoint> hypsometricCurve,
                         double lengthM, double minElevationM, double maxElevationM,
                         double reliefM, double meanElevationM, double meanSlopeDegrees,
                         double maxSlopeDegrees, double cumulativeAscentM,
                         double cumulativeDescentM, LineString profile3D) {}

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private TerrainProfile() {}

    public static Result sample(AnalysisRaster dem, double startX, double startY,
                                double endX, double endY, int sampleCount) {
        return sample(dem, startX, startY, endX, endY, sampleCount, 20);
    }

    public static Result sample(AnalysisRaster dem, double startX, double startY,
                                double endX, double endY, int sampleCount,
                                int hypsometricBands) {
        if (dem == null) {
            throw new IllegalArgumentException("El perfil requiere un DEM.");
        }
        if (sampleCount < 2) {
            throw new IllegalArgumentException("El perfil requiere al menos dos muestras.");
        }
        if (hypsometricBands < 2) {
            throw new IllegalArgumentException("La curva hipsometrica requiere al menos 2 bandas.");
        }
        if (!inside(dem, startX, startY) || !inside(dem, endX, endY)) {
            throw new IllegalArgumentException("El perfil debe estar dentro de la extension del DEM.");
        }
        double length = Math.hypot(endX - startX, endY - startY);
        if (!(length > 0)) {
            throw new IllegalArgumentException("El perfil necesita una longitud mayor a cero.");
        }

        double step = length / (sampleCount - 1);
        double[] elevations = new double[sampleCount];
        List<ProfileSample> provisional = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            double t = (double) i / (sampleCount - 1);
            double x = startX + (endX - startX) * t;
            double y = startY + (endY - startY) * t;
            elevations[i] = bilinearElevation(dem, x, y);
            provisional.add(new ProfileSample(step * i, x, y, elevations[i], Double.NaN));
        }

        List<ProfileSample> samples = new ArrayList<>();
        double sumElevation = 0;
        long validCount = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double slopeSum = 0;
        double slopeMax = 0;
        long slopeCount = 0;
        double ascent = 0;
        double descent = 0;
        for (int i = 0; i < sampleCount; i++) {
            double slope = slopeAt(elevations, i, step);
            ProfileSample sample = provisional.get(i);
            samples.add(new ProfileSample(sample.distanceM(), sample.x(), sample.y(),
                    sample.elevationM(), slope));
            if (Double.isFinite(sample.elevationM())) {
                validCount++;
                sumElevation += sample.elevationM();
                min = Math.min(min, sample.elevationM());
                max = Math.max(max, sample.elevationM());
            }
            if (Double.isFinite(slope)) {
                slopeCount++;
                slopeSum += slope;
                slopeMax = Math.max(slopeMax, slope);
            }
            if (i > 0) {
                double previous = elevations[i - 1];
                double current = elevations[i];
                if (Double.isFinite(previous) && Double.isFinite(current)) {
                    double delta = current - previous;
                    if (delta >= 0) {
                        ascent += delta;
                    } else {
                        descent += -delta;
                    }
                }
            }
        }
        if (validCount == 0) {
            throw new IllegalStateException("El perfil no tiene elevaciones validas.");
        }
        double relief = max - min;
        List<HypsometricPoint> curve = hypsometricCurve(elevations, min, max, hypsometricBands);
        LineString line3D = build3DLine(samples);
        return new Result(samples, curve, length, min, max, relief, sumElevation / validCount,
                slopeCount == 0 ? Double.NaN : slopeSum / slopeCount, slopeMax,
                ascent, descent, line3D);
    }

    /** Bilinear elevation at world coordinates; NaN when the DEM has NoData. */
    static double bilinearElevation(AnalysisRaster dem, double x, double y) {
        double fx = (x - dem.minX()) / dem.cellSizeX() - 0.5;
        double fy = (dem.maxY() - y) / dem.cellSizeY() - 0.5;
        int x0 = (int) Math.floor(fx);
        int y0 = (int) Math.floor(fy);
        double tx = fx - x0;
        double ty = fy - y0;
        int[] columns = {x0, x0 + 1};
        int[] rows = {y0, y0 + 1};
        double[][] values = new double[2][2];
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                int col = clamp(columns[c], 0, dem.width() - 1);
                int row = clamp(rows[r], 0, dem.height() - 1);
                if (dem.isNoData(col, row)) {
                    return Double.NaN;
                }
                values[r][c] = dem.valueAt(col, row);
            }
        }
        double top = values[0][0] * (1 - tx) + values[0][1] * tx;
        double bottom = values[1][0] * (1 - tx) + values[1][1] * tx;
        return top * (1 - ty) + bottom * ty;
    }

    private static List<HypsometricPoint> hypsometricCurve(double[] elevations, double min,
                                                           double max, int bands) {
        List<Double> valid = new ArrayList<>();
        for (double elevation : elevations) {
            if (Double.isFinite(elevation)) {
                valid.add(elevation);
            }
        }
        List<HypsometricPoint> curve = new ArrayList<>();
        double relief = max - min;
        for (int band = 0; band <= bands; band++) {
            double relativeHeight = (double) band / bands;
            if (relief <= 0) {
                curve.add(new HypsometricPoint(relativeHeight,
                        relativeHeight == 0 ? 1.0 : 0.0));
                continue;
            }
            double threshold = min + relief * relativeHeight;
            long above = 0;
            for (double elevation : valid) {
                if (elevation >= threshold - 1.0e-9) {
                    above++;
                }
            }
            curve.add(new HypsometricPoint(relativeHeight,
                    (double) above / valid.size()));
        }
        return curve;
    }

    private static LineString build3DLine(List<ProfileSample> samples) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (ProfileSample sample : samples) {
            if (!Double.isFinite(sample.elevationM())) {
                continue;
            }
            coordinates.add(new Coordinate(sample.x(), sample.y(), sample.elevationM()));
        }
        if (coordinates.size() < 2) {
            return null;
        }
        return GEOMETRY_FACTORY.createLineString(coordinates.toArray(new Coordinate[0]));
    }

    private static double slopeAt(double[] elevations, int index, double step) {
        int previous = index - 1;
        while (previous >= 0 && !Double.isFinite(elevations[previous])) {
            previous--;
        }
        int next = index + 1;
        while (next < elevations.length && !Double.isFinite(elevations[next])) {
            next++;
        }
        if (previous < 0 || next >= elevations.length) {
            return Double.NaN;
        }
        double run = (next - previous) * step;
        if (!(run > 0)) {
            return Double.NaN;
        }
        return Math.toDegrees(Math.atan((elevations[next] - elevations[previous]) / run));
    }

    private static boolean inside(AnalysisRaster dem, double x, double y) {
        return x >= dem.minX() && x <= dem.maxX()
                && y >= dem.minY() && y <= dem.maxY();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
