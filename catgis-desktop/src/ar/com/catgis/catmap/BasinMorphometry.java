package ar.com.catgis.catmap;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Basin morphometry over analytical rasters: delineation upstream of an outlet
 * with the D8 direction grid, plus area, perimeter, elevation/slope means,
 * Strahler order and drainage density.
 */
public final class BasinMorphometry {

    /** Morphometric summary of one basin. */
    public record BasinStats(long cellCount, double areaKm2, double perimeterKm,
                             double minElevationM, double maxElevationM,
                             double meanElevationM, double reliefM,
                             double meanSlopeDegrees, int strahlerOrder,
                             double channelLengthKm, double drainageDensityKmPerKm2) {}

    private BasinMorphometry() {}

    /**
     * Delineate the basin upstream of the outlet and compute its morphometry.
     *
     * @param dem              elevation raster (NoData aware)
     * @param direction        D8 flow direction raster (ESRI codes)
     * @param accumulation     flow accumulation raster (cells)
     * @param slope            slope raster in degrees, or null to derive it
     * @param outletX/outletY  outlet cell
     * @param channelThreshold channel cells are those with accumulation >= threshold
     */
    public static BasinStats compute(AnalysisRaster dem, AnalysisRaster direction,
                                     AnalysisRaster accumulation, AnalysisRaster slope,
                                     int outletX, int outletY, int channelThreshold) {
        if (dem == null || direction == null || accumulation == null) {
            throw new IllegalArgumentException("La morfometria requiere DEM, direccion y acumulacion.");
        }
        if (dem.width() != direction.width() || dem.height() != direction.height()
                || dem.width() != accumulation.width()
                || dem.height() != accumulation.height()) {
            throw new IllegalArgumentException("Los rasters de la cuenca deben compartir geometria.");
        }
        if (!isValidCell(dem, outletX, outletY)) {
            throw new IllegalArgumentException("El punto de salida no es una celda valida del DEM.");
        }
        AnalysisRaster slopeRaster = slope != null ? slope
                : TerrainRasterAlgorithms.slopeDegrees(dem);
        int threshold = Math.max(2, channelThreshold);

        boolean[] basin = delineate(direction, dem, outletX, outletY);
        int width = dem.width();
        int height = dem.height();
        MetricScale.GridScale scale = MetricScale.forRaster(dem);
        double cellAreaM2 = scale.cellAreaM2(dem.cellSizeX(), dem.cellSizeY());
        double cellSizeM = scale.lengthMeters(dem.cellSizeX(), dem.cellSizeY());

        long cells = 0;
        double minElevation = Double.POSITIVE_INFINITY;
        double maxElevation = Double.NEGATIVE_INFINITY;
        double elevationSum = 0;
        double slopeSum = 0;
        long slopeCount = 0;
        double perimeterM = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!basin[y * width + x]) {
                    continue;
                }
                cells++;
                double elevation = dem.valueAt(x, y);
                minElevation = Math.min(minElevation, elevation);
                maxElevation = Math.max(maxElevation, elevation);
                elevationSum += elevation;
                if (!slopeRaster.isNoData(x, y)) {
                    slopeSum += slopeRaster.valueAt(x, y);
                    slopeCount++;
                }
                perimeterM += boundaryLengthMeters(basin, width, height, x, y, dem, scale);
            }
        }
        if (cells == 0) {
            throw new IllegalStateException("La cuenca no tiene celdas validas.");
        }

        double[] channel = channelLengths(direction, accumulation, dem, basin, threshold, scale);
        double areaKm2 = cells * cellAreaM2 / 1.0e6;
        double perimeterKm = perimeterM / 1000.0;
        double channelLengthKm = channel[0] / 1000.0;
        double density = areaKm2 > 0 ? channelLengthKm / areaKm2 : Double.NaN;
        return new BasinStats(
                cells,
                areaKm2,
                perimeterKm,
                minElevation,
                maxElevation,
                elevationSum / cells,
                maxElevation - minElevation,
                slopeCount == 0 ? Double.NaN : slopeSum / slopeCount,
                (int) channel[1],
                channelLengthKm,
                density
        );
    }

    /** True for cells draining to the outlet (outlet included), via reverse D8. */
    static boolean[] delineate(AnalysisRaster direction, AnalysisRaster dem,
                               int outletX, int outletY) {
        int width = dem.width();
        int height = dem.height();
        boolean[] basin = new boolean[width * height];
        Deque<int[]> queue = new ArrayDeque<>();
        basin[outletY * width + outletX] = true;
        queue.add(new int[]{outletX, outletY});
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] codes = {1, 2, 4, 8, 16, 32, 64, 128};
        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            int x = cell[0];
            int y = cell[1];
            for (int i = 0; i < codes.length; i++) {
                int nx = x - dx[i];
                int ny = y - dy[i];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
                int index = ny * width + nx;
                if (basin[index] || !isValidCell(dem, nx, ny)) {
                    continue;
                }
                if (Math.round(direction.valueAt(nx, ny)) == codes[i]) {
                    basin[index] = true;
                    queue.addLast(new int[]{nx, ny});
                }
            }
        }
        return basin;
    }

    /**
     * Channel length (m) and maximum Strahler order inside the basin.
     * Channels are cells with accumulation >= threshold; order is computed
     * upstream-first on the D8 graph restricted to channel cells.
     */
    static double[] channelLengths(AnalysisRaster direction, AnalysisRaster accumulation,
                                   AnalysisRaster dem, boolean[] basin, int threshold,
                                   MetricScale.GridScale scale) {
        int width = dem.width();
        int height = dem.height();
        boolean[] channel = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (basin[index] && !accumulation.isNoData(x, y)
                        && accumulation.valueAt(x, y) >= threshold) {
                    channel[index] = true;
                }
            }
        }
        Integer[] order = new Integer[width * height];
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (channel[y * width + x]) {
                    order[count++] = y * width + x;
                }
            }
        }
        final AnalysisRaster demRef = dem;
        final int widthRef = width;
        Arrays.sort(order, 0, count,
                (a, b) -> Double.compare(
                        demRef.valueAt(b % widthRef, b / widthRef),
                        demRef.valueAt(a % widthRef, a / widthRef)));

        int[] strahler = new int[width * height];
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] codes = {1, 2, 4, 8, 16, 32, 64, 128};
        double lengthMeters = 0;
        int maxOrder = 0;
        for (int i = 0; i < count; i++) {
            int index = order[i];
            int x = index % width;
            int y = index / width;
            int maxUpstream = 0;
            int maxUpstreamCount = 0;
            for (int n = 0; n < codes.length; n++) {
                int nx = x - dx[n];
                int ny = y - dy[n];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
                int upstream = ny * width + nx;
                if (channel[upstream] && Math.round(direction.valueAt(nx, ny)) == codes[n]) {
                    int upstreamOrder = strahler[upstream];
                    if (upstreamOrder > maxUpstream) {
                        maxUpstream = upstreamOrder;
                        maxUpstreamCount = 1;
                    } else if (upstreamOrder == maxUpstream) {
                        maxUpstreamCount++;
                    }
                }
            }
            strahler[index] = maxUpstream == 0 ? 1
                    : (maxUpstreamCount >= 2 ? maxUpstream + 1 : maxUpstream);
            maxOrder = Math.max(maxOrder, strahler[index]);

            int directionCode = Math.round(direction.valueAt(x, y));
            if (directionCode > 0) {
                // ESRI D8: E=1, SE=2, S=4, SW=8, W=16, NW=32, N=64, NE=128.
                double step;
                if (directionCode == 1 || directionCode == 16) {
                    step = Math.abs(dem.cellSizeX()) * scale.metersPerUnitX();
                } else if (directionCode == 4 || directionCode == 64) {
                    step = Math.abs(dem.cellSizeY()) * scale.metersPerUnitY();
                } else {
                    step = scale.lengthMeters(dem.cellSizeX(), dem.cellSizeY());
                }
                lengthMeters += step;
            }
        }
        return new double[]{lengthMeters, maxOrder};
    }

    private static double boundaryLengthMeters(boolean[] basin, int width, int height,
                                               int x, int y, AnalysisRaster dem,
                                               MetricScale.GridScale scale) {
        double length = 0;
        double cellX = Math.abs(dem.cellSizeX()) * scale.metersPerUnitX();
        double cellY = Math.abs(dem.cellSizeY()) * scale.metersPerUnitY();
        if (x == 0 || !basin[y * width + x - 1]) {
            length += cellY;
        }
        if (x == width - 1 || !basin[y * width + x + 1]) {
            length += cellY;
        }
        if (y == 0 || !basin[(y - 1) * width + x]) {
            length += cellX;
        }
        if (y == height - 1 || !basin[(y + 1) * width + x]) {
            length += cellX;
        }
        return length;
    }

    private static boolean isValidCell(AnalysisRaster dem, int x, int y) {
        return x >= 0 && x < dem.width() && y >= 0 && y < dem.height()
                && !dem.isNoData(x, y);
    }

    /** Cells of a basin, exposed for reporting/tests. */
    public static int basinCellCount(AnalysisRaster direction, AnalysisRaster dem,
                                     int outletX, int outletY) {
        return (int) countTrue(delineate(direction, dem, outletX, outletY));
    }

    private static long countTrue(boolean[] values) {
        long count = 0;
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    /** Channel cells of a basin (accumulation >= threshold), for reporting. */
    public static Set<Long> channelCells(AnalysisRaster direction, AnalysisRaster accumulation,
                                         AnalysisRaster dem, int outletX, int outletY,
                                         int channelThreshold) {
        boolean[] basin = delineate(direction, dem, outletX, outletY);
        Set<Long> cells = new LinkedHashSet<>();
        int width = dem.width();
        for (int y = 0; y < dem.height(); y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (basin[index] && !accumulation.isNoData(x, y)
                        && accumulation.valueAt(x, y) >= Math.max(2, channelThreshold)) {
                    cells.add(((long) y << 32) | (x & 0xffffffffL));
                }
            }
        }
        return cells;
    }
}
