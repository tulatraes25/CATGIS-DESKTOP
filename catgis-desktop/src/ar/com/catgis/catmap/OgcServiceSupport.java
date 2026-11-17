package ar.com.catgis.catmap;

import java.util.List;
import java.util.Locale;

/**
 * OGC service entry points for CATMAP standalone (REQ-PROD-1.2). PostGIS stays
 * optional: it is only reached through the existing browser dialog.
 * <p>
 * WMS/WFS are wired to the existing CATGIS dialogs and WMTS is supported through
 * the online-tile template pipeline ({@link WmtsSupport}); the supported matrix
 * sets are those aligned with the web-mercator XYZ grid.
 */
public final class OgcServiceSupport {

    public enum Protocol {
        WMS("1.3.0", "Web Map Service"),
        WFS("2.0.0", "Web Feature Service"),
        WMTS("1.0.0", "Web Map Tile Service");

        private final String version;
        private final String label;

        Protocol(String version, String label) {
            this.version = version;
            this.label = label;
        }

        public String version() {
            return version;
        }

        public String label() {
            return label;
        }

        public String serviceName() {
            return name();
        }
    }

    private OgcServiceSupport() {}

    /** Protocols CATMAP can open today. */
    public static List<Protocol> supportedProtocols() {
        return List.of(Protocol.WMS, Protocol.WFS, Protocol.WMTS);
    }

    public static boolean supports(Protocol protocol) {
        return protocol != null && supportedProtocols().contains(protocol);
    }

    /** Message for malformed or unsupported service requests. */
    public static String unsupportedMessage(Protocol protocol) {
        if (protocol == null) {
            return "Servicio OGC desconocido.";
        }
        return protocol.label() + " (" + protocol.serviceName()
                + ") requiere una URL/plantilla válida; revisa la configuración.";
    }

    /**
     * Build the GetCapabilities URL for a service endpoint. Accepts URLs with or
     * without existing query parameters and normalizes the service/version.
     */
    public static String capabilitiesUrl(Protocol protocol, String baseUrl) {
        if (protocol == null) {
            throw new IllegalArgumentException("El protocolo OGC es obligatorio.");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del servicio OGC es obligatoria.");
        }
        String url = baseUrl.trim();
        while (url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        String lowered = url.toLowerCase(Locale.ROOT);
        StringBuilder built = new StringBuilder(url);
        built.append(url.contains("?") ? '&' : '?');
        if (!lowered.contains("service=")) {
            built.append("SERVICE=").append(protocol.serviceName()).append('&');
        }
        if (!lowered.contains("request=")) {
            built.append("REQUEST=GetCapabilities&");
        }
        if (!lowered.contains("version=")) {
            built.append("VERSION=").append(protocol.version()).append('&');
        }
        String result = built.toString();
        while (result.endsWith("&") || result.endsWith("?")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
