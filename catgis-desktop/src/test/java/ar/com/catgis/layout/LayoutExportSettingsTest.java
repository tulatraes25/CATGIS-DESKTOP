package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutExportSettingsTest {

    @Test
    public void testRenderContextMode() {
        assertEquals("PREVIEW", LayoutRenderContext.Mode.PREVIEW.name());
        assertEquals("EXPORT_IMAGE", LayoutRenderContext.Mode.EXPORT_IMAGE.name());
        assertEquals("EXPORT_PDF", LayoutRenderContext.Mode.EXPORT_PDF.name());
        assertEquals("PRINT", LayoutRenderContext.Mode.PRINT.name());
    }

    @Test
    public void testExportContextAllModes() {
        for (LayoutRenderContext.Mode mode : LayoutRenderContext.Mode.values()) {
            LayoutRenderContext ctx = new LayoutRenderContext(mode, 200, 297, 210);
            assertEquals(mode, ctx.getMode());
            assertEquals(200, ctx.getDpi(), 0.01);
        }
    }

    @Test
    public void testPxToMmRoundtrip() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, 300, 420, 297);
        double mm = 50.8;
        int px = ctx.mmToPxInt(mm);
        double back = ctx.pxToMm(px);
        assertTrue(Math.abs(mm - back) < 0.5, "Roundtrip should be within 0.5mm");
    }

    @Test
    public void testRectangleMmEquality() {
        LayoutRenderContext.RectangleMm r1 = new LayoutRenderContext.RectangleMm(10, 20, 100, 50);
        assertEquals(10, r1.x, 0.01);
        assertEquals(20, r1.y, 0.01);
        assertEquals(100, r1.w, 0.01);
        assertEquals(50, r1.h, 0.01);
    }
}
