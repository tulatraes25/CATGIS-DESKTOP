package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.geom.Rectangle2D;

public class LayoutModelTest {

    @Test
    public void testAddAndGetElements() {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("l1", "Test", 10, 10, 100, 20);
        model.addElement(label);
        assertEquals(1, model.size());
        assertEquals("l1", model.getElements().get(0).getId());
    }

    @Test
    public void testRemoveElement() {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("l1", "Test", 10, 10, 100, 20);
        model.addElement(label);
        model.removeElement("l1");
        assertEquals(0, model.size());
    }

    @Test
    public void testZOrderAndVisibility() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1); r1.setVisible(true);
        LayoutRectangle r2 = new LayoutRectangle("r2", 10, 10, 80, 80);
        r2.setZOrder(2); r2.setVisible(true);
        LayoutRectangle r3 = new LayoutRectangle("r3", 20, 20, 60, 60);
        r3.setZOrder(3); r3.setVisible(false);
        model.addElement(r1); model.addElement(r2); model.addElement(r3);

        assertEquals(2, model.getVisibleElementsSortedByZ().size());
        assertEquals("r2", model.getVisibleElementsSortedByZ().get(1).getId());
        assertEquals("r1", model.getVisibleElementsSortedByZ().get(0).getId());
    }

    @Test
    public void testFindTopmostElementAtMm() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1); r1.setVisible(true);
        LayoutRectangle r2 = new LayoutRectangle("r2", 10, 10, 80, 80);
        r2.setZOrder(2); r2.setVisible(true);
        model.addElement(r1); model.addElement(r2);

        // Point inside both should return r2 (higher z-order)
        LayoutElement found = model.findTopmostElementAtMm(50, 50);
        assertNotNull(found);
        assertEquals("r2", found.getId());

        // Point only in r1
        found = model.findTopmostElementAtMm(5, 5);
        assertNotNull(found);
        assertEquals("r1", found.getId());
    }

    @Test
    public void testFindTopmostElementAtMmWithTolerance() {
        LayoutModel model = new LayoutModel();
        LayoutLabel tiny = new LayoutLabel("tiny", "x", 50, 50, 1, 1); // Very small
        tiny.setZOrder(1); tiny.setVisible(true);
        model.addElement(tiny);

        // Should find the tiny element via 5mm tolerance fallback
        LayoutElement found = model.findTopmostElementAtMm(50.5, 50.5);
        assertNotNull(found);
        assertEquals("tiny", found.getId());
    }

    @Test
    public void testMoveToFrontAndBack() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        model.addElement(r1); model.addElement(r2);
        r1.setZOrder(1); r2.setZOrder(2);

        model.moveToFront(r1);
        assertTrue(r1.getZOrder() > r2.getZOrder());

        model.moveToBack(r2);
        assertTrue(r2.getZOrder() < r1.getZOrder());
    }

    @Test
    public void testClearSelection() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        r1.setSelected(true); r2.setSelected(true);
        model.addElement(r1); model.addElement(r2);
        model.clearSelection();
        assertNull(model.getSelected());
    }

    @Test
    public void testNextZ() {
        LayoutModel model = new LayoutModel();
        assertEquals(1, model.nextZ());
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(5); model.addElement(r1);
        assertEquals(6, model.nextZ());
    }
}
