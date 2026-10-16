package ar.com.catgis;

import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RasterBandExtractorContractTest {

    @Test
    void ushortExtractionPreservesValueAndTransferType() {
        BufferedImage source = createMultiBand(4, DataBuffer.TYPE_USHORT);
        source.getRaster().setSample(0, 0, 2, 513);

        BufferedImage band = RasterBandExtractor.extractZeroBased(source, 2);

        assertEquals(DataBuffer.TYPE_USHORT, band.getRaster().getDataBuffer().getDataType());
        assertEquals(513.0, band.getRaster().getSampleDouble(0, 0, 0), 0.0);
    }

    @Test
    void floatExtractionPreservesFractionAndNegativeValue() {
        BufferedImage source = createMultiBand(3, DataBuffer.TYPE_FLOAT);
        source.getRaster().setSample(0, 0, 1, 0.5);
        source.getRaster().setSample(0, 0, 2, -2.25);

        assertEquals(0.5, RasterBandExtractor.extractZeroBased(source, 1)
                .getRaster().getSampleDouble(0, 0, 0), 0.0);
        assertEquals(-2.25, RasterBandExtractor.extractZeroBased(source, 2)
                .getRaster().getSampleDouble(0, 0, 0), 0.0);
    }

    @Test
    void oneBasedBandNumbersSelectExactlyTheHumanNamedBand() {
        BufferedImage source = createMultiBand(4, DataBuffer.TYPE_USHORT);
        double[] values = {100, 200, 1000, 3000};
        for (int band = 0; band < values.length; band++) {
            source.getRaster().setSample(0, 0, band, values[band]);
        }

        assertEquals(3000.0, RasterBandExtractor.extractOneBased(source, 4)
                .getRaster().getSampleDouble(0, 0, 0), 0.0);
        assertEquals(1000.0, RasterBandExtractor.extractOneBased(source, 3)
                .getRaster().getSampleDouble(0, 0, 0), 0.0);
    }

    @Test
    void invalidIndicesAreRejectedInsteadOfClamped() {
        BufferedImage source = createMultiBand(3, DataBuffer.TYPE_BYTE);
        assertThrows(IllegalArgumentException.class,
                () -> RasterBandExtractor.extractZeroBased(source, -1));
        assertThrows(IllegalArgumentException.class,
                () -> RasterBandExtractor.extractZeroBased(source, 3));
        assertThrows(IllegalArgumentException.class,
                () -> RasterBandExtractor.extractOneBased(source, 0));
        assertThrows(IllegalArgumentException.class,
                () -> RasterBandExtractor.extractOneBased(source, 4));
    }

    @Test
    void preservedUshortInputsFeedTheExpectedNdviFormula() {
        BufferedImage source = createMultiBand(4, DataBuffer.TYPE_USHORT);
        source.getRaster().setSample(0, 0, 2, 1000);
        source.getRaster().setSample(0, 0, 3, 3000);
        BufferedImage nir = RasterBandExtractor.extractOneBased(source, 4);
        BufferedImage red = RasterBandExtractor.extractOneBased(source, 3);

        assertEquals(0.5, SpectralIndexEngine.computeRaw2(
                nir.getRaster().getSampleDouble(0, 0, 0),
                red.getRaster().getSampleDouble(0, 0, 0), "NDVI"), 1e-12);
    }

    private static BufferedImage createMultiBand(int bands, int dataType) {
        int width = 1;
        int height = 1;
        SampleModel sampleModel = new BandedSampleModel(dataType, width, height, bands);
        WritableRaster raster = Raster.createWritableRaster(sampleModel,
                RasterBandExtractor.createBuffer(dataType, width * height, bands), null);
        boolean hasAlpha = bands == 4;
        ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(bands == 1 ? ColorSpace.CS_GRAY : ColorSpace.CS_sRGB),
                hasAlpha, false, hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
                dataType);
        return new BufferedImage(colorModel, raster, false, null);
    }
}
