package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * Extracted rendering utilities from MapPanel.
 * Contains static methods for raster display, geometry rendering,
 * and selection flash coordinate resolution.
 */
public final class MapRenderingPipeline {

    private MapRenderingPipeline() {}

    // --- Raster Display ---

    /**
     * Build a display image from raster data applying band selection and auto-contrast.
     */
    public static BufferedImage buildDisplayImage(LocalRasterData data, MapPanel.RasterStyle style) {
        if (data == null || data.getImage() == null) return null;
        BufferedImage src = data.getImage();
        int w = src.getWidth(), h = src.getHeight();
        int bands = Math.min(src.getRaster().getNumBands(), Math.max(1, data.getBandCount()));

        int rb = Math.min(style.redBand, bands - 1);
        int gb = Math.min(style.greenBand, bands - 1);
        int bb = Math.min(style.blueBand, bands - 1);

        if (bands == 1) { rb = 0; gb = 0; bb = 0; }

        double[] mins = new double[bands], maxs = new double[bands];
        if (style.autoContrast) {
            for (int b = 0; b < bands; b++) { mins[b] = Double.POSITIVE_INFINITY; maxs[b] = Double.NEGATIVE_INFINITY; }
            Raster raster = src.getRaster();
            double[] px = new double[bands];
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                raster.getPixel(x, y, px);
                for (int b = 0; b < bands; b++) {
                    double v = px[b];
                    if (!Double.isFinite(v)) {
                        continue;
                    }
                    if (v < mins[b]) mins[b] = v;
                    if (v > maxs[b]) maxs[b] = v;
                }
            }
            for (int b = 0; b < bands; b++) {
                if (!Double.isFinite(mins[b])) mins[b] = 0d;
                if (!Double.isFinite(maxs[b]) || maxs[b] <= mins[b]) maxs[b] = mins[b] + 1d;
            }
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        WritableRaster outRaster = out.getRaster();
        Raster raster = src.getRaster();
        double[] px = new double[bands];

        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            raster.getPixel(x, y, px);
            int r, g, b;
            if (bands == 1) {
                r = g = b = scaleSample(px[0], mins[0], maxs[0], style.autoContrast);
            } else {
                r = scaleSample(px[rb], mins[rb], maxs[rb], style.autoContrast);
                g = scaleSample(px[gb], mins[gb], maxs[gb], style.autoContrast);
                b = scaleSample(px[bb], mins[bb], maxs[bb], style.autoContrast);
            }
            if (style.grayscale) { int gray = (r + g + b) / 3; r = g = b = gray; }
            outRaster.setSample(x, y, 0, r);
            outRaster.setSample(x, y, 1, g);
            outRaster.setSample(x, y, 2, b);
        }
        return out;
    }

    public static int scaleSample(double value, double min, double max, boolean auto) {
        if (!auto) return clampToByte(value);
        if (!Double.isFinite(value)) return 0;
        if (!Double.isFinite(min) || !Double.isFinite(max) || max <= min) return 128;
        return clampToByte((value - min) * 255.0 / (max - min));
    }

    public static int scaleSample(int value, int min, int max, boolean auto) {
        return scaleSample((double) value, (double) min, (double) max, auto);
    }

    private static int clampToByte(double value) {
        if (!Double.isFinite(value)) {
            return 0;
        }
        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }

    // --- Geometry Rendering ---

    public static Path2D buildStarPath(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + r * Math.cos(angle);
            double y = centerY - r * Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        return path;
    }

    public static Coordinate resolveSelectionFlashCoordinate(Geometry geometry) {
        if (geometry == null) return null;
        if (geometry instanceof Point pt) return pt.getCoordinate();
        Geometry interior = geometry.getInteriorPoint();
        if (interior != null) return interior.getCoordinate();
        return geometry.getCentroid().getCoordinate();
    }

    // --- Raster Style Management ---

    public static MapPanel.RasterStyle getOrCreateRasterStyle(Map<Layer, MapPanel.RasterStyle> styles, Layer layer, int bandCount) {
        MapPanel.RasterStyle style = styles.get(layer);
        if (style == null) {
            style = new MapPanel.RasterStyle();
            style.redBand = 0;
            style.greenBand = Math.min(1, bandCount - 1);
            style.blueBand = Math.min(2, bandCount - 1);
            styles.put(layer, style);
        }
        return style;
    }

    public static BufferedImage getCachedDisplayImage(Map<Layer, CachedRasterDisplay> cache, Layer layer,
                                                       LocalRasterData data, MapPanel.RasterStyle style) {
        CachedRasterDisplay cached = cache.get(layer);
        if (cached != null && cached.matches(data, style)) {
            return cached.image();
        }
        BufferedImage image = buildDisplayImage(data, style);
        cache.put(layer, new CachedRasterDisplay(data, style, image));
        return image;
    }

    public record CachedRasterDisplay(LocalRasterData sourceData, MapPanel.RasterStyle style, BufferedImage image) {
        boolean matches(LocalRasterData data, MapPanel.RasterStyle otherStyle) {
            return sourceData == data
                    && style.grayscale == otherStyle.grayscale
                    && style.autoContrast == otherStyle.autoContrast
                    && style.redBand == otherStyle.redBand
                    && style.greenBand == otherStyle.greenBand
                    && style.blueBand == otherStyle.blueBand;
        }
    }
}
