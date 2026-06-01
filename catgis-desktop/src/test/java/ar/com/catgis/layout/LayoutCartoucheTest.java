package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;

public class LayoutCartoucheTest {

    @Test
    public void testDefaultFields() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        assertEquals(6, c.getFields().size());
        assertEquals("", c.getField("Estudio"));
        assertEquals("", c.getField("Empresa"));
        assertEquals("Vista actual del proyecto", c.getField("Fuente"));
    }

    @Test
    public void testSetAndGetFields() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        c.setField("Estudio", "GeoTech S.A.");
        c.setField("Empresa", "CATGIS");
        c.setField("Cartografo", "Juan Perez");
        assertEquals("GeoTech S.A.", c.getField("Estudio"));
        assertEquals("CATGIS", c.getField("Empresa"));
        assertEquals("Juan Perez", c.getField("Cartografo"));
    }

    @Test
    public void testNullFieldValue() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        c.setField("Estudio", null);
        assertEquals("", c.getField("Estudio"));
        assertEquals("", c.getField("NoExiste")); // missing key returns ""
    }

    @Test
    public void testFontProperties() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        c.setLabelFont(new Font("Serif", Font.BOLD, 12));
        c.setValueFont(new Font("SansSerif", Font.ITALIC, 10));
        assertEquals("Serif", c.getLabelFont().getFamily());
        assertEquals("SansSerif", c.getValueFont().getFamily());
        assertTrue(c.getLabelFont().isBold());
        assertTrue(c.getValueFont().isItalic());
    }

    @Test
    public void testBorderProperties() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        assertTrue(c.isShowBorder());
        c.setShowBorder(false);
        assertFalse(c.isShowBorder());
        c.setBorderWidth(3f);
        assertEquals(3f, c.getBorderWidth(), 0.01);
        c.setBorderColor(Color.RED);
        assertEquals(Color.RED, c.getBorderColor());
    }

    @Test
    public void testBackground() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        Color bg = new Color(255, 255, 200, 128);
        c.setBgColor(bg);
        assertEquals(bg, c.getBgColor());
    }

    @Test
    public void testPadding() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        assertEquals(1.5, c.getPaddingMm(), 0.01);
        c.setPaddingMm(3);
        assertEquals(3, c.getPaddingMm(), 0.01);
    }

    @Test
    public void testVisibilityLockSelection() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        assertTrue(c.isVisible());
        c.setVisible(false);
        assertFalse(c.isVisible());
        assertFalse(c.isLocked());
        c.setLocked(true);
        assertTrue(c.isLocked());
        assertFalse(c.isSelected());
        c.setSelected(true);
        assertTrue(c.isSelected());
    }

    @Test
    public void testBoundsAndContains() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 100, 80);
        assertEquals(10, c.getBoundsMm().x, 0.01);
        assertEquals(10, c.getBoundsMm().y, 0.01);
        assertTrue(c.containsMm(50, 50));
        assertFalse(c.containsMm(5, 5));
        c.setBoundsMm(0, 0, 200, 100);
        assertEquals(200, c.getBoundsMm().width, 0.01);
    }

    @Test
    public void testGetFieldsMapMutability() {
        LayoutCartouche c = new LayoutCartouche("c1", 10, 10, 200, 80);
        var fields = c.getFields();
        fields.put("Nuevo", "Valor"); // modifying returned map
        assertEquals("Valor", c.getField("Nuevo")); // should reflect on cartouche
    }
}
