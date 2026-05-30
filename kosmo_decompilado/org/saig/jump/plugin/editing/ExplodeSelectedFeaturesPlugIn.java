/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public class ExplodeSelectedFeaturesPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(ExplodeSelectedFeaturesPlugIn.class);
    public static final String NAME = I18N.getString(ExplodeSelectedFeaturesPlugIn.class, "explode-selected-features");
    public static final Icon ICON = IconLoader.icon("explodeSelectedFeatures.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return ExplodeSelectedFeaturesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer editableLayer = this.getLayer(context);
        FeatureSchema schema = editableLayer.getFeatureSchema();
        String pkAttrName = schema.getPrimaryKeyName();
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        Collection<Feature> selectedFeatures = selectionManager.getFeaturesWithSelectedItems(editableLayer);
        int numSelectedItems = selectedFeatures.size();
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>(numSelectedItems);
        ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        monitor.report(I18N.getMessage(ExplodeSelectedFeaturesPlugIn.class, "exploding-{0}-selected-features", new Object[]{numSelectedItems}));
        int cont = 0;
        for (Feature currentFeat : selectedFeatures) {
            boolean isMultiGeometry;
            monitor.report(cont++, numSelectedItems, I18N.getString(ExplodeSelectedFeaturesPlugIn.class, "exploded-features"));
            Geometry currentGeom = currentFeat.getGeometry();
            boolean bl = isMultiGeometry = currentGeom.getNumGeometries() > 1;
            if (!isMultiGeometry) continue;
            int i = 0;
            while (i < currentGeom.getNumGeometries()) {
                Geometry part = currentGeom.getGeometryN(i);
                Feature cloneFeat = FeatureUtil.copyFeature(schema, currentFeat);
                cloneFeat.setGeometry(geomFac.createGeometry(part));
                if (i == 0) {
                    featsToUpdate.add(cloneFeat);
                } else {
                    cloneFeat.setAttribute(pkAttrName, null);
                    featsToAdd.add(cloneFeat);
                }
                ++i;
            }
        }
        if (!featsToAdd.isEmpty() || !featsToUpdate.isEmpty()) {
            this.applyChanges(selectionManager, editableLayer, selectedFeatures, featsToAdd, featsToUpdate, context);
        }
    }

    protected void applyChanges(final SelectionManager selectionManager, final Layer editableLayer, final Collection<Feature> featsSelectedToUpdate, final List<Feature> featsToAdd, final List<Feature> featsToUpdate, PlugInContext context) throws Exception {
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + featsSelectedToUpdate.size() + I18N.getString(ExplodeSelectedFeaturesPlugIn.class, "modified-elements") + " (<I>" + editableLayer.getName() + "</I>)"){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    try {
                        editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
                    }
                    catch (TopologyRelationException e) {
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                        return;
                    }
                }
                if (!featsToUpdate.isEmpty()) {
                    try {
                        editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, editableLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsToUpdate);
                    }
                    catch (TopologyRelationException e) {
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                        return;
                    }
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    try {
                        editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                    }
                    catch (TopologyRelationException e) {
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                        return;
                    }
                }
                if (!featsToUpdate.isEmpty()) {
                    try {
                        editableLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                        editableLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, editableLayer, featsToUpdate);
                    }
                    catch (TopologyRelationException e) {
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                        return;
                    }
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
            }
        }, context);
    }

    protected Layer getLayer(PlugInContext context) {
        return context.getLayerManager().getEditableLayers().iterator().next();
    }
}

