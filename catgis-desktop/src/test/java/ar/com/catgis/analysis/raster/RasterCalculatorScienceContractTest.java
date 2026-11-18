package ar.com.catgis.analysis.raster;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T2/#256: scientific raster algebra keeps float values, NoData and CRS. */
class RasterCalculatorScienceContractTest {

    private static final double MINX = 1000;
    private static final double MINY = 2000;

    private static BufferedImage gray(int width, int height, int value) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.setSample(x, y, 0, value);
            }
        }
        return image;
    }

    private static RasterCalculatorEngine.RasterSource source(String name, BufferedImage image,
                                                              int w, int h) {
        return new RasterCalculatorEngine.RasterSource(image,
                new Envelope(MINX, MINX + w, MINY, MINY + h), name);
    }

    @Test
    void preservesFractionalAndNegativeResults() {
        var a = source("a", gray(4, 3, 800), 4, 3);
        var b = source("b", gray(4, 3, 200), 4, 3);
        var ratio = RasterCalculatorEngine.evaluateAnalysis(List.of(a, b),
                "(a-b)/(a+b)", "EPSG:32719", null);
        assertEquals(0.6, ratio.valueAt(0, 0), 1.0e-6, "must not clamp to a byte");
        assertEquals("EPSG:32719", ratio.crsCode());
        assertEquals(MINX, ratio.minX(), 1.0e-9);
        assertEquals(MINY + 3.0, ratio.maxY(), 1.0e-9);

        var diff = RasterCalculatorEngine.evaluateAnalysis(List.of(a, b),
                "a-b-1000", "EPSG:32719", null);
        assertEquals(-400.0, diff.valueAt(1, 1), 1.0e-6, "negative values must survive");
    }

    @Test
    void domainErrorsBecomeNoDataAndPreviewTransparent() {
        var a = source("a", gray(2, 2, 800), 2, 2);
        var result = RasterCalculatorEngine.evaluateAnalysis(List.of(a),
                "log(a-800)", "EPSG:32719", null);
        assertTrue(result.isNoData(0, 0), "log(0) must be NoData, not silent zero");
        assertEquals(4, result.stats().noDataCount());
        BufferedImage preview = RasterCalculatorEngine.toPreviewImage(result);
        assertEquals(0, preview.getRGB(0, 0) >>> 24, "NoData preview must be transparent");
    }

    @Test
    void mismatchedGridsFailClosedInsteadOfSilentIntersection() {
        var a = source("a", gray(4, 4, 10), 4, 4);
        var b = source("b", gray(3, 4, 10), 3, 4);
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> RasterCalculatorEngine.evaluateAnalysis(List.of(a, b),
                        "a+b", "EPSG:32719", null));
        assertTrue(failure.getMessage().contains("grilla"), failure.getMessage());
    }

    @Test
    void missingCrsIsRejected() {
        var a = source("a", gray(2, 2, 10), 2, 2);
        assertThrows(IllegalArgumentException.class,
                () -> RasterCalculatorEngine.evaluateAnalysis(List.of(a), "a", " ", null));
        assertThrows(IllegalArgumentException.class,
                () -> RasterCalculatorEngine.evaluateAnalysis(List.of(a), " ", "EPSG:32719", null));
    }

    @Test
    void legacyPreviewStillClampsButScientificPathDoesNot() {
        var a = source("a", gray(2, 2, 800), 2, 2);
        BufferedImage legacy = RasterCalculatorEngine.evaluate(List.of(a), "a");
        assertNotNull(legacy);
        assertEquals(255, legacy.getRaster().getSample(0, 0, 0),
                "legacy 8-bit preview keeps its documented clamping");
        var scientific = RasterCalculatorEngine.evaluateAnalysis(List.of(a),
                "a", "EPSG:32719", null);
        assertEquals(800.0, scientific.valueAt(0, 0), 1.0e-6);
    }
}
