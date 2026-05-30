/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JDirectoryChooser
 */
package org.saig.jump.plugin.utils;

import com.l2fprod.common.swing.JDirectoryChooser;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.datasource.ShapeFileSaveQueryChooser;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;
import org.saig.jump.widgets.util.DialogFactory;

public class SaveAllViewLayersToShapePlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Save-layers-from-view-to-shape");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String savingDirectoryPath;
    private JDirectoryChooser directoryChooser;

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        int returnVal;
        if (this.directoryChooser == null) {
            this.directoryChooser = new JDirectoryChooser();
        }
        if ((returnVal = this.directoryChooser.showSaveDialog((Component)JUMPWorkbench.getFrameInstance())) == 0) {
            File file = this.directoryChooser.getSelectedFile();
            this.savingDirectoryPath = file.getAbsolutePath();
            if (file.listFiles().length > 0) {
                returnVal = DialogFactory.showYesNoDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Existing-files-will-be-overwritten-into-path-{0}", new Object[]{this.savingDirectoryPath})) + I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Wish-to-continue"), I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Overwrite-existing-files"));
                return returnVal == 0;
            }
            return true;
        }
        return false;
    }

    @Override
    public void finish(PlugInContext context) {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    @Override
    public EnableCheck getCheck() {
        return SaveAllViewLayersToShapePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createAtLeastNLayersMustNotBeRasterCheck(1));
        return solucion;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Saving-layers-to-path-{0}", new Object[]{this.savingDirectoryPath})) + " ...");
        List<Layer> layersToSave = context.getTask().getLayerManager().getLayers();
        if (layersToSave.size() == 0) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.No-layer-to-save-was-found"));
        }
        TreeMap messageMap = new TreeMap();
        ArrayList<SummaryMessage> messageList = new ArrayList<SummaryMessage>();
        int numSavedLayers = 0;
        int numProcessedLayers = 0;
        int numLayers = layersToSave.size();
        Iterator<Layer> iter = layersToSave.iterator();
        while (iter.hasNext() && !monitor.isCancelRequested()) {
            Layer currentLayer = iter.next();
            monitor.report(numProcessedLayers++, numLayers, I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Saving-layer-{0}", new Object[]{currentLayer.getName()}));
            if (currentLayer.isRaster()) continue;
            if (currentLayer.getUltimateFeatureCollectionWrapper().size() == 0) {
                messageList.add(this.buildNoElementsMessage(currentLayer.getName()));
                continue;
            }
            String filePath = FileUtil.addValidExtension(String.valueOf(this.savingDirectoryPath) + File.separatorChar + this.getFileNameForLayer(currentLayer), "shp");
            ShapeFileDataSource.toShape(currentLayer.getUltimateFeatureCollectionWrapper(), filePath, false, ShapeFileSaveQueryChooser.DEFAULT_SAVE_PRIMARY_KEY_VALUE, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
            ++numSavedLayers;
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
            return;
        }
        messageList.add(this.buildSummaryMessage(numLayers, numSavedLayers));
        messageMap.put(I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Summary"), messageList);
        this.showSummary(messageMap, context.getTask().getName());
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Saved-layers-from-view-{0}-into-path-{1}", new Object[]{context.getTask().getName(), this.savingDirectoryPath}));
    }

    private SummaryMessage buildSummaryMessage(int numLayers, int numSavedLayers) {
        String basicMessage = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Layers")) + ": " + I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Processed") + numLayers + " - " + I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Saved") + numSavedLayers;
        String extendedMessage = I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.{0}-layers-were-saved-into-path-{1}", new Object[]{Integer.toString(numSavedLayers), this.savingDirectoryPath});
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 0);
        return message;
    }

    private String getFileNameForLayer(Layer currentLayer) {
        String fileName = "";
        if (currentLayer.isDataBaseDataSource()) {
            AbstractJDBCDataSource datasource = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)currentLayer.getUltimateFeatureCollectionWrapper()).getDataAccesor();
            fileName = datasource.getTableName();
        } else if (!currentLayer.isMemory()) {
            String query = currentLayer.getDataSourceQuery().getQuery();
            fileName = new File(query).getName();
        } else {
            fileName = String.valueOf(currentLayer.getName()) + "_" + I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Memory");
        }
        return fileName;
    }

    private SummaryMessage buildNoElementsMessage(String layerName) {
        String basicMessage = I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Layer-{0}-empty", new Object[]{layerName});
        String extendedMessage = I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Layer-{0}-has-no-elements", new Object[]{layerName});
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 1);
        return message;
    }

    private void showSummary(Map messageMap, String taskName) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Summary-of-export-view-{0}", new Object[]{taskName}), messageMap);
        dialog.setVisible(true);
    }
}

