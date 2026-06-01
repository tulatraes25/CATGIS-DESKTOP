package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class LayoutRenderTest {

    @Test
    public void testRenderContextModes() {
        LayoutRenderContext preview = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        assertEquals(LayoutRenderContext.Mode.PREVIEW, preview.getMode());

        LayoutRenderContext export = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, 300, 297, 210);
        assertEquals(LayoutRenderContext.Mode.EXPORT_IMAGE, export.getMode());
    }

    @Test
    public void testLabelRenderDoesNotThrow() {
        LayoutLabel l = new LayoutLabel("l1", "Test", 10, 10, 200, 30);
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        l.setColor(Color.BLUE);
        BufferedImage img = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        l.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testLabelRenderEmptyText() {
        LayoutLabel l = new LayoutLabel("l1", "", 10, 10, 200, 30);
        BufferedImage img = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        l.render(g, ctx); // should not throw
        g.dispose();
    }

    @Test
    public void testLabelRenderNullText() {
        LayoutLabel l = new LayoutLabel("l1", null, 10, 10, 200, 30);
        BufferedImage img = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        l.render(g, ctx); // should not throw
        g.dispose();
    }

    @Test
    public void testRectangleRender() {
        LayoutRectangle r = new LayoutRectangle("r1", 10, 10, 100, 60);
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        r.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testEllipseRender() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 60);
        e.setFillColor(new Color(255, 0, 0, 100));
        e.setBorderColor(Color.BLACK);
        e.setBorderWidth(2f);
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        e.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testLineRender() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        l.setDashed(true);
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        l.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testCartoucheRender() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 250, 80);
        c.setField("Estudio", "GeoTech");
        c.setField("Empresa", "CATGIS");
        c.setBgColor(new Color(255, 255, 255, 200));
        c.setShowBorder(true);
        BufferedImage img = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        c.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testGraticuleRender() {
        LayoutGraticule gr = new LayoutGraticule("g1", 0, 0, 200, 150);
        gr.setShowLabels(true);
        gr.setGeographic(true);
        gr.setIntervalX(0.5);
        gr.setIntervalY(0.5);
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        gr.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testTableRenderEmpty() {
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 50);
        BufferedImage img = new BufferedImage(300, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        t.render(g, ctx); // should not throw with empty rows
        g.dispose();
    }

    @Test
    public void testNorthArrowRender() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 10, 10, 30, 30);
        BufferedImage img = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        n.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testScaleBarRender() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setMapScaleDenominator(25000);
        BufferedImage img = new BufferedImage(300, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        s.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testScaleBarRenderZeroWidth() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 0, 12);
        BufferedImage img = new BufferedImage(300, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        s.render(g, ctx); // should return early, no exception
        g.dispose();
    }

    @Test
    public void testImageRender() {
        BufferedImage src = new BufferedImage(40, 30, BufferedImage.TYPE_INT_ARGB);
        LayoutImage li = new LayoutImage("img1", src, 10, 10, 50, 40);
        BufferedImage img = new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        li.render(g, ctx);
        g.dispose();
    }
}
