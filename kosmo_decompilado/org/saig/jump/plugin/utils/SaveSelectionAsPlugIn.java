/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserDialog;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.Icon;
import org.saig.core.model.feature.TemporalFeatureDataset;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class SaveSelectionAsPlugIn
extends ThreadedBasePlugIn {
    private static final String NAME = I18N.getString("org.saig.jump.plugin.utils.SaveSelectionAsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("SaveTheme.gif");

    protected DataSourceQueryChooserDialog getDialog(PlugInContext context) {
        String KEY = String.valueOf(this.getClass().getName()) + " - DIALOG";
        if (JUMPWorkbench.getBlackboard().get(KEY) == null) {
            JUMPWorkbench.getBlackboard().put(KEY, new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard()).getSaveDataSourceQueryChoosers(), context.getWorkbenchFrame(), this.getName(), true));
        }
        return (DataSourceQueryChooserDialog)JUMPWorkbench.getBlackboard().get(KEY);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        GUIUtil.centreOnWindow(this.getDialog(context));
        this.getDialog(context).setVisible(true);
        return this.getDialog(context).wasOKPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer selectedLayer = (Layer)context.getSelectedLayer(0);
        int numFeatureSelected = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(selectedLayer);
        if (numFeatureSelected > 0) {
            Collection<Feature> selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(selectedLayer);
            Assert.isTrue((this.getDialog(context).getCurrentChooser().getDataSourceQueries().size() == 1 ? 1 : 0) != 0);
            DataSourceQuery dataSourceQuery = this.getDialog(context).getCurrentChooser().getDataSourceQueries().iterator().next();
            Assert.isTrue((boolean)dataSourceQuery.getDataSource().isWritable());
            Connection connection = dataSourceQuery.getDataSource().getConnection();
            monitor.report(I18N.getMessage("workbench.datasource.SaveDatasetAsPlugIn.saving-layer-{0}", new Object[]{selectedLayer.getName()}));
            TemporalFeatureDataset fcToSave = new TemporalFeatureDataset(selectedFeatures, selectedLayer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
            fcToSave.setName(selectedLayer.getTitle());
            boolean saveCalculatedAttributes = false;
            if (selectedLayer.getFeatureSchema().hasCalculatedAttributes()) {
                int result = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.utils.SaveSelectionAsPlugIn.the-layer-that-you-want-to-save-has-related-fields-do-you-want-to-save-them-too"), I18N.getString("org.saig.jump.plugin.utils.SaveSelectionAsPlugIn.Layer-with-related-fields"));
                if (result == 0) {
                    saveCalculatedAttributes = true;
                } else if (result == 2) {
                    JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("org.saig.jump.plugin.utils.SaveSelectionAsPlugIn.Operation-cancelled-by-the-user"));
                    return;
                }
            }
            try {
                connection.executeUpdate(dataSourceQuery.getQuery(), fcToSave, saveCalculatedAttributes, selectedLayer.getModelStyle());
            }
            finally {
                connection.close();
            }
            context.getWorkbenchFrame().warnUser(I18N.getMessage("workbench.datasource.SaveDatasetAsPlugIn.layer-{0}-successfully-saved", new Object[]{selectedLayer.getName()}));
            ((Layer)context.getSelectedLayer(0)).setFeatureCollectionModified(false);
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveSelectionAsPlugIn.the-layer-{0}-does-not-have-selected-items", new Object[]{selectedLayer.getName()}));
        }
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}

