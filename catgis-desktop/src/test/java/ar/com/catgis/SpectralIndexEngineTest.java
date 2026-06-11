package ar.com.catgis;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static org.junit.jupiter.api.Assertions.*;

class SpectralIndexEngineTest {

    @Test
    void computeNDVIReturnsValidRange() {
        BufferedImage red = createConstantImage(10, 10, 50);
        BufferedImage nir = createConstantImage(10, 10, 200);
        BufferedImage result = SpectralIndexEngine.computeNDVI(red, nir);
        assertNotNull(result);
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(5, 5, 0);
        assertTrue(val > 150 && val < 255, "NDVI should be in valid range, got: " + val);
    }

    @Test
    void computeNDVIReturnsNullForNullInputs() {
        assertNull(SpectralIndexEngine.computeNDVI(null, null));
        assertNull(SpectralIndexEngine.computeNDVI(
                new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_GRAY), null));
    }

    @Test
    void allIndicesHaveValidRanges() {
        for (SpectralIndexEngine.SpectralIndex si : SpectralIndexEngine.getIndices()) {
            double[] range = SpectralIndexEngine.getIndexRange(si.id());
            assertNotNull(range);
            assertEquals(2, range.length);
            assertTrue(range[0] < range[1], "Range min must be < max for " + si.id());
        }
    }

    @Test
    void allIndicesHaveColorRampNames() {
        for (SpectralIndexEngine.SpectralIndex si : SpectralIndexEngine.getIndices()) {
            String ramp = SpectralIndexEngine.getColorRampName(si.id());
            assertNotNull(ramp);
            assertFalse(ramp.isEmpty());
        }
    }

    @Test
    void getIndicesReturns12Items() {
        assertEquals(12, SpectralIndexEngine.getIndices().size());
    }

    @Test
    void ndviFormulaCorrect() {
        // NDVI = (NIR - Red) / (NIR + Red)
        // a=NIR=200, b=Red=50 => (200-50)/(200+50) = 150/255 = 0.588
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage result = SpectralIndexEngine.computeNDVI(red, nir);
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (0.588+1)/2*255 = 202
        assertTrue(val > 190 && val < 215, "NDVI(200,50) should be ~202, got: " + val);
    }

    @Test
    void ndwiFormulaCorrect() {
        // NDWI = (Green - NIR) / (Green + NIR)
        // a=Green=200, b=NIR=50 => (200-50)/(200+50) = 0.588
        BufferedImage green = createConstantImage(1, 1, 200);
        BufferedImage nir = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeNDWI(green, nir);
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        assertTrue(val > 190 && val < 215, "NDWI(200,50) should be ~202, got: " + val);
    }

    @Test
    void eviFormulaCorrect() {
        // EVI simplified = 2.5 * (a - b) / (a + 6*b + 1) where a=NIR, b=Red
        // a=NIR=200, b=Red=50 => 2.5*150/(200+300+1) = 375/501 = 0.749
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, red, "EVI");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (0.749+1)/2*255 = 222
        assertTrue(val > 215 && val < 230, "EVI(200,50) should be ~222, got: " + val);
    }

    @Test
    void bsiFormulaCorrect() {
        // BSI = (a - b) / (a + b) where a=SWIR, b=Red
        // a=SWIR=200, b=Red=50 => 150/250 = 0.6
        BufferedImage swir = createConstantImage(1, 1, 200);
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(swir, red, "BSI");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (0.6+1)/2*255 = 204
        assertTrue(val > 190 && val < 215, "BSI(200,50) should be ~204, got: " + val);
    }

    @Test
    void saviFormulaCorrect() {
        // SAVI = ((a-b)/(a+b+0.5)) * 1.5 where a=NIR, b=Red
        // a=NIR=200, b=Red=50 => (150/250.5)*1.5 = 0.898
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, red, "SAVI");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (0.898+1)/2*255 = 242
        assertTrue(val > 235 && val < 255, "SAVI(200,50) should be ~242, got: " + val);
    }

    @Test
    void nbrFormulaCorrect() {
        // NBR = (a - b) / (a + b) where a=NIR, b=SWIR
        // a=NIR=200, b=SWIR=50 => 150/250 = 0.6
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage swir = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, swir, "NBR");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        assertTrue(val > 190 && val < 215, "NBR(200,50) should be ~204, got: " + val);
    }

    @Test
    void ndmiFormulaCorrect() {
        // NDMI = (NIR - SWIR) / (NIR + SWIR) — same as NBR
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage swir = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, swir, "NDMI");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        assertTrue(val > 190 && val < 215, "NDMI(200,50) should be ~204, got: " + val);
    }

    @Test
    void msavi2FormulaCorrect() {
        // MSAVI2 = (2*a+1 - sqrt((2*a+1)^2 - 8*(a-b))) / 2 where a=NIR, b=Red
        // a=NIR=200, b=Red=50 => (401 - sqrt(160801 - 1200)) / 2 = (401-399.5)/2 = 0.75
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, red, "MSAVI2");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (0.75+1)/2*255 = 222
        assertTrue(val > 215 && val < 230, "MSAVI2(200,50) should be ~222, got: " + val);
    }

    @Test
    void tndviFormulaCorrect() {
        // TNDVI = sqrt((a-b)/(a+b) + 0.5) where a=NIR, b=Red
        // a=NIR=200, b=Red=50 => sqrt(0.6+0.5) = sqrt(1.1) = 1.049
        BufferedImage nir = createConstantImage(1, 1, 200);
        BufferedImage red = createConstantImage(1, 1, 50);
        BufferedImage result = SpectralIndexEngine.computeIndex(nir, red, "TNDVI");
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        // Normalized: (1.049+1)/2*255 = 261 -> clamped to 255
        assertTrue(val >= 250, "TNDVI(200,50) should be ~255, got: " + val);
    }

    @Test
    void indexWithZeroValues() {
        BufferedImage zero = createConstantImage(1, 1, 0);
        BufferedImage result = SpectralIndexEngine.computeIndex(zero, zero, "NDVI");
        assertNotNull(result);
        // All zero bands => index = 0 => normalized = 127.5
        WritableRaster raster = result.getRaster();
        double val = raster.getSampleDouble(0, 0, 0);
        assertTrue(val > 120 && val < 135, "Zero inputs should give ~127, got: " + val);
    }

    @Test
    void computeIndexReturnsNullForNullBands() {
        assertNull(SpectralIndexEngine.computeIndex(null, null, "NDVI"));
        assertNull(SpectralIndexEngine.computeIndex(
                new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_GRAY), null, "NDVI"));
    }

    private BufferedImage createConstantImage(int w, int h, int value) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = img.getRaster();
        double[] pixel = {value};
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                raster.setPixel(x, y, pixel);
            }
        }
        return img;
    }
}
