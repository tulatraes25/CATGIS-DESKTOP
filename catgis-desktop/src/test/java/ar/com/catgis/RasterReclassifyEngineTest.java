package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RasterReclassifyEngineTest {

    @Test
    void reclassifyReturnsSourceWhenSourceIsNull() {
        assertNull(RasterReclassifyEngine.reclassify(null, List.of(), -1, false));
    }

    @Test
    void reclassifyReturnsSourceWhenRulesAreNull() {
        BufferedImage img = createRaster(2, 2, 5);
        assertSame(img, RasterReclassifyEngine.reclassify(img, null, -1, false));
    }

    @Test
    void reclassifyReturnsSourceWhenRulesAreEmpty() {
        BufferedImage img = createRaster(2, 2, 5);
        assertSame(img, RasterReclassifyEngine.reclassify(img, List.of(), -1, false));
    }

    @Test
    void reclassifyMapsValuesToNewClasses() {
        BufferedImage src = createRaster(3, 1, 10, 50, 90);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "Low"),
                new RasterReclassifyEngine.ReclassRule(20, 70, 2, "Mid"),
                new RasterReclassifyEngine.ReclassRule(70, 100, 3, "High")
        );

        BufferedImage result = RasterReclassifyEngine.reclassify(src, rules, -1, false);

        assertNotNull(result);
        assertEquals(3, result.getWidth());
        assertEquals(1, result.getHeight());
        assertEquals(1.0, readPixel(result, 0, 0), 1e-9);
        assertEquals(2.0, readPixel(result, 1, 0), 1e-9);
        assertEquals(3.0, readPixel(result, 2, 0), 1e-9);
    }

    @Test
    void reclassifyUnmatchedValuesGetNoData() {
        BufferedImage src = createRaster(1, 1, 250);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 10, 1, "A")
        );

        BufferedImage result = RasterReclassifyEngine.reclassify(src, rules, 255, false);

        assertEquals(255.0, readPixel(result, 0, 0), 1e-9);
    }

    @Test
    void reclassifyPreservesNodataWhenUseNodataIsTrue() {
        BufferedImage src = createRaster(2, 1, 200, 15);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "Low")
        );

        BufferedImage result = RasterReclassifyEngine.reclassify(src, rules, 200, true);

        assertEquals(200.0, readPixel(result, 0, 0), 1e-9);
        assertEquals(1.0, readPixel(result, 1, 0), 1e-9);
    }

    @Test
    void reclassifyDoesNotPreserveNodataWhenUseNodataIsFalse() {
        BufferedImage src = createRaster(2, 1, 200, 15);
        List<RasterReclassifyEngine.ReclassRule> rules = List.of(
                new RasterReclassifyEngine.ReclassRule(100, 250, 5, "HighRange"),
                new RasterReclassifyEngine.ReclassRule(0, 20, 1, "Low")
        );

        BufferedImage result = RasterReclassifyEngine.reclassify(src, rules, 200, false);

        assertEquals(5.0, readPixel(result, 0, 0), 1e-9);
        assertEquals(1.0, readPixel(result, 1, 0), 1e-9);
    }

    @Test
    void equalIntervalBreaksProducesCorrectNumberOfClasses() {
        List<RasterReclassifyEngine.ReclassRule> rules =
                RasterReclassifyEngine.equalIntervalBreaks(0, 100, 4);

        assertEquals(4, rules.size());
        assertEquals(0.0, rules.get(0).from(), 1e-9);
        assertEquals(100.0, rules.get(3).to(), 1e-9);
        assertEquals(1.0, rules.get(0).newValue(), 1e-9);
        assertEquals(4.0, rules.get(3).newValue(), 1e-9);
    }

    @Test
    void equalIntervalBreaksRangesAreContiguous() {
        List<RasterReclassifyEngine.ReclassRule> rules =
                RasterReclassifyEngine.equalIntervalBreaks(0, 30, 3);

        for (int i = 1; i < rules.size(); i++) {
            assertEquals(rules.get(i - 1).to(), rules.get(i).from(), 1e-9);
        }
        assertEquals(0.0, rules.get(0).from(), 1e-9);
        assertEquals(30.0, rules.get(rules.size() - 1).to(), 1e-9);
    }

    @Test
    void computeStatisticsReturnsCorrectMin() {
        BufferedImage img = createRaster(3, 1, 10, 50, 30);
        double[] stats = RasterReclassifyEngine.computeStatistics(img, -1, true);

        assertEquals(10.0, stats[0], 1e-9);
    }

    @Test
    void computeStatisticsSkipsNodata() {
        BufferedImage img = createRaster(3, 1, 200, 50, 30);
        double[] stats = RasterReclassifyEngine.computeStatistics(img, 200, true);

        assertEquals(2.0, stats[4], 1e-9);
        assertEquals(30.0, stats[0], 1e-9);
    }

    @Test
    void fillNodata_interpolatesSingleGap() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster = img.getRaster();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                raster.setSample(x, y, 0, 100);
        raster.setSample(1, 1, 0, 0); // 0 = NoData

        BufferedImage filled = RasterReclassifyEngine.fillNodata(img, 0);
        double center = filled.getRaster().getSample(1, 1, 0);
        assertEquals(100.0, center, 0.001);
    }

    @Test
    void fillNodata_returnsSameIfNoGaps() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster = img.getRaster();
        raster.setSample(0, 0, 0, 100);
        raster.setSample(1, 0, 0, 200);
        raster.setSample(0, 1, 0, 300);
        raster.setSample(1, 1, 0, 400);
        BufferedImage filled = RasterReclassifyEngine.fillNodata(img, 0);
        assertEquals(100.0, readPixel(filled, 0, 0), 0.001);
        assertEquals(400.0, readPixel(filled, 1, 1), 0.001);
    }

    @Test
    void fillNodata_interpolatesEdgeGap() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster = img.getRaster();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                raster.setSample(x, y, 0, 50);
        raster.setSample(0, 0, 0, 0); // corner gap
        raster.setSample(1, 0, 0, 0); // edge gap

        BufferedImage filled = RasterReclassifyEngine.fillNodata(img, 0);
        double corner = filled.getRaster().getSample(0, 0, 0);
        double edge = filled.getRaster().getSample(1, 0, 0);
        assertTrue(corner > 0, "Corner should be filled");
        assertTrue(edge > 0, "Edge should be filled");
        assertEquals(50.0, corner, 10.0);
        assertEquals(50.0, edge, 10.0);
    }

    private static double readPixel(BufferedImage img, int x, int y) {
        double[] px = new double[img.getRaster().getNumBands()];
        img.getRaster().getPixel(x, y, px);
        return px[0];
    }

    private static BufferedImage createRaster(int w, int h, double... values) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = img.getRaster();
        int total = w * h;
        for (int i = 0; i < total; i++) {
            int x = i % w;
            int y = i / w;
            double v = (values.length == 1) ? values[0] : values[i];
            raster.setSample(x, y, 0, v);
        }
        return img;
    }
}
