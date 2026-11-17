package ar.com.catgis.catmap.ui;

import ar.com.catgis.AppVersion;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** REQ-BETA-3: the CATMAP splash is painted, branded and headless-safe. */
class CatmapSplashContractTest {

    @Test
    void labelShowsCatmapBetaVersion() {
        String label = CatmapSplash.label();
        assertTrue(label.contains("CATMAP"));
        assertTrue(label.contains(AppVersion.VERSION));
        assertTrue(label.contains("beta"));
    }

    @Test
    void paintedSplashIsNonEmptyAndDeterministic() {
        BufferedImage first = CatmapSplash.render(480, 220);
        BufferedImage second = CatmapSplash.render(480, 220);
        assertEquals(480, first.getWidth());
        assertEquals(220, first.getHeight());
        assertTrue(first.getRGB(2, 2) != first.getRGB(240, 200),
                "splash must paint a branded background, not a flat colour");
        for (int x = 0; x < first.getWidth(); x += 7) {
            for (int y = 0; y < first.getHeight(); y += 7) {
                assertEquals(first.getRGB(x, y), second.getRGB(x, y),
                        "splash painting must be deterministic");
            }
        }
    }
}
