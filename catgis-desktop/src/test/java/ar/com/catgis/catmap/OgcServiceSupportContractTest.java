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
    void allThreeOgcProtocolsAreSupported() {
        assertEquals(List.of(OgcServiceSupport.Protocol.WMS, OgcServiceSupport.Protocol.WFS,
                        OgcServiceSupport.Protocol.WMTS),
                OgcServiceSupport.supportedProtocols());
        assertTrue(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WMS));
        assertTrue(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WFS));
        assertTrue(OgcServiceSupport.supports(OgcServiceSupport.Protocol.WMTS));
        assertFalse(OgcServiceSupport.supports(null));
    }

    @Test
    void wmtsCapabilitiesAndVersionsAreDeclared() {
        String message = OgcServiceSupport.unsupportedMessage(
                OgcServiceSupport.Protocol.WMTS);
        assertTrue(message.contains("WMTS"));
        assertTrue(message.contains("válida"));
        assertEquals(OgcServiceSupport.Protocol.WFS.version(), "2.0.0");
        assertEquals(OgcServiceSupport.Protocol.WMTS.version(), "1.0.0");
        assertEquals("WMTS", OgcServiceSupport.Protocol.WMTS.serviceName());
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
