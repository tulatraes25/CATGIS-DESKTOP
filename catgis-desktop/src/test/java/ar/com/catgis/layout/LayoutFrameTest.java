package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class LayoutFrameTest {

    @Test
    public void testMapFrameDefaults() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        assertEquals(0, map.getFrameColor().getAlpha()); // transparent = no frame
        assertEquals(1f, map.getFrameWidth(), 0.01);
        assertEquals(0, map.getFrameCornerRadius());
    }

    @Test
    public void testMapFrameStyle() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setFrameColor(Color.BLACK);
        map.setFrameWidth(2f);
        map.setFrameCornerRadius(8);
        assertEquals(Color.BLACK, map.getFrameColor());
        assertEquals(2f, map.getFrameWidth(), 0.01);
        assertEquals(8, map.getFrameCornerRadius());
    }

    @Test
    public void testMapFrameRoundCorners() {
        LayoutMap map = new LayoutMap("m1", 0, 0, 100, 100);
        map.setFrameColor(new Color(50, 50, 50));
        map.setFrameWidth(3f);
        map.setFrameCornerRadius(12);
        assertEquals(12, map.getFrameCornerRadius());
    }

    @Test
    public void testLabelDynamicExpression() {
        LayoutLabel l = new LayoutLabel("l1", "Static", 10, 10, 100, 20);
        assertNull(l.getDynamicExpression());
        l.setDynamicExpression("@scale");
        assertEquals("@scale", l.getDynamicExpression());
    }
}
