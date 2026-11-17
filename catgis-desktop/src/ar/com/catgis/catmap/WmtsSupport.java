package ar.com.catgis.catmap;

import java.util.Locale;

/**
 * WMTS support for CATMAP standalone (REQ-BETA-4/T15). CATMAP renders online
 * layers through {@code {z}/{x}/{y}} templates, so a WMTS service is exposed as
 * either its RESTful template or its KVP GetTile template.
 * <p>
 * Scope (declared honestly): matrix sets whose row/column numbering matches the
 * web-mercator XYZ grid (for example {@code GoogleMapsCompatible} or
 * {@code EPSG:3857}); other matrix sets must be adapted by the operator.
 */
public final class WmtsSupport {

    public static final String DEFAULT_STYLE = "default";
    public static final String DEFAULT_MATRIX_SET = "GoogleMapsCompatible";
    public static final String DEFAULT_EXTENSION = "png";
    public static final int DEFAULT_MIN_ZOOM = 0;
    public static final int DEFAULT_MAX_ZOOM = 18;

    /** A WMTS service description entered by the user. */
    public record Request(String serviceUrl, String layer, String style,
                          String tileMatrixSet, String extension,
                          int minZoom, int maxZoom) {
        public Request {
            if (serviceUrl == null || serviceUrl.isBlank()) {
                throw new IllegalArgumentException("La URL del servicio WMTS es obligatoria.");
            }
            if (layer == null || layer.isBlank()) {
                throw new IllegalArgumentException("La capa WMTS es obligatoria.");
            }
            style = blankTo(style, DEFAULT_STYLE);
            tileMatrixSet = blankTo(tileMatrixSet, DEFAULT_MATRIX_SET);
            extension = normalizeExtension(extension);
            minZoom = Math.max(0, minZoom);
            maxZoom = Math.max(minZoom, maxZoom);
        }
    }

    private WmtsSupport() {}

    /** True when the URL already is a full WMTS template with XYZ placeholders. */
    public static boolean isTemplate(String url) {
        return url != null && url.contains("{z}") && url.contains("{y}") && url.contains("{x}");
    }

    /**
     * Build the template CATMAP uses to fetch tiles. Accepts an existing
     * {@code {z}/{x}/{y}} template unchanged; otherwise builds a RESTful WMTS
     * template for the GoogleMapsCompatible-style matrix set.
     */
    public static String restTemplate(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud WMTS es obligatoria.");
        }
        String root = stripTrailingSlashes(request.serviceUrl().trim());
        String template = root + "/" + encodePath(request.layer())
                + "/" + encodePath(request.style())
                + "/" + encodePath(request.tileMatrixSet())
                + "/{z}/{y}/{x}." + request.extension();
        validateTemplate(template);
        return template;
    }

    /** Build a KVP GetTile template (SERVICE=WMTS&REQUEST=GetTile). */
    public static String kvpTemplate(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud WMTS es obligatoria.");
        }
        String root = stripTrailingSlashes(request.serviceUrl().trim());
        String separator = root.contains("?") ? "&" : "?";
        String template = root + separator
                + "SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0"
                + "&LAYER=" + encodeQuery(request.layer())
                + "&STYLE=" + encodeQuery(request.style())
                + "&TILEMATRIXSET=" + encodeQuery(request.tileMatrixSet())
                + "&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}"
                + "&FORMAT=image/" + request.extension();
        validateTemplate(template);
        return template;
    }

    /** Normalise a user-supplied template or build one from the request. */
    public static String buildTemplate(Request request, boolean rest) {
        if (request != null && isTemplate(request.serviceUrl())) {
            validateTemplate(request.serviceUrl().trim());
            return request.serviceUrl().trim();
        }
        return rest ? restTemplate(request) : kvpTemplate(request);
    }

    /**
     * Validate a tile template, returning a human-readable problem or null when
     * it is usable. CATMAP requires HTTP(S) and the three placeholders.
     */
    public static String validateTemplate(String template) {
        if (template == null || template.isBlank()) {
            return "La plantilla WMTS esta vacia.";
        }
        String trimmed = template.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("https://") && !lower.startsWith("http://")) {
            return "La plantilla WMTS debe usar http(s).";
        }
        if (!trimmed.contains("{z}") || !trimmed.contains("{y}")
                || !trimmed.contains("{x}")) {
            return "La plantilla WMTS debe contener {z}, {y} y {x}.";
        }
        return null;
    }

    /** Zoom clamp used by the dialog. */
    public static int clampMaxZoom(int minZoom, int maxZoom) {
        int min = Math.max(0, minZoom);
        int max = Math.max(min, Math.min(24, maxZoom));
        return max;
    }

    static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String normalizeExtension(String extension) {
        String value = blankTo(extension, DEFAULT_EXTENSION).toLowerCase(Locale.ROOT);
        if (value.startsWith("image/")) {
            value = value.substring("image/".length());
        }
        if (value.equals("jpg")) {
            value = "jpeg";
        }
        if (!value.matches("[a-z0-9]{2,5}")) {
            throw new IllegalArgumentException("Extension WMTS no valida: " + extension);
        }
        return value;
    }

    private static String stripTrailingSlashes(String url) {
        String result = url;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String encodePath(String value) {
        return encodeQuery(value);
    }

    private static String encodeQuery(String value) {
        try {
            return java.net.URLEncoder.encode(value,
                    java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception unsupported) {
            return value;
        }
    }
}
