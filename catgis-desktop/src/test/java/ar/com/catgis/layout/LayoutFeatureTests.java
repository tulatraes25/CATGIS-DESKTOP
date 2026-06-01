package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LayoutFeatureTests {

    @Test
    public void testLabelWithBackground() {
        LayoutLabel l = new LayoutLabel("l1", "Con fondo", 10, 10, 200, 40);
        l.setBgColor(new Color(255, 255, 200, 180));
        l.setBorderColor(Color.GRAY);
        l.setBorderWidth(1.5f);
        l.setPaddingPx(4);

        BufferedImage img = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        l.render(g, ctx);
        g.dispose();
    }

    @Test
    public void testMultipleMapsDifferentExtents() {
        LayoutModel model = new LayoutModel();

        LayoutMap map1 = new LayoutMap("map-left", 12, 36, 130, 130);
        map1.setZOrder(1);
        map1.setOwnExtent(true);
        map1.setOwnViewMinX(0); map1.setOwnViewMinY(0); map1.setOwnZoomFactor(2);
        model.addElement(map1);

        LayoutMap map2 = new LayoutMap("map-right", 155, 36, 130, 130);
        map2.setZOrder(2);
        map2.setOwnExtent(true);
        map2.setOwnViewMinX(1000); map2.setOwnViewMinY(1000); map2.setOwnZoomFactor(4);
        model.addElement(map2);

        assertEquals(2, model.size());
        // Each map has different extent
        assertNotEquals(map1.getOwnZoomFactor(), map2.getOwnZoomFactor(), 0.01);
    }

    @Test
    public void testFullCompositionWithAllElements() {
        LayoutModel model = new LayoutModel();
        int z = 0;

        LayoutMap map = new LayoutMap("map", 15, 36, 267, 100);
        map.setZOrder(z++); map.setName("Mapa"); model.addElement(map);

        LayoutLabel title = new LayoutLabel("title", "Mapa Completo", 15, 8, 267, 14);
        title.setZOrder(z++); title.setName("Titulo");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBgColor(new Color(255,255,255,200));
        model.addElement(title);

        LayoutLabel subtitle = new LayoutLabel("subtitle", "Informe tecnico", 15, 24, 267, 10);
        subtitle.setZOrder(z++); subtitle.setName("Subtitulo");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 9));
        subtitle.setColor(new Color(0x5B6778));
        model.addElement(subtitle);

        LayoutLegend legend = new LayoutLegend("legend", 15, 140, 130, 40);
        legend.setZOrder(z++); legend.setAutoHeight(true);
        legend.getItems().add(new LayoutLegend.LegendItem("Pozos", Color.RED, "POINT"));
        legend.getItems().add(new LayoutLegend.LegendItem("Caminos", Color.BLUE, "LINE"));
        model.addElement(legend);

        LayoutScaleBar scale = new LayoutScaleBar("scale", 15, 195, 120, 10);
        scale.setZOrder(z++); scale.setMapScaleDenominator(25000);
        model.addElement(scale);

        LayoutNorthArrow north = new LayoutNorthArrow("north", 268, 120, 16, 16);
        north.setZOrder(z++); model.addElement(north);

        LayoutCartouche cartouche = new LayoutCartouche("cartouche", 148, 140, 137, 48);
        cartouche.setZOrder(z++);
        cartouche.setField("Estudio", "GeoTech S.A.");
        cartouche.setField("Cartografo", "Juan Perez");
        cartouche.setField("Fuente", "Trabajo de campo 2024");
        model.addElement(cartouche);

        LayoutGraticule grid = new LayoutGraticule("grid", 15, 36, 267, 100);
        grid.setZOrder(z++); grid.setShowLabels(true);
        model.addElement(grid);

        assertEquals(8, model.size());
        assertEquals(8, model.getVisibleElementsSortedByZ().size());
    }

    @Test
    public void testElementZOrderConsistency() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle bottom = new LayoutRectangle("bottom", 0, 0, 100, 100);
        LayoutRectangle middle = new LayoutRectangle("middle", 10, 10, 80, 80);
        LayoutRectangle top = new LayoutRectangle("top", 20, 20, 60, 60);

        bottom.setZOrder(1); middle.setZOrder(2); top.setZOrder(3);
        model.addElement(bottom); model.addElement(middle); model.addElement(top);

        // Top element should be found first
        LayoutElement found = model.findTopmostElementAtMm(50, 50);
        assertEquals("top", found.getId());
    }

    @Test
    public void testScaleBarUpdatesWithMapExtent() {
        LayoutScaleBar bar = new LayoutScaleBar("bar", 10, 100, 100, 12);
        assertEquals(10000, bar.getMapScaleDenominator(), 0.01);
        bar.setMapScaleDenominator(50000);
        assertEquals(50000, bar.getMapScaleDenominator(), 0.01);
        bar.setMapScaleDenominator(0); // invalid
        assertEquals(10000, bar.getMapScaleDenominator(), 0.01); // clamps to default
    }

    @Test
    public void testCartoucheWithAllFields() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        c.setField("Estudio", "GeoConsultores SRL");
        c.setField("Proyecto", "CAT-2024-001");
        c.setField("Empresa", "CATGIS S.A.");
        c.setField("Cartografo", "Maria Lopez");
        c.setField("Fuente", "Relevamiento GPS 2024");
        c.setField("Coord.", "POSGAR 2007 / Faja 4");

        assertEquals("GeoConsultores SRL", c.getField("Estudio"));
        assertEquals("CAT-2024-001", c.getField("Proyecto"));
        assertEquals("POSGAR 2007 / Faja 4", c.getField("Coord."));
    }
}
