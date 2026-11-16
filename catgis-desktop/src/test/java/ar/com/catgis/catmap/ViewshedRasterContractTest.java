package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Product viewshed: mask semantics and overlay colours on known fixtures. */
class ViewshedRasterContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;

    private static AnalysisRaster dem(int width, int height, float value) {
        float[] values = new float[width * height];
        java.util.Arrays.fill(values, value);
        return new AnalysisRaster("DEM", "m", width, height, values, NO_DATA,
                0, 0, width * 10.0, height * 10.0, "EPSG:32719");
    }

    @Test
    void flatTerrainSeesEverythingInsideTheRadius() {
        AnalysisRaster dem = dem(9, 9, 100f);
        ViewshedRaster.Viewshed viewshed =
                ViewshedRaster.compute(dem, 4, 4, 1.7, 3);
        AnalysisRaster mask = viewshed.mask();
        assertEquals(ViewshedRaster.VISIBLE, mask.valueAt(4, 4), 1.0e-6);
        assertEquals(ViewshedRaster.VISIBLE, mask.valueAt(4, 1), 1.0e-6);
        assertEquals(ViewshedRaster.VISIBLE, mask.valueAt(1, 4), 1.0e-6);
        assertTrue(mask.isNoData(4, 0), "outside the radius is NoData");
        assertTrue(mask.isNoData(0, 0));
        assertEquals("EPSG:32719", mask.crsCode());
        assertEquals(4, viewshed.observerX());
        assertEquals(4, viewshed.observerY());
    }

    @Test
    void wallBlocksCellsBehindIt() {
        float[] values = new float[5 * 3];
        java.util.Arrays.fill(values, 10f);
        values[1 * 5 + 2] = 100f;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", 5, 3, values, NO_DATA,
                0, 0, 50, 30, "EPSG:32719");
        ViewshedRaster.Viewshed viewshed =
                ViewshedRaster.compute(dem, 0, 1, 1.5, 10);
        AnalysisRaster mask = viewshed.mask();
        assertEquals(ViewshedRaster.VISIBLE, mask.valueAt(0, 1), 1.0e-6);
        assertEquals(ViewshedRaster.VISIBLE, mask.valueAt(1, 1), 1.0e-6);
        assertEquals(ViewshedRaster.HIDDEN, mask.valueAt(4, 1), 1.0e-6);
    }

    @Test
    void noDataCellsStayNoDataAndValidationFailsClosed() {
        float[] values = new float[5 * 5];
        java.util.Arrays.fill(values, 10f);
        values[2 * 5 + 2] = NO_DATA;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", 5, 5, values, NO_DATA,
                0, 0, 50, 50, "EPSG:32719");
        ViewshedRaster.Viewshed viewshed =
                ViewshedRaster.compute(dem, 0, 0, 1.7, 5);
        assertTrue(viewshed.mask().isNoData(2, 2));

        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.compute(null, 0, 0, 1.7, 5));
        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.compute(dem, -1, 0, 1.7, 5));
        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.compute(dem, 2, 2, 1.7, 5));
        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.compute(dem, 0, 0, -1.0, 5));
        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.compute(dem, 0, 0, 1.7, 0));
    }

    @Test
    void overlayUsesGreenForVisibleCellsAndMarksTheObserver() {
        AnalysisRaster dem = dem(9, 9, 100f);
        ViewshedRaster.Viewshed viewshed =
                ViewshedRaster.compute(dem, 4, 4, 1.7, 3);
        BufferedImage overlay = ViewshedRaster.overlayImage(viewshed);
        assertEquals(9, overlay.getWidth());
        assertEquals(9, overlay.getHeight());
        assertEquals(0xFF993300, overlay.getRGB(4, 4), "observer marker");
        assertEquals(0x8C228B22, overlay.getRGB(4, 3), "visible cell");
        assertEquals(0x00000000, overlay.getRGB(0, 0), "outside radius is transparent");
        assertNotEquals(overlay.getRGB(4, 4), overlay.getRGB(4, 3));
        assertThrows(IllegalArgumentException.class,
                () -> ViewshedRaster.overlayImage(null));
    }
}
