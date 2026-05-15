package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgisConnectionPresetTest {

    @Test
    void catserverPresetKeepsInstitutionalDefaultsSeparatedFromGenericPostgis() {
        PostgisConnectionPreset preset = PostgisConnectionPreset.catserver();
        PostgisConnectionInfo defaults = preset.getDefaults();

        assertEquals("catserver", preset.getId());
        assertEquals("Conectar CATSERVER", preset.getDialogTitle());
        assertEquals("CATSERVER", preset.getTitle());
        assertEquals("localhost", defaults.getHost());
        assertEquals(5432, defaults.getPort());
        assertEquals("catserver", defaults.getDatabase());
        assertEquals("public", defaults.getSchema());
        assertTrue(defaults.isRememberPassword());
        assertTrue(preset.hasBanner());
        assertTrue(preset.hasProfileId());
    }
}
