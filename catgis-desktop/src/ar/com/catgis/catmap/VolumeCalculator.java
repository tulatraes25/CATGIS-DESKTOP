package ar.com.catgis.catmap;

/**
 * Cut/fill volume computation over analytical rasters: against a reference
 * elevation plane or between an existing and a proposed DEM.
 */
public final class VolumeCalculator {

    /** Volumetric summary for a valid cell set. */
    public record VolumeResult(long validCells, double areaM2,
                               double cutM3, double fillM3, double netM3,
                               double minDeltaM, double maxDeltaM, double meanDeltaM) {}

    private VolumeCalculator() {}

    /** Cut/fill of the DEM against a constant reference elevation. */
    public static VolumeResult againstPlane(AnalysisRaster dem, double baseElevation) {
        require(dem);
        if (!Double.isFinite(baseElevation)) {
            throw new IllegalArgumentException("La cota de referencia debe ser finita.");
        }
        double[] delta = new double[dem.width() * dem.height()];
        boolean[] valid = new boolean[delta.length];
        for (int y = 0; y < dem.height(); y++) {
            for (int x = 0; x < dem.width(); x++) {
                int index = y * dem.width() + x;
                if (dem.isNoData(x, y)) {
                    continue;
                }
                delta[index] = dem.valueAt(x, y) - baseElevation;
                valid[index] = true;
            }
        }
        return summarize(dem, delta, valid, true);
    }

    /** Cut/fill between the existing and the proposed DEM (proposed - existing). */
    public static VolumeResult between(AnalysisRaster existing, AnalysisRaster proposed) {
        require(existing);
        require(proposed);
        requireSameGeometry(existing, proposed);
        double[] delta = differenceValues(existing, proposed);
        boolean[] valid = new boolean[delta.length];
        for (int y = 0; y < existing.height(); y++) {
            for (int x = 0; x < existing.width(); x++) {
                valid[y * existing.width() + x] = !existing.isNoData(x, y)
                        && !proposed.isNoData(x, y);
            }
        }
        return summarize(existing, delta, valid, false);
    }

    /** Delta raster (proposed - existing) preserving NoData; exportable. */
    public static AnalysisRaster differenceRaster(AnalysisRaster existing,
                                                  AnalysisRaster proposed) {
        require(existing);
        require(proposed);
        requireSameGeometry(existing, proposed);
        double[] delta = differenceValues(existing, proposed);
        float[] values = new float[delta.length];
        for (int y = 0; y < existing.height(); y++) {
            for (int x = 0; x < existing.width(); x++) {
                int index = y * existing.width() + x;
                if (existing.isNoData(x, y) || proposed.isNoData(x, y)) {
                    values[index] = TerrainRasterAlgorithms.NO_DATA;
                } else {
                    values[index] = (float) delta[index];
                }
            }
        }
        return new AnalysisRaster("Diferencia de elevacion", "m",
                existing.width(), existing.height(), values,
                TerrainRasterAlgorithms.NO_DATA, existing.minX(), existing.minY(),
                existing.maxX(), existing.maxY(), existing.crsCode());
    }

    private static double[] differenceValues(AnalysisRaster existing, AnalysisRaster proposed) {
        double[] delta = new double[existing.width() * existing.height()];
        for (int y = 0; y < existing.height(); y++) {
            for (int x = 0; x < existing.width(); x++) {
                int index = y * existing.width() + x;
                if (existing.isNoData(x, y) || proposed.isNoData(x, y)) {
                    continue;
                }
                delta[index] = proposed.valueAt(x, y) - existing.valueAt(x, y);
            }
        }
        return delta;
    }

    private static VolumeResult summarize(AnalysisRaster geometry, double[] delta,
                                          boolean[] valid, boolean positiveIsCut) {
        MetricScale.GridScale scale = MetricScale.forRaster(geometry);
        double cellArea = scale.cellAreaM2(geometry.cellSizeX(), geometry.cellSizeY());
        long cells = 0;
        double cut = 0;
        double fill = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        for (int i = 0; i < delta.length; i++) {
            if (!valid[i]) {
                continue;
            }
            cells++;
            double value = delta[i];
            // againstPlane: positive delta = material above the reference = cut.
            // between: positive delta = proposed above existing = material to
            // fill. The flag keeps both conventions explicit and testable.
            boolean isCut = positiveIsCut ? value >= 0 : value < 0;
            if (isCut) {
                cut += Math.abs(value) * cellArea;
            } else {
                fill += Math.abs(value) * cellArea;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
        }
        if (cells == 0) {
            throw new IllegalStateException("No hay celdas validas para calcular volumen.");
        }
        return new VolumeResult(cells, cells * cellArea, cut, fill, cut - fill,
                min, max, sum / cells);
    }

    private static void require(AnalysisRaster raster) {
        if (raster == null) {
            throw new IllegalArgumentException("El volumen requiere un raster de elevacion.");
        }
    }

    private static void requireSameGeometry(AnalysisRaster first, AnalysisRaster second) {
        if (first.width() != second.width() || first.height() != second.height()
                || !first.crsCode().equalsIgnoreCase(second.crsCode())) {
            throw new IllegalArgumentException(
                    "Los rasters de corte/relleno deben compartir grilla y CRS.");
        }
        double toleranceX = Math.max(1.0e-6, Math.abs(first.maxX() - first.minX()) * 1.0e-6);
        double toleranceY = Math.max(1.0e-6, Math.abs(first.maxY() - first.minY()) * 1.0e-6);
        if (Math.abs(first.minX() - second.minX()) > toleranceX
                || Math.abs(first.maxX() - second.maxX()) > toleranceX
                || Math.abs(first.minY() - second.minY()) > toleranceY
                || Math.abs(first.maxY() - second.maxY()) > toleranceY) {
            throw new IllegalArgumentException(
                    "Los rasters de corte/relleno deben compartir extension.");
        }
    }
}
