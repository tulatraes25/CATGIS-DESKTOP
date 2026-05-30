/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import es.kosmo.desktop.images.DesktopIconLoader;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class CommitPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.name");
    public static final Icon ICON = DesktopIconLoader.icon("filesave.png");
    private static final Logger LOGGER = Logger.getLogger(CommitPlugIn.class);
    protected Layer layerSelected;
    protected boolean commitCanceled;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Collection<Layer> layers = context.getLayerManager().getEditableLayers();
        LayerManager manager = context.getLayerManager();
        if (manager == null) {
            return false;
        }
        if (layers.size() > 0) {
            this.layerSelected = layers.iterator().next();
            return true;
        }
        context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.no-modified-layers"));
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        SelectionManager selectManager;
        block12: {
            FeatureCollection fc;
            block11: {
                this.commitCanceled = false;
                context.getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.commiting-changes")) + "...");
                selectManager = null;
                if (context.getActiveInternalFrame() instanceof SelectionManagerProxy) {
                    selectManager = ((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager();
                }
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.commiting-layer")) + " " + this.layerSelected.getName() + " ...");
                fc = this.layerSelected.getFeatureCollectionWrapper().getUltimateWrappee();
                if (!(fc instanceof FeatureDataset)) break block11;
                SaveDatasetAsPlugIn savePlugIn = new SaveDatasetAsPlugIn(true);
                int resultado = DialogFactory.showYesNoDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.do-you-want-to-save-the-layer-permanently"), I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.save-new-layer"));
                if (resultado == 0) {
                    boolean execute = savePlugIn.execute(context);
                    if (!execute) {
                        this.commitCanceled = true;
                        return;
                    }
                    new TaskMonitorManager().execute(savePlugIn, context);
                    if (savePlugIn.getUpdateDataSourceFeatureCollection() != null) {
                        this.layerSelected.setFeatureCollection(savePlugIn.getUpdateDataSourceFeatureCollection());
                    }
                    break block12;
                } else {
                    fc.commit();
                    if (selectManager != null) {
                        selectManager.unselectItems(this.layerSelected);
                    }
                    this.layerSelected.fireAppearanceChanged();
                }
                break block12;
            }
            try {
                fc.commit();
                if (selectManager != null) {
                    selectManager.unselectItems(this.layerSelected);
                }
                this.layerSelected.fireAppearanceChanged();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.commitCanceled = true;
                DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.editing.CommitPlugIn.errors-have-been-produced-while-the-changes-were-saved-in-the-layer-{0}", new Object[]{this.layerSelected.getName()})) + ". " + I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.the-error-description-is") + " :\n" + e.getMessage(), I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.error-saving-changes"));
                return;
            }
        }
        ((LayerManagerProxy)((Object)context.getWorkbenchContext().getWorkbench().getFrame().getActiveInternalFrame())).getLayerManager().getUndoableEditReceiver().getUndoManager().discardAllEdits();
        if (selectManager != null) {
            selectManager.unselectItems(this.layerSelected);
        }
        this.layerSelected.setFeatureCollectionModified(false);
        if (context.getActiveInternalFrame() instanceof TaskFrame) {
            context.getWorkbenchFrame().activateFrame(context.getActiveInternalFrame());
        }
        CursorTool cursor = context.getWorkbenchFrame().getToolBar().getDefaultCursorTool();
        AbstractButton boton = context.getWorkbenchFrame().getToolBar().getButton(cursor.getClass());
        boton.doClick();
        context.getLayerManager().fireLayerChanged(this.layerSelected, LayerEventType.COMMITED);
    }

    public boolean isCommitCanceled() {
        return this.commitCanceled;
    }

    @Override
    public EnableCheck getCheck() {
        return CommitPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = null;
                Collection<Layer> layers = null;
                if (workbenchContext == null || workbenchContext.getLayerNamePanel() == null) {
                    return I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.at-least-one-layer-must-be-modified");
                }
                layers = workbenchContext.getLayerManager().getEditableLayers();
                if (layers != null && layers.size() > 0) {
                    layer = layers.iterator().next();
                }
                if (layer == null || layer != null && !layer.isFeatureCollectionModified() || !layer.isEditable()) {
                    return I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.at-least-one-layer-must-be-modified");
                }
                return null;
            }
        };
        return new MultiEnableCheck().add(check);
    }
}

