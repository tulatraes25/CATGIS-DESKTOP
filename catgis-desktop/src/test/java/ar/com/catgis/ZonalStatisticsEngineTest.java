package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ZonalStatisticsEngineTest {

    @Test
    void computeZonalStatsReturnsCorrectAggregates() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = img.getRaster();
        double[] pixel = {10};
        for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) raster.setPixel(x, y, pixel);

        double[][] zones = {
            {1, 1, 2, 2},
            {1, 1, 2, 2},
            {3, 3, 4, 4},
            {3, 3, 4, 4}
        };
        Map<Integer, ZonalStatisticsEngine.ZoneStats> stats = ZonalStatisticsEngine.computeZonalStats(img, zones, 4, -1);
        assertNotNull(stats);
        assertEquals(4, stats.size());
        ZonalStatisticsEngine.ZoneStats zone1 = stats.get(1);
        assertNotNull(zone1);
        assertEquals(4, zone1.count());
        assertEquals(10.0, zone1.mean(), 0.01);
    }

    @Test
    void computeZonalStatsSkipsNoData() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        java.awt.image.WritableRaster raster = img.getRaster();
        raster.setPixel(0, 0, new double[]{10, 0, 0, 255});
        raster.setPixel(1, 0, new double[]{20, 0, 0, 255});
        raster.setPixel(0, 1, new double[]{30, 0, 0, 255});
        raster.setPixel(1, 1, new double[]{40, 0, 0, 255});

        double[][] zones = {{1, 1}, {1, 1}};
        Map<Integer, ZonalStatisticsEngine.ZoneStats> stats = ZonalStatisticsEngine.computeZonalStats(img, zones, 1, -1);
        ZonalStatisticsEngine.ZoneStats zone1 = stats.get(1);
        assertNotNull(zone1);
        assertEquals(4, zone1.count());
        assertEquals(25.0, zone1.mean(), 0.01);
    }
}
