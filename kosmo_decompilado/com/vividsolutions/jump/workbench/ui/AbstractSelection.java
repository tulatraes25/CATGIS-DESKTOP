/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.jump.lang.I18N;

public abstract class AbstractSelection {
    private static final Logger LOGGER = Logger.getLogger(AbstractSelection.class);
    private Map<Layer, CollectionMap> layerMap = Collections.synchronizedMap(new HashMap());
    private AbstractSelection child;
    private AbstractSelection parent;
    protected SelectionManager selectionManager;
    protected Hashtable<String, FeatureDataset> selectionToPaintForLayers = new Hashtable();

    public abstract String getRendererContentID();

    public AbstractSelection(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    public abstract List<Geometry> items(Geometry var1);

    public Collection<Geometry> items(Geometry geometry, Collection<Integer> indices) {
        List<Geometry> allItems = this.items(geometry);
        ArrayList<Geometry> items = new ArrayList<Geometry>();
        for (Integer index : indices) {
            items.add(allItems.get(index));
        }
        return items;
    }

    public CollectionMap getFeatureToSelectedItemIndexCollectionMap(Layer layer) {
        if (!this.layerMap.containsKey(layer)) {
            this.layerMap.put(layer, new CollectionMap(HashMap.class, HashSet.class));
        }
        return this.layerMap.get(layer);
    }

    public Collection<Integer> getSelectedItemIndices(Layer layer, Feature feature) {
        Collection indices = this.getFeatureToSelectedItemIndexCollectionMap(layer).getItems(feature);
        return indices == null ? new ArrayList() : indices;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CollectionMap getFeatureToSelectedItemCollectionMap(Layer layer) {
        Set<Feature> keySet;
        CollectionMap collectionMap = new CollectionMap();
        Set<Feature> set = keySet = Collections.synchronizedSet(this.getFeatureToSelectedItemIndexCollectionMap(layer).keySet());
        synchronized (set) {
            for (Feature feature : keySet) {
                collectionMap.put(feature, this.items(feature.getGeometry(), this.getFeatureToSelectedItemIndexCollectionMap(layer).getItems(feature)));
            }
        }
        return collectionMap;
    }

    public Collection<Feature> getFeatureToSelectedItemCollectionMap(Layer layer, Envelope envelope) {
        if (this.selectionToPaintForLayers.containsKey(layer.getName())) {
            FeatureDataset dataset = this.selectionToPaintForLayers.get(layer.getName());
            return dataset.query(envelope);
        }
        return new ArrayList<Feature>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<Layer> getLayersWithSelectedItems() {
        ArrayList<Layer> layersWithSelectedItems = new ArrayList<Layer>();
        Map<Layer, CollectionMap> map = this.layerMap;
        synchronized (map) {
            for (Layer layer : this.layerMap.keySet()) {
                if (!this.hasFeaturesWithSelectedItems(layer)) continue;
                layersWithSelectedItems.add(layer);
            }
        }
        return layersWithSelectedItems;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<Feature> getFeaturesWithSelectedItems() {
        ArrayList<Feature> featuresWithSelectedItems = new ArrayList<Feature>();
        Map<Layer, CollectionMap> map = this.layerMap;
        synchronized (map) {
            for (Layer layer : this.layerMap.keySet()) {
                featuresWithSelectedItems.addAll(this.getFeaturesWithSelectedItems(layer));
            }
        }
        return featuresWithSelectedItems;
    }

    public Collection<Feature> getFeaturesWithSelectedItems(Layer layer) {
        ArrayList<Feature> featuresWithSelectedItems = new ArrayList<Feature>();
        CollectionMap col = this.getFeatureToSelectedItemIndexCollectionMap(layer);
        if (col == null || col.isEmpty()) {
            return featuresWithSelectedItems;
        }
        for (Feature feature : col.keySet()) {
            featuresWithSelectedItems.add(feature);
        }
        return featuresWithSelectedItems;
    }

    public boolean hasFeaturesWithSelectedItems(Layer layer) {
        CollectionMap col = this.getFeatureToSelectedItemIndexCollectionMap(layer);
        return col != null && !col.isEmpty();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<Geometry> getSelectedItems() {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        Map<Layer, CollectionMap> map = this.layerMap;
        synchronized (map) {
            for (Layer layer : new ArrayList<Layer>(this.layerMap.keySet())) {
                selectedItems.addAll(this.getSelectedItems(layer));
            }
        }
        return selectedItems;
    }

    public Collection<Geometry> getSelectedItems(Layer layer) {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        for (Feature feature : this.getFeatureToSelectedItemIndexCollectionMap(layer).keySet()) {
            selectedItems.addAll(this.getSelectedItems(layer, feature));
        }
        return selectedItems;
    }

    public Collection<Geometry> getSelectedItems(Layer layer, Feature feature) {
        return this.getSelectedItems(layer, feature, feature.getGeometry());
    }

    public Collection<Geometry> getSelectedItems(Layer layer, Feature feature, Geometry geometry) {
        return this.items(geometry, this.getFeatureToSelectedItemIndexCollectionMap(layer).getItems(feature));
    }

    public Collection<Integer> indices(Geometry geometry, Collection<Geometry> items) {
        List<Geometry> allItems = this.items(geometry);
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (Geometry item : items) {
            boolean found = false;
            for (Geometry geom : allItems) {
                if (!geom.equalsExact(item)) continue;
                indices.add(allItems.indexOf(geom));
                found = true;
                break;
            }
            if (found) continue;
            indices.add(-1);
        }
        return indices;
    }

    public void unselectItems(Layer layer, CollectionMap featureToItemCollectionMap) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            for (Feature feature : featureToItemCollectionMap.keySet()) {
                this.unselectItems(layer, feature, featureToItemCollectionMap.getItems(feature));
            }
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.regenerateDataset(layer);
        this.updatePanel();
    }

    public void selectItems(Layer layer, CollectionMap featureToItemCollectionMap) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            for (Feature feature : featureToItemCollectionMap.keySet()) {
                this.selectItems(layer, feature, featureToItemCollectionMap.getItems(feature), false);
            }
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.regenerateDataset(layer);
        this.updatePanel();
    }

    public void selectItems(Layer layer, Feature feature, Collection<Geometry> items, boolean regenerateDataset) {
        Collection<Geometry> itemsToSelect = this.itemsNotSelectedInAncestors(layer, feature, items);
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            this.unselectInDescendants(layer, feature, itemsToSelect);
            this.getFeatureToSelectedItemIndexCollectionMap(layer).addItems(feature, this.indices(feature.getGeometry(), itemsToSelect));
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        if (regenerateDataset) {
            this.regenerateDataset(layer);
        }
        this.updatePanel();
    }

    public void unselectItems(Layer layer, Feature feature, Collection<Geometry> items) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            this.getFeatureToSelectedItemIndexCollectionMap(layer).removeItems(feature, this.indices(feature.getGeometry(), items));
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public Collection<Geometry> itemsNotSelectedInAncestors(Layer layer, Feature feature, Collection<Geometry> items) {
        ArrayList<Geometry> itemsNotSelectedInAncestors = new ArrayList<Geometry>();
        for (Geometry item : items) {
            if (this.selectedInAncestors(layer, feature, item)) continue;
            itemsNotSelectedInAncestors.add(item);
        }
        return itemsNotSelectedInAncestors;
    }

    protected abstract boolean selectedInAncestors(Layer var1, Feature var2, Geometry var3);

    protected abstract void unselectInDescendants(Layer var1, Feature var2, Collection<Geometry> var3);

    public void selectItems(Layer layer, Feature feature) {
        this.selectItems(layer, feature, true);
    }

    public void selectItems(Layer layer, Feature feature, boolean regenerateDataset) {
        if (feature == null) {
            LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.AbstractSelection.null-feature"));
            return;
        }
        this.selectItems(layer, feature, this.items(feature.getGeometry()), regenerateDataset);
    }

    public void selectItems(Layer layer, Collection<Feature> features) {
        this.selectItems(layer, features, false);
    }

    public void selectItems(Layer layer, Collection<Feature> features, boolean check) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            for (Feature feature : features) {
                this.selectItems(layer, feature, false);
            }
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.regenerateDataset(layer);
        this.updatePanel(check);
    }

    protected void clearDataset(Layer layer) {
        if (this.selectionToPaintForLayers.containsKey(layer.getName())) {
            this.selectionToPaintForLayers.get(layer.getName()).clear();
        }
        if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && ((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource) {
            layer.refreshSelection(new ArrayList<Feature>());
        }
    }

    public void unselectFromFeaturesWithModifiedItemCounts(Layer layer, Collection<Feature> features, Collection<Feature> oldFeatureClones) {
        ArrayList<Feature> featuresToUnselect = new ArrayList<Feature>();
        Iterator<Feature> j = oldFeatureClones.iterator();
        j.hasNext();
        for (Feature feature : features) {
            Feature oldFeatureClone = j.next();
            if (this.items(feature.getGeometry()).size() == this.items(oldFeatureClone.getGeometry()).size()) continue;
            featuresToUnselect.add(feature);
        }
        this.unselectItems(layer, featuresToUnselect);
    }

    public void unselectItems() {
        this.unselectItems(false);
    }

    public void unselectItems(boolean check) {
        for (Layer layer : this.layerMap.keySet()) {
            if (layer == null) continue;
            this.clearDataset(layer);
            this.selectionToPaintForLayers.remove(layer.getName());
        }
        this.layerMap.clear();
        this.selectionToPaintForLayers.clear();
        this.updatePanel(true);
    }

    public void unselectItems(Layer layer) {
        this.clearDataset(layer);
        this.selectionToPaintForLayers.remove(layer.getName());
        this.layerMap.remove(layer);
        this.selectionToPaintForLayers.remove(layer.getName());
        this.updatePanel();
    }

    public void unselectItems(Layer layer, Collection<Feature> features) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            for (Feature feature : features) {
                this.unselectItems(layer, feature);
            }
            this.regenerateDataset(layer);
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public void unselectItems(Layer layer, Feature feature) {
        this.getFeatureToSelectedItemIndexCollectionMap(layer).remove(feature);
        this.updatePanel();
    }

    public void unselectItem(Layer layer, Feature feature, int selectedItemIndex) {
        this.getFeatureToSelectedItemIndexCollectionMap(layer).removeItem(feature, selectedItemIndex);
    }

    protected void updatePanel() {
        this.selectionManager.updatePanel();
    }

    private void updatePanel(boolean check) {
        this.selectionManager.updatePanel(check);
    }

    public void setChild(AbstractSelection child) {
        this.child = child;
    }

    public void setParent(AbstractSelection parent) {
        this.parent = parent;
    }

    protected AbstractSelection getChild() {
        return this.child;
    }

    protected AbstractSelection getParent() {
        return this.parent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getNumLayersWithSelectedItems() {
        int numLayers = 0;
        Map<Layer, CollectionMap> map = this.layerMap;
        synchronized (map) {
            for (Layer layer : this.layerMap.keySet()) {
                if (this.getFeatureToSelectedItemIndexCollectionMap(layer).isEmpty()) continue;
                ++numLayers;
            }
        }
        return numLayers;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getNumFeaturesWithSelectedItems() {
        int numFeatures = 0;
        Map<Layer, CollectionMap> map = this.layerMap;
        synchronized (map) {
            for (Layer layer : this.layerMap.keySet()) {
                int numCurrent = this.getFeatureToSelectedItemIndexCollectionMap(layer).size();
                numFeatures += numCurrent;
            }
        }
        return numFeatures;
    }

    public int getNumFeaturesWithSelectedItems(Layer layer) {
        return this.getFeatureToSelectedItemIndexCollectionMap(layer).size();
    }

    protected void regenerateDataset(Layer layer) {
        Collection<Feature> selectedFeatures = this.getFeaturesWithSelectedItems(layer);
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        for (Feature element : selectedFeatures) {
            geoms.addAll(this.getSelectedItems(layer, element));
        }
        try {
            if (this.selectionToPaintForLayers.containsKey(layer.getName())) {
                this.selectionToPaintForLayers.get(layer.getName()).clear();
            }
            this.selectionToPaintForLayers.put(layer.getName(), (FeatureDataset)FeatureDatasetFactory.createFromGeometry(geoms));
            if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && ((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource) {
                layer.refreshSelection(selectedFeatures);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }
}

