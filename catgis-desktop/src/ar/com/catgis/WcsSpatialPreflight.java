package ar.com.catgis;

import java.io.IOException;

/** Pure spatial validation shared by the form, transport and metadata checks. */
final class WcsSpatialPreflight {

    private static final String DEFAULT_BBOX = "-180,-90,180,90";
    private static final String DEFAULT_CRS = "EPSG:4326";
    private static final int MAX_BBOX_LENGTH = 1024;
    private static final String DECIMAL = "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?";

    private WcsSpatialPreflight() {}

    /** The interactive form requires both fields; never invent user input. */
    static void validate(String bbox, String crs) throws IOException {
        double[] bounds = parseBounds(bbox);
        if (crs == null || crs.isBlank()) {
            throw new IOException("El CRS WCS es obligatorio (por ejemplo EPSG:4326).");
        }
        String code = crs.trim();
        if (!code.matches("[A-Za-z][A-Za-z0-9.:/_-]{1,127}") || !code.contains(":")) {
            throw new IOException("El CRS WCS debe ser un identificador valido sin espacios ni parametros URL.");
        }
        // This is syntax/range checking, NOT a CRS authority lookup or WCS
        // axis negotiation. The existing GeoTIFF checks remain authoritative.
        if ("EPSG:4326".equalsIgnoreCase(code)
                && (bounds[0] < -180.0 || bounds[2] > 180.0
                    || bounds[1] < -90.0 || bounds[3] > 90.0)) {
            throw new IOException("EPSG:4326 requiere longitud entre -180 y 180 "
                    + "y latitud entre -90 y 90; orden BBOX: xmin,ymin,xmax,ymax.");
        }
    }

    /** Public API compatibility: omitted fields have long-standing defaults. */
    static void validateApi(String bbox, String crs) throws IOException {
        validate(effectiveBbox(bbox), effectiveCrs(crs));
    }

    static String effectiveBbox(String bbox) {
        if (bbox == null || bbox.isBlank()) {
            return DEFAULT_BBOX;
        }
        return bbox.trim();
    }

    static String effectiveCrs(String crs) {
        return crs == null || crs.isBlank() ? DEFAULT_CRS : crs.trim();
    }

    /** Independent of the client, HTTP, files, Swing and CRS database I/O. */
    static double[] parseBounds(String bbox) throws IOException {
        if (bbox == null || bbox.isBlank()) {
            throw new IOException("El BBOX WCS es obligatorio: xmin,ymin,xmax,ymax.");
        }
        if (bbox.length() > MAX_BBOX_LENGTH) {
            throw new IOException("El BBOX WCS excede el limite de 1024 caracteres.");
        }
        String[] parts = bbox.split(",", -1);
        if (parts.length != 4) {
            throw new IOException("El BBOX WCS debe contener xmin,ymin,xmax,ymax.");
        }
        double[] bounds = new double[4];
        for (int i = 0; i < parts.length; i++) {
            String coordinate = parts[i].trim();
            if (!coordinate.matches(DECIMAL)) {
                throw new IOException("El BBOX WCS debe contener cuatro coordenadas decimales finitas.");
            }
            try {
                bounds[i] = Double.parseDouble(coordinate);
            } catch (NumberFormatException invalid) {
                throw new IOException("El BBOX WCS contiene una coordenada invalida.", invalid);
            }
            if (!Double.isFinite(bounds[i])) {
                throw new IOException("El BBOX WCS contiene una coordenada no finita.");
            }
        }
        double width = bounds[2] - bounds[0];
        double height = bounds[3] - bounds[1];
        if (!(width > 0.0) || !(height > 0.0)
                || !Double.isFinite(width) || !Double.isFinite(height)) {
            throw new IOException("El BBOX WCS debe tener ancho y alto positivos y finitos.");
        }
        return bounds;
    }
}
