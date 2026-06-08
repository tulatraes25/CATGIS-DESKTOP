package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayerManager {
    private final MapPanel panel;

    public LayerManager(MapPanel panel) {
        this.panel = panel;
    }

    /**
     * Adds a layer to the panel (project + layers panel + internal maps).
     */
    public void addLayer(Layer layer) {
        if (layer == null || CatgisDesktopApp.currentProject == null) {
            return;
        }
        CatgisDesktopApp.currentProject.addLayer(layer);
        CatgisDesktopApp.layersPanel.addLayer(layer);
        panel.repaint();
    }

    public void removeLayer(Layer layer) {
        if (layer == null) {
            return;
        }

        panel.cleanupLayerReferencesOnRemoval(layer);
        panel.shapefileLayers.remove(layer);
        panel.rasterLayers.remove(layer);
        panel.onlineTileLayers.remove(layer);
        panel.onlineWmsLayers.remove(layer);
        panel.rasterStyles.remove(layer);
        invalidateRasterDisplay(layer);

        if (!panel.shapefileLayers.isEmpty() || !panel.rasterLayers.isEmpty() || !panel.onlineTileLayers.isEmpty() || !panel.onlineWmsLayers.isEmpty()) {
            panel.fitToAllLayers();
        }

        panel.repaint();
    }

    public void reorderLayers(List<Layer> orderedLayers) {
        if (orderedLayers == null || orderedLayers.isEmpty()) {
            return;
        }

        LinkedHashMap<Layer, ShapefileData> reorderedVectors = new LinkedHashMap<>();
        LinkedHashMap<Layer, LocalRasterData> reorderedRasters = new LinkedHashMap<>();
        LinkedHashMap<Layer, OnlineRasterSource> reorderedOnline = new LinkedHashMap<>();
        LinkedHashMap<Layer, OnlineWmsLayer> reorderedWms = new LinkedHashMap<>();

        for (Layer layer : orderedLayers) {
            if (layer == null) {
                continue;
            }
            if (panel.shapefileLayers.containsKey(layer)) {
                reorderedVectors.put(layer, panel.shapefileLayers.get(layer));
            }
            if (panel.rasterLayers.containsKey(layer)) {
                reorderedRasters.put(layer, panel.rasterLayers.get(layer));
            }
            if (panel.onlineTileLayers.containsKey(layer)) {
                reorderedOnline.put(layer, panel.onlineTileLayers.get(layer));
            }
            if (panel.onlineWmsLayers.containsKey(layer)) {
                reorderedWms.put(layer, panel.onlineWmsLayers.get(layer));
            }
        }

        for (Map.Entry<Layer, ShapefileData> entry : panel.shapefileLayers.entrySet()) {
            reorderedVectors.putIfAbsent(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Layer, LocalRasterData> entry : panel.rasterLayers.entrySet()) {
            reorderedRasters.putIfAbsent(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Layer, OnlineRasterSource> entry : panel.onlineTileLayers.entrySet()) {
            reorderedOnline.putIfAbsent(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Layer, OnlineWmsLayer> entry : panel.onlineWmsLayers.entrySet()) {
            reorderedWms.putIfAbsent(entry.getKey(), entry.getValue());
        }

        panel.shapefileLayers.clear();
        panel.shapefileLayers.putAll(reorderedVectors);
        panel.rasterLayers.clear();
        panel.rasterLayers.putAll(reorderedRasters);
        panel.onlineTileLayers.clear();
        panel.onlineTileLayers.putAll(reorderedOnline);
        panel.onlineWmsLayers.clear();
        panel.onlineWmsLayers.putAll(reorderedWms);
        panel.repaint();
    }

    public void moveLayerUp(Layer layer) {
        if (layer == null) {
            return;
        }
        if (panel.rasterLayers.containsKey(layer)) {
            moveRasterUp(layer);
            return;
        }
        if (!panel.shapefileLayers.containsKey(layer)) {
            return;
        }

        Layer[] keys = panel.shapefileLayers.keySet().toArray(new Layer[0]);

        for (int i = 1; i < keys.length; i++) {
            if (keys[i] == layer) {
                Layer previous = keys[i - 1];
                ShapefileData currentData = panel.shapefileLayers.get(layer);
                ShapefileData previousData = panel.shapefileLayers.get(previous);

                LinkedHashMap<Layer, ShapefileData> reordered = new LinkedHashMap<>();

                for (int j = 0; j < keys.length; j++) {
                    if (j == i - 1) {
                        reordered.put(layer, currentData);
                        reordered.put(previous, previousData);
                        j++;
                    } else {
                        reordered.put(keys[j], panel.shapefileLayers.get(keys[j]));
                    }
                }

                panel.shapefileLayers.clear();
                panel.shapefileLayers.putAll(reordered);
                panel.repaint();
                return;
            }
        }
    }

    public void moveLayerDown(Layer layer) {
        if (layer == null) {
            return;
        }
        if (panel.rasterLayers.containsKey(layer)) {
            moveRasterDown(layer);
            return;
        }
        if (!panel.shapefileLayers.containsKey(layer)) {
            return;
        }

        Layer[] keys = panel.shapefileLayers.keySet().toArray(new Layer[0]);

        for (int i = 0; i < keys.length - 1; i++) {
            if (keys[i] == layer) {
                Layer next = keys[i + 1];
                ShapefileData currentData = panel.shapefileLayers.get(layer);
                ShapefileData nextData = panel.shapefileLayers.get(next);

                LinkedHashMap<Layer, ShapefileData> reordered = new LinkedHashMap<>();

                for (int j = 0; j < keys.length; j++) {
                    if (j == i) {
                        reordered.put(next, nextData);
                        reordered.put(layer, currentData);
                        j++;
                    } else {
                        reordered.put(keys[j], panel.shapefileLayers.get(keys[j]));
                    }
                }

                panel.shapefileLayers.clear();
                panel.shapefileLayers.putAll(reordered);
                panel.repaint();
                return;
            }
        }
    }

    /* package-private */ void moveRasterUp(Layer layer) {
        Layer[] keys = panel.rasterLayers.keySet().toArray(new Layer[0]);
        for (int i = 1; i < keys.length; i++) {
            if (keys[i] == layer) {
                Layer previous = keys[i - 1];
                LocalRasterData currentData = panel.rasterLayers.get(layer);
                LocalRasterData previousData = panel.rasterLayers.get(previous);
                LinkedHashMap<Layer, LocalRasterData> reordered = new LinkedHashMap<>();
                for (int j = 0; j < keys.length; j++) {
                    if (j == i - 1) {
                        reordered.put(layer, currentData);
                        reordered.put(previous, previousData);
                        j++;
                    } else {
                        reordered.put(keys[j], panel.rasterLayers.get(keys[j]));
                    }
                }
                panel.rasterLayers.clear();
                panel.rasterLayers.putAll(reordered);
                panel.repaint();
                return;
            }
        }
    }

    /* package-private */ void moveRasterDown(Layer layer) {
        Layer[] keys = panel.rasterLayers.keySet().toArray(new Layer[0]);
        for (int i = 0; i < keys.length - 1; i++) {
            if (keys[i] == layer) {
                Layer next = keys[i + 1];
                LocalRasterData currentData = panel.rasterLayers.get(layer);
                LocalRasterData nextData = panel.rasterLayers.get(next);
                LinkedHashMap<Layer, LocalRasterData> reordered = new LinkedHashMap<>();
                for (int j = 0; j < keys.length; j++) {
                    if (j == i) {
                        reordered.put(next, nextData);
                        reordered.put(layer, currentData);
                        j++;
                    } else {
                        reordered.put(keys[j], panel.rasterLayers.get(keys[j]));
                    }
                }
                panel.rasterLayers.clear();
                panel.rasterLayers.putAll(reordered);
                panel.repaint();
                return;
            }
        }
    }

    public boolean isLayerEffectivelyVisible(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (CatgisDesktopApp.currentProject != null) {
            return CatgisDesktopApp.currentProject.isLayerEffectivelyVisible(layer);
        }
        return layer.isVisible();
    }

    public List<Layer> getRenderOrderLayers() {
        List<Layer> ordered = new ArrayList<>();

        if (CatgisDesktopApp.currentProject != null) {
            for (Layer layer : CatgisDesktopApp.currentProject.getUngroupedLayers()) {
                if (layer != null
                        && (panel.shapefileLayers.containsKey(layer) || panel.rasterLayers.containsKey(layer) || panel.onlineTileLayers.containsKey(layer) || panel.onlineWmsLayers.containsKey(layer))
                        && !ordered.contains(layer)) {
                    ordered.add(layer);
                }
            }
            for (LayerGroup group : CatgisDesktopApp.currentProject.getLayerGroups()) {
                if (group == null) {
                    continue;
                }
                for (Layer layer : CatgisDesktopApp.currentProject.getLayersForGroup(group.getName())) {
                    if (layer != null
                            && (panel.shapefileLayers.containsKey(layer) || panel.rasterLayers.containsKey(layer) || panel.onlineTileLayers.containsKey(layer) || panel.onlineWmsLayers.containsKey(layer))
                            && !ordered.contains(layer)) {
                        ordered.add(layer);
                    }
                }
            }
        }

        for (Layer layer : panel.onlineTileLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        for (Layer layer : panel.onlineWmsLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        for (Layer layer : panel.rasterLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        for (Layer layer : panel.shapefileLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        java.util.Collections.reverse(ordered);
        return ordered;
    }

    /* package-private */ List<Layer> getHitTestLayers(boolean preferEditingLayer) {
        List<Layer> orderedLayers = new ArrayList<>();
        if (preferEditingLayer
                && panel.activeVectorEditingLayer != null
                && panel.shapefileLayers.containsKey(panel.activeVectorEditingLayer)
                && isLayerEffectivelyVisible(panel.activeVectorEditingLayer)) {
            orderedLayers.add(panel.activeVectorEditingLayer);
            return orderedLayers;
        }

        List<Layer> renderOrder = getRenderOrderLayers();
        for (int i = renderOrder.size() - 1; i >= 0; i--) {
            Layer layer = renderOrder.get(i);
            if (panel.shapefileLayers.containsKey(layer)) {
                orderedLayers.add(layer);
            }
        }
        return orderedLayers;
    }

    public boolean isReadOnlyVectorLayer(Layer layer) {
        return VectorLayerUtils.isReadOnlyVectorLayer(layer);
    }

    public boolean isLayerArmedForEditing(Layer layer) {
        return layer != null && layer == panel.activeVectorEditingLayer;
    }

    private void invalidateRasterDisplay(Layer layer) {
        panel.invalidateRasterDisplay(layer);
    }
}
