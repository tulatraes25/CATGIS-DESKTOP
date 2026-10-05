package ar.com.catgis;

import java.awt.image.*;
import java.util.*;

public final class ZonalStatisticsEngine {

    private ZonalStatisticsEngine() {}

    public record ZoneStats(double min, double max, double mean, double sum, double stddev, int count, int zoneId) {}

    public static Map<Integer, ZoneStats> computeZonalStats(BufferedImage valueRaster, double[][] zoneGrid, int numZones, double noDataValue) {
        Map<Integer, List<Double>> zoneValues = new LinkedHashMap<>();
        for (int z = 1; z <= numZones; z++) zoneValues.put(z, new ArrayList<>());

        int w = valueRaster.getWidth();
        int h = valueRaster.getHeight();
        int gridH = zoneGrid.length;
        int gridW = gridH > 0 ? zoneGrid[0].length : 0;
        double scaleX = gridW > 0 ? (double) w / gridW : 1.0;
        double scaleY = gridH > 0 ? (double) h / gridH : 1.0;

        Raster raster = valueRaster.getRaster();
        double[] pixel = new double[raster.getNumBands()];

        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                int zone = (int) zoneGrid[gy][gx];
                if (zone <= 0 || zone > numZones) continue;
                int px = (int) (gx * scaleX);
                int py = (int) (gy * scaleY);
                if (px >= w || py >= h) continue;
                raster.getPixel(px, py, pixel);
                double val = pixel[0];
                if (Math.abs(val - noDataValue) < 1e-9) continue;
                zoneValues.get(zone).add(val);
            }
        }

        Map<Integer, ZoneStats> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Double>> entry : zoneValues.entrySet()) {
            List<Double> vals = entry.getValue();
            if (vals.isEmpty()) continue;
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, sum = 0;
            for (double v : vals) {
                if (v < min) min = v;
                if (v > max) max = v;
                sum += v;
            }
            double mean = sum / vals.size();
            double sumSq = 0;
            for (double v : vals) sumSq += (v - mean) * (v - mean);
            double stddev = vals.size() > 1 ? Math.sqrt(sumSq / (vals.size() - 1)) : 0;
            result.put(entry.getKey(), new ZoneStats(min, max, mean, sum, stddev, vals.size(), entry.getKey()));
        }
        return result;
    }
}
