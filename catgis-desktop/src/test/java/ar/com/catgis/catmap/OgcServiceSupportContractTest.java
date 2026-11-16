package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T15: standalone OGC entry points (WMS/WFS real, WMTS honest, PostGIS optional). */
class OgcServiceSupportContractTest {

    @Test
    void capabilitiesUrlsCarryServiceRequestAndVersion() {
        assertEquals(
                "https://geo.example.com/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0",
                OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WMS,
                        "https://geo.example.com/wms"));
        assertEquals(
                "https://geo.example.com/wfs?map=/x&SERVICE=WFS&REQUEST=GetCapabilities&VERSION=2.0.0",
                OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WFS,
                        "https://geo.example.com/wfs?map=/x"));
        assertEquals(
                "https://geo.example.com/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0",
                OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WMS,
                        "https://geo.example.com/wms?"));
    }

    @Test
    void existingParametersAreNotDuplicated() {
        String url = OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WMS,
                "https://geo.example.com/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1");
        assertEquals("https://geo.example.com/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1",
                url);
    }

    @Test
    void onlyWmsAndWfsAreSupported() {
        assertEquals(List.of(OgcServiceSupport.Protocol.WMS, OgcServiceSupport.Protocol.WFS),
                OgcServiceSupport.supportedProtocols());
        assertTrue(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WMS));
        assertTrue(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WFS));
        assertFalse(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WMTS));
        assertFalse(OgcServiceSupport.supports(null));
    }

    @Test
    void wmtsIsReportedHonestly() {
        assertTrue(OgcServiceSupport.unsupportedMessage(OgcServiceSupport.Protocol.WMTS)
                .contains("WMTS"));
        assertTrue(OgcServiceSupport.unsupportedMessage(OgcServiceSupport.Protocol.WMTS)
                .contains("no"));
        assertEquals(OgcServiceSupport.Protocol.WFS.version(), "2.0.0");
        assertEquals(OgcServiceSupport.Protocol.WMTS.version(), "1.0.0");
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> OgcServiceSupport.capabilitiesUrl(null, "https://x/wms"));
        assertThrows(IllegalArgumentException.class,
                () -> OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WMS, " "));
        assertThrows(IllegalArgumentException.class,
                () -> OgcServiceSupport.capabilitiesUrl(OgcServiceSupport.Protocol.WMS, null));
    }
}
