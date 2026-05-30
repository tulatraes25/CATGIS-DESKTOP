/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.FeatureSelection;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LineStringSelection;
import com.vividsolutions.jump.workbench.ui.PartSelection;
import com.vividsolutions.jump.workbench.ui.SegmentSelection;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SelectionManager {
    private FeatureSelection featureSelection;
    private PartSelection partSelection;
    private LineStringSelection lineStringSelection;
    private LayerManagerProxy layerManagerProxy;
    private LayerViewPanel panel;
    private List<ViewportListener> listeners = new ArrayList<ViewportListener>();
    private SegmentSelection segmentSelection;
    private List<AbstractSelection> selections;
    private boolean panelUpdatesEnabled = true;
    private LayerListener layerListener = new LayerListener(){

        @Override
        public void featuresChanged(FeatureEvent e) {
            if (e.getType() == FeatureEventType.DELETED) {
                SelectionManager.this.unselectItems(e.getLayer(), e.getFeatures());
            } else if (e.getType() == FeatureEventType.GEOMETRY_MODIFIED) {
                SelectionManager.this.unselectFromFeaturesWithModifiedItemCounts(e.getLayer(), e.getFeatures(), e.getOldFeatureClones());
            }
        }

        @Override
        public void layerChanged(LayerEvent e) {
            if (!(e.getLayerable() instanceof Layer)) {
                return;
            }
            if (e.getType() == LayerEventType.REMOVED || e.getType() == LayerEventType.VISIBILITY_CHANGED && !e.getLayerable().isVisible()) {
                SelectionManager.this.unselectItems((Layer)e.getLayerable());
            }
        }

        @Override
        public void categoryChanged(CategoryEvent e) {
        }
    };

    public SelectionManager(LayerViewPanel panel, LayerManagerProxy layerManagerProxy) {
        this.panel = panel;
        this.layerManagerProxy = layerManagerProxy;
        this.featureSelection = new FeatureSelection(this);
        this.lineStringSelection = new LineStringSelection(this);
        this.partSelection = new PartSelection(this);
        this.segmentSelection = new SegmentSelection(this);
        this.featureSelection.setParent(null);
        this.featureSelection.setChild(this.partSelection);
        this.partSelection.setParent(this.featureSelection);
        this.partSelection.setChild(this.lineStringSelection);
        this.lineStringSelection.setParent(this.partSelection);
        this.lineStringSelection.setChild(null);
        this.selections = Collections.unmodifiableList(Arrays.asList(this.featureSelection, this.partSelection, this.lineStringSelection, this.segmentSelection));
        this.addLayerListenerTo(layerManagerProxy.getLayerManager());
    }

    public Collection<Feature> createFeaturesFromSelectedItems() {
        ArrayList<Feature> newFeatures = new ArrayList<Feature>();
        for (Layer layer : this.getLayersWithSelectedItems()) {
            newFeatures.addAll(this.createFeaturesFromSelectedItems(layer));
        }
        return newFeatures;
    }

    public Collection<Feature> createFeaturesFromSelectedItems(Layer layer) {
        ArrayList<Feature> newFeatures = new ArrayList<Feature>();
        for (Feature feature : this.getFeaturesWithSelectedItems(layer)) {
            for (Geometry item : this.getSelectedItems(layer, feature)) {
                Feature newFeature = (Feature)feature.clone();
                newFeature.setGeometry(item);
                newFeatures.add(newFeature);
            }
        }
        return newFeatures;
    }

    private void addLayerListenerTo(LayerManager layerManager) {
        layerManager.addLayerListener(this.layerListener);
    }

    public void clear() {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            for (AbstractSelection selection : this.selections) {
                selection.unselectItems();
            }
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public void clearSegmentSelection(boolean check) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            this.segmentSelection.unselectItems();
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.panel.fireSelectionChanged(check);
        this.panel.getRenderingManager().render("SELECTED_SEGMENTS");
    }

    public void clearFeatureSelection(boolean check) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            this.featureSelection.unselectItems();
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel(check);
    }

    public void clearSegmentSelection() {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            this.segmentSelection.unselectItems();
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.panel.getRenderingManager().render("SELECTED_SEGMENTS");
    }

    public void clearFeatureSelection() {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            this.featureSelection.unselectItems();
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public void clear(boolean check) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            for (AbstractSelection selection : this.selections) {
                selection.unselectItems();
            }
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel(check);
    }

    public FeatureSelection getFeatureSelection() {
        return this.featureSelection;
    }

    public LineStringSelection getLineStringSelection() {
        return this.lineStringSelection;
    }

    public SegmentSelection getSegmentSelection() {
        return this.segmentSelection;
    }

    public Collection<AbstractSelection> getSelections() {
        return this.selections;
    }

    public Collection<Geometry> getSelectedItems() {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        for (AbstractSelection selection : this.selections) {
            selectedItems.addAll(selection.getSelectedItems());
        }
        return selectedItems;
    }

    public Collection<Geometry> getSelectedItems(Layer layer) {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        for (AbstractSelection selection : this.selections) {
            selectedItems.addAll(selection.getSelectedItems(layer));
        }
        return selectedItems;
    }

    public Collection<Geometry> getSelectedItems(Layer layer, Feature feature) {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        for (AbstractSelection selection : this.selections) {
            selectedItems.addAll(selection.getSelectedItems(layer, feature));
        }
        return selectedItems;
    }

    public Collection<Geometry> getSelectedItems(Layer layer, Feature feature, Geometry geometry) {
        ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
        for (AbstractSelection selection : this.selections) {
            selectedItems.addAll(selection.getSelectedItems(layer, feature, geometry));
        }
        return selectedItems;
    }

    public Collection<Layer> getLayersWithSelectedItems() {
        HashSet<Layer> layersWithSelectedItems = new HashSet<Layer>();
        for (AbstractSelection selection : this.selections) {
            layersWithSelectedItems.addAll(selection.getLayersWithSelectedItems());
        }
        return layersWithSelectedItems;
    }

    public PartSelection getPartSelection() {
        return this.partSelection;
    }

    public void updatePanel(boolean check) {
        if (!this.panelUpdatesEnabled) {
            return;
        }
        this.panel.fireSelectionChanged(check);
        this.panel.getRenderingManager().render("SELECTION_BACKGROUND");
        for (AbstractSelection selection : this.selections) {
            this.panel.getRenderingManager().render(selection.getRendererContentID());
        }
    }

    public void updatePanel() {
        this.updatePanel(false);
    }

    public void setPanelUpdatesEnabled(boolean panelUpdatesEnabled) {
        this.panelUpdatesEnabled = panelUpdatesEnabled;
    }

    public Collection<Feature> getFeaturesWithSelectedItems(Layer layer) {
        HashSet<Feature> featuresWithSelectedItems = new HashSet<Feature>();
        for (AbstractSelection selection : this.selections) {
            featuresWithSelectedItems.addAll(selection.getFeaturesWithSelectedItems(layer));
        }
        return featuresWithSelectedItems;
    }

    public int getNumFeaturesWithSelectedItems(Layer layer) {
        return this.getFeatureSelection().getNumFeaturesWithSelectedItems(layer);
    }

    public void unselectItems(Layer layer) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            for (AbstractSelection selection : this.selections) {
                selection.unselectItems(layer);
            }
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public void unselectItems(Layer layer, Collection<Feature> features) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            for (AbstractSelection selection : this.selections) {
                selection.unselectItems(layer, features);
            }
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public void unselectFromFeaturesWithModifiedItemCounts(Layer layer, Collection<Feature> features, Collection<Feature> oldFeatureClones) {
        boolean originalPanelUpdatesEnabled = this.arePanelUpdatesEnabled();
        this.setPanelUpdatesEnabled(false);
        try {
            for (AbstractSelection selection : this.selections) {
                selection.unselectFromFeaturesWithModifiedItemCounts(layer, features, oldFeatureClones);
            }
        }
        finally {
            this.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        this.updatePanel();
    }

    public Collection<Feature> getFeaturesWithSelectedItems() {
        ArrayList<Feature> featuresWithSelectedItems = new ArrayList<Feature>();
        for (Layer layer : this.getLayersWithSelectedItems()) {
            featuresWithSelectedItems.addAll(this.getFeaturesWithSelectedItems(layer));
        }
        return featuresWithSelectedItems;
    }

    public int getNumFeaturesWithSelectedItems() {
        int numFeatures = 0;
        for (Layer layer : this.getLayersWithSelectedItems()) {
            numFeatures += this.getNumFeaturesWithSelectedItems(layer);
        }
        return numFeatures;
    }

    public boolean arePanelUpdatesEnabled() {
        return this.panelUpdatesEnabled;
    }

    public void dispose() {
        this.layerManagerProxy.getLayerManager().removeLayerListener(this.layerListener);
        this.listeners = null;
        this.panel = null;
    }

    public void addListener(ViewportListener l) {
        this.listeners.add(l);
    }

    public void removeListener(ViewportListener l) {
        this.listeners.remove(l);
    }
}

