package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.OnlineWmsLayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Safety preflight for required local sources before replacing the live project.
 * This is NOT detached GIS reconstruction or a full project transaction.
 */
final class ProjectLocalSourcePreflight {

    private ProjectLocalSourcePreflight() {}

    static void requireExistingLocalSources(List<? extends Layer> layers) throws IOException {
        if (layers == null) return;
        for (Layer layer : layers) {
            requireExistingLocalSource(layer);
        }
    }

    static void requireExistingLocalSource(Layer layer) throws IOException {
        if (layer == null || isDeferredNonLocal(layer) || isRegenerated(layer)) {
            return;
        }
        String type = layer.getType() != null
                ? layer.getType().replace("[", "").replace("]", "").trim().toUpperCase(Locale.ROOT)
                : "";
        boolean raster = layer instanceof RasterLayer || "RASTER".equals(type);
        boolean localVector = "VECTOR".equals(type) || "GPX".equals(type)
                || "GEOPACKAGE".equals(type) || "SPATIALITE".equals(type);
        if (!raster && !localVector) {
            return;
        }
        String source = layer.getPath() != null ? layer.getPath().trim() : "";
        if (source.regionMatches(true, 0, "spatialite://", 0, "spatialite://".length())) {
            source = source.substring("spatialite://".length());
            int tableSeparator = source.lastIndexOf('#');
            if (tableSeparator >= 0) {
                source = source.substring(0, tableSeparator);
            }
        }
        if (source.isBlank()) {
            throw new IOException("Falta la ruta de origen de la capa local: " + safeName(layer));
        }
        try {
            Path file = Path.of(source);
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(file) <= 0) {
                throw new IOException("La capa local no tiene un archivo regular legible: "
                        + safeName(layer));
            }
        } catch (InvalidPathException invalid) {
            throw new IOException("La capa local tiene una ruta no válida: "
                    + safeName(layer), invalid);
        }
    }

    private static boolean isDeferredNonLocal(Layer layer) {
        return layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer
                || layer instanceof OnlineWfsLayer || layer instanceof PostgisLayer;
    }

    private static boolean isRegenerated(Layer layer) {
        if (layer instanceof RasterLayer raster && raster.isDerivedLayer()) {
            return true;
        }
        return TopographyWorkflowSupport.isTransientTopographyVector(layer);
    }

    private static String safeName(Layer layer) {
        String name = layer.getName();
        return name == null || name.isBlank() ? "sin nombre" : name.replace("\n", " ");
    }
}
