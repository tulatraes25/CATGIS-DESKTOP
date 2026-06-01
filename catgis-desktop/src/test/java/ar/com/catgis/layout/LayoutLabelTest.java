package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

public class LayoutLabelTest {

    @Test
    public void testDefaults() {
        LayoutLabel l = new LayoutLabel("l1", "Hello", 10, 20, 150, 30);
        assertEquals("Hello", l.getText());
        assertEquals("l1", l.getId());
        assertEquals(10, l.getBoundsMm().x, 0.01);
    }

    @Test
    public void testSetText() {
        LayoutLabel l = new LayoutLabel("l1", "Old", 10, 10, 100, 20);
        l.setText("New Text");
        assertEquals("New Text", l.getText());
    }

    @Test
    public void testFont() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        Font f = new Font("Serif", Font.BOLD | Font.ITALIC, 18);
        l.setFont(f);
        assertEquals("Serif", l.getFont().getFamily());
        assertEquals(18, l.getFont().getSize());
        assertTrue(l.getFont().isBold());
        assertTrue(l.getFont().isItalic());
    }

    @Test
    public void testColor() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        l.setColor(Color.RED);
        assertEquals(Color.RED, l.getColor());
    }

    @Test
    public void testContains() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        assertTrue(l.containsMm(50, 15));
        assertFalse(l.containsMm(5, 5));
    }

    @Test
    public void testVisibilityLockSelection() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        assertTrue(l.isVisible());
        l.setVisible(false);
        assertFalse(l.isVisible());
        l.setLocked(true);
        assertTrue(l.isLocked());
        l.setSelected(true);
        assertTrue(l.isSelected());
    }

    @Test
    public void testBounds() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 20, 100, 50);
        assertEquals(10, l.getBoundsMm().x, 0.01);
        assertEquals(20, l.getBoundsMm().y, 0.01);
        assertEquals(100, l.getBoundsMm().width, 0.01);
        assertEquals(50, l.getBoundsMm().height, 0.01);
        l.setBoundsMm(0, 0, 200, 80);
        assertEquals(200, l.getBoundsMm().width, 0.01);
    }

    @Test
    public void testZOrder() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        l.setZOrder(5);
        assertEquals(5, l.getZOrder());
    }

    @Test
    public void testName() {
        LayoutLabel l = new LayoutLabel("l1", "Text", 10, 10, 100, 20);
        l.setName("Titulo Principal");
        assertEquals("Titulo Principal", l.getName());
    }
}
