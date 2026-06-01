package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutShapeTests {

    @Test
    public void testEllipseFillAndBorder() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 80);
        assertEquals(new java.awt.Color(59, 130, 246, 60), e.getFillColor());
        assertEquals(new java.awt.Color(59, 130, 246), e.getBorderColor());
        assertEquals(1.5f, e.getBorderWidth(), 0.01);
    }

    @Test
    public void testEllipseSetColors() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 80);
        e.setFillColor(java.awt.Color.GREEN);
        e.setBorderColor(java.awt.Color.BLACK);
        e.setBorderWidth(3f);
        assertEquals(java.awt.Color.GREEN, e.getFillColor());
        assertEquals(java.awt.Color.BLACK, e.getBorderColor());
        assertEquals(3f, e.getBorderWidth(), 0.01);
    }

    @Test
    public void testEllipseZeroBorder() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 80);
        e.setBorderWidth(0f);
        assertEquals(0f, e.getBorderWidth(), 0.01);
    }

    @Test
    public void testEllipseContainsCenter() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 100, 50);
        assertTrue(e.containsMm(60, 35));   // center
        assertTrue(e.containsMm(10, 35));   // left
        assertTrue(e.containsMm(60, 10));   // top
        assertFalse(e.containsMm(-10, 10)); // outside
        assertFalse(e.containsMm(200, 35)); // far right
    }

    @Test
    public void testEllipseContainZeroSize() {
        LayoutEllipse e = new LayoutEllipse("e1", 10, 10, 0, 0);
        assertFalse(e.containsMm(10, 10));
    }

    @Test
    public void testLineEndpoints() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        l.setEndpoints(10, 10, 200, 50);
        assertTrue(l.containsMm(105, 30)); // on the line
    }

    @Test
    public void testLineColorAndWidth() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        l.setColor(java.awt.Color.RED);
        l.setLineWidth(5f);
        assertEquals(java.awt.Color.RED, l.getColor());
        assertEquals(5f, l.getLineWidth(), 0.01);
    }

    @Test
    public void testLineDashed() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        assertFalse(l.isDashed());
        l.setDashed(true);
        assertTrue(l.isDashed());
        l.setDashed(false);
        assertFalse(l.isDashed());
    }

    @Test
    public void testLineContainsVertical() {
        LayoutLine l = new LayoutLine("l1", 50, 0, 50, 100);
        assertTrue(l.containsMm(50, 50));   // on line
        assertTrue(l.containsMm(51, 50));   // near line (1mm away, within 3mm tolerance)
        assertFalse(l.containsMm(70, 50));  // far from line
    }

    @Test
    public void testLineContainsHorizontal() {
        LayoutLine l = new LayoutLine("l1", 0, 50, 100, 50);
        assertTrue(l.containsMm(50, 50));   // on line
        assertFalse(l.containsMm(50, 70));  // far from line
    }

    @Test
    public void testLineBoundsUpdateOnSetEndpoints() {
        LayoutLine l = new LayoutLine("l1", 0, 0, 100, 100);
        l.setEndpoints(20, 30, 80, 90);
        assertTrue(l.getBoundsMm().x <= 20);
        assertTrue(l.getBoundsMm().y <= 30);
        assertTrue(l.getBoundsMm().x + l.getBoundsMm().width >= 80);
    }

    @Test
    public void testLineBoundsUpdateOnSetBounds() {
        LayoutLine l = new LayoutLine("l1", 10, 10, 50, 50);
        double oldX = l.getBoundsMm().x;
        l.setBoundsMm(0, 0, 100, 100);
        assertTrue(l.getBoundsMm().width > 0);
    }
}
