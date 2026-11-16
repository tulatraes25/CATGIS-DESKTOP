package ar.com.catgis.catmap;

import ar.com.catgis.TerrainHydrologyAnalysisService;

import java.awt.image.BufferedImage;

/**
 * Product viewshed: computes visibility from an observer over an analytical DEM
 * and produces both a float mask raster and a colour overlay for the map.
 */
public final class ViewshedRaster {

    public static final float VISIBLE = 1f;
    public static final float HIDDEN = 0f;

    public record Viewshed(AnalysisRaster mask, int observerX, int observerY, int radiusCells) {}

    private ViewshedRaster() {}

    /**
     * Compute visibility from a cell observer over a DEM grid.
     *
     * @param dem            elevation raster (NoData aware)
     * @param observerX/Y    observer cell
     * @param observerHeight height above ground in metres
     * @param radiusCells    maximum visibility radius in cells
     */
    public static Viewshed compute(AnalysisRaster dem, int observerX, int observerY,
                                   double observerHeight, int radiusCells) {
        if (dem == null) {
            throw new IllegalArgumentException("El viewshed requiere un DEM.");
        }
        if (observerX < 0 || observerX >= dem.width()
                || observerY < 0 || observerY >= dem.height()
                || dem.isNoData(observerX, observerY)) {
            throw new IllegalArgumentException("El observador no es una celda valida del DEM.");
        }
        if (!Double.isFinite(observerHeight) || observerHeight < 0) {
            throw new IllegalArgumentException("La altura del observador debe ser >= 0 m.");
        }
        if (radiusCells <= 0) {
            throw new IllegalArgumentException("El radio del viewshed debe ser positivo.");
        }
        float[] values = dem.copyValues();
        double[] flat = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            flat[i] = dem.isNoData(i % dem.width(), i / dem.width())
                    ? Double.NaN : values[i];
        }
        TerrainHydrologyAnalysisService.ViewshedResult result =
                TerrainHydrologyAnalysisService.computeViewshed(
                        flat, dem.width(), dem.height(), observerX, observerY,
                        observerHeight, radiusCells);
        if (result == null) {
            throw new IllegalStateException("El motor de viewshed no pudo calcular la vista.");
        }
        int width = dem.width();
        int height = dem.height();
        float[] mask = new float[width * height];
        long radiusSquared = (long) radiusCells * radiusCells;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                long dx = x - observerX;
                long dy = y - observerY;
                if (dem.isNoData(x, y)) {
                    mask[index] = TerrainRasterAlgorithms.NO_DATA;
                } else if (dx * dx + dy * dy > radiusSquared) {
                    mask[index] = TerrainRasterAlgorithms.NO_DATA;
                } else {
                    mask[index] = result.visible()[index] ? VISIBLE : HIDDEN;
                }
            }
        }
        AnalysisRaster raster = new AnalysisRaster("Visibilidad", "visible",
                width, height, mask, TerrainRasterAlgorithms.NO_DATA,
                dem.minX(), dem.minY(), dem.maxX(), dem.maxY(), dem.crsCode());
        return new Viewshed(raster, observerX, observerY, radiusCells);
    }

    /** Transparent overlay: visible green, hidden transparent, observer orange. */
    public static BufferedImage overlayImage(Viewshed viewshed) {
        if (viewshed == null) {
            throw new IllegalArgumentException("El viewshed requerido para el overlay.");
        }
        AnalysisRaster mask = viewshed.mask();
        BufferedImage image = new BufferedImage(mask.width(), mask.height(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < mask.height(); y++) {
            for (int x = 0; x < mask.width(); x++) {
                int index = y * mask.width() + x;
                if (x == viewshed.observerX() && y == viewshed.observerY()) {
                    image.setRGB(x, y, 0xFF993300);
                    continue;
                }
                if (mask.isNoData(x, y) || mask.valueAt(x, y) != VISIBLE) {
                    image.setRGB(x, y, 0x00000000);
                } else {
                    image.setRGB(x, y, 0x8C228B22);
                }
            }
        }
        return image;
    }
}
