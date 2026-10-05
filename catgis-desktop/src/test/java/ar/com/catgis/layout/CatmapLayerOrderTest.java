package ar.com.catgis.layout;

import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.OnlineWmsLayer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class CatmapLayerOrderTest {

    private static Layer thematic(String name) {
        return new Layer(name, "", "VECTOR");
    }

    private static OnlineTileLayer tile(String name) {
        return new OnlineTileLayer(name);
    }

    private static OnlineWmsLayer wms(String name) {
        return new OnlineWmsLayer(name);
    }

    @Test
    void sortDefaultMapOrderPlacesThematicLayersBeforeBasemapsAndKeepsRelativeOrder() {
        Layer a = thematic("A");
        OnlineTileLayer tile = tile("Tile");
        Layer b = thematic("B");
        OnlineWmsLayer wms = wms("Wms");

        List<Layer> result = CatmapLayerOrder.sortDefaultMapOrder(Arrays.asList(a, tile, b, wms));

        assertEquals(Arrays.asList(a, b, tile, wms), result);
    }

    @Test
    void sortDefaultMapOrderDropsNullsAndHandlesEmptyInput() {
        Layer a = thematic("A");
        List<Layer> result = CatmapLayerOrder.sortDefaultMapOrder(Arrays.asList(a, null, null));
        assertEquals(List.of(a), result);

        assertEquals(List.of(), CatmapLayerOrder.sortDefaultMapOrder(null));
        assertEquals(List.of(), CatmapLayerOrder.sortDefaultMapOrder(List.of()));
    }

    @Test
    void reverseForRenderReversesWithoutMutatingInput() {
        Layer a = thematic("A");
        Layer b = thematic("B");
        Layer c = thematic("C");
        List<Layer> input = Arrays.asList(a, b, c);

        List<Layer> result = CatmapLayerOrder.reverseForRender(input);

        assertEquals(Arrays.asList(c, b, a), result);
        assertEquals(Arrays.asList(a, b, c), input);
        assertEquals(List.of(), CatmapLayerOrder.reverseForRender(null));
        assertEquals(List.of(), CatmapLayerOrder.reverseForRender(List.of()));
    }

    @Test
    void moveKeyMovesToAbsoluteIndexAndClampsOutOfBounds() {
        List<String> keys = Arrays.asList("A", "B", "C", "D");

        assertEquals(Arrays.asList("B", "A", "C", "D"), CatmapLayerOrder.moveKey(keys, "A", 1));
        assertEquals(Arrays.asList("C", "A", "B", "D"), CatmapLayerOrder.moveKey(keys, "C", -1));
        assertEquals(Arrays.asList("A", "B", "D", "C"), CatmapLayerOrder.moveKey(keys, "C", 99));
        assertEquals(Arrays.asList("A", "B", "C", "D"), CatmapLayerOrder.moveKey(keys, "A", 0));
        assertEquals(Arrays.asList("B", "C", "D", "A"), CatmapLayerOrder.moveKey(keys, "A", 3));
    }

    @Test
    void moveKeyIsNoOpForMissingKeyOrSameIndexAndDoesNotMutateInput() {
        List<String> keys = Arrays.asList("A", "B", "C");

        assertEquals(keys, CatmapLayerOrder.moveKey(keys, "Z", 1));
        assertEquals(keys, CatmapLayerOrder.moveKey(keys, "B", 1));
        assertEquals(Arrays.asList("A", "B", "C"), keys);

        List<String> result = CatmapLayerOrder.moveKey(keys, "A", 1);
        assertNotSame(keys, result);
    }

    @Test
    void removeKeyRemovesPresentKeyAndIgnoresMissingKeyWithoutMutatingInput() {
        List<String> keys = Arrays.asList("A", "B", "C");

        assertEquals(Arrays.asList("A", "C"), CatmapLayerOrder.removeKey(keys, "B"));
        assertEquals(keys, CatmapLayerOrder.removeKey(keys, "Z"));
        assertEquals(Arrays.asList("A", "B", "C"), keys);
    }
}
