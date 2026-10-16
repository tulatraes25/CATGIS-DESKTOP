import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/** Isolated characterization of the existing helper, NOT a full CATGIS test. */
public final class BatchRasterPrecisionProbe {
    // Body copied from BatchProcessor blob 4c231ffb567af7f7b3fdb4e8800acce1094e9c05.
    private static java.awt.image.BufferedImage extractBand(java.awt.image.BufferedImage composite, int bandIndex) {
        int w = composite.getWidth();
        int h = composite.getHeight();
        java.awt.image.BufferedImage band = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        java.awt.image.Raster src = composite.getRaster();
        java.awt.image.WritableRaster dst = band.getRaster();
        double[] pixel = new double[src.getNumBands()];
        double[] out = new double[1];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                src.getPixel(x, y, pixel);
                int idx = Math.min(bandIndex, pixel.length - 1);
                out[0] = pixel[idx];
                dst.setPixel(x, y, out);
            }
        }
        return band;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        BufferedImage u16 = new BufferedImage(1, 1, BufferedImage.TYPE_USHORT_GRAY);
        u16.getRaster().setSample(0, 0, 0, 513);
        double original16 = u16.getRaster().getSampleDouble(0, 0, 0);
        double actual16 = extractBand(u16, 0).getRaster().getSampleDouble(0, 0, 0);
        require(original16 == 513 && actual16 != original16, "Expected existing 16-bit loss not reproduced");
        System.out.println("REPRODUCED ushort sample " + original16 + " -> " + actual16);

        var sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, 1, 1, 1);
        WritableRaster raster = Raster.createWritableRaster(sampleModel,
                new DataBufferFloat(new float[]{0.5f}, 1), new Point(0, 0));
        var colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        BufferedImage f32 = new BufferedImage(colorModel, raster, false, null);
        double originalFloat = f32.getRaster().getSampleDouble(0, 0, 0);
        double actualFloat = extractBand(f32, 0).getRaster().getSampleDouble(0, 0, 0);
        require(originalFloat == 0.5 && actualFloat != originalFloat, "Expected existing fractional loss not reproduced");
        System.out.println("REPRODUCED float sample " + originalFloat + " -> " + actualFloat);

        BufferedImage three = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        three.getRaster().setPixel(0, 0, new int[]{11, 22, 33});
        double invalidBand = extractBand(three, 99).getRaster().getSampleDouble(0, 0, 0);
        require(invalidBand == 33, "Expected existing last-band substitution not reproduced");
        System.out.println("REPRODUCED invalid band 99 with 3 bands -> last-band value " + invalidBand);

        boolean negativeRuleRefused = false;
        try {
            // Exact leading parse operations from existing applyReclassify.
            String[] parts = "-5--1=7".split("-");
            Double.parseDouble(parts[0]);
        } catch (NumberFormatException expected) {
            negativeRuleRefused = true;
        }
        require(negativeRuleRefused, "Expected negative-bound parser defect not reproduced");
        System.out.println("REPRODUCED negative interval -5--1=7 -> NumberFormatException");
        System.out.println("STATUS=FOUR_CASES_THREE_DEFECT_CLASSES_REPRODUCED; NOT_A_FIX; NOT_FULL_CATGIS_CERTIFICATION");
    }
}
