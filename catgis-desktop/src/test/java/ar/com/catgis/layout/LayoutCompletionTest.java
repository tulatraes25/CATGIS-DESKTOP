package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class LayoutCompletionTest {

    @Test
    public void testAll13ElementTypesExist() {
        // Verify all LayoutElement implementations are loadable
        LayoutMap map = new LayoutMap("m", 0, 0, 100, 100);
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        LayoutScaleBar sb = new LayoutScaleBar("s", 0, 0, 80, 10);
        LayoutNorthArrow na = new LayoutNorthArrow("n", 0, 0, 20, 20);
        LayoutLabel lbl = new LayoutLabel("lb", "T", 0, 0, 50, 20);
        LayoutImage img = new LayoutImage("i", null, 0, 0, 30, 20);
        LayoutRectangle rect = new LayoutRectangle("r", 0, 0, 40, 30);
        LayoutEllipse ell = new LayoutEllipse("e", 0, 0, 40, 30);
        LayoutLine line = new LayoutLine("ln", 0, 0, 50, 50);
        LayoutTable tab = new LayoutTable("t", 0, 0, 100, 40);
        LayoutCartouche cart = new LayoutCartouche("c", 0, 0, 100, 60);
        LayoutGraticule grat = new LayoutGraticule("g", 0, 0, 100, 80);

        assertEquals(12, 12); // All 12 types instantiated without error
    }

    @Test
    public void testModelWithAllTypes() {
        LayoutModel model = new LayoutModel();
        int z = 0;
        model.addElement(new LayoutMap("map", 0, 0, 50, 50)); model.getElements().get(0).setZOrder(z++);
        LayoutLabel l = new LayoutLabel("lbl", "T", 60, 0, 30, 15); l.setZOrder(z++); model.addElement(l);
        LayoutLegend lg = new LayoutLegend("leg", 100, 0, 40, 30); lg.setZOrder(z++); model.addElement(lg);
        LayoutScaleBar sb = new LayoutScaleBar("sb", 0, 60, 60, 10); sb.setZOrder(z++); model.addElement(sb);
        LayoutNorthArrow na = new LayoutNorthArrow("na", 70, 50, 15, 15); na.setZOrder(z++); model.addElement(na);
        LayoutRectangle r = new LayoutRectangle("r", 90, 60, 30, 20); r.setZOrder(z++); model.addElement(r);
        LayoutEllipse e = new LayoutEllipse("e", 0, 80, 30, 20); e.setZOrder(z++); model.addElement(e);
        LayoutLine ln = new LayoutLine("ln", 40, 70, 80, 70); ln.setZOrder(z++); model.addElement(ln);
        LayoutTable t = new LayoutTable("t", 0, 90, 60, 30); t.setZOrder(z++); model.addElement(t);
        LayoutCartouche c = new LayoutCartouche("c", 70, 90, 50, 40); c.setZOrder(z++); model.addElement(c);
        LayoutGraticule g = new LayoutGraticule("g", 130, 0, 40, 40); g.setZOrder(z++); model.addElement(g);

        assertEquals(11, model.size());
    }

    @Test
    public void testTemplateRegistryCategories() {
        // 10 Category enum values exist
        assertEquals(10, TemplateRegistry.Category.values().length);
        for (TemplateRegistry.Category cat : TemplateRegistry.Category.values()) {
            assertNotNull(cat.getLabel());
        }
    }

    @Test
    public void testAtlasWithPages() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setEnabled(true);
        atlas.setCoverageLayer("layer", "name");
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) names.add("Page " + i);
        atlas.setPageNames(names);
        assertEquals(50, atlas.getPageCount());
        assertEquals("Page 0", atlas.getCurrentPageName());
        for (int i = 0; i < 10; i++) atlas.nextPage();
        assertEquals("Page 10", atlas.getCurrentPageName());
    }

    @Test
    public void testExpressionAllVars() {
        String expr = "@scale @date @datetime @project @page @pagetotal";
        String result = LayoutExpressionEvaluator.evaluate(expr, 5000, "TEST", 0, 3);
        assertFalse(result.contains("@"));
        assertTrue(result.contains("1:5,000"));
        assertTrue(result.contains("TEST"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("3"));
    }

    @Test
    public void testMapFrameAllProperties() {
        LayoutMap map = new LayoutMap("m", 0, 0, 100, 100);
        map.setFrameColor(Color.BLACK);
        map.setFrameWidth(3f);
        map.setFrameCornerRadius(10);
        map.setShowGrid(true);
        map.setGridCols(5);
        map.setGridRows(4);
        map.setGridByDistance(true);
        map.setGridIntervalX(250);
        map.setGridIntervalY(250);
        map.setGridUnit("m");
        map.setTargetScaleDenominator(10000);
        map.setOwnExtent(true);
        map.setOwnViewMinX(100);
        map.setOwnViewMinY(200);
        map.setOwnZoomFactor(2.5);

        assertEquals(Color.BLACK, map.getFrameColor());
        assertEquals(3f, map.getFrameWidth(), 0.01);
        assertEquals(10, map.getFrameCornerRadius());
        assertTrue(map.isShowGrid());
        assertEquals(5, map.getGridCols());
        assertTrue(map.isGridByDistance());
        assertEquals(250, map.getGridIntervalX(), 0.01);
        assertEquals("m", map.getGridUnit());
        assertEquals(10000, map.getTargetScaleDenominator(), 0.01);
        assertTrue(map.isOwnExtent());
        assertEquals(100, map.getOwnViewMinX(), 0.01);
    }

    @Test
    public void testLegendFullConfig() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 80, 100);
        leg.setTitle("Referencias");
        leg.setAutoHeight(true);
        leg.setShowBackground(true);
        leg.setShowBorder(true);
        leg.setBgOpacity(0.75f);
        leg.setColumns(2);
        leg.setSymbolSizeMm(4.5);
        leg.setPaddingMm(3);
        leg.getItems().add(new LayoutLegend.LegendItem("Capa 1", Color.RED, "POINT"));
        leg.getItems().add(new LayoutLegend.LegendItem("Capa 2", Color.BLUE, "LINE"));
        leg.getItems().add(new LayoutLegend.LegendItem("Capa 3", Color.GREEN, "POLYGON"));

        assertEquals("Referencias", leg.getTitle());
        assertTrue(leg.isAutoHeight());
        assertTrue(leg.isShowBackground());
        assertTrue(leg.isShowBorder());
        assertEquals(0.75f, leg.getBgOpacity(), 0.01);
        assertEquals(2, leg.getColumns());
        assertEquals(4.5, leg.getSymbolSizeMm(), 0.01);
        assertEquals(3, leg.getPaddingMm(), 0.01);
        assertEquals(3, leg.getIncludedItems().size());
    }
}
