package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** REQ-BETA-1: beta identity is explicit and consistent. */
class AppVersionContractTest {

    @Test
    void versionIsABetaSemanticVersion() {
        assertTrue(AppVersion.VERSION.matches("\\d+\\.\\d+\\.\\d+-beta\\.\\d+"),
                "unexpected beta version: " + AppVersion.VERSION);
        assertEquals("beta", AppVersion.CHANNEL);
        assertTrue(AppVersion.isBeta());
        assertTrue(AppVersion.YEAR >= 2026);
    }

    @Test
    void descriptionsExposeProductVersionAndChannel() {
        assertTrue(AppVersion.versionText().contains("CATGIS Desktop"));
        assertTrue(AppVersion.versionText().contains(AppVersion.VERSION));
        assertTrue(AppVersion.versionText().contains("beta"));
        assertTrue(AppVersion.catmapVersionText().contains("CATMAP"));
        assertTrue(AppVersion.catmapVersionText().contains(AppVersion.VERSION));
        assertTrue(AppVersion.catmapAboutText().contains(AppVersion.VERSION));
        assertFalse(AppVersion.catmapAboutText().contains("Version 1.0\n"));
    }
}
