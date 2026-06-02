package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class LayoutLegendReorderTest {

    @Test
    public void testMoveItemDown() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        LayoutLegend.LegendItem a = new LayoutLegend.LegendItem("A", Color.RED, "POINT");
        LayoutLegend.LegendItem b = new LayoutLegend.LegendItem("B", Color.BLUE, "LINE");
        LayoutLegend.LegendItem c = new LayoutLegend.LegendItem("C", Color.GREEN, "POLYGON");
        leg.getItems().add(a); leg.getItems().add(b); leg.getItems().add(c);

        leg.moveItemDown(0); // move A down
        assertEquals("B", leg.getItems().get(0).label);
        assertEquals("A", leg.getItems().get(1).label);

        leg.moveItemDown(1); // move A down again
        assertEquals("B", leg.getItems().get(0).label);
        assertEquals("C", leg.getItems().get(1).label);
        assertEquals("A", leg.getItems().get(2).label);
    }

    @Test
    public void testMoveItemUp() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        LayoutLegend.LegendItem a = new LayoutLegend.LegendItem("A", Color.RED, "POINT");
        LayoutLegend.LegendItem b = new LayoutLegend.LegendItem("B", Color.BLUE, "LINE");
        leg.getItems().add(a); leg.getItems().add(b);

        leg.moveItemUp(1); // move B up
        assertEquals("B", leg.getItems().get(0).label);
        assertEquals("A", leg.getItems().get(1).label);
    }

    @Test
    public void testMoveItemUpBoundary() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        LayoutLegend.LegendItem a = new LayoutLegend.LegendItem("A", Color.RED, "POINT");
        leg.getItems().add(a);

        leg.moveItemUp(0); // should not throw
        assertEquals("A", leg.getItems().get(0).label);
    }

    @Test
    public void testMoveItemDownBoundary() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        LayoutLegend.LegendItem a = new LayoutLegend.LegendItem("A", Color.RED, "POINT");
        leg.getItems().add(a);

        leg.moveItemDown(0); // should not throw (only 1 item)
        assertEquals("A", leg.getItems().get(0).label);
    }

    @Test
    public void testEmptyLegendMove() {
        LayoutLegend leg = new LayoutLegend("l", 0, 0, 50, 50);
        leg.moveItemUp(0); // should not throw
        leg.moveItemDown(0); // should not throw
    }
}
