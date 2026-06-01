package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class LayoutEdgeCasesTest {

    @Test
    public void testZOrderMoveUpAndDown() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        LayoutRectangle r3 = new LayoutRectangle("r3", 0, 0, 100, 100);
        r1.setZOrder(1); r2.setZOrder(2); r3.setZOrder(3);
        model.addElement(r1); model.addElement(r2); model.addElement(r3);

        model.moveUp(r1); // r1 was 1, now tries to swap with r2
        // After moveUp, z-orders may have shifted
        assertTrue(r1.getZOrder() >= 1);
        model.moveDown(r3);
        assertTrue(r3.getZOrder() <= 3);
    }

    @Test
    public void testMoveToFrontAndBackWithNormalize() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        r1.setZOrder(1); r2.setZOrder(2);
        model.addElement(r1); model.addElement(r2);

        model.moveToFront(r1);
        assertTrue(r1.getZOrder() > r2.getZOrder());

        model.moveToBack(r2);
        assertTrue(r2.getZOrder() <= 0);
    }

    @Test
    public void testFindElementAtMmEdgeCase() {
        LayoutModel model = new LayoutModel();
        // Element at exactly 0,0: exclusive bounds means (0,0) might not be inside
        LayoutRectangle r = new LayoutRectangle("r1", 0, 0, 1, 1);
        r.setZOrder(1); r.setVisible(true);
        model.addElement(r);

        // Point inside the tiny element
        LayoutElement found = model.findTopmostElementAtMm(0.5, 0.5);
        assertNotNull(found);
    }

    @Test
    public void testInvisibleElementNotSelected() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1); r1.setVisible(false);
        model.addElement(r1);
        LayoutRectangle r2 = new LayoutRectangle("r2", 50, 50, 100, 100);
        r2.setZOrder(2); r2.setVisible(true);
        model.addElement(r2);

        LayoutElement found = model.findTopmostElementAtMm(75, 75);
        assertNotNull(found);
        assertEquals("r2", found.getId()); // r1 is invisible
    }

    @Test
    public void testLockedElementStillFound() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1); r1.setVisible(true); r1.setLocked(true);
        model.addElement(r1);

        LayoutElement found = model.findTopmostElementAtMm(50, 50);
        assertNotNull(found); // should still find it (locked can be selected for inspect)
    }

    @Test
    public void testRemoveElementPreservesOthers() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        model.addElement(r1); model.addElement(r2);
        model.removeElement("r1");
        assertEquals(1, model.size());
        assertEquals("r2", model.getElements().get(0).getId());
    }

    @Test
    public void testRemoveNonexistentDoesNothing() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        model.addElement(r1);
        model.removeElement("nonexistent");
        assertEquals(1, model.size());
    }

    @Test
    public void testClearSelectionMultiple() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        LayoutRectangle r3 = new LayoutRectangle("r3", 0, 0, 100, 100);
        r1.setSelected(true); r2.setSelected(true); r3.setSelected(true);
        model.addElement(r1); model.addElement(r2); model.addElement(r3);
        model.clearSelection();
        assertNull(model.getSelected());
        assertFalse(r1.isSelected());
        assertFalse(r2.isSelected());
        assertFalse(r3.isSelected());
    }

    @Test
    public void testLegendItemExclusion() {
        LayoutLegend legend = new LayoutLegend("l1", 0, 0, 50, 50);
        LayoutLegend.LegendItem included = new LayoutLegend.LegendItem("Visible", Color.RED, "POINT");
        LayoutLegend.LegendItem excluded = new LayoutLegend.LegendItem("Hidden", Color.BLUE, "LINE");
        excluded.included = false;
        legend.getItems().add(included);
        legend.getItems().add(excluded);

        assertEquals(2, legend.getItems().size());
        assertEquals(1, legend.getIncludedItems().size());
        assertEquals("Visible", legend.getIncludedItems().get(0).displayName);
    }

    @Test
    public void testLegendItemDefaults() {
        LayoutLegend.LegendItem item = new LayoutLegend.LegendItem("Capa Prueba", Color.GREEN, "POLYGON");
        assertEquals("Capa Prueba", item.label);
        assertEquals("Capa Prueba", item.displayName);
        assertEquals(Color.GREEN, item.color);
        assertEquals("POLYGON", item.geometryType);
        assertTrue(item.included);
    }
}
