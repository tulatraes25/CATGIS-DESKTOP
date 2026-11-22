package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for the Argentina Gauss-Kruger faja table: EPSG:22182 must be
 * faja 2 (-69 W), not a shifted copy of faja 1 (-72 W). Also guards the
 * equirectangular planisphere asset used by the CRS selector.
 */
class CRSArgentinaBoundsContractTest {

    @Test
    void posgarFajaTwoIs69WestAndCoversComodoroLongitude() {
        CRSDefinitions.CrsTechnicalDetails faja2 = CRSDefinitions.describe("EPSG:22182");
        assertEquals(-70.5, faja2.west(), 1e-9);
        assertEquals(-67.5, faja2.east(), 1e-9);
        assertEquals(-55.0, faja2.south(), 1e-9);
        assertEquals(-22.0, faja2.north(), 1e-9);
        assertTrue(faja2.name().contains("faja 2"), faja2.name());
        assertTrue(faja2.name().contains("-69"), faja2.name());
        assertFalse(faja2.name().contains("-72"), faja2.name());
    }

    @Test
    void fajaTableHasSevenDistinctCentralMeridians() {
        double[] expectedWest = {-73.5, -70.5, -67.5, -64.5, -61.5, -58.5, -55.5};
        double[] expectedEast = {-70.5, -67.5, -64.5, -61.5, -58.5, -55.5, -52.5};
        for (int zone = 1; zone <= 7; zone++) {
            CRSDefinitions.CrsTechnicalDetails details =
                    CRSDefinitions.describe("EPSG:" + (22180 + zone));
            assertEquals(expectedWest[zone - 1], details.west(), 1e-9,
                    "faja " + zone + " west");
            assertEquals(expectedEast[zone - 1], details.east(), 1e-9,
                    "faja " + zone + " east");
            assertTrue(details.name().contains("faja " + zone), details.name());
        }
        assertEquals(-72.0,
                CRSDefinitions.describe("EPSG:22181").west() + 1.5, 1e-9);
        assertEquals(-54.0,
                CRSDefinitions.describe("EPSG:22187").west() + 1.5, 1e-9);
    }

    @Test
    void planisphereAssetIsTrueEquirectangular2To1() throws Exception {
        try (InputStream input = CRSArgentinaBoundsContractTest.class.getClassLoader()
                .getResourceAsStream("help/assets/world-planisphere.png")) {
            assertNotNull(input, "world-planisphere.png must be packaged");
            BufferedImage image = ImageIO.read(input);
            assertNotNull(image);
            assertEquals(2.0, image.getWidth() / (double) image.getHeight(), 1e-6,
                    "equirectangular planisphere must be 2:1");
            assertEquals(1800, image.getWidth());
            assertEquals(900, image.getHeight());
        }
    }
}
