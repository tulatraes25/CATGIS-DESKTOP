package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutImageAndGuideLineTest {

    @Test
    public void testLayoutImage() {
        LayoutImage img = new LayoutImage("img1", null, 10, 20, 50, 40);
        assertEquals("img1", img.getId());
        assertTrue(img.containsMm(35, 40));
        img.setVisible(false);
        assertFalse(img.isVisible());
    }

    @Test
    public void testGuideLine() {
        GuideLine g = new GuideLine("g1", 100, GuideLine.Orientation.VERTICAL);
        assertEquals("g1", g.id);
        assertEquals(100, g.mmPos, 0.01);
        assertFalse(g.locked);
        g.locked = true;
        assertTrue(g.locked);
    }

    @Test
    public void testGuideLineOrientation() {
        GuideLine h = new GuideLine("h1", 50, GuideLine.Orientation.HORIZONTAL);
        assertEquals(GuideLine.Orientation.HORIZONTAL, h.orientation);

        GuideLine v = new GuideLine("v1", 75, GuideLine.Orientation.VERTICAL);
        assertEquals(GuideLine.Orientation.VERTICAL, v.orientation);
    }

    @Test
    public void testGuideLineContainsPx() {
        GuideLine v = new GuideLine("v1", 100, GuideLine.Orientation.VERTICAL);
        // At 200 DPI, 100mm = 787px. Tests proximity detection
        assertTrue(v.containsPx(787, 300, 0, 0, 1000, 800, 200, 1.0, 8));
        assertFalse(v.containsPx(800, 300, 0, 0, 1000, 800, 200, 1.0, 8));
    }

    @Test
    public void testRulerRenderer() {
        assertEquals(18, RulerRenderer.getRulerSize());
    }
}
