package ar.com.catgis;

import java.io.IOException;

/**
 * Lightweight, deterministic WCS form and request preflight. A geographic
 * request must not become an unbounded full-coverage download because its
 * BBOX is malformed. Parsing here is syntactic; server-specific WCS 2.x axis
 * labels and output CRS require separate DescribeCoverage negotiation.
 */
final class WcsSpatialPreflight {

    private WcsSpatialPreflight() {}

    static void validate(String bbox, String crs) throws IOException {
        if (bbox == null || bbox.isBlank()) {
            throw new IOException("El BBOX WCS es obligatorio: xmin,ymin,xmax,ymax.");
        }
        double[] bounds = WcsClient.parseRequestedBbox(bbox);
        if (crs == null || crs.isBlank()) {
            throw new IOException("El CRS WCS es obligatorio (por ejemplo EPSG:4326).");
        }
        String code = crs.trim();
        if (!code.matches("[A-Za-z][A-Za-z0-9.:/_-]{1,127}") || !code.contains(":")) {
            throw new IOException("El CRS WCS debe ser un identificador valido sin espacios ni parametros URL.");
        }
        if ("EPSG:4326".equalsIgnoreCase(code)
                && (bounds[0] < -180.0 || bounds[2] > 180.0
                    || bounds[1] < -90.0 || bounds[3] > 90.0)) {
            throw new IOException("EPSG:4326 requiere longitud entre -180 y 180 "
                    + "y latitud entre -90 y 90; orden BBOX: xmin,ymin,xmax,ymax.");
        }
    }
}
