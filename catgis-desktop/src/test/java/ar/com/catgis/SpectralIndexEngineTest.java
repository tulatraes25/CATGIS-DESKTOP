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
        // NDVI = (200-50)/(200+50) = 0.6, normalized to byte: (0.6+1)/2*255 = 204
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
