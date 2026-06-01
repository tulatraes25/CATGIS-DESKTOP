package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;

public class LayoutScaleBarTest {

    @Test
    public void testDefaults() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        assertEquals("s1", s.getId());
        assertEquals(10000, s.getMapScaleDenominator(), 0.01);
        assertEquals("m", s.getUnitLabel());
        assertEquals(4, s.getSegments());
    }

    @Test
    public void testScaleDenominator() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setMapScaleDenominator(25000);
        assertEquals(25000, s.getMapScaleDenominator(), 0.01);
        s.setMapScaleDenominator(-1); // should clamp to > 0
        assertEquals(10000, s.getMapScaleDenominator(), 0.01); // default for invalid
    }

    @Test
    public void testSegments() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setSegments(6);
        assertEquals(6, s.getSegments());
        s.setSegments(1); // should clamp to min 2
        assertEquals(2, s.getSegments());
    }

    @Test
    public void testUnitLabel() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setUnitLabel("km");
        assertEquals("km", s.getUnitLabel());
    }

    @Test
    public void testColorAndFont() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setColor(Color.RED);
        assertEquals(Color.RED, s.getColor());
        Font f = new Font("Serif", Font.BOLD, 14);
        s.setFont(f);
        assertEquals("Serif", s.getFont().getFamily());
    }

    @Test
    public void testMetersPerUnit() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 120, 12);
        s.setMetersPerUnit(1000);
        assertEquals(1000, s.getMetersPerUnit(), 0.01);
    }

    @Test
    public void testContains() {
        LayoutScaleBar s = new LayoutScaleBar("s1", 10, 100, 80, 12);
        assertTrue(s.containsMm(50, 106));
        assertFalse(s.containsMm(5, 105));
    }
}
