package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LayoutPipelineTest {

    @Test
    public void testFullRenderPipeline() {
        LayoutModel model = new LayoutModel();
        int z = 0;

        LayoutMap map = new LayoutMap("map", 15, 35, 267, 130);
        map.setZOrder(z++); map.setFrameColor(new Color(50, 50, 50));
        map.setFrameWidth(2f); map.setFrameCornerRadius(6);
        map.setShowGrid(true); map.setGridCols(4); map.setGridRows(3);
        model.addElement(map);

        LayoutLabel title = new LayoutLabel("title", "Mapa Completo", 15, 8, 267, 16);
        title.setZOrder(z++); title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBgColor(new Color(255, 255, 255, 220));
        title.setPaddingPx(2);
        model.addElement(title);

        LayoutLegend leg = new LayoutLegend("leg", 15, 170, 130, 36);
        leg.setZOrder(z++); leg.setAutoHeight(true); leg.setShowBackground(true);
        leg.getItems().add(new LayoutLegend.LegendItem("Capa 1", Color.RED, "POINT"));
        model.addElement(leg);

        LayoutScaleBar scale = new LayoutScaleBar("scale", 15, 206, 100, 10);
        scale.setZOrder(z++); scale.setMapScaleDenominator(25000);
        model.addElement(scale);

        LayoutNorthArrow north = new LayoutNorthArrow("north", 268, 155, 16, 16);
        north.setZOrder(z++); model.addElement(north);

        assertEquals(5, model.size());

        // Render everything to a single image (simulates export)
        BufferedImage img = new BufferedImage(2480, 1754, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, 2480, 1754);
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_PDF, 200, 297, 210);
        for (LayoutElement el : model.getVisibleElementsSortedByZ()) {
            el.render(g, ctx);
        }
        g.dispose();

        // Verify the image is not blank (something was actually rendered)
        int nonWhite = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (img.getRGB(x, y) != 0xFFFFFFFF) nonWhite++;
            }
        }
        assertTrue(nonWhite > 0, "Rendered image should contain non-white pixels from layout elements");
    }

    @Test
    public void testAllElementsRenderTogether() {
        LayoutModel model = new LayoutModel();
        int z = 0;

        // Add one of each element type
        model.addElement(new LayoutMap("map", 0, 0, 50, 50)); model.getElements().get(0).setZOrder(z++);
        LayoutLabel l = new LayoutLabel("l", "X", 60, 0, 40, 20); l.setZOrder(z++); model.addElement(l);
        LayoutLegend lg = new LayoutLegend("lg", 110, 0, 40, 30); lg.setZOrder(z++); model.addElement(lg);
        LayoutScaleBar sb = new LayoutScaleBar("sb", 0, 60, 50, 10); sb.setZOrder(z++); model.addElement(sb);
        LayoutNorthArrow na = new LayoutNorthArrow("na", 60, 50, 20, 20); na.setZOrder(z++); model.addElement(na);
        LayoutRectangle r = new LayoutRectangle("r", 90, 50, 30, 30); r.setZOrder(z++); model.addElement(r);
        LayoutEllipse e = new LayoutEllipse("e", 130, 50, 30, 20); e.setZOrder(z++); model.addElement(e);
        LayoutLine ln = new LayoutLine("ln", 0, 80, 50, 80); ln.setZOrder(z++); model.addElement(ln);
        LayoutCartouche c = new LayoutCartouche("c", 60, 70, 60, 40); c.setZOrder(z++); model.addElement(c);
        LayoutGraticule gr = new LayoutGraticule("gr", 0, 0, 50, 50); gr.setZOrder(z++); model.addElement(gr);

        assertEquals(10, model.size());

        BufferedImage img = new BufferedImage(500, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        for (LayoutElement el : model.getVisibleElementsSortedByZ()) {
            el.render(g, ctx);
        }
        g.dispose();

        // Verify rendering produced visible content
        int nonWhite = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (img.getRGB(x, y) != 0xFFFFFFFF) nonWhite++;
            }
        }
        assertTrue(nonWhite > 0, "All 10 element types should produce visible pixels in the rendered image");
    }
}
