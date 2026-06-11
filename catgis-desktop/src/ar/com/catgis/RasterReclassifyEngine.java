package ar.com.catgis;

import java.awt.image.*;
import java.util.*;

public final class RasterReclassifyEngine {

    private RasterReclassifyEngine() {}

    public record ReclassRule(double from, double to, double newValue, String label) {}

    public static BufferedImage reclassify(BufferedImage source, List<ReclassRule> rules, double noDataValue, boolean useNodata) {
        if (source == null || rules == null || rules.isEmpty()) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Raster raster = source.getRaster();
        WritableRaster out = result.getRaster();
        double[] pixel = new double[raster.getNumBands()];
        double[] outPixel = new double[1];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                raster.getPixel(x, y, pixel);
                double val = pixel[0];
                if (useNodata && Math.abs(val - noDataValue) < 1e-9) {
                    outPixel[0] = noDataValue;
                } else {
                    boolean matched = false;
                    for (ReclassRule rule : rules) {
                        if (val >= rule.from() && val <= rule.to()) {
                            outPixel[0] = rule.newValue();
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) outPixel[0] = noDataValue;
                }
                out.setSample(x, y, 0, outPixel[0]);
            }
        }
        return result;
    }

    public static double[] computeStatistics(BufferedImage source, double noData, boolean skipNodata) {
        int w = source.getWidth(), h = source.getHeight();
        Raster raster = source.getRaster();
        double[] pixel = new double[raster.getNumBands()];
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, sum = 0;
        int count = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                raster.getPixel(x, y, pixel);
                double v = pixel[0];
                if (skipNodata && Math.abs(v - noData) < 1e-9) continue;
                if (v < min) min = v;
                if (v > max) max = v;
                sum += v;
                count++;
            }
        }
        double mean = count > 0 ? sum / count : 0;
        double sumSq = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                raster.getPixel(x, y, pixel);
                double v = pixel[0];
                if (skipNodata && Math.abs(v - noData) < 1e-9) continue;
                sumSq += (v - mean) * (v - mean);
            }
        }
        double stddev = count > 1 ? Math.sqrt(sumSq / (count - 1)) : 0;
        return new double[]{min, max, mean, stddev, (double) count};
    }

    public static List<ReclassRule> equalIntervalBreaks(double min, double max, int classes) {
        List<ReclassRule> rules = new ArrayList<>();
        double step = (max - min) / classes;
        for (int i = 0; i < classes; i++) {
            double from = min + i * step;
            double to = i == classes - 1 ? max : min + (i + 1) * step;
            rules.add(new ReclassRule(from, to, i + 1, "Class " + (i + 1)));
        }
        return rules;
    }
}
