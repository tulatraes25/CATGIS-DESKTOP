package ar.com.catgis.layout;

import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.OnlineWmsLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure, UI-free layer ordering helpers for CATMAP.
 *
 * <p>The display order (tree order) is thematic-first, basemap-last, with the
 * topmost row drawn on top. The render order is the reverse of the display
 * order because the map draws later layers over earlier ones.</p>
 */
public final class CatmapLayerOrder {

    private CatmapLayerOrder() {
    }

    /**
     * Partitions layers into thematic layers first and basemap/background layers
     * last, preserving the relative order within each group. Null entries are
     * dropped. Online tile and WMS layers are treated as basemaps.
     */
    public static List<Layer> sortDefaultMapOrder(List<Layer> layers) {
        List<Layer> thematic = new ArrayList<>();
        List<Layer> basemap = new ArrayList<>();
        if (layers != null) {
            for (Layer layer : layers) {
                if (layer == null) {
                    continue;
                }
                if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                    basemap.add(layer);
                } else {
                    thematic.add(layer);
                }
            }
        }
        List<Layer> ordered = new ArrayList<>(thematic.size() + basemap.size());
        ordered.addAll(thematic);
        ordered.addAll(basemap);
        return ordered;
    }

    /**
     * Returns the render order for a display-ordered list: the reverse of the
     * input, so that the first (topmost) displayed layer is drawn last and
     * therefore appears on top.
     */
    public static List<Layer> reverseForRender(List<Layer> layers) {
        if (layers == null || layers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Layer> reversed = new ArrayList<>(layers);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Moves a layer key to an absolute target index (clamped to the list bounds)
     * and returns a new list. The index is interpreted against the original
     * list, matching the Subir/Bajar/Enviar al frente/Enviar al fondo semantics.
     * A no-op (missing key or already-at-target) returns an equal list.
     */
    public static List<String> moveKey(List<String> keys, String key, int targetIndex) {
        List<String> result = keys == null ? new ArrayList<>() : new ArrayList<>(keys);
        int currentIndex = result.indexOf(key);
        if (currentIndex < 0) {
            return result;
        }
        if (targetIndex < 0) {
            targetIndex = 0;
        }
        if (targetIndex > result.size() - 1) {
            targetIndex = result.size() - 1;
        }
        if (currentIndex == targetIndex) {
            return result;
        }
        result.remove(currentIndex);
        result.add(targetIndex, key);
        return result;
    }

    /**
     * Removes a layer key and returns a new list without it. If the key is not
     * present, an equal list is returned.
     */
    public static List<String> removeKey(List<String> keys, String key) {
        List<String> result = keys == null ? new ArrayList<>() : new ArrayList<>(keys);
        result.remove(key);
        return result;
    }
}
