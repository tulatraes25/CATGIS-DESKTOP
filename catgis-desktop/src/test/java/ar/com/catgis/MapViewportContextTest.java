package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies MapViewportContext interface is decoupled from MapPanel.
 * A pure mock implementation should be usable by any collaborator.
 */
class MapViewportContextTest {

    @Test
    void mockViewportProvidesCoordinateConversion() {
        MapViewportContext ctx = new MapViewportContext() {
            @Override public void repaint() {}
            @Override public double screenToWorldX(int sx) { return sx * 0.01; }
            @Override public double screenToWorldY(int sy) { return sy * 0.01; }
            @Override public int worldToScreenX(double wx) { return (int) (wx * 100); }
            @Override public int worldToScreenY(double wy) { return (int) (wy * 100); }
        };

        assertEquals(10.0, ctx.screenToWorldX(1000), 0.01);
        assertEquals(5.0, ctx.screenToWorldY(500), 0.01);
        assertEquals(1000, ctx.worldToScreenX(10.0));
        assertEquals(500, ctx.worldToScreenY(5.0));
    }

    @Test
    void mockViewportRepaintDoesNotThrow() {
        MapViewportContext ctx = new MapViewportContext() {
            @Override public void repaint() {}
            @Override public double screenToWorldX(int sx) { return 0; }
            @Override public double screenToWorldY(int sy) { return 0; }
            @Override public int worldToScreenX(double wx) { return 0; }
            @Override public int worldToScreenY(double wy) { return 0; }
        };
        assertDoesNotThrow(ctx::repaint);
    }
}
