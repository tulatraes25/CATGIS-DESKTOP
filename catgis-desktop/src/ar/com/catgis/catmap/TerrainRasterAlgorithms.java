package ar.com.catgis.catmap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Terrain analysis algorithms over {@link AnalysisRaster} DEM values: slope and
 * aspect (Horn), hillshade, D8 flow direction and flow accumulation. Results
 * stay in floating point with the DEM NoData contract.
 */
public final class TerrainRasterAlgorithms {

    public static final float NO_DATA = -9999f;
    public static final String UNIT_DEGREES = "degrees";
    public static final String UNIT_FRACTION = "fraction";
    public static final String UNIT_CODE = "d8-code";
    public static final String UNIT_CELLS = "cells";

    private TerrainRasterAlgorithms() {}

    public static AnalysisRaster slopeDegrees(AnalysisRaster dem) {
        AnalysisRaster out = dem.emptyLike("Pendiente", UNIT_DEGREES);
        float[] target = out.copyValues();
        for (int y = 1; y < dem.height() - 1; y++) {
            for (int x = 1; x < dem.width() - 1; x++) {
                double[] gradient = gradient(dem, x, y);
                if (gradient == null) {
                    continue;
                }
                double slope = Math.toDegrees(Math.atan(Math.hypot(gradient[0], gradient[1])));
                target[y * dem.width() + x] = (float) slope;
            }
        }
        return rebuild(out, target);
    }

    public static AnalysisRaster aspectDegrees(AnalysisRaster dem) {
        AnalysisRaster out = dem.emptyLike("Aspecto", UNIT_DEGREES);
        float[] target = out.copyValues();
        for (int y = 1; y < dem.height() - 1; y++) {
            for (int x = 1; x < dem.width() - 1; x++) {
                double[] gradient = gradient(dem, x, y);
                if (gradient == null) {
                    continue;
                }
                double east = gradient[0];
                double north = -gradient[1];
                if (Math.abs(east) < 1.0e-12 && Math.abs(north) < 1.0e-12) {
                    continue; // flat: NoData aspect
                }
                double azimuth = Math.toDegrees(Math.atan2(-east, -north));
                if (azimuth < 0) {
                    azimuth += 360.0;
                }
                target[y * dem.width() + x] = (float) azimuth;
            }
        }
        return rebuild(out, target);
    }

    public static AnalysisRaster hillshade(AnalysisRaster dem) {
        return hillshade(dem, 315.0, 45.0);
    }

    public static AnalysisRaster hillshade(AnalysisRaster dem, double azimuthDeg,
                                           double altitudeDeg) {
        if (!Double.isFinite(azimuthDeg) || !Double.isFinite(altitudeDeg)) {
            throw new IllegalArgumentException("hillshade requires finite azimuth/altitude.");
        }
        double zenith = Math.toRadians(90.0 - altitudeDeg);
        double azimuthMath = Math.toRadians(360.0 - azimuthDeg + 90.0);
        AnalysisRaster out = dem.emptyLike("Hillshade", UNIT_FRACTION);
        float[] target = out.copyValues();
        for (int y = 1; y < dem.height() - 1; y++) {
            for (int x = 1; x < dem.width() - 1; x++) {
                double[] gradient = gradient(dem, x, y);
                if (gradient == null) {
                    continue;
                }
                double slopeRad = Math.atan(Math.hypot(gradient[0], gradient[1]));
                double east = gradient[0];
                double north = -gradient[1];
                double aspectMath = Math.atan2(east, north);
                double shade = Math.cos(zenith) * Math.cos(slopeRad)
                        + Math.sin(zenith) * Math.sin(slopeRad)
                        * Math.cos(azimuthMath - aspectMath);
                target[y * dem.width() + x] = (float) Math.max(0.0, Math.min(1.0, shade));
            }
        }
        return rebuild(out, target);
    }

    /** ESRI D8 codes: E=1, SE=2, S=4, SW=8, W=16, NW=32, N=64, NE=128. */
    public static AnalysisRaster flowDirection(AnalysisRaster dem) {
        AnalysisRaster out = dem.emptyLike("Direccion de flujo", UNIT_CODE);
        float[] target = out.copyValues();
        for (int y = 0; y < dem.height(); y++) {
            for (int x = 0; x < dem.width(); x++) {
                if (dem.isNoData(x, y)) {
                    continue;
                }
                int code = steepestDescent(dem, x, y);
                target[y * dem.width() + x] = code;
            }
        }
        return rebuild(out, target);
    }

    /** Number of cells draining into each cell (including itself). */
    public static AnalysisRaster flowAccumulation(AnalysisRaster dem) {
        final int width = dem.width();
        final int height = dem.height();
        float[] accumulation = new float[width * height];
        Arrays.fill(accumulation, dem.noData());
        Integer[] order = new Integer[width * height];
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dem.isNoData(x, y)) {
                    continue;
                }
                accumulation[y * width + x] = 1f;
                order[count++] = y * width + x;
            }
        }
        final AnalysisRaster demRef = dem;
        Arrays.sort(order, 0, count, Comparator.comparingDouble(
                (Integer cell) -> demRef.valueAt(cell % width, cell / width)).reversed());
        for (int i = 0; i < count; i++) {
            int cell = order[i];
            int x = cell % width;
            int y = cell / width;
            int direction = steepestDescent(dem, x, y);
            if (direction == 0) {
                continue;
            }
            int nx = x;
            int ny = y;
            switch (direction) {
                case 1 -> nx += 1;
                case 2 -> { nx += 1; ny += 1; }
                case 4 -> ny += 1;
                case 8 -> { nx -= 1; ny += 1; }
                case 16 -> nx -= 1;
                case 32 -> { nx -= 1; ny -= 1; }
                case 64 -> ny -= 1;
                case 128 -> { nx += 1; ny -= 1; }
                default -> { continue; }
            }
            if (nx < 0 || nx >= width || ny < 0 || ny >= height || dem.isNoData(nx, ny)) {
                continue;
            }
            accumulation[ny * width + nx] += accumulation[y * width + x];
        }
        AnalysisRaster out = dem.emptyLike("Acumulacion de flujo", UNIT_CELLS);
        return rebuild(out, accumulation);
    }

    private static int steepestDescent(AnalysisRaster dem, int x, int y) {
        double center = dem.valueAt(x, y);
        double cellX = dem.cellSizeX();
        double cellY = dem.cellSizeY();
        double diagonal = Math.hypot(cellX, cellY);
        int[] codes = {1, 2, 4, 8, 16, 32, 64, 128};
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        double bestDrop = 0.0;
        int bestCode = 0;
        for (int i = 0; i < codes.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx < 0 || nx >= dem.width() || ny < 0 || ny >= dem.height()
                    || dem.isNoData(nx, ny)) {
                continue;
            }
            double distance = (dx[i] == 0 || dy[i] == 0) ? Math.min(cellX, cellY) : diagonal;
            double drop = (center - dem.valueAt(nx, ny)) / distance;
            if (drop > bestDrop + 1.0e-12) {
                bestDrop = drop;
                bestCode = codes[i];
            }
        }
        return bestCode;
    }

    /** East/north gradient at an interior cell, or null when it touches NoData. */
    private static double[] gradient(AnalysisRaster dem, int x, int y) {
        if (dem.isNoData(x, y)) {
            return null;
        }
        double a = safe(dem, x - 1, y - 1);
        double b = safe(dem, x, y - 1);
        double c = safe(dem, x + 1, y - 1);
        double d = safe(dem, x - 1, y);
        double f = safe(dem, x + 1, y);
        double g = safe(dem, x - 1, y + 1);
        double h = safe(dem, x, y + 1);
        double i = safe(dem, x + 1, y + 1);
        if (Double.isNaN(a + b + c + d + f + g + h + i)) {
            return null;
        }
        double cellX = dem.cellSizeX();
        double cellY = dem.cellSizeY();
        double east = ((c + 2 * f + i) - (a + 2 * d + g)) / (8.0 * cellX);
        double south = ((g + 2 * h + i) - (a + 2 * b + c)) / (8.0 * cellY);
        return new double[]{east, south};
    }

    private static double safe(AnalysisRaster dem, int x, int y) {
        return dem.isNoData(x, y) ? Double.NaN : dem.valueAt(x, y);
    }

    private static AnalysisRaster rebuild(AnalysisRaster template, float[] values) {
        return new AnalysisRaster(template.name(), template.unit(), template.width(),
                template.height(), values, template.noData(), template.minX(),
                template.minY(), template.maxX(), template.maxY(), template.crsCode());
    }

    static String formatCell(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
