package ar.com.catgis.layout;

import ar.com.catgis.PointSymbolCatalog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class LayoutUtilsTest {

    @Test
    public void testExportContextCreation() {
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        LayoutExportContext ctx = new LayoutExportContext(200, 297, 210, 2480, 1754, img);
        assertEquals(200, ctx.dpi, 0.01);
        assertEquals(297, ctx.pageWidthMm, 0.01);
        assertEquals(210, ctx.pageHeightMm, 0.01);
        assertNotNull(ctx.graphics);
        assertNotNull(ctx.toRenderContext());
        ctx.dispose();
    }

    @Test
    public void testExportContextMmToPx() {
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        LayoutExportContext ctx = new LayoutExportContext(200, 297, 210, 2480, 1754, img);
        int px = ctx.mmToPx(25.4);
        assertEquals(200, px);
        ctx.dispose();
    }

    @Test
    public void testCanvasDropTarget() {
        // CanvasDropTarget is a utility with DropHandler interface
        // Just verify the class loads and interfaces exist
        CanvasDropTarget.DropHandler handler = new CanvasDropTarget.DropHandler() {
            public void onImageDropped(BufferedImage img, double mmX, double mmY) {}
            public void onFileDropped(java.io.File file, double mmX, double mmY) {}
        };
        assertNotNull(handler);
    }

    @Test
    public void testGuideLineCreation() {
        GuideLine g = new GuideLine("g1", 100, GuideLine.Orientation.VERTICAL);
        assertEquals("g1", g.id);
        assertEquals(100, g.mmPos, 0.01);
        assertEquals(GuideLine.Orientation.VERTICAL, g.orientation);
        assertFalse(g.locked);
    }

    @Test
    public void testGuideLineMovement() {
        GuideLine g = new GuideLine("g1", 50, GuideLine.Orientation.HORIZONTAL);
        g.mmPos = 75;
        assertEquals(75, g.mmPos, 0.01);
    }

    @Test
    public void testGuideLineHorizontalContains() {
        GuideLine g = new GuideLine("g1", 50, GuideLine.Orientation.HORIZONTAL);
        // At 200 DPI, 50mm = 393px
        assertTrue(g.containsPx(400, 393, 0, 0, 1000, 800, 200, 1.0, 8));
        assertFalse(g.containsPx(400, 500, 0, 0, 1000, 800, 200, 1.0, 8));
    }

    @Test
    public void testGuideLineLocked() {
        GuideLine g = new GuideLine("g1", 50, GuideLine.Orientation.VERTICAL);
        g.locked = true;
        assertFalse(g.containsPx(393, 400, 0, 0, 1000, 800, 200, 1.0, 8));
    }

    @Test
    public void testGuideLineVerticalContains() {
        GuideLine g = new GuideLine("g1", 50, GuideLine.Orientation.VERTICAL);
        assertTrue(g.containsPx(393, 400, 0, 0, 1000, 800, 200, 1.0, 8));
        assertFalse(g.containsPx(500, 400, 0, 0, 1000, 800, 200, 1.0, 8));
    }

    @Test
    public void testRulerRendererSize() {
        assertEquals(18, RulerRenderer.getRulerSize());
    }

    @Test
    public void testRulerHitTestVertical() {
        // Ruler area is top 18px for horizontal ruler (creates vertical guides)
        GuideLine.Orientation result = RulerRenderer.rulerHitTest(50, 5, 0, 0, 800, 600);
        assertEquals(GuideLine.Orientation.VERTICAL, result);
    }

    @Test
    public void testRulerHitTestHorizontal() {
        GuideLine.Orientation result = RulerRenderer.rulerHitTest(5, 50, 0, 0, 800, 600);
        assertEquals(GuideLine.Orientation.HORIZONTAL, result);
    }

    @Test
    public void testRulerHitTestNone() {
        GuideLine.Orientation result = RulerRenderer.rulerHitTest(30, 30, 0, 0, 800, 600);
        assertNull(result);
    }

    @Test
    public void testPointSymbolCatalogRenderAllSymbols() {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        String[] ids = {"circle","square","diamond","triangle","star","cross","pin","target","oil-well","wind-turbine"};
        for (String id : ids) {
            PointSymbolCatalog.render(g, id, 15, 15, 16, Color.BLUE, Color.BLACK, 1f);
        }
        g.dispose();
        // No exceptions thrown = pass
    }

    @Test
    public void testPointSymbolCatalogRenderWithNullStroke() {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        PointSymbolCatalog.render(g, "pin", 15, 15, 20, new Color(255,100,0), null, 0f);
        g.dispose();
    }
}
