package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class RasterReclassifyEngineTest {

    @Test
    void reclassifyReturnsSourceWhenSourceIsNull() {
        assertNull(RasterReclassifyEngine.reclassify(null, List.of(), -1, false));
    }

    @Test
    void reclassifyReturnsSourceWhenRulesAreNull() {
        BufferedImage image = createByteRaster(2, 2, 5);
        assertSame(image, RasterReclassifyEngine.reclassify(image, null, -1, false));
    }

    @Test
    void reclassifyReturnsSourceWhenRulesAreEmpty() {
        BufferedImage image = createByteRaster(2, 2, 5);
        assertSame(image, RasterReclassifyEngine.reclassify(image, List.of(), -1, false));
    }

    @Test
    void reclassifyMapsValuesToNewClasses() {
        BufferedImage source = createByteRaster(3, 1, 10, 50, 90);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "Low"),
                new RasterReclassifyEngine.ReclassRule(20, 70, 2, "Mid"),
                new RasterReclassifyEngine.ReclassRule(70, 100, 3, "High"));

        BufferedImage result =
                RasterReclassifyEngine.reclassify(source, rules, -1, false);

        assertNotNull(result);
        assertEquals(DataBuffer.TYPE_DOUBLE,
                result.getRaster().getDataBuffer().getDataType());
        assertEquals(1.0, readPixel(result, 0, 0), 0.0);
        assertEquals(2.0, readPixel(result, 1, 0), 0.0);
        assertEquals(3.0, readPixel(result, 2, 0), 0.0);
    }

    @Test
    void reclassifyPreservesSignedFractionalAndLargeClassValues() {
        BufferedImage source = createByteRaster(3, 1, 5, 15, 25);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 10, -5.5, "negative"),
                new RasterReclassifyEngine.ReclassRule(11, 20, 300.25, "fraction"),
                new RasterReclassifyEngine.ReclassRule(21, 30, 1000, "large"));

        BufferedImage result =
                RasterReclassifyEngine.reclassify(source, rules, -9999, false);

        assertEquals(-5.5, readPixel(result, 0, 0), 0.0);
        assertEquals(300.25, readPixel(result, 1, 0), 0.0);
        assertEquals(1000.0, readPixel(result, 2, 0), 0.0);
    }

    @Test
    void firstInclusiveRuleWinsAtAnOverlappingBoundary() {
        BufferedImage source = createByteRaster(1, 1, 20);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "first"),
                new RasterReclassifyEngine.ReclassRule(20, 40, 2, "second"));

        BufferedImage result =
                RasterReclassifyEngine.reclassify(source, rules, -1, false);

        assertEquals(1.0, readPixel(result, 0, 0), 0.0);
    }

    @Test
    void unmatchedValuesGetDeclaredNoDataWithoutByteClipping() {
        BufferedImage source = createByteRaster(1, 1, 250);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 10, 1, "A"));

        BufferedImage result =
                RasterReclassifyEngine.reclassify(source, rules, -9999.5, false);

        assertEquals(-9999.5, readPixel(result, 0, 0), 0.0);
    }

    @Test
    void reclassifyPreservesFiniteNoDataWhenRequested() {
        BufferedImage source = createByteRaster(2, 1, 200, 15);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "Low"));

        BufferedImage result =
                RasterReclassifyEngine.reclassify(source, rules, 200, true);

        assertEquals(200.0, readPixel(result, 0, 0), 0.0);
        assertEquals(1.0, readPixel(result, 1, 0), 0.0);
    }

    @Test
    void reclassifyTreatsNaNInputAsNoData() {
        BufferedImage source = RasterNumericImages.createDoubleBand(2, 1);
        source.getRaster().setSample(0, 0, 0, Double.NaN);
        source.getRaster().setSample(1, 0, 0, 5.0);

        BufferedImage result = RasterReclassifyEngine.reclassify(
                source,
                List.of(new RasterReclassifyEngine.ReclassRule(0, 10, 7, "")),
                Double.NaN,
                true);

        assertTrue(Double.isNaN(readPixel(result, 0, 0)));
        assertEquals(7.0, readPixel(result, 1, 0), 0.0);
    }

    @Test
    void ruleRejectsNonFiniteAndReversedRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new RasterReclassifyEngine.ReclassRule(
                        Double.NaN, 1, 2, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RasterReclassifyEngine.ReclassRule(
                        10, 1, 2, ""));
    }

    @Test
    void equalIntervalBreaksProducesCorrectNumberOfClasses() {
        List<RasterReclassifyEngine.ReclassRule> rules =
                RasterReclassifyEngine.equalIntervalBreaks(0, 100, 4);

        assertEquals(4, rules.size());
        assertEquals(0.0, rules.get(0).from(), 0.0);
        assertEquals(100.0, rules.get(3).to(), 0.0);
        assertEquals(1.0, rules.get(0).newValue(), 0.0);
        assertEquals(4.0, rules.get(3).newValue(), 0.0);
    }

    @Test
    void equalIntervalBreaksRejectsInvalidRequests() {
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.equalIntervalBreaks(0, 100, 0));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.equalIntervalBreaks(100, 0, 4));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.equalIntervalBreaks(
                        Double.NaN, 100, 4));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.equalIntervalBreaks(10, 10, 2));
        assertEquals(1,
                RasterReclassifyEngine.equalIntervalBreaks(10, 10, 1).size());
    }

    @Test
    void computeStatisticsUsesStableValuesAndSkipsFiniteNoData() {
        BufferedImage image = createByteRaster(3, 1, 200, 50, 30);
        double[] stats =
                RasterReclassifyEngine.computeStatistics(image, 200, true);

        assertEquals(2.0, stats[4], 0.0);
        assertEquals(30.0, stats[0], 0.0);
        assertEquals(50.0, stats[1], 0.0);
        assertEquals(40.0, stats[2], 1e-12);
        assertEquals(Math.sqrt(200.0), stats[3], 1e-12);
    }

    @Test
    void computeStatisticsReturnsNaNsWhenNoValidSamplesExist() {
        BufferedImage image = RasterNumericImages.createDoubleBand(2, 1);
        image.getRaster().setSample(0, 0, 0, Double.NaN);
        image.getRaster().setSample(1, 0, 0, Double.NaN);

        double[] stats =
                RasterReclassifyEngine.computeStatistics(image, Double.NaN, true);

        assertTrue(Double.isNaN(stats[0]));
        assertTrue(Double.isNaN(stats[1]));
        assertTrue(Double.isNaN(stats[2]));
        assertTrue(Double.isNaN(stats[3]));
        assertEquals(0.0, stats[4], 0.0);
    }

    @Test
    void fillNodataPromotesIntegerRasterAndPreservesFractionalMean() {
        BufferedImage image = new BufferedImage(
                3, 1, BufferedImage.TYPE_USHORT_GRAY);
        image.getRaster().setSample(0, 0, 0, 1);
        image.getRaster().setSample(1, 0, 0, 0);
        image.getRaster().setSample(2, 0, 0, 2);

        BufferedImage filled =
                RasterReclassifyEngine.fillNodata(image, 0, 2);

        assertEquals(DataBuffer.TYPE_DOUBLE,
                filled.getRaster().getDataBuffer().getDataType());
        assertEquals(1.5, readPixel(filled, 1, 0), 0.0);
    }

    @Test
    void fillNodataSupportsDoubleNaNAndLeavesSourceUnmodified() {
        BufferedImage source = RasterNumericImages.createDoubleBand(3, 1);
        source.getRaster().setSample(0, 0, 0, 2.5);
        source.getRaster().setSample(1, 0, 0, Double.NaN);
        source.getRaster().setSample(2, 0, 0, 3.5);

        BufferedImage filled =
                RasterReclassifyEngine.fillNodata(source, Double.NaN, 2);

        assertEquals(3.0, readPixel(filled, 1, 0), 0.0);
        assertTrue(Double.isNaN(readPixel(source, 1, 0)));
    }

    @Test
    void fillNodataReturnsSameRasterWhenThereAreNoGaps() {
        BufferedImage image = new BufferedImage(
                2, 1, BufferedImage.TYPE_USHORT_GRAY);
        image.getRaster().setSample(0, 0, 0, 100);
        image.getRaster().setSample(1, 0, 0, 200);

        assertSame(image, RasterReclassifyEngine.fillNodata(image, 0));
    }

    @Test
    void fillNodataRejectsImpossibleNeighborCounts() {
        BufferedImage image = createByteRaster(1, 1, 0);
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.fillNodata(image, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassifyEngine.fillNodata(image, 0, 9));
    }

    private static double readPixel(BufferedImage image, int x, int y) {
        return image.getRaster().getSampleDouble(x, y, 0);
    }

    private static BufferedImage createByteRaster(int width,
                                                   int height,
                                                   double... values) {
        BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        int total = width * height;
        for (int index = 0; index < total; index++) {
            int x = index % width;
            int y = index / width;
            double value = values.length == 1 ? values[0] : values[index];
            raster.setSample(x, y, 0, value);
        }
        return image;
    }
}
