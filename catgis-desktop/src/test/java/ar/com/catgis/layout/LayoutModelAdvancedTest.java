package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class LayoutModelAdvancedTest {

    @Test
    public void testNextZConsistency() {
        LayoutModel model = new LayoutModel();
        assertEquals(1, model.nextZ());
        model.addElement(new LayoutRectangle("r1", 0, 0, 100, 100));
        model.getElements().get(0).setZOrder(5);
        assertEquals(6, model.nextZ());
    }

    @Test
    public void testGetSelectedReturnsFirst() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        r1.setSelected(true); r2.setSelected(true);
        model.addElement(r1); model.addElement(r2);
        assertNotNull(model.getSelected());
    }

    @Test
    public void testDuplicateZOrderHandling() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 100, 100);
        r1.setZOrder(1); r2.setZOrder(1); // duplicate
        model.addElement(r1); model.addElement(r2);
        assertEquals(2, model.getVisibleElementsSortedByZ().size());
    }

    @Test
    public void testEmptyModelOperations() {
        LayoutModel model = new LayoutModel();
        assertEquals(0, model.size());
        assertEquals(0, model.getElements().size());
        assertNull(model.getSelected());
        assertNull(model.findTopmostElementAtMm(0, 0));
        assertEquals(0, model.getVisibleElementsSortedByZ().size());
        model.clearSelection(); // should not throw
        model.removeElement("nothing"); // should not throw
    }

    @Test
    public void testFindHoverAtMm() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r = new LayoutRectangle("r1", 10, 10, 100, 100);
        r.setZOrder(1); r.setVisible(true);
        model.addElement(r);
        LayoutElement found = model.findHoverAtMm(50, 50);
        assertNotNull(found);
        assertEquals("r1", found.getId());
    }

    @Test
    public void testFindElementAtMmDelegates() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r = new LayoutRectangle("r1", 10, 10, 100, 100);
        r.setZOrder(1); r.setVisible(true);
        model.addElement(r);
        assertNotNull(model.findElementAtMm(50, 50));
        assertNull(model.findElementAtMm(5, 5));
    }
}
