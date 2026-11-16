package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import java.util.Locale;

/**
 * Separate source-file CRS from the projected, in-memory display CRS.
 *
 * A vector layer is reprojected into the project coordinate system before
 * rendering; its mutable sourceCRS then describes the in-memory geometry.
 * The project file must instead retain the original local vector file-source CRS (FGB, LAS, SHP, GeoJSON, KML)
 * so reopening the same on-disk file does not treat its coordinates as
 * already projected. This marker is transient: only the authoritative
 * original CRS is written to the existing project-file sourceCRS field.
 */
final class LocalVectorSourceCrsProvenance {
    private static final String SOURCE_FILE_CRS_KEY =
            "catgis.local-vector.original-file-crs.v1";

    private record Origin(String sourcePath, String sourceCrs) {
    }

    private LocalVectorSourceCrsProvenance() {
    }

    static void rememberBeforeProjection(Layer layer) {
        rememberBeforeProjection(layer, null);
    }

    /**
     * Shapefile sources may have a valid WKT .prj that cannot be represented
     * as an EPSG code. Capture the schema CRS before the geometry projection
     * overwrites the in-memory layer's sourceCRS with the project target.
     */
    static void rememberBeforeProjection(Layer layer, ShapefileData data) {
        if (!isSourceReferencedFile(layer)) {
            return;
        }
        Object existing = layer.getUserData(SOURCE_FILE_CRS_KEY);
        if (existing instanceof Origin retained
                && retained.sourcePath().equals(layer.getPath())) {
            // A later projection must never replace the file CRS with a map CRS.
            return;
        }
        String source = layer.getSourceCRS();
        if ((source == null || source.isBlank()) && isShapefile(layer) && data != null) {
            source = VectorCrsTransformSupport.schemaText(data.getSchema());
        }
        if (source != null && !source.isBlank()) {
            layer.putUserData(SOURCE_FILE_CRS_KEY, new Origin(layer.getPath(), source.trim()));
        }
    }

    static String crsForProjectFile(Layer layer) {
        if (layer == null) {
            return "";
        }
        if (isSourceReferencedFile(layer)) {
            Object saved = layer.getUserData(SOURCE_FILE_CRS_KEY);
            if (saved instanceof Origin original
                    && original.sourcePath().equals(layer.getPath())) {
                return original.sourceCrs();
            }
        }
        return layer.getSourceCRS();
    }

    private static boolean isSourceReferencedFile(Layer layer) {
        if (layer == null || layer.getPath() == null) {
            return false;
        }
        String path = layer.getPath().toLowerCase(Locale.ROOT);
        return path.endsWith(".fgb") || path.endsWith(".las") || path.endsWith(".laz")
                || path.endsWith(".shp") || path.endsWith(".geojson")
                || path.endsWith(".json") || path.endsWith(".kml") || path.endsWith(".kmz");
    }

    private static boolean isShapefile(Layer layer) {
        return layer != null && layer.getPath() != null
                && layer.getPath().toLowerCase(Locale.ROOT).endsWith(".shp");
    }
}
