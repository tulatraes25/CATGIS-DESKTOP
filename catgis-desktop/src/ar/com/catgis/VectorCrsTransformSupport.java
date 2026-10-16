package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.Locale;

/** Strict, GeoTools-backed CRS resolution for data-mutating vector workflows. */
final class VectorCrsTransformSupport {

    private VectorCrsTransformSupport() {}

    static CoordinateReferenceSystem resolve(String text) throws Exception {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("CRS vacio.");
        }
        String trimmed = text.trim();
        if (looksLikeWkt(trimmed)) {
            return CRS.parseWKT(trimmed);
        }
        return CRSDefinitions.decode(trimmed, true);
    }

    static Geometry reprojectGeometry(
            Geometry geometry, String sourceText, String targetText) throws Exception {
        if (geometry == null) {
            return null;
        }
        if (geometry.isEmpty()) {
            return geometry.copy();
        }
        CoordinateReferenceSystem source = resolve(sourceText);
        CoordinateReferenceSystem target = resolve(targetText);
        if (CRS.equalsIgnoreMetadata(source, target)) {
            return geometry.copy();
        }
        MathTransform transform = CRS.findMathTransform(source, target, true);
        Geometry transformed = JTS.transform(geometry, transform);
        ensureFinite(transformed);
        if (transformed == null || transformed.isEmpty()) {
            throw new IllegalStateException("La reproyeccion produjo geometria vacia.");
        }
        return transformed;
    }

    static boolean equivalent(String left, String right) throws Exception {
        if (left == null || left.isBlank() || right == null || right.isBlank()) {
            return false;
        }
        return CRS.equalsIgnoreMetadata(resolve(left), resolve(right));
    }

    static String resolveSourceText(Layer layer, ShapefileData data) {
        String layerText = layer != null ? layer.getSourceCRS() : "";
        if (layerText != null && !layerText.isBlank()) {
            return layerText.trim();
        }
        return data != null ? schemaText(data.getSchema()) : "";
    }

    static String schemaText(SimpleFeatureType schema) {
        if (schema == null || schema.getCoordinateReferenceSystem() == null) {
            return "";
        }
        CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
        try {
            String srs = CRS.toSRS(crs, true);
            if (srs != null && !srs.isBlank()) {
                return CRSDefinitions.normalizeCode(srs);
            }
        } catch (Exception ignored) {
            // Fall through to exact WKT; no default CRS is invented.
        }
        try {
            String wkt = crs.toWKT();
            return wkt != null ? wkt.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    static String canonicalText(String text) throws Exception {
        CoordinateReferenceSystem crs = resolve(text);
        try {
            String srs = CRS.toSRS(crs, true);
            if (srs != null && !srs.isBlank()) {
                return CRSDefinitions.normalizeCode(srs);
            }
        } catch (Exception ignored) {
            // WKT-only CRS remains WKT rather than a fabricated authority code.
        }
        return crs.toWKT();
    }

    private static boolean looksLikeWkt(String text) {
        String upper = text.toUpperCase(Locale.ROOT);
        return upper.startsWith("GEOGCS[")
                || upper.startsWith("PROJCS[")
                || upper.startsWith("GEOCCS[")
                || upper.startsWith("COMPD_CS[")
                || upper.startsWith("VERT_CS[")
                || upper.startsWith("GEOGCRS[")
                || upper.startsWith("PROJCRS[")
                || upper.startsWith("BOUNDCRS[")
                || upper.startsWith("COMPOUNDCRS[")
                || upper.startsWith("VERTCRS[");
    }

    private static void ensureFinite(Geometry geometry) {
        if (geometry == null) {
            throw new IllegalStateException("La reproyeccion no produjo geometria.");
        }
        for (Coordinate coordinate : geometry.getCoordinates()) {
            if (coordinate == null
                    || !Double.isFinite(coordinate.x)
                    || !Double.isFinite(coordinate.y)) {
                throw new IllegalStateException(
                        "La reproyeccion produjo coordenadas no finitas.");
            }
        }
    }
}
