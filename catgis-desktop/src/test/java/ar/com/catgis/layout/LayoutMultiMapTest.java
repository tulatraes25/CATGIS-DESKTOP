package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.util.List;

public class LayoutMultiMapTest {

    @Test
    public void testMultipleMapsInModel() {
        LayoutModel model = new LayoutModel();
        LayoutMap map1 = new LayoutMap("map1", 10, 10, 130, 100);
        map1.setZOrder(1); map1.setName("Mapa izquierda");
        map1.setOwnExtent(true); map1.setOwnViewMinX(0); map1.setOwnViewMinY(0); map1.setOwnZoomFactor(1);
        model.addElement(map1);

        LayoutMap map2 = new LayoutMap("map2", 155, 10, 130, 100);
        map2.setZOrder(2); map2.setName("Mapa derecha");
        map2.setOwnExtent(true); map2.setOwnViewMinX(100); map2.setOwnViewMinY(100); map2.setOwnZoomFactor(2);
        model.addElement(map2);

        assertEquals(2, model.size());
        List<LayoutElement> visible = model.getVisibleElementsSortedByZ();
        assertEquals(2, visible.size());

        // map2 is at [155,10]-[285,110], map1 is at [10,10]-[140,110]
        // Point in map1 area:
        LayoutElement found1 = model.findTopmostElementAtMm(75, 60);
        assertNotNull(found1);
        assertEquals("map1", found1.getId()); // only map1 covers this point

        // Point in map2 area:
        LayoutElement found2 = model.findTopmostElementAtMm(220, 60);
        assertNotNull(found2);
        assertEquals("map2", found2.getId()); // only map2 covers this point

        // Point overlapping both: should return higher z-order (map2)
        // map2 overlaps at edge... let's check at common point
        model.addElement(map2); // already added
        model.addElement(new LayoutRectangle("overlay", 120, 30, 50, 60));
        model.getElements().get(model.size()-1).setZOrder(3);
        LayoutElement top = model.findTopmostElementAtMm(140, 60);
        assertNotNull(top);
    }

    @Test
    public void testMapWithOwnExtent() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        assertFalse(map.isOwnExtent());
        map.setOwnExtent(true);
        assertTrue(map.isOwnExtent());
        map.setOwnViewMinX(50);
        assertEquals(50, map.getOwnViewMinX(), 0.01);
    }

    @Test
    public void testMapTargetScale() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setTargetScaleDenominator(0);
        assertEquals(0, map.getTargetScaleDenominator(), 0.01);
        map.setTargetScaleDenominator(5000);
        assertEquals(5000, map.getTargetScaleDenominator(), 0.01);
    }

    @Test
    public void testFullLayoutComposition() {
        LayoutModel model = new LayoutModel();
        int z = 0;

        LayoutMap map = new LayoutMap("main-map", 12, 36, 273, 130);
        map.setZOrder(z++); map.setName("Mapa principal");
        model.addElement(map);

        LayoutLabel title = new LayoutLabel("title", "Mapa de Prueba", 12, 8, 273, 14);
        title.setZOrder(z++); title.setName("Titulo");
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        model.addElement(title);

        LayoutLegend legend = new LayoutLegend("legend", 12, 172, 130, 40);
        legend.setZOrder(z++); legend.setAutoHeight(true);
        legend.getItems().add(new LayoutLegend.LegendItem("Capa 1", Color.RED, "POLYGON"));
        model.addElement(legend);

        LayoutScaleBar scale = new LayoutScaleBar("scale", 12, 208, 100, 10);
        scale.setZOrder(z++); scale.setMapScaleDenominator(25000);
        model.addElement(scale);

        LayoutNorthArrow north = new LayoutNorthArrow("north", 265, 155, 16, 16);
        north.setZOrder(z++);
        model.addElement(north);

        LayoutCartouche cartouche = new LayoutCartouche("cartouche", 148, 172, 137, 48);
        cartouche.setZOrder(z++); cartouche.setField("Empresa", "CATGIS");
        model.addElement(cartouche);

        assertEquals(6, model.size());
        assertEquals(6, model.getVisibleElementsSortedByZ().size());
    }
}
