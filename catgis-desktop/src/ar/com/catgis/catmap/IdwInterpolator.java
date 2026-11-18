package ar.com.catgis.catmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Inverse Distance Weighting (IDW) interpolation from scattered points onto a
 * floating-point {@link AnalysisRaster} grid. Own implementation (no external
 * dependency); power, neighbour count and search radius are explicit.
 */
public final class IdwInterpolator {

    public static final int MAX_GRID_CELLS = 4_000_000;

    /** A measured point in the same CRS as the target grid. */
    public record Sample(double x, double y, double value) {
        public Sample {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(value)) {
                throw new IllegalArgumentException("IDW samples must be finite.");
            }
        }
    }

    private IdwInterpolator() {}

    /**
     * Interpolate the samples over an explicit grid.
     *
     * @param power           inverse distance power (typically 2)
     * @param maxNeighbors    maximum number of samples used per cell (>=1)
     * @param maxSearchRadius search radius in CRS units; &lt;= 0 means unbounded
     */
    public static AnalysisRaster interpolate(List<Sample> samples, String name, String unit,
                                             double minX, double minY, double maxX, double maxY,
                                             double cellSize, double power, int maxNeighbors,
                                             double maxSearchRadius, String crsCode) {
        if (samples == null || samples.size() < 3) {
            throw new IllegalArgumentException("IDW requires at least three samples.");
        }
        if (!(maxX > minX) || !(maxY > minY)
                || !Double.isFinite(minX) || !Double.isFinite(minY)
                || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            throw new IllegalArgumentException("IDW grid extent is invalid.");
        }
        if (!Double.isFinite(cellSize) || cellSize <= 0) {
            throw new IllegalArgumentException("IDW cell size must be positive.");
        }
        if (!Double.isFinite(power) || power <= 0) {
            throw new IllegalArgumentException("IDW power must be positive.");
        }
        if (maxNeighbors < 1) {
            throw new IllegalArgumentException("IDW maxNeighbors must be at least one.");
        }
        if (crsCode == null || crsCode.isBlank()) {
            throw new IllegalArgumentException("IDW requires an explicit CRS.");
        }
        long columns = (long) Math.ceil((maxX - minX) / cellSize);
        long rows = (long) Math.ceil((maxY - minY) / cellSize);
        if (columns <= 0 || rows <= 0 || columns * rows > MAX_GRID_CELLS) {
            throw new IllegalArgumentException("IDW grid exceeds the supported size ("
                    + MAX_GRID_CELLS + " cells).");
        }
        int width = (int) columns;
        int height = (int) rows;
        float[] values = new float[width * height];
        Arrays.fill(values, TerrainRasterAlgorithms.NO_DATA);
        List<Sample> validSamples = new ArrayList<>();
        for (Sample sample : samples) {
            if (sample != null) {
                validSamples.add(sample);
            }
        }
        if (validSamples.size() < 3) {
            throw new IllegalArgumentException("IDW requires at least three valid samples.");
        }
        boolean bounded = maxSearchRadius > 0 && Double.isFinite(maxSearchRadius);

        // The grid envelope is aligned to whole cells from (minX, minY). The
        // requested maxX/maxY only define the coverage, so the vertical origin
        // must be the aligned north edge, not the requested (possibly rounded)
        // maxY. Otherwise cells shift when the span is not a multiple of cellSize.
        double north = minY + height * cellSize;

        for (int row = 0; row < height; row++) {
            double cellY = north - (row + 0.5) * cellSize;
            for (int col = 0; col < width; col++) {
                double cellX = minX + (col + 0.5) * cellSize;
                double[] distances = new double[validSamples.size()];
                for (int i = 0; i < validSamples.size(); i++) {
                    Sample sample = validSamples.get(i);
                    double dx = sample.x() - cellX;
                    double dy = sample.y() - cellY;
                    distances[i] = Math.hypot(dx, dy);
                }
                int[] nearest = nearestIndices(distances, maxNeighbors, bounded
                        ? maxSearchRadius : Double.POSITIVE_INFINITY);
                if (nearest.length == 0) {
                    continue;
                }
                double weighted = 0;
                double weightSum = 0;
                boolean exact = false;
                for (int index : nearest) {
                    double distance = distances[index];
                    if (distance < 1.0e-12) {
                        weighted = validSamples.get(index).value();
                        weightSum = 1;
                        exact = true;
                        break;
                    }
                    double weight = 1.0 / Math.pow(distance, power);
                    weighted += weight * validSamples.get(index).value();
                    weightSum += weight;
                }
                if (exact || weightSum > 0) {
                    values[row * width + col] = (float) (weighted / weightSum);
                }
            }
        }
        return new AnalysisRaster(name == null || name.isBlank() ? "IDW" : name.trim(),
                unit == null ? "" : unit.trim(), width, height, values,
                TerrainRasterAlgorithms.NO_DATA, minX, minY, minX + width * cellSize,
                north, crsCode.trim());
    }

    /** Indices of the nearest samples (unsorted), limited by count and radius. */
    private static int[] nearestIndices(double[] distances, int maxNeighbors, double radius) {
        int limit = Math.min(maxNeighbors, distances.length);
        int[] best = new int[limit];
        double[] bestDistance = new double[limit];
        Arrays.fill(bestDistance, Double.POSITIVE_INFINITY);
        Arrays.fill(best, -1);
        for (int i = 0; i < distances.length; i++) {
            double distance = distances[i];
            if (distance > radius || !Double.isFinite(distance)) {
                continue;
            }
            int worst = worstIndex(bestDistance);
            if (worst >= 0 && distance < bestDistance[worst]) {
                best[worst] = i;
                bestDistance[worst] = distance;
            }
        }
        int count = 0;
        for (int index : best) {
            if (index >= 0) {
                count++;
            }
        }
        int[] result = new int[count];
        int cursor = 0;
        for (int index : best) {
            if (index >= 0) {
                result[cursor++] = index;
            }
        }
        return result;
    }

    private static int worstIndex(double[] distances) {
        int worst = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] > max) {
                max = distances[i];
                worst = i;
            }
        }
        return worst;
    }

    /** Human-readable description for reports. */
    public static String describe(double power, int maxNeighbors, double maxSearchRadius) {
        String radius = maxSearchRadius > 0 && Double.isFinite(maxSearchRadius)
                ? String.format(Locale.ROOT, "%.3f", maxSearchRadius) : "sin limite";
        return "IDW power=" + power + " vecinos=" + maxNeighbors + " radio=" + radius;
    }
}
