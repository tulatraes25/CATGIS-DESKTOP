/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class ReverseLinestringPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(ReverseLinestringPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.ReverseLinestringPlugIn.reverse-selected-lines");
    public static final Icon ICON = GUIUtil.resize(IconLoader.icon("refreshCursor.png"), 20);

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        final Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        final SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        if (editableLayer == null) {
            return false;
        }
        final Collection<Feature> selectedFeatures = selectionManager.getFeaturesWithSelectedItems(editableLayer);
        final ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        final ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        for (Feature currentFeature : selectedFeatures) {
            Feature clonedFeature = (Feature)currentFeature.clone();
            Geometry g = clonedFeature.getGeometry();
            Geometry reverse = null;
            if (g instanceof LineString) {
                reverse = ((LineString)g).reverse();
                clonedFeature.setGeometry(reverse);
            } else if (g instanceof MultiLineString) {
                reverse = ((MultiLineString)g).reverse();
                clonedFeature.setGeometry(reverse);
            }
            featsToUpdate.add(clonedFeature);
            featsSelectedToUpdate.add(currentFeature);
        }
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("org.saig.jump.plugin.editing.ReverseLinestringPlugIn.{0}-reversed-features", new Object[]{featsToUpdate.size()})){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToUpdate.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                    editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, editableLayer, featsSelectedToUpdate);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToUpdate);
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToUpdate.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    editableLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, editableLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
            }
        }, context);
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
        return ReverseLinestringPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createOnlyOneLayerMayHaveSelectedFeaturesCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(new int[]{3, 2}, new int[]{9}, 1));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }
}

