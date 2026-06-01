package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.geom.Rectangle2D;

public class LayoutElementTests {

    @Test
    public void testLayoutRectangle() {
        LayoutRectangle r = new LayoutRectangle("r1", 10, 20, 100, 60);
        assertEquals("r1", r.getId());
        assertEquals(100, r.getBoundsMm().width, 0.01);
        assertTrue(r.containsMm(50, 50));
        assertFalse(r.containsMm(5, 5));
        r.setBoundsMm(0, 0, 200, 100);
        assertEquals(200, r.getBoundsMm().width, 0.01);
    }

    @Test
    public void testLayoutEllipse() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 80);
        assertTrue(e.containsMm(60, 50));   // center
        assertTrue(e.containsMm(10, 50));   // left edge
        assertFalse(e.containsMm(5, 50));   // outside
        e.setBorderWidth(3f);
        assertEquals(3f, e.getBorderWidth(), 0.01);
    }

    @Test
    public void testLayoutLine() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        assertTrue(l.containsMm(50, 50));      // on line
        assertTrue(l.containsMm(48, 52));      // near line within tolerance
        assertFalse(l.containsMm(0, 200));     // far away
        l.setDashed(true);
        assertTrue(l.isDashed());
        l.setLineWidth(3f);
        assertEquals(3f, l.getLineWidth(), 0.01);
    }

    @Test
    public void testLayoutNorthArrow() {
        LayoutNorthArrow n = new LayoutNorthArrow("n1", 10, 10, 30, 30);
        assertEquals("n1", n.getId());
        assertTrue(n.containsMm(25, 25));      // center
        assertFalse(n.containsMm(0, 0));        // outside
    }

    @Test
    public void testLayoutScaleBar() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 180, 100, 12);
        s.setMapScaleDenominator(25000);
        assertEquals(25000, s.getMapScaleDenominator(), 0.01);
        assertEquals("m", s.getUnitLabel());
        assertEquals(4, s.getSegments());
    }

    @Test
    public void testLayoutCartouche() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 170, 270, 55);
        assertEquals(6, c.getFields().size());
        assertTrue(c.getFields().containsKey("Estudio"));
        c.setField("Empresa", "CATGIS S.A.");
        assertEquals("CATGIS S.A.", c.getField("Empresa"));
    }

    @Test
    public void testLayoutGraticule() {
        LayoutGraticule g = new LayoutGraticule("g1", 0, 0, 200, 150);
        assertTrue(g.isGeographic());
        assertEquals(1.0, g.getIntervalX(), 0.01);
        g.setIntervalX(0.5);
        assertEquals(0.5, g.getIntervalX(), 0.01);
        g.setShowLabels(false);
        assertFalse(g.isShowLabels());
    }

    @Test
    public void testLayoutTable() {
        LayoutTable t = new LayoutTable("t1", 10, 100, 200, 80);
        t.setFirstRowIsHeader(true);
        t.setShowBorders(true);
        t.setAlternateRows(true);
        assertTrue(t.getRows().isEmpty());
    }
}
