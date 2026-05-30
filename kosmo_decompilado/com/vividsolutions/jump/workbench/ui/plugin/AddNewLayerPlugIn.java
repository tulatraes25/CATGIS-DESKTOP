/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.cresques.cts.IProjection;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SelectGeometryTypeDialog;

public class AddNewLayerPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.AddNewLayerPlugIn.name");
    public static final Icon ICON = IconLoader.icon("layer_new.png");
    private static SelectGeometryTypeDialog dialog;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return AddNewLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static FeatureCollection createBlankFeatureCollection(int geomType) {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        featureSchema.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        featureSchema.setGeometryType(geomType);
        return new FeatureDataset(featureSchema);
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (dialog == null) {
            dialog = new SelectGeometryTypeDialog(context.getWorkbenchFrame(), true, true, null);
            GUIUtil.centreOnScreen(dialog);
        } else {
            dialog.recalculateLayers();
        }
        dialog.setVisible(true);
        if (dialog.wasOKPressed()) {
            String categoryName = LoadDatasetPlugIn.chooseCategory(context);
            int geomType = dialog.getGeometryType();
            IProjection projection = context.getTask().getProjection();
            Layer newLayer = null;
            if (dialog.isListEnabled() && dialog.getSelectedLayerName() != null && !dialog.getSelectedLayerName().equals("-------")) {
                FeatureSchema selectedFs = (FeatureSchema)dialog.getSelectedSchema().clone();
                selectedFs.setGeometryType(geomType);
                FeatureDataset fds = new FeatureDataset(selectedFs);
                fds.set3d(dialog.is3D());
                newLayer = context.addLayer(categoryName, I18N.getString("workbench.ui.plugin.AddNewLayerPlugIn.new"), fds);
            } else {
                FeatureCollection fcol = AddNewLayerPlugIn.createBlankFeatureCollection(geomType);
                fcol.set3d(dialog.is3D());
                newLayer = context.addLayer(categoryName, I18N.getString("workbench.ui.plugin.AddNewLayerPlugIn.new"), fcol);
            }
            newLayer.setProjection(projection);
            newLayer.setFeatureCollectionModified(false);
        }
        return true;
    }
}

