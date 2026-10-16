package ar.com.catgis;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/** Creates analytical single-band rasters without an implicit 8-bit display cast. */
final class RasterNumericImages {
    private RasterNumericImages() {}

    static BufferedImage createDoubleBand(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Raster dimensions must be positive.");
        }
        long pixels = (long) width * height;
        if (pixels > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Raster is too large for an in-memory Java image.");
        }

        SampleModel sampleModel =
                new BandedSampleModel(DataBuffer.TYPE_DOUBLE, width, height, 1);
        WritableRaster raster = Raster.createWritableRaster(
                sampleModel,
                RasterBandExtractor.createBuffer(DataBuffer.TYPE_DOUBLE, (int) pixels, 1),
                null);
        ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                false,
                false,
                Transparency.OPAQUE,
                DataBuffer.TYPE_DOUBLE);
        return new BufferedImage(colorModel, raster, false, null);
    }
}
