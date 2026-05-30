/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.ArrayUtils
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class SelectTool
extends DragTool {
    private String rendererID;
    protected AbstractSelection selection;

    protected SelectTool(String rendererID) {
        this.rendererID = rendererID;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!this.activate) {
            return;
        }
        try {
            super.mouseClicked(e);
            this.setViewSource(e.getPoint());
            this.setViewDestination(e.getPoint());
            this.fireGestureFinished();
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    protected void gestureFinished() throws Exception {
        Collection<Layer> layerEditables;
        this.reportNothingToUndoYet();
        if (!this.wasShiftPressed()) {
            this.getPanel().getSelectionManager().clear();
        }
        Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap = this.getPanel().visibleLayerToFeaturesInFenceMap(EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates()));
        Object[] layers = this.getWorkbench().getContext().getLayerNamePanel().getSelectedLayers();
        if (ArrayUtils.isEmpty((Object[])layers) && !(layerEditables = this.getWorkbench().getContext().getLayerManager().getEditableLayers()).isEmpty()) {
            layers = new Layer[]{layerEditables.iterator().next()};
        }
        int i = 0;
        while (i < layers.length) {
            Layer layer = (Layer)layers[i];
            boolean originalPanelUpdatesEnabled = this.getPanel().getSelectionManager().arePanelUpdatesEnabled();
            this.getPanel().getSelectionManager().setPanelUpdatesEnabled(false);
            try {
                Collection<Feature> col = layerToFeaturesInFenceMap.get(layer);
                if (col != null) {
                    CollectionMap featureToItemsToSelectMap = this.featureToItemsInFenceMap(col, layer, false);
                    CollectionMap featureToItemsToUnselectMap = this.featureToItemsInFenceMap(col, layer, true);
                    this.selection.selectItems(layer, featureToItemsToSelectMap);
                    if (this.wasShiftPressed()) {
                        this.selection.unselectItems(layer, featureToItemsToUnselectMap);
                    }
                    this.featureToItemsInFenceMap(col, layer, true);
                }
            }
            finally {
                this.getPanel().getSelectionManager().setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
            }
            ++i;
        }
        this.getPanel().getSelectionManager().updatePanel();
    }

    protected boolean selectedLayersOnly() {
        return this.wasControlPressed();
    }

    protected CollectionMap featureToItemsInFenceMap(Collection<Feature> features, Layer layer, boolean selected) throws NoninvertibleTransformException {
        CollectionMap featureToSelectedItemsMap = this.selection.getFeatureToSelectedItemCollectionMap(layer);
        CollectionMap featureToItemsInFenceMap = new CollectionMap();
        for (Feature feature : features) {
            Collection selectedItems = featureToSelectedItemsMap.getItems(feature);
            Collection<Geometry> itemsToReturn = this.itemsInFence(feature);
            if (selected) {
                itemsToReturn.retainAll(selectedItems);
            } else {
                itemsToReturn.removeAll(selectedItems);
            }
            featureToItemsInFenceMap.put(feature, itemsToReturn);
        }
        return featureToItemsInFenceMap;
    }

    private Collection<Geometry> itemsInFence(Feature feature) throws NoninvertibleTransformException {
        ArrayList<Geometry> itemsInFence = new ArrayList<Geometry>();
        Geometry fence = EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates());
        for (Geometry selectedItem : this.selection.items(feature.getGeometry())) {
            if (!LayerViewPanel.intersects(selectedItem, fence)) continue;
            itemsInFence.add(selectedItem);
        }
        return itemsInFence;
    }

    protected void refreshSelection(Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap) throws Exception {
        Layer[] layers = new Layer[layerToFeaturesInFenceMap.keySet().size()];
        layerToFeaturesInFenceMap.keySet().toArray(layers);
        int i = 0;
        while (i < layers.length) {
            block11: {
                Layer layer = layers[i];
                boolean originalPanelUpdatesEnabled = this.getPanel().getSelectionManager().arePanelUpdatesEnabled();
                this.getPanel().getSelectionManager().setPanelUpdatesEnabled(false);
                try {
                    Collection<Feature> featuresToSelect = layerToFeaturesInFenceMap.get(layer);
                    if (featuresToSelect == null) break block11;
                    Collection<Feature> featuresToUnselect = this.selection.getFeaturesWithSelectedItems(layer);
                    featuresToUnselect.retainAll(featuresToSelect);
                    if (layer.isEditable() && layer.isDataBaseDataSource() && this.isConcurrentEditionActivated()) {
                        try {
                            HashSet<Feature> listaTotal = new HashSet<Feature>();
                            for (Feature feature : featuresToSelect) {
                                if (featuresToUnselect.contains(feature)) continue;
                                listaTotal.add(feature);
                                listaTotal.addAll(Utils.getColindantes(feature.getGeometry(), layer));
                            }
                            layer.getTransactionalDataSource().lockFeatures(listaTotal);
                        }
                        catch (SQLException e) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.another-user-has-blocked-the-records-selected-for-editing")) + ".\n" + I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.the-transaction-is-cancelled"), I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.blocking-error"));
                            this.getPanel().getSelectionManager().setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
                            return;
                        }
                    }
                    this.selection.selectItems(layer, featuresToSelect);
                    if (this.wasShiftPressed()) {
                        this.selection.unselectItems(layer, featuresToUnselect);
                    }
                }
                finally {
                    this.getPanel().getSelectionManager().setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
                }
            }
            ++i;
        }
        this.getPanel().getSelectionManager().updatePanel();
    }
}

