package ar.com.catgis;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Objects;

/** Numeric band extraction for analytical raster work. */
final class RasterBandExtractor {
    private RasterBandExtractor() {}

    static BufferedImage extractOneBased(BufferedImage source, int bandNumber) {
        if (bandNumber <= 0) {
            throw new IllegalArgumentException(
                    "Raster band numbers are one-based and must be >= 1.");
        }
        return extractZeroBased(source, bandNumber - 1);
    }

    static BufferedImage extractZeroBased(BufferedImage source, int bandIndex) {
        Objects.requireNonNull(source, "source");
        Raster src = source.getRaster();
        int bands = src.getNumBands();
        if (bandIndex < 0 || bandIndex >= bands) {
            throw new IllegalArgumentException("Raster band " + bandIndex
                    + " is outside the available zero-based range 0.." + (bands - 1) + ".");
        }

        int width = source.getWidth();
        int height = source.getHeight();
        int dataType = src.getSampleModel().getDataType();
        SampleModel sampleModel = new BandedSampleModel(dataType, width, height, 1);
        WritableRaster destination = Raster.createWritableRaster(
                sampleModel, createBuffer(dataType, width * height, 1), null);
        ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
                Transparency.OPAQUE, dataType);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                destination.setSample(x, y, 0, src.getSampleDouble(x, y, bandIndex));
            }
        }
        return new BufferedImage(colorModel, destination, false, null);
    }

    static DataBuffer createBuffer(int dataType, int size, int banks) {
        return switch (dataType) {
            case DataBuffer.TYPE_BYTE -> new DataBufferByte(size, banks);
            case DataBuffer.TYPE_USHORT -> new DataBufferUShort(size, banks);
            case DataBuffer.TYPE_SHORT -> new DataBufferShort(size, banks);
            case DataBuffer.TYPE_INT -> new DataBufferInt(size, banks);
            case DataBuffer.TYPE_FLOAT -> new DataBufferFloat(size, banks);
            case DataBuffer.TYPE_DOUBLE -> new DataBufferDouble(size, banks);
            default -> throw new IllegalArgumentException(
                    "Unsupported raster sample type: " + dataType + ".");
        };
    }
}
