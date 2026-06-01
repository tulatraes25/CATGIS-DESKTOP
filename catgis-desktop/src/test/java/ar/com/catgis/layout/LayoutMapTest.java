package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class LayoutMapTest {

    @Test
    public void testDefaults() {
        LayoutMap map = new LayoutMap("m1", 10, 20, 100, 80);
        assertEquals("m1", map.getId());
        assertEquals("m1", map.getName());
        assertEquals(10, map.getBoundsMm().x, 0.01);
        assertEquals(20, map.getBoundsMm().y, 0.01);
        assertEquals(100, map.getBoundsMm().width, 0.01);
        assertEquals(80, map.getBoundsMm().height, 0.01);
        assertTrue(map.isVisible());
        assertFalse(map.isLocked());
        assertFalse(map.isSelected());
    }

    @Test
    public void testGridDefaults() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        assertFalse(map.isShowGrid());
        assertEquals(3, map.getGridCols());
        assertEquals(3, map.getGridRows());
    }

    @Test
    public void testGridSetters() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setShowGrid(true);
        assertTrue(map.isShowGrid());
        map.setGridCols(5);
        assertEquals(5, map.getGridCols());
        map.setGridRows(6);
        assertEquals(6, map.getGridRows());
    }

    @Test
    public void testGridColor() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        Color c = new Color(255, 0, 0, 100);
        map.setGridColor(c);
        assertEquals(c, map.getGridColor());
    }

    @Test
    public void testDistanceGrid() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setGridByDistance(true);
        assertTrue(map.isGridByDistance());
        map.setGridIntervalX(250);
        assertEquals(250, map.getGridIntervalX(), 0.01);
        map.setGridIntervalY(500);
        assertEquals(500, map.getGridIntervalY(), 0.01);
        map.setGridUnit("km");
        assertEquals("km", map.getGridUnit());
    }

    @Test
    public void testTargetScale() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setTargetScaleDenominator(5000);
        assertEquals(5000, map.getTargetScaleDenominator(), 0.01);
    }

    @Test
    public void testOwnExtent() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        assertFalse(map.isOwnExtent());
        map.setOwnExtent(true);
        assertTrue(map.isOwnExtent());
        map.setOwnViewMinX(100);
        map.setOwnViewMinY(200);
        map.setOwnZoomFactor(2.5);
        assertEquals(100, map.getOwnViewMinX(), 0.01);
        assertEquals(200, map.getOwnViewMinY(), 0.01);
        assertEquals(2.5, map.getOwnZoomFactor(), 0.01);
    }
}
