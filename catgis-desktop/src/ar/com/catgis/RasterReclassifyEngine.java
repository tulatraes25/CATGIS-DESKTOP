package ar.com.catgis;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public final class RasterReclassifyEngine {

    private RasterReclassifyEngine() {}

    public record ReclassRule(double from, double to, double newValue, String label) {
        public ReclassRule {
            if (!Double.isFinite(from) || !Double.isFinite(to)
                    || !Double.isFinite(newValue)) {
                throw new IllegalArgumentException(
                        "Reclassification rules require finite numeric values.");
            }
            if (from > to) {
                throw new IllegalArgumentException(
                        "Reclassification rule lower bound exceeds upper bound.");
            }
            label = label == null ? "" : label;
        }
    }

    public static BufferedImage reclassify(BufferedImage source,
                                           List<ReclassRule> rules,
                                           double noDataValue,
                                           boolean useNodata) {
        if (source == null || rules == null || rules.isEmpty()) {
            return source;
        }
        validateRules(rules);

        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = RasterNumericImages.createDoubleBand(width, height);
        Raster input = source.getRaster();
        WritableRaster output = result.getRaster();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = input.getSampleDouble(x, y, 0);
                double classified = noDataValue;

                if (Double.isFinite(value)
                        && !(useNodata && isNoDataSample(value, noDataValue))) {
                    for (ReclassRule rule : rules) {
                        // Backward-compatible deterministic policy:
                        // both endpoints are inclusive and the first matching rule wins.
                        if (value >= rule.from() && value <= rule.to()) {
                            classified = rule.newValue();
                            break;
                        }
                    }
                }
                output.setSample(x, y, 0, classified);
            }
        }
        return result;
    }

    public static double[] computeStatistics(BufferedImage source,
                                             double noData,
                                             boolean skipNodata) {
        if (source == null) {
            throw new IllegalArgumentException("Source raster is required.");
        }

        Raster raster = source.getRaster();
        long count = 0L;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double mean = 0.0;
        double m2 = 0.0;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                double value = raster.getSampleDouble(x, y, 0);
                if (!Double.isFinite(value)
                        || (skipNodata && isNoDataSample(value, noData))) {
                    continue;
                }

                count++;
                min = Math.min(min, value);
                max = Math.max(max, value);
                double delta = value - mean;
                mean += delta / count;
                double delta2 = value - mean;
                m2 += delta * delta2;
            }
        }

        if (count == 0L) {
            return new double[]{
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.0
            };
        }
        double stddev = count > 1L ? Math.sqrt(m2 / (count - 1L)) : 0.0;
        return new double[]{min, max, mean, stddev, (double) count};
    }

    public static List<ReclassRule> equalIntervalBreaks(double min,
                                                         double max,
                                                         int classes) {
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            throw new IllegalArgumentException("Class bounds must be finite.");
        }
        if (classes <= 0) {
            throw new IllegalArgumentException("Class count must be positive.");
        }
        if (max < min) {
            throw new IllegalArgumentException("Maximum must be >= minimum.");
        }
        if (max == min && classes != 1) {
            throw new IllegalArgumentException(
                    "A zero-width range can only produce one class.");
        }

        List<ReclassRule> rules = new ArrayList<>(classes);
        double step = classes == 1 ? 0.0 : (max - min) / classes;
        for (int index = 0; index < classes; index++) {
            double from = min + index * step;
            double to = index == classes - 1 ? max : min + (index + 1) * step;
            rules.add(new ReclassRule(
                    from, to, index + 1, "Class " + (index + 1)));
        }
        return List.copyOf(rules);
    }

    /**
     * Fill NoData gaps in a single-band raster using iterative mean interpolation.
     * Integer inputs are promoted to a double analytical raster if interpolation
     * is required so fractional means cannot be silently truncated.
     */
    public static BufferedImage fillNodata(BufferedImage source, double noDataValue) {
        return fillNodata(source, noDataValue, 1);
    }

    public static BufferedImage fillNodata(BufferedImage source,
                                           double noDataValue,
                                           int minValidNeighbors) {
        if (source == null) {
            return null;
        }
        if (minValidNeighbors < 1 || minValidNeighbors > 8) {
            throw new IllegalArgumentException(
                    "minValidNeighbors must be between 1 and 8.");
        }

        Raster sourceRaster = source.getRaster();
        int width = source.getWidth();
        int height = source.getHeight();
        boolean hasGap = false;
        for (int y = 0; y < height && !hasGap; y++) {
            for (int x = 0; x < width; x++) {
                if (isNoDataSample(
                        sourceRaster.getSampleDouble(x, y, 0), noDataValue)) {
                    hasGap = true;
                    break;
                }
            }
        }
        if (!hasGap) {
            return source;
        }

        BufferedImage work = RasterNumericImages.createDoubleBand(width, height);
        WritableRaster workRaster = work.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                workRaster.setSample(
                        x, y, 0, sourceRaster.getSampleDouble(x, y, 0));
            }
        }

        boolean changed;
        int passes = 0;
        int maxPasses = Math.max(width, height);
        do {
            changed = false;
            passes++;
            double[][] snapshot = new double[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    snapshot[y][x] = workRaster.getSampleDouble(x, y, 0);
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!isNoDataSample(snapshot[y][x], noDataValue)) {
                        continue;
                    }
                    double sum = 0.0;
                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) {
                                continue;
                            }
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                                continue;
                            }
                            double neighbor = snapshot[ny][nx];
                            if (!isNoDataSample(neighbor, noDataValue)) {
                                sum += neighbor;
                                count++;
                            }
                        }
                    }
                    if (count >= minValidNeighbors) {
                        workRaster.setSample(x, y, 0, sum / count);
                        changed = true;
                    }
                }
            }
        } while (changed && passes < maxPasses);

        return work;
    }

    static boolean isNoDataSample(double value, double noData) {
        if (!Double.isFinite(value)) {
            return true;
        }
        if (Double.isNaN(noData)) {
            return false;
        }
        if (!Double.isFinite(noData)) {
            return false;
        }
        return Double.compare(value, noData) == 0;
    }

    private static void validateRules(List<ReclassRule> rules) {
        for (ReclassRule rule : rules) {
            if (rule == null) {
                throw new IllegalArgumentException(
                        "Reclassification rules cannot contain null entries.");
            }
        }
    }
}
