package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T15: WMTS tile templates for CATMAP standalone (REST and KVP GetTile). */
class WmtsSupportContractTest {

    private static WmtsSupport.Request request() {
        return new WmtsSupport.Request(
                "https://geo.example.com/wmts/", "orto2026", "default",
                "GoogleMapsCompatible", "png", 0, 18);
    }

    @Test
    void restTemplateMatchesTheXyzGrid() {
        assertEquals(
                "https://geo.example.com/wmts/orto2026/default/GoogleMapsCompatible/{z}/{y}/{x}.png",
                WmtsSupport.restTemplate(request()));
    }

    @Test
    void kvpTemplateUsesGetTileWithPlaceholders() {
        String template = WmtsSupport.kvpTemplate(request());
        assertTrue(template.contains("SERVICE=WMTS"));
        assertTrue(template.contains("REQUEST=GetTile"));
        assertTrue(template.contains("VERSION=1.0.0"));
        assertTrue(template.contains("LAYER=orto2026"));
        assertTrue(template.contains("TILEMATRIX={z}"));
        assertTrue(template.contains("TILEROW={y}"));
        assertTrue(template.contains("TILECOL={x}"));
        assertTrue(template.contains("FORMAT=image/png"));
    }

    @Test
    void existingTemplatesArePreservedAndValidated() {
        String custom = "https://tiles.example.com/wmts/{z}/{y}/{x}.jpg";
        assertTrue(WmtsSupport.isTemplate(custom));
        assertEquals(custom, WmtsSupport.buildTemplate(
                new WmtsSupport.Request(custom, "x", null, null, null, 0, 5), true));
        assertNull(WmtsSupport.validateTemplate(custom));
    }

    @Test
    void invalidTemplatesAreRejectedWithClearMessages() {
        assertTrue(WmtsSupport.validateTemplate(null).contains("vacia"));
        assertTrue(WmtsSupport.validateTemplate("ftp://x/{z}/{y}/{x}").contains("http"));
        assertTrue(WmtsSupport.validateTemplate("https://x/{z}").contains("{z}"));
        assertThrows(IllegalArgumentException.class, () -> new WmtsSupport.Request(
                " ", "capa", null, null, null, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new WmtsSupport.Request(
                "https://x", " ", null, null, null, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new WmtsSupport.Request(
                "https://x", "capa", null, null, "exe!", 0, 1));
    }

    @Test
    void defaultsAndZoomAreNormalized() {
        WmtsSupport.Request request = new WmtsSupport.Request(
                "https://x", "capa", " ", " ", "jpg", 3, 2);
        assertEquals(WmtsSupport.DEFAULT_STYLE, request.style());
        assertEquals(WmtsSupport.DEFAULT_MATRIX_SET, request.tileMatrixSet());
        assertEquals("jpeg", request.extension());
        assertEquals(3, request.minZoom());
        assertEquals(3, request.maxZoom());
        assertEquals(3, WmtsSupport.clampMaxZoom(3, 2));
        assertEquals(24, WmtsSupport.clampMaxZoom(0, 99));
        assertFalse(WmtsSupport.isTemplate("https://x/{z}/{x}.png"));
    }
}
