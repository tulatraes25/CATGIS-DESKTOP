/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.OrderedMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InfoModel {
    private OrderedMap<Layer, LayerTableModel> layerToTableModelMap = new OrderedMap();
    private List<InfoModelListener> listeners = new ArrayList<InfoModelListener>();

    public void dispose() {
        for (Layer layer : new ArrayList<Layer>(this.getLayers())) {
            this.remove(layer);
        }
    }

    public Collection<LayerTableModel> getLayerTableModels() {
        return Collections.unmodifiableCollection(this.layerToTableModelMap.values());
    }

    public void add(Layer layer, Collection<Feature> features) {
        boolean layerNew = !this.layerToTableModelMap.containsKey(layer);
        LayerTableModel layerTableModel = this.getTableModel(layer);
        layerTableModel.addAll(features);
        if (layerNew) {
            for (InfoModelListener listener : this.listeners) {
                listener.layerAdded(layerTableModel);
            }
        }
    }

    public void addKeys(Layer layer, Collection keys) {
        boolean layerNew = !this.layerToTableModelMap.containsKey(layer);
        LayerTableModel layerTableModel = this.getTableModel(layer);
        layerTableModel.addAllKeys(keys);
        if (layerNew) {
            for (InfoModelListener listener : this.listeners) {
                listener.layerAdded(layerTableModel);
            }
        }
    }

    public void remove(Layer layer) {
        LayerTableModel layerTableModel = this.getTableModel(layer);
        this.layerToTableModelMap.get(layer).dispose();
        this.layerToTableModelMap.remove(layer);
        for (InfoModelListener listener : this.listeners) {
            listener.layerRemoved(layerTableModel);
        }
    }

    public void clear() {
        ArrayList<Layer> layers = new ArrayList<Layer>(this.layerToTableModelMap.keySet());
        for (Layer layer : layers) {
            this.remove(layer);
        }
    }

    public LayerTableModel getTableModel(Layer layer) {
        if (!this.layerToTableModelMap.containsKey(layer)) {
            this.layerToTableModelMap.put(layer, new LayerTableModel(layer));
        }
        return this.layerToTableModelMap.get(layer);
    }

    public List<Layer> getLayers() {
        return Collections.unmodifiableList(this.layerToTableModelMap.keyList());
    }

    public void addListener(InfoModelListener listener) {
        this.listeners.add(listener);
    }

    public List<LayerListener> getLayerListeners() {
        ArrayList<LayerListener> layerListeners = new ArrayList<LayerListener>();
        Collection<LayerTableModel> tableModels = this.getLayerTableModels();
        for (LayerTableModel tableModel : tableModels) {
            layerListeners.add(tableModel.getLayerListener());
        }
        return layerListeners;
    }
}

