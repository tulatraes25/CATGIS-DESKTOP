package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutRenderContextTest {

    @Test
    public void testDpiConversion() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        assertEquals(200, ctx.getDpi(), 0.01);
        assertEquals(297, ctx.getPageWidthMm(), 0.01);
        assertEquals(210, ctx.getPageHeightMm(), 0.01);
    }

    @Test
    public void testMmToPx() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, 200, 297, 210);
        // 25.4mm = 1 inch = 200px at 200dpi
        assertEquals(200, ctx.mmToPx(25.4), 0.1);
        assertEquals(100, ctx.mmToPx(12.7), 0.1);
    }

    @Test
    public void testPxToMm() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_PDF, 300, 297, 210);
        assertEquals(25.4, ctx.pxToMm(300), 0.1);
        assertEquals(12.7, ctx.pxToMm(150), 0.1);
    }

    @Test
    public void testMmToPxInt() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PRINT, 150, 210, 297);
        int result = ctx.mmToPxInt(25.4);
        assertEquals(150, result);
    }

    @Test
    public void testPxRectToMm() {
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        LayoutRenderContext.RectangleMm rect = ctx.pxRectToMm(0, 0, 200, 200);
        assertEquals(25.4, rect.w, 0.1);
        assertEquals(25.4, rect.h, 0.1);
    }

    @Test
    public void testModes() {
        assertEquals(LayoutRenderContext.Mode.PREVIEW, LayoutRenderContext.Mode.valueOf("PREVIEW"));
        assertEquals(LayoutRenderContext.Mode.EXPORT_IMAGE, LayoutRenderContext.Mode.valueOf("EXPORT_IMAGE"));
        assertEquals(LayoutRenderContext.Mode.EXPORT_PDF, LayoutRenderContext.Mode.valueOf("EXPORT_PDF"));
        assertEquals(LayoutRenderContext.Mode.PRINT, LayoutRenderContext.Mode.valueOf("PRINT"));
    }
}
