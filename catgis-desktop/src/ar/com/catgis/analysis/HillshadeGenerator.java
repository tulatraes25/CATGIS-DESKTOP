package ar.com.catgis.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Hillshade generator from DEM rasters.
 * Uses the standard Horn (1981) algorithm for shaded relief.
 */
public final class HillshadeGenerator {

    private HillshadeGenerator() {}

    /**
     * Generate hillshade from a DEM.
     *
     * @param dem        single-band DEM raster (values = elevation)
     * @param azimuth    light direction in degrees (0=N, 90=E, 180=S, 270=W)
     * @param altitude   light height in degrees (0=horizon, 90=zenith)
     * @param cellSize   ground distance per pixel in map units
     * @param zFactor    vertical exaggeration
     * @return grayscale image 0-255, same dimensions as input
     */
    public static BufferedImage generate(BufferedImage dem, double azimuth,
                                          double altitude, double cellSize,
                                          double zFactor) {
        if (dem == null) return null;

        int w = dem.getWidth();
        int h = dem.getHeight();
        if (w < 3 || h < 3) return dem;

        double azRad = Math.toRadians(360 - azimuth + 90);
        double altRad = Math.toRadians(altitude);
        double cosAlt = Math.cos(altRad);
        double sinAlt = Math.sin(altRad);
        double zPerCell = zFactor / cellSize;

        BufferedImage shade = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster out = shade.getRaster();
        double[] pixel = new double[1];

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                double a = dem.getRaster().getSample(x - 1, y - 1, 0);
                double b = dem.getRaster().getSample(x, y - 1, 0);
                double c = dem.getRaster().getSample(x + 1, y - 1, 0);
                double d = dem.getRaster().getSample(x - 1, y, 0);
                double f = dem.getRaster().getSample(x + 1, y, 0);
                double g = dem.getRaster().getSample(x - 1, y + 1, 0);
                double hh = dem.getRaster().getSample(x, y + 1, 0);
                double i = dem.getRaster().getSample(x + 1, y + 1, 0);

                double dzdx = ((c + 2 * f + i) - (a + 2 * d + g)) / (8 * cellSize);
                double dzdy = ((g + 2 * hh + i) - (a + 2 * b + c)) / (8 * cellSize);

                double slope = Math.atan(zPerCell * Math.sqrt(dzdx * dzdx + dzdy * dzdy));
                double aspect = Math.atan2(dzdy, -dzdx);
                if (aspect < 0) aspect += 2 * Math.PI;

                double hs = cosAlt * Math.cos(slope)
                        + sinAlt * Math.sin(slope) * Math.cos(azRad - aspect);
                if (hs < 0) hs = 0;

                pixel[0] = hs * 255;
                out.setPixel(x, y, pixel);
            }
        }
        return shade;
    }
}
