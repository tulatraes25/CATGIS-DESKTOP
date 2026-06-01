package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;

public class LayoutLegendTest {

    @Test
    public void testDefaults() {
        LayoutLegend legend = new LayoutLegend("leg1", 10, 20, 80, 100);
        assertEquals("leg1", legend.getId());
        assertEquals("Leg1", "Leg1", legend.getName()); // default name = id
        assertTrue(legend.isVisible());
        assertTrue(legend.isAutoHeight());
        assertEquals("Leyenda", legend.getTitle());
    }

    @Test
    public void testTitle() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        legend.setTitle("Referencias del Mapa");
        assertEquals("Referencias del Mapa", legend.getTitle());
    }

    @Test
    public void testItems() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        assertEquals(0, legend.getItems().size());
        legend.getItems().add(new LayoutLegend.LegendItem("Capa 1", Color.RED, "POLYGON"));
        legend.getItems().add(new LayoutLegend.LegendItem("Capa 2", Color.BLUE, "LINE"));
        assertEquals(2, legend.getItems().size());
        assertEquals(2, legend.getIncludedItems().size());
    }

    @Test
    public void testExcludedItems() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        LayoutLegend.LegendItem item = new LayoutLegend.LegendItem("Hidden", Color.GRAY, "POINT");
        item.included = false;
        legend.getItems().add(item);
        assertEquals(1, legend.getItems().size());
        assertEquals(0, legend.getIncludedItems().size());
    }

    @Test
    public void testBackgroundAndBorder() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        assertFalse(legend.isShowBackground());
        assertFalse(legend.isShowBorder());
        legend.setShowBackground(true);
        legend.setShowBorder(true);
        assertTrue(legend.isShowBackground());
        assertTrue(legend.isShowBorder());
    }

    @Test
    public void testOpacity() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        legend.setBgOpacity(0.5f);
        assertEquals(0.5f, legend.getBgOpacity(), 0.01);
    }

    @Test
    public void testFonts() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        Font tf = new Font("Serif", Font.BOLD, 16);
        legend.setTitleFont(tf);
        assertEquals("Serif", legend.getTitleFont().getFamily());
        assertEquals(16, legend.getTitleFont().getSize());
        assertTrue(legend.getTitleFont().isBold());
    }

    @Test
    public void testColumnsAndSymbolSize() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        legend.setColumns(2);
        assertEquals(2, legend.getColumns());
        legend.setSymbolSizeMm(5.5);
        assertEquals(5.5, legend.getSymbolSizeMm(), 0.01);
    }

    @Test
    public void testPadding() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        assertEquals(2.5, legend.getPaddingMm(), 0.01);
        legend.setPaddingMm(4);
        assertEquals(4, legend.getPaddingMm(), 0.01);
    }

    @Test
    public void testBasemapDetection() {
        assertTrue(LayoutLegend.isBasemapName("OSM Standard"));
        assertTrue(LayoutLegend.isBasemapName("Esri World Imagery"));
        assertTrue(LayoutLegend.isBasemapName("Mapa Base"));
        assertTrue(LayoutLegend.isBasemapName("Satellite Tiles"));
        assertFalse(LayoutLegend.isBasemapName("Limites Municipales"));
        assertFalse(LayoutLegend.isBasemapName("Pozos Petroleros"));
        assertFalse(LayoutLegend.isBasemapName(null));
    }
}
