package ar.com.catgis.analysis.raster;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-08: RasterCalculator with combined functions.
 * Tests parser + evaluation + result creation.
 */
class RasterCalculatorEngineTest {

    @Test
    void evaluateSimpleAddition() {
        BufferedImage a = createRaster(2, 2, 10);
        BufferedImage b = createRaster(2, 2, 20);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "a + b");
        assertNotNull(result);
        assertEquals(2, result.getWidth());
        assertEquals(2, result.getHeight());
        // Verify pixel value: (10+20)=30, clamped to byte
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(30, val);
    }

    @Test
    void evaluateSubtraction() {
        BufferedImage a = createRaster(2, 2, 50);
        BufferedImage b = createRaster(2, 2, 30);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "a - b");
        assertNotNull(result);
        // (50-30)=20
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(20, val);
    }

    @Test
    void evaluateWithSqrt() {
        BufferedImage a = createRaster(2, 2, 64);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "sqrt(a)");
        assertNotNull(result);
        // sqrt(64)=8, clamped to byte
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(8, val);
    }

    @Test
    void evaluateWithConditional() {
        // a=150 (high), b=50 (low)
        // if(a > 100, a, b) should return a=150 (clamped to byte 150)
        BufferedImage a = createRaster(2, 2, 150);
        BufferedImage b = createRaster(2, 2, 50);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "if(a > 100, a, b)");
        assertNotNull(result);
        // a=150 > 100, so result should be a=150
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(150, val);
    }

    @Test
    void evaluateConditionalReturnsElseBranch() {
        // a=50 (low), b=200 (high)
        // if(a > 100, a, b) should return b=200 (clamped to byte 200)
        BufferedImage a = createRaster(2, 2, 50);
        BufferedImage b = createRaster(2, 2, 200);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "if(a > 100, a, b)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(200, val);
    }

    @Test
    void evaluateNDVIReturnsExpectedValue() {
        // ndvi(a,b) pops: nir=stack.pop() gets b=50, red=stack.pop() gets a=200
        // ndviVal = (50+200)>0 ? (50-200)/(50+200) : 0 = -0.6
        // clampToByte(-0.6) = 0
        BufferedImage nir = createRaster(2, 2, 200);
        BufferedImage red = createRaster(2, 2, 50);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(nir, null, "a"),
                new RasterCalculatorEngine.RasterSource(red, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "ndvi(a, b)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(0, val); // -0.6 clamps to 0
    }

    @Test
    void evaluateNDVIWithSwappedArgsReturnsPositive() {
        // ndvi(b,a) pops: nir=stack.pop() gets a=200, red=stack.pop() gets b=50
        // ndviVal = (200+50)>0 ? (200-50)/(200+50) : 0 = 0.6
        // clampToByte(0.6) = 1
        BufferedImage nir = createRaster(2, 2, 200);
        BufferedImage red = createRaster(2, 2, 50);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(nir, null, "a"),
                new RasterCalculatorEngine.RasterSource(red, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "ndvi(b, a)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(1, val); // 0.6 rounds to 1
    }

    @Test
    void evaluateReturnsNullForNullSources() {
        assertNull(RasterCalculatorEngine.evaluate(null, "a + b"));
    }

    @Test
    void evaluateReturnsNullForEmptyExpression() {
        BufferedImage a = createRaster(2, 2, 10);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        assertNull(RasterCalculatorEngine.evaluate(sources, ""));
    }

    @Test
    void evaluateCotReturnsExpectedValue() {
        BufferedImage a = createRaster(2, 2, 45);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "cot(a)");
        assertNotNull(result);
        // cot(45°) = 1/tan(45°) ≈ 1.0 → clamp to byte = 1
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(1, val);
    }

    @Test
    void evaluateMinReturnsMinValue() {
        BufferedImage a = createRaster(2, 2, 30);
        BufferedImage b = createRaster(2, 2, 70);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "min(a, b)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(30, val);
    }

    @Test
    void evaluateMaxReturnsMaxValue() {
        BufferedImage a = createRaster(2, 2, 30);
        BufferedImage b = createRaster(2, 2, 70);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a"),
                new RasterCalculatorEngine.RasterSource(b, null, "b")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "max(a, b)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(70, val);
    }

    @Test
    void evaluateClampRestrictsRange() {
        BufferedImage a = createRaster(2, 2, 200);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        // clamp(lo, hi, val) → clamp(0, 100, a) = 100 since a=200
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "clamp(0, 100, a)");
        assertNotNull(result);
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(100, val);
    }

    @Test
    void evaluateModReturnsRemainder() {
        BufferedImage a = createRaster(2, 2, 17);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "mod(a, 5)");
        assertNotNull(result);
        // 17 % 5 = 2
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(2, val);
    }

    @Test
    void evaluateSignReturnsSignum() {
        BufferedImage a = createRaster(2, 2, 42);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "sign(a)");
        assertNotNull(result);
        // sign(42) = 1.0 → clamp = 1
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(1, val);
    }

    @Test
    void evaluatePiReturnsConstant() {
        BufferedImage a = createRaster(2, 2, 0);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "pi");
        assertNotNull(result);
        // pi ≈ 3.14 → clamp = 3
        int val = result.getRaster().getSample(0, 0, 0);
        assertEquals(3, val);
    }

    @Test
    void evaluateProgressListenerCalled() {
        BufferedImage a = createRaster(4, 4, 50);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        int[] progressCount = {0};
        BufferedImage result = RasterCalculatorEngine.evaluate(sources, "a * 2",
                (current, total) -> progressCount[0]++);
        assertNotNull(result);
        assertTrue(progressCount[0] > 0, "Progress listener should be called at least once");
    }

    @Test
    void evaluateProgressListenerReceivesFinalTotal() {
        BufferedImage a = createRaster(3, 3, 50);
        List<RasterCalculatorEngine.RasterSource> sources = List.of(
                new RasterCalculatorEngine.RasterSource(a, null, "a")
        );
        int[] lastTotal = {0};
        RasterCalculatorEngine.evaluate(sources, "a", (current, total) -> lastTotal[0] = total);
        assertEquals(9, lastTotal[0], "Final total should be width * height = 9");
    }

    private BufferedImage createRaster(int w, int h, int value) {
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
