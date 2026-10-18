package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import java.util.Locale;

/**
 * Separate source-file CRS from the projected, in-memory display CRS.
 *
 * A vector layer is reprojected into the project coordinate system before
 * rendering; its mutable sourceCRS then describes the in-memory geometry.
 * The project file must instead retain the original FlatGeobuf source CRS
 * so reopening the same on-disk file does not treat its coordinates as
 * already projected. This marker is transient: only the authoritative
 * original CRS is written to the existing project-file sourceCRS field.
 */
final class FlatGeobufSourceCrsProvenance {
    private static final String SOURCE_FILE_CRS_KEY =
            "catgis.flatgeobuf.original-file-crs.v1";

    private record Origin(String sourcePath, String sourceCrs) {
    }

    private FlatGeobufSourceCrsProvenance() {
    }

    static void rememberBeforeProjection(Layer layer) {
        if (!isFlatGeobuf(layer)) {
            return;
        }
        Object existing = layer.getUserData(SOURCE_FILE_CRS_KEY);
        if (existing instanceof Origin retained
                && retained.sourcePath().equals(layer.getPath())) {
            // A later projection must never replace the file CRS with a map CRS.
            return;
        }
        String source = layer.getSourceCRS();
        if (source != null && !source.isBlank()) {
            layer.putUserData(SOURCE_FILE_CRS_KEY, new Origin(layer.getPath(), source.trim()));
        }
    }

    static String crsForProjectFile(Layer layer) {
        if (layer == null) {
            return "";
        }
        if (isFlatGeobuf(layer)) {
            Object saved = layer.getUserData(SOURCE_FILE_CRS_KEY);
            if (saved instanceof Origin original
                    && original.sourcePath().equals(layer.getPath())) {
                return original.sourceCrs();
            }
        }
        return layer.getSourceCRS();
    }

    private static boolean isFlatGeobuf(Layer layer) {
        return layer != null && layer.getPath() != null
                && layer.getPath().toLowerCase(Locale.ROOT).endsWith(".fgb");
    }
}
