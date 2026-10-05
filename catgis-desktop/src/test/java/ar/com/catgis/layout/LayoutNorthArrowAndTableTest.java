package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutNorthArrowAndTableTest {

    @Test
    public void testNorthArrowDefaults() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 250, 170, 20, 20);
        assertEquals("n1", n.getId());
        assertEquals(250, n.getBoundsMm().x, 0.01);
        assertEquals(170, n.getBoundsMm().y, 0.01);
    }

    @Test
    public void testNorthArrowContains() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 100, 100, 40, 40);
        assertTrue(n.containsMm(120, 120));
        assertFalse(n.containsMm(80, 80));
    }

    @Test
    public void testNorthArrowVisibility() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 0, 0, 20, 20);
        assertTrue(n.isVisible());
        n.setVisible(false);
        assertFalse(n.isVisible());
    }

    @Test
    public void testNorthArrowLockSelection() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 0, 0, 20, 20);
        n.setLocked(true);
        assertTrue(n.isLocked());
        n.setSelected(true);
        assertTrue(n.isSelected());
    }

    @Test
    public void testTableDefaults() {
        LayoutTable t = new LayoutTable("t1", 10, 100, 267, 80);
        assertEquals("t1", t.getId());
        assertTrue(t.getRows().isEmpty());
    }

    @Test
    public void testTableProperties() {
        LayoutTable t = new LayoutTable("t1", 10, 100, 267, 80);
        t.setShowBorders(true);
        t.setAlternateRows(true);
        t.setFirstRowIsHeader(false);
        t.setTextColor(new java.awt.Color(100, 100, 100));
        t.setBorderColor(new java.awt.Color(200, 200, 200));
        t.setMaxVisibleRows(10);
        assertTrue(t.getRows().isEmpty()); // still empty, no data loaded
    }

    @Test
    public void testTableContains() {
        LayoutTable t = new LayoutTable("t1", 10, 100, 200, 80);
        assertTrue(t.containsMm(100, 140));
        assertFalse(t.containsMm(5, 5));
    }

    @Test
    public void testTableFont() {
        LayoutTable t = new LayoutTable("t1", 10, 100, 200, 80);
        java.awt.Font f = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 10);
        t.setFont(f);
        t.setHeaderFont(f.deriveFont(java.awt.Font.BOLD, 11f));
        // no getters - just verify no exception
    }
}
