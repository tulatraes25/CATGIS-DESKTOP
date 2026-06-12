package ar.com.catgis.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Raster colorization: apply color maps and symbology to single-band rasters.
 */
public final class RasterColorizer {

    private RasterColorizer() {}

    /**
     * A color stop in a gradient.
     */
    public record ColorStop(double value, Color color) {}

    /**
     * Apply a color map to a single-band raster.
     *
     * @param source    source raster (single band, values as doubles)
     * @param stops     ordered color stops (by value, ascending)
     * @param noData    NoData value to leave transparent
     * @return RGBA image with color map applied
     */
    public static BufferedImage applyColorMap(BufferedImage source, List<ColorStop> stops, double noData) {
        if (source == null || stops == null || stops.size() < 2) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        WritableRaster out = result.getRaster();
        java.awt.image.Raster in = source.getRaster();
        double[] pixel = new double[1];
        double[] rgba = new double[4];

        double minVal = stops.get(0).value();
        double maxVal = stops.get(stops.size() - 1).value();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                in.getPixel(x, y, pixel);
                double val = pixel[0];
                if (Double.isNaN(val) || Math.abs(val - noData) < 1e-9) {
                    rgba[0] = 0; rgba[1] = 0; rgba[2] = 0; rgba[3] = 0;
                } else {
                    Color c = interpolateColor(val, stops, minVal, maxVal);
                    rgba[0] = c.getRed();
                    rgba[1] = c.getGreen();
                    rgba[2] = c.getBlue();
                    rgba[3] = c.getAlpha();
                }
                out.setPixel(x, y, rgba);
            }
        }
        return result;
    }

    /**
     * Apply a grayscale color map with a single hue.
     */
    public static BufferedImage applyGrayscale(BufferedImage source, double noData) {
        List<ColorStop> stops = List.of(
                new ColorStop(0, Color.BLACK),
                new ColorStop(255, Color.WHITE)
        );
        return applyColorMap(source, stops, noData);
    }

    /**
     * Apply a terrain color map (green→yellow→brown→white).
     */
    public static BufferedImage applyTerrainColorMap(BufferedImage source, double noData, double minVal, double maxVal) {
        List<ColorStop> stops = List.of(
                new ColorStop(minVal, new Color(34, 139, 34)),    // forest green
                new ColorStop(minVal + (maxVal - minVal) * 0.3, new Color(154, 205, 50)), // yellow-green
                new ColorStop(minVal + (maxVal - minVal) * 0.5, new Color(210, 180, 140)), // tan
                new ColorStop(minVal + (maxVal - minVal) * 0.7, new Color(139, 90, 43)),   // brown
                new ColorStop(maxVal, Color.WHITE)                // snow
        );
        return applyColorMap(source, stops, noData);
    }

    /**
     * Apply a bathymetry color map (blue→cyan→white).
     */
    public static BufferedImage applyBathymetryColorMap(BufferedImage source, double noData, double minVal, double maxVal) {
        List<ColorStop> stops = List.of(
                new ColorStop(minVal, new Color(0, 0, 128)),      // deep blue
                new ColorStop(minVal + (maxVal - minVal) * 0.5, new Color(0, 100, 200)), // medium blue
                new ColorStop(minVal + (maxVal - minVal) * 0.8, new Color(0, 200, 255)), // cyan
                new ColorStop(maxVal, Color.WHITE)                 // shallow
        );
        return applyColorMap(source, stops, noData);
    }

    private static Color interpolateColor(double val, List<ColorStop> stops, double min, double max) {
        if (val <= stops.get(0).value()) return stops.get(0).color();
        if (val >= stops.get(stops.size() - 1).value()) return stops.get(stops.size() - 1).color();

        for (int i = 1; i < stops.size(); i++) {
            ColorStop prev = stops.get(i - 1);
            ColorStop next = stops.get(i);
            if (val <= next.value()) {
                double t = (val - prev.value()) / (next.value() - prev.value());
                return new Color(
                        (int) Math.round(prev.color().getRed() + (next.color().getRed() - prev.color().getRed()) * t),
                        (int) Math.round(prev.color().getGreen() + (next.color().getGreen() - prev.color().getGreen()) * t),
                        (int) Math.round(prev.color().getBlue() + (next.color().getBlue() - prev.color().getBlue()) * t),
                        (int) Math.round(prev.color().getAlpha() + (next.color().getAlpha() - prev.color().getAlpha()) * t)
                );
            }
        }
        return stops.get(stops.size() - 1).color();
    }
}
