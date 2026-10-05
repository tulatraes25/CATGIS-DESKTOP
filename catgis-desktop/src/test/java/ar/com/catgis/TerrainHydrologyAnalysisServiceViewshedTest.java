package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerrainHydrologyAnalysisServiceViewshedTest {

    @Test
    void viewshed_observerSeesItself() {
        double[] dem = new double[25]; // 5x5 flat
        for (int i = 0; i < 25; i++) dem[i] = 100;
        TerrainHydrologyAnalysisService.ViewshedResult r =
                TerrainHydrologyAnalysisService.computeViewshed(dem, 5, 5, 2, 2, 1.5, 5);
        assertNotNull(r);
        assertTrue(r.visible()[2 * 5 + 2]); // observer sees itself
    }

    @Test
    void viewshed_wallBlocksView() {
        // 5x3 grid: observer at left, wall in middle, target at right
        double[] dem = new double[15];
        for (int i = 0; i < 15; i++) dem[i] = 10; // flat ground at 10m
        dem[1 * 5 + 2] = 100; // wall at row 1, col 2 — blocks view
        TerrainHydrologyAnalysisService.ViewshedResult r =
                TerrainHydrologyAnalysisService.computeViewshed(dem, 5, 3, 0, 1, 1.5, 10);

        assertNotNull(r);
        boolean[] v = r.visible();
        assertTrue(v[1 * 5 + 0]); // observer
        assertTrue(v[1 * 5 + 1]); // next to observer
        assertFalse(v[1 * 5 + 4]); // target behind wall should be invisible
    }

    @Test
    void viewshed_nullDem() {
        assertNull(TerrainHydrologyAnalysisService.computeViewshed(null, 5, 5, 0, 0, 1, 5));
    }

    @Test
    void viewshed_outOfBounds() {
        double[] dem = new double[25];
        assertNull(TerrainHydrologyAnalysisService.computeViewshed(dem, 5, 5, 99, 99, 1, 5));
    }
}
