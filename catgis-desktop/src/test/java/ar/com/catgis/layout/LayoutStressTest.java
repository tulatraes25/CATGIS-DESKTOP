package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutStressTest {

    @Test
    public void testManyElements() {
        LayoutModel model = new LayoutModel();
        for (int i = 0; i < 50; i++) {
            LayoutRectangle r = new LayoutRectangle("r" + i, i * 5, 10, 40, 30);
            r.setZOrder(i);
            r.setVisible(i % 3 != 0); // 2/3 visible
            model.addElement(r);
        }
        assertEquals(50, model.size());
        assertTrue(model.getVisibleElementsSortedByZ().size() > 0);
        assertTrue(model.getVisibleElementsSortedByZ().size() < 50);
    }

    @Test
    public void testElementReuse() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r = new LayoutRectangle("r1", 0, 0, 100, 100);
        r.setZOrder(1);
        model.addElement(r);

        // Move element to front multiple times
        for (int i = 0; i < 100; i++) model.moveToFront(r);
        assertTrue(r.getZOrder() >= 1);

        // Move to back multiple times
        for (int i = 0; i < 100; i++) model.moveToBack(r);
        // Normalize should have fixed z-order
    }

    @Test
    public void testSelectionStateTransitions() {
        LayoutRectangle r = new LayoutRectangle("r1", 0, 0, 100, 100);
        r.setVisible(true);
        r.setLocked(false);

        assertFalse(r.isSelected());
        r.setSelected(true);
        assertTrue(r.isSelected());
        r.setLocked(true);
        assertTrue(r.isLocked());
        r.setSelected(false);
        assertFalse(r.isSelected());
        // Locked element can still be deselected
    }

    @Test
    public void testBoundsEdgeCases() {
        LayoutRectangle r = new LayoutRectangle("r1", Double.MAX_VALUE / 2, Double.MAX_VALUE / 2, 1, 1);
        assertTrue(r.getBoundsMm().width > 0);
        assertTrue(r.getBoundsMm().height > 0);

        // Very large bounds
        r.setBoundsMm(-1000, -1000, 100000, 100000);
        assertEquals(100000, r.getBoundsMm().width, 0.01);
    }
}
