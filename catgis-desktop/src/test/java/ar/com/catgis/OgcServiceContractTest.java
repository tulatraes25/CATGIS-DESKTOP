package ar.com.catgis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OgcServiceContractTest {

    private HttpServer server;
    private String origin;

    @BeforeEach
    void openLocalFixtureServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        origin = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void closeLocalFixtureServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void buildsCanonicalWmsAndWfsUrlsWithoutDroppingOpaqueCredentials() {
        assertEquals(
                "https://gis.example.test/wms?token=abc123&apiKey=unchanged&SERVICE=WMS&REQUEST=GetCapabilities",
                WmsCapabilitiesService.buildCapabilitiesUrl(
                        " https://gis.example.test/wms?SERVICE=WFS&Request=GetMap&LAYERS=roads"
                                + "&token=abc123&apiKey=unchanged&VERSION=1.1.1 ")
        );
        assertEquals(
                "https://gis.example.test/wfs?token=abc123&apiKey=unchanged&SERVICE=WFS&REQUEST=GetCapabilities",
                WfsCapabilitiesService.buildCapabilitiesUrl(
                        " https://gis.example.test/wfs?SERVICE=WFS&request=GetFeature&TYPENAMES=roads"
                                + "&COUNT=2&token=abc123&apiKey=unchanged&VERSION=2.0.0 ")
        );
        assertEquals("", WmsCapabilitiesService.buildCapabilitiesUrl(" "));
        assertEquals("", WfsCapabilitiesService.buildCapabilitiesUrl(null));
    }

    @Test
    void sendsOneCanonicalRequestPerProtocolToLocalHttpFixture() throws Exception {
        AtomicReference<String> wmsQuery = new AtomicReference<>();
        AtomicReference<String> wfsQuery = new AtomicReference<>();
        server.createContext("/wms", exchange -> {
            wmsQuery.set(exchange.getRequestURI().getRawQuery());
            respond(exchange, 200,
                    "<WMS_Capabilities version=\"1.3.0\"><Service><Title>Local WMS</Title></Service>"
                    + "<Capability><Layer><Name>roads</Name><Title>Roads</Title></Layer></Capability>"
                    + "</WMS_Capabilities>");
        });
        server.createContext("/wfs", exchange -> {
            wfsQuery.set(exchange.getRequestURI().getRawQuery());
            respond(exchange, 200,
                    "<WFS_Capabilities version=\"2.0.0\"><ServiceIdentification><Title>Local WFS</Title>"
                    + "</ServiceIdentification><FeatureTypeList><FeatureType><Name>roads</Name>"
                    + "<DefaultCRS>EPSG:4326</DefaultCRS></FeatureType></FeatureTypeList></WFS_Capabilities>");
        });

        assertEquals("Local WMS",
                WmsCapabilitiesService.fetchCapabilities(origin + "/wms?token=secret&LAYERS=old").getServiceTitle());
        assertEquals("Local WFS",
                WfsCapabilitiesService.fetchCapabilities(origin + "/wfs?token=secret&TYPENAMES=old").getServiceTitle());

        assertEquals(Map.of("token", "secret", "SERVICE", "WMS", "REQUEST", "GetCapabilities"),
                parameters(wmsQuery.get()));
        assertEquals(Map.of("token", "secret", "SERVICE", "WFS", "REQUEST", "GetCapabilities"),
                parameters(wfsQuery.get()));
    }

    @Test
    void acceptsPrefixedWmsCapabilitiesAndInheritsLayerCrsAndStyle() throws Exception {
        server.createContext("/prefixed", exchange -> respond(exchange, 200,
                "<wms:WMS_Capabilities xmlns:wms=\"http://www.opengis.net/wms\" version=\"1.3.0\">"
                + "<wms:Service><wms:Title>Prefixed WMS</wms:Title></wms:Service>"
                + "<wms:Capability><wms:Request><wms:GetMap><wms:Format>image/png</wms:Format>"
                + "</wms:GetMap></wms:Request><wms:Layer><wms:CRS>EPSG:4326</wms:CRS>"
                + "<wms:Style><wms:Name>default</wms:Name><wms:Title>Default</wms:Title></wms:Style>"
                + "<wms:Layer><wms:Name>roads</wms:Name><wms:Title>Roads</wms:Title>"
                + "</wms:Layer></wms:Layer></wms:Capability></wms:WMS_Capabilities>"));

        WmsCapabilities caps = WmsCapabilitiesService.fetchCapabilities(origin + "/prefixed");
        assertEquals("Prefixed WMS", caps.getServiceTitle());
        assertTrue(caps.getFormats().contains("image/png"));
        assertEquals(1, caps.getLayers().size());
        WmsLayerInfo layer = caps.getLayers().get(0);
        assertEquals("roads", layer.getName());
        assertTrue(layer.getCrsCodes().contains("EPSG:4326"));
        assertEquals(1, layer.getStyles().size());
        assertEquals("default", layer.getStyles().get(0).getName());
    }

    @Test
    void supportsLegacyWms111DtdWithoutDownloadingItsExternalSchema() throws Exception {
        AtomicInteger dtdRequests = new AtomicInteger();
        server.createContext("/capabilities.dtd", exchange -> {
            dtdRequests.incrementAndGet();
            respond(exchange, 200, "<!ELEMENT WMT_MS_Capabilities ANY>");
        });
        String xml = "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"" + origin
                + "/capabilities.dtd\" [<!ELEMENT VendorSpecificCapabilities EMPTY>]>"
                + "<WMT_MS_Capabilities version=\"1.1.1\">"
                + "<Service><Title>Legacy WMS</Title></Service>"
                + "<Capability><Layer><Name>legacy_layer</Name><Title>Legacy Layer</Title>"
                + "</Layer></Capability></WMT_MS_Capabilities>";
        server.createContext("/legacy-wms", exchange -> respond(exchange, 200, xml));

        WmsCapabilities caps = WmsCapabilitiesService.fetchCapabilities(origin + "/legacy-wms");
        assertEquals("1.1.1", caps.getVersion());
        assertEquals("Legacy WMS", caps.getServiceTitle());
        assertEquals("legacy_layer", caps.getLayers().get(0).getName());
        assertEquals(0, dtdRequests.get(), "Legacy WMS DTD must never be downloaded.");
    }

    @Test
    void rejectsExternalXmlEntitiesInBothServicesWithoutFetchingTheEntity() {
        AtomicInteger exfiltrationRequests = new AtomicInteger();
        server.createContext("/private", exchange -> {
            exfiltrationRequests.incrementAndGet();
            respond(exchange, 200, "confidential-data");
        });
        String entity = "<!DOCTYPE CAPS [<!ENTITY secret SYSTEM \"" + origin + "/private\">]>";
        server.createContext("/unsafe-wms", exchange -> respond(exchange, 200,
                entity.replace("CAPS", "WMS_Capabilities")
                + "<WMS_Capabilities><Service><Title>&secret;</Title></Service></WMS_Capabilities>"));
        server.createContext("/unsafe-wfs", exchange -> respond(exchange, 200,
                entity.replace("CAPS", "WFS_Capabilities")
                + "<WFS_Capabilities><ServiceIdentification><Title>&secret;</Title>"
                + "</ServiceIdentification></WFS_Capabilities>"));

        assertThrows(Exception.class, () ->
                WmsCapabilitiesService.fetchCapabilities(origin + "/unsafe-wms"));
        assertThrows(Exception.class, () ->
                WfsCapabilitiesService.fetchCapabilities(origin + "/unsafe-wfs"));
        assertEquals(0, exfiltrationRequests.get(),
                "External entity was fetched from XML supplied by an untrusted GIS service.");
    }

    @Test
    void propagatesHttpServiceErrorsInsteadOfReturningAnEmptySuccess() {
        server.createContext("/down", exchange -> respond(exchange, 503, "Service unavailable"));
        IllegalArgumentException wms = assertThrows(IllegalArgumentException.class, () ->
                WmsCapabilitiesService.fetchCapabilities(origin + "/down"));
        IllegalArgumentException wfs = assertThrows(IllegalArgumentException.class, () ->
                WfsCapabilitiesService.fetchCapabilities(origin + "/down"));
        assertTrue(wms.getMessage().contains("503"));
        assertTrue(wfs.getMessage().contains("503"));
    }

    private static Map<String, String> parameters(String query) {
        assertNotNull(query);
        assertFalse(query.isBlank());
        return Arrays.stream(query.split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(
                        part -> part[0],
                        part -> part.length > 1 ? part[1] : "",
                        (first, second) -> {
                            throw new AssertionError("Duplicated OGC parameter");
                        }));
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
