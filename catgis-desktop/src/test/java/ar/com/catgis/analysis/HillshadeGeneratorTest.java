package ar.com.catgis.analysis;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class HillshadeGeneratorTest {

    @Test
    void generateReturnsNullForNullInput() {
        assertNull(HillshadeGenerator.generate(null, 315, 45, 10, 2));
    }

    @Test
    void generateReturnsSameSizeImage() {
        BufferedImage dem = new BufferedImage(10, 10, BufferedImage.TYPE_USHORT_GRAY);
        // Fill with a gentle slope
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                dem.getRaster().setSample(x, y, 0, 100 + x + y);
            }
        }

        BufferedImage shade = HillshadeGenerator.generate(dem, 315, 45, 10, 1);
        assertNotNull(shade);
        assertEquals(10, shade.getWidth());
        assertEquals(10, shade.getHeight());
    }

    @Test
    void generateProducesValidPixelRange() {
        BufferedImage dem = new BufferedImage(20, 20, BufferedImage.TYPE_USHORT_GRAY);
        // Create terrain with clear variation: a ridge running diagonally
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                // Ridge along x=y, elevation drops with distance from ridge
                double dist = Math.abs(x - y);
                dem.getRaster().setSample(x, y, 0, 100 + dist * 10);
            }
        }

        BufferedImage shade = HillshadeGenerator.generate(dem, 315, 45, 1, 1);
        assertNotNull(shade);

        int min = 255, max = 0;
        for (int y = 1; y < 19; y++) {
            for (int x = 1; x < 19; x++) {
                int val = shade.getRaster().getSample(x, y, 0);
                assertTrue(val >= 0 && val <= 255, "Pixel out of range: " + val);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }
        assertTrue(max - min > 10, "Hillshade should have variation, min=" + min + " max=" + max);
    }

    @Test
    void smallImageReturnsInputUnchanged() {
        // 2x2 is too small for Horn algorithm (needs 3x3 neighborhood)
        BufferedImage dem = new BufferedImage(2, 2, BufferedImage.TYPE_USHORT_GRAY);
        BufferedImage result = HillshadeGenerator.generate(dem, 315, 45, 10, 1);
        // Returns input unchanged for tiny images
        assertSame(dem, result);
    }
}
