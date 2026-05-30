/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class EditTransaction {
    public static final String INVALID_GEOMETRY_CANCELLED = I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid-cancelled");
    public static final String INVALID_GEOMETRY = I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid");
    private List<Feature> features;
    private List<Geometry> originalGeometries;
    private List<Geometry> proposedGeometries;
    private Layer layer;
    private String name;
    private boolean rollingBackEdits;
    private LayerViewPanelContext layerViewPanelContext;
    private GeometryFactory geomFact = new GeometryFactory();
    private GeometryEditor editor = new GeometryEditor();
    private boolean allowAddingAndRemovingFeatures;

    public EditTransaction(Collection<Feature> features, String name, Layer layer, boolean rollingBackEdits, boolean allowAddingAndRemovingFeatures, LayerViewPanel layerViewPanel) {
        this(features, name, layer, rollingBackEdits, allowAddingAndRemovingFeatures, layerViewPanel.getContext());
    }

    public EditTransaction(Collection<Feature> features, String name, Layer layer, boolean rollingBackEdits, boolean allowAddingAndRemovingFeatures, LayerViewPanelContext layerViewPanelContext) {
        this.layerViewPanelContext = layerViewPanelContext;
        this.layer = layer;
        this.rollingBackEdits = rollingBackEdits;
        this.allowAddingAndRemovingFeatures = allowAddingAndRemovingFeatures;
        this.name = name;
        this.features = new ArrayList<Feature>(features);
        this.originalGeometries = this.geometryClones(features);
        this.proposedGeometries = this.geometryClones(features);
    }

    public static EditTransaction createTransactionOnSelection(SelectionEditor editor, SelectionManagerProxy selectionManagerProxy, LayerViewPanelContext layerViewPanelContext, String name, Layer layer, boolean rollingBackEdits, boolean allowAddingAndRemovingFeatures) {
        Map<Feature, Geometry> featureToNewGeometryMap = EditTransaction.featureToNewGeometryMap(editor, selectionManagerProxy, layer);
        EditTransaction transaction = new EditTransaction(featureToNewGeometryMap.keySet(), name, layer, rollingBackEdits, allowAddingAndRemovingFeatures, layerViewPanelContext);
        transaction.setGeometries(featureToNewGeometryMap);
        return transaction;
    }

    private static Map<Feature, Geometry> featureToNewGeometryMap(SelectionEditor editor, SelectionManagerProxy selectionManagerProxy, Layer layer) {
        HashMap<Feature, Geometry> featureToNewGeometryMap = new HashMap<Feature, Geometry>();
        for (Feature feature : selectionManagerProxy.getSelectionManager().getFeaturesWithSelectedItems(layer)) {
            Geometry newGeometry = (Geometry)feature.getGeometry().clone();
            ArrayList<Geometry> selectedItems = new ArrayList<Geometry>();
            for (AbstractSelection selection : selectionManagerProxy.getSelectionManager().getSelections()) {
                selectedItems.addAll(selection.items(newGeometry, selection.getSelectedItemIndices(layer, feature)));
            }
            newGeometry = editor.edit(newGeometry, selectedItems);
            featureToNewGeometryMap.put(feature, newGeometry);
        }
        return featureToNewGeometryMap;
    }

    public Geometry getGeometry(int i) {
        return this.proposedGeometries.get(i);
    }

    public Geometry getGeometry(Feature feature) {
        return this.getGeometry(this.features.indexOf(feature));
    }

    public void setGeometry(Feature feature, Geometry geometry) {
        this.setGeometry(this.features.indexOf(feature), geometry);
    }

    public void setGeometries(Map<Feature, Geometry> featureToGeometryMap) {
        for (Feature feature : featureToGeometryMap.keySet()) {
            this.setGeometry(feature, featureToGeometryMap.get(feature));
        }
    }

    public void setGeometry(int i, Geometry geometry) {
        this.proposedGeometries.set(i, this.editor.removeRepeatedPoints(geometry));
    }

    public boolean commit() throws Exception {
        return EditTransaction.commit(Collections.singleton(this));
    }

    public static boolean commit(Collection<EditTransaction> editTransactions) throws Exception {
        return EditTransaction.commit(editTransactions, new SuccessAction(){

            @Override
            public void run() {
            }
        });
    }

    public static boolean commit(Collection<EditTransaction> editTransactions, SuccessAction successAction) throws Exception {
        if (editTransactions.isEmpty()) {
            return true;
        }
        final ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        for (EditTransaction editTransaction : editTransactions) {
            editTransaction.clearEnvelopeCaches();
            if (!editTransaction.proposedGeometriesValid()) {
                if (editTransaction.rollingBackEdits) {
                    editTransaction.layerViewPanelContext.warnUser(INVALID_GEOMETRY_CANCELLED);
                    return false;
                }
                editTransaction.layerViewPanelContext.warnUser(INVALID_GEOMETRY);
            }
            commands.add(editTransaction.createCommand());
        }
        successAction.run();
        UndoableCommand command = new UndoableCommand(((UndoableCommand)commands.iterator().next()).getName()){

            @Override
            public void execute() throws Exception {
                for (UndoableCommand subCommand : commands) {
                    subCommand.execute();
                }
            }

            @Override
            public void unexecute() throws Exception {
                for (UndoableCommand subCommand : commands) {
                    subCommand.unexecute();
                }
            }
        };
        command.execute();
        editTransactions.iterator().next().layer.getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
        return true;
    }

    public boolean commit(SuccessAction successAction) throws Exception {
        return EditTransaction.commit(Collections.singleton(this), successAction);
    }

    public void clearEnvelopeCaches() {
        int i = 0;
        while (i < this.proposedGeometries.size()) {
            Geometry proposedGeometry = this.proposedGeometries.get(i);
            proposedGeometry.geometryChanged();
            ++i;
        }
    }

    public boolean proposedGeometriesValid() {
        int i = 0;
        while (i < this.proposedGeometries.size()) {
            Geometry proposedGeometry = this.proposedGeometries.get(i);
            if (!proposedGeometry.isValid()) {
                return false;
            }
            ++i;
        }
        return true;
    }

    protected UndoableCommand createCommand() {
        UndoableCommand command = new UndoableCommand(this.name){

            @Override
            public void execute() throws Exception {
                try {
                    EditTransaction.this.changeGeometries(EditTransaction.this.proposedGeometries, EditTransaction.this.originalGeometries, EditTransaction.this.layer);
                }
                catch (Exception e) {
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    this.unexecute();
                }
            }

            @Override
            public void unexecute() throws Exception {
                EditTransaction.this.changeGeometries(EditTransaction.this.originalGeometries, EditTransaction.this.proposedGeometries, EditTransaction.this.layer);
            }
        };
        return command;
    }

    private List<Geometry> geometryClones(Collection<Feature> features) {
        ArrayList<Geometry> geometryClones = new ArrayList<Geometry>();
        for (Feature feature : features) {
            geometryClones.add((Geometry)feature.getGeometry().clone());
        }
        return geometryClones;
    }

    private void changeGeometries(List<Geometry> newGeometries, List<Geometry> oldGeometries, Layer layer) throws Exception {
        ArrayList<Feature> modifiedFeatures = new ArrayList<Feature>();
        ArrayList<Feature> modifiedFeaturesOldClones = new ArrayList<Feature>();
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        ArrayList<Feature> featuresToRemove = new ArrayList<Feature>();
        int i = 0;
        while (i < this.size()) {
            Feature feature = this.features.get(i);
            Geometry oldGeometry = oldGeometries.get(i);
            Geometry newGeometry = newGeometries.get(i);
            if (this.allowAddingAndRemovingFeatures && oldGeometry.isEmpty() && !newGeometry.isEmpty()) {
                featuresToAdd.add(feature);
            } else if (this.allowAddingAndRemovingFeatures && newGeometry.isEmpty() && !oldGeometry.isEmpty()) {
                featuresToRemove.add(feature);
            } else {
                modifiedFeatures.add(feature);
                modifiedFeaturesOldClones.add((Feature)feature.clone());
                feature.setGeometry(newGeometry);
            }
            ++i;
        }
        if (!featuresToRemove.isEmpty()) {
            layer.getFeatureCollectionWrapper().removeAll(featuresToRemove);
        }
        if (!featuresToAdd.isEmpty()) {
            layer.getFeatureCollectionWrapper().addAll(featuresToAdd);
        }
        if (!modifiedFeatures.isEmpty()) {
            layer.getFeatureCollectionWrapper().getUltimateWrappee().updateAll(modifiedFeatures);
            layer.getLayerManager().fireGeometryModified(modifiedFeatures, layer, modifiedFeaturesOldClones);
        }
    }

    public int size() {
        return this.features.size();
    }

    public Feature getFeature(int i) {
        return this.features.get(i);
    }

    public void createFeature(Feature feature) {
        Assert.isTrue((boolean)this.allowAddingAndRemovingFeatures);
        Assert.isTrue((!this.features.contains(feature) ? 1 : 0) != 0);
        this.features.add(feature);
        this.originalGeometries.add((Geometry)this.geomFact.createPoint(null));
        this.proposedGeometries.add((Geometry)feature.getGeometry().clone());
    }

    public void deleteFeature(Feature feature) {
        Assert.isTrue((boolean)this.allowAddingAndRemovingFeatures);
        Assert.isTrue((!this.features.contains(feature) ? 1 : 0) != 0);
        this.features.add(feature);
        this.originalGeometries.add((Geometry)feature.getGeometry().clone());
        this.proposedGeometries.add((Geometry)this.geomFact.createPoint(null));
    }

    public Layer getLayer() {
        return this.layer;
    }

    public static int emptyGeometryCount(Collection<EditTransaction> transactions) {
        int count = 0;
        for (EditTransaction transaction : transactions) {
            count += transaction.getEmptyGeometryCount();
        }
        return count;
    }

    private int getEmptyGeometryCount() {
        int count = 0;
        int i = 0;
        while (i < this.size()) {
            if (this.getGeometry(i).isEmpty()) {
                ++count;
            }
            ++i;
        }
        return count;
    }

    public static interface SelectionEditor {
        public Geometry edit(Geometry var1, Collection<Geometry> var2);
    }

    public static interface SuccessAction {
        public void run();
    }
}

