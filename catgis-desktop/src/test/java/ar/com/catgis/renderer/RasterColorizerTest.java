package ar.com.catgis.renderer;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RasterColorizerTest {

    @Test
    void applyColorMap_basic() {
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        src.getRaster().setSample(0, 0, 0, 50);
        src.getRaster().setSample(1, 0, 0, 150);
        src.getRaster().setSample(0, 1, 0, 0); // NoData = 0
        src.getRaster().setSample(1, 1, 0, 200);

        List<RasterColorizer.ColorStop> stops = List.of(
                new RasterColorizer.ColorStop(1, Color.RED),
                new RasterColorizer.ColorStop(255, Color.BLUE)
        );
        BufferedImage result = RasterColorizer.applyColorMap(src, stops, 0);
        assertNotNull(result);

        // NoData pixel (0) should be transparent
        int[] rgba = new int[4];
        result.getRaster().getPixel(0, 1, rgba);
        assertEquals(0, rgba[3]); // alpha = 0

        // Valid pixel should be visible
        result.getRaster().getPixel(0, 0, rgba);
        assertTrue(rgba[3] > 0);
    }

    @Test
    void applyTerrainColorMap() {
        BufferedImage src = new BufferedImage(3, 1, BufferedImage.TYPE_BYTE_GRAY);
        src.getRaster().setSample(0, 0, 0, 0);
        src.getRaster().setSample(1, 0, 0, 128);
        src.getRaster().setSample(2, 0, 0, 255);

        BufferedImage result = RasterColorizer.applyTerrainColorMap(src, -9999, 0, 255);
        assertNotNull(result);
        assertEquals(3, result.getWidth());
        // All pixels should be non-transparent
        int[] rgba = new int[4];
        result.getRaster().getPixel(0, 0, rgba);
        assertTrue(rgba[3] > 0);
    }

    @Test
    void applyGrayscale() {
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < 4; i++) src.getRaster().setSample(i % 2, i / 2, 0, 128);
        BufferedImage result = RasterColorizer.applyGrayscale(src, -9999);
        assertNotNull(result);
    }

    @Test
    void applyBathymetryColorMap() {
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < 4; i++) src.getRaster().setSample(i % 2, i / 2, 0, 100);
        BufferedImage result = RasterColorizer.applyBathymetryColorMap(src, -9999, 0, 200);
        assertNotNull(result);
    }
}
