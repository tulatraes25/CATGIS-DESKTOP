package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;

import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Point-to-pixel typographic conversion shared by preview and export. */
class RenderMetricsContractTest {

    @Test
    void pointsBecomePixelsAtTheRenderDpi() {
        assertEquals(9.0, RenderMetrics.pointsToPixels(12.0, 72), 1.0e-9);
        assertEquals(12.0, RenderMetrics.pointsToPixels(12.0, 96), 1.0e-9);
        assertEquals(24.0, RenderMetrics.pointsToPixels(12.0, 192), 1.0e-9);
        assertEquals(0.1875, RenderMetrics.pointsToPixels(1.0, 18), 1.0e-9);
    }

    @Test
    void invalidValuesStayRenderable() {
        assertEquals(1.0, RenderMetrics.pointsToPixels(0, 96));
        assertEquals(1.0, RenderMetrics.pointsToPixels(Double.NaN, 96));
        assertEquals(1.0, RenderMetrics.pointsToPixels(12, 0));
        assertEquals(0.0, RenderMetrics.pixelsToPoints(-1, 96));
    }

    @Test
    void scaledFontKeepsFamilyAndStyle() {
        Font base = new Font("SansSerif", Font.BOLD, 16);
        Font scaled = RenderMetrics.scaledFont(base, 96);
        assertEquals("SansSerif", scaled.getFamily());
        assertEquals(Font.BOLD, scaled.getStyle());
        assertEquals(16.0, scaled.getSize2D(), 1.0e-6,
                "96 DPI is the baseline: font keeps its size there");
        Font doubled = RenderMetrics.scaledFont(base, 192);
        assertEquals(32.0, doubled.getSize2D(), 1.0e-6);
    }

    @Test
    void pixelAndPointConversionsRoundTrip() {
        double pixels = RenderMetrics.pointsToPixels(11.5, 200);
        assertEquals(11.5, RenderMetrics.pixelsToPoints(pixels, 200), 1.0e-9);
    }
}
