/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.CommitPlugIn;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.DialogFactory;

public class EditablePlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(EditablePlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.plugin.EditablePlugIn.name");
    public static final Icon ICON = IconLoader.icon("Draw.gif");
    private EditingPlugIn editingPlugIn;

    public EditablePlugIn(EditingPlugIn editingPlugIn) {
        this.editingPlugIn = editingPlugIn;
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
    public EnableCheck getCheck() {
        return EditablePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        boolean makeEditable;
        this.reportNothingToUndoYet(context);
        Collection<Layer> col = context.getWorkbenchContext().getLayerNamePanel().getLayerManager().getEditableLayers();
        Layer editableLayer = null;
        if (!col.isEmpty()) {
            editableLayer = col.iterator().next();
            boolean check = EditablePlugIn.askCommitIfLayerIsEditable(editableLayer, context);
            if (!check) {
                return false;
            }
            if (context.getLayerViewPanel() != null) {
                context.getLayerViewPanel().getRenderingManager().render("SELECTED_FEATURES");
                context.getLayerViewPanel().getRenderingManager().render("SELECTED_SEGMENTS");
            }
            editableLayer.setEditable(false);
        }
        if (context.getSelectedLayers().length == 0 || context.getSelectedLayer(0).equals(editableLayer)) {
            if (context.getLayerViewPanel() != null) {
                context.getLayerViewPanel().getRenderingManager().render("SELECTED_FEATURES");
                context.getLayerViewPanel().getRenderingManager().render("SELECTED_SEGMENTS");
            }
            this.resetCursorTool();
            return true;
        }
        Layer selectedLayer = (Layer)context.getSelectedLayer(0);
        boolean bl = makeEditable = !selectedLayer.isEditable();
        if (makeEditable) {
            SelectionManager selectionManager;
            Collection<Feature> selectedFeatures;
            for (Layer currentLayer : context.getLayerManager().getLayers()) {
                currentLayer.setEditable(false);
            }
            selectedLayer.setEditable(true);
            if (selectedLayer.isDataBaseDataSource() && context.getLayerViewPanel() != null && (selectedFeatures = (selectionManager = context.getLayerViewPanel().getSelectionManager()).getFeaturesWithSelectedItems(selectedLayer)) != null && !selectedFeatures.isEmpty() && this.isConcurrentEditionActivated()) {
                selectionManager.getFeatureSelection().unselectItems(selectedLayer);
                HashSet<Feature> listaTotal = new HashSet<Feature>();
                for (Feature feature : selectedFeatures) {
                    listaTotal.add(feature);
                    listaTotal.addAll(Utils.getColindantes(feature.getGeometry(), selectedLayer));
                }
                selectedLayer.getTransactionalDataSource().lockFeatures(listaTotal);
                selectionManager.getFeatureSelection().selectItems(selectedLayer, selectedFeatures);
            }
            if (!this.editingPlugIn.getToolbox(context.getWorkbenchContext()).isVisible()) {
                this.editingPlugIn.execute(context);
            }
        } else {
            ((Layer)context.getSelectedLayer(0)).setEditable(false);
        }
        if (context.getLayerViewPanel() != null) {
            context.getLayerViewPanel().getRenderingManager().render("SELECTED_FEATURES");
            context.getLayerViewPanel().getRenderingManager().render("SELECTED_SEGMENTS");
        }
        this.resetCursorTool();
        return true;
    }

    private void resetCursorTool() {
        CursorTool tool = JUMPWorkbench.getFrameInstance().getToolBar().getDefaultEditingCursorTool();
        if (tool != null) {
            AbstractButton boton = JUMPWorkbench.getFrameInstance().getToolBar().getButton(tool.getClass());
            boton.doClick();
        }
    }

    public static boolean askCommitIfLayerIsEditable(Layer layer, PlugInContext context) {
        if (layer.isFeatureCollectionModified()) {
            block15: {
                int opcion = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getMessage("workbench.ui.plugin.EditablePlugIn.do-you-want-to-save-the-changes-made-to-the-layer-{0}", new Object[]{layer.getName()}), I18N.getString("workbench.ui.plugin.EditablePlugIn.save-changes"));
                if (opcion == 0) {
                    try {
                        CommitPlugIn commitPlugIn = new CommitPlugIn();
                        if (commitPlugIn.execute(context)) {
                            new TaskMonitorManager().execute(commitPlugIn, context);
                        }
                        if (commitPlugIn.isCommitCanceled()) {
                            return false;
                        }
                        break block15;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("workbench.ui.plugin.EditablePlugIn.an-unexpected-error-have-been-produced-while-saving-the-changes-in-the-layer-{0}", new Object[]{layer.getName()})) + ".\n" + e.getMessage(), I18N.getString("workbench.ui.plugin.EditablePlugIn.error-saving-changes"));
                        return false;
                    }
                }
                if (opcion == 1) {
                    layer.getFeatureCollectionWrapper().getUltimateWrappee().rollBack();
                    context.getLayerViewPanel().getSelectionManager().unselectItems(layer);
                    if (layer.isDataBaseDataSource()) {
                        try {
                            layer.getTransactionalDataSource().clearTransaction();
                        }
                        catch (SQLException e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    }
                    context.getWorkbenchContext().getLayerManager().fireLayerChanged(layer, LayerEventType.COMMITED);
                } else {
                    return false;
                }
            }
            UndoManager undoManager = ((LayerManagerProxy)((Object)context.getWorkbenchContext().getWorkbench().getFrame().getActiveInternalFrame())).getLayerManager().getUndoableEditReceiver().getUndoManager();
            undoManager.discardAllEdits();
            layer.setFeatureCollectionModified(false);
            layer.setEditable(false);
        } else if (layer.isDataBaseDataSource()) {
            try {
                layer.getTransactionalDataSource().clearTransaction();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(cf.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(cf.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(cf.createSelectedLayersMustNotBeWMSLayersCheck());
        check.add(cf.createSelectedLayersMustBeNoInternals());
        check.add(cf.createSelectedLayersMustNotBeAppInternalSystemLayersCheck());
        check.add(cf.createAttributeTabLayersMustNotBeHidden());
        check.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)workbenchContext.createPlugInContext().getSelectedLayer(0);
                if (component instanceof JCheckBoxMenuItem) {
                    ((JCheckBoxMenuItem)component).setSelected(layer.isEditable());
                }
                return null;
            }
        });
        check.add(cf.createSelectedLayersMustNotBeRasterCheck());
        check.add(cf.createSelectedLayerMustBeActiveCheck());
        check.add(cf.createSelectedLayersWithPrimaryKeyCheck());
        check.add(cf.createSelectedLayersMustNotBeReprojectedCheck());
        check.add(cf.createSelectedLayersMustNotVersionableWithTimeCheck());
        check.add(cf.createSelectedLayersMustNotBeInMemoryCheck());
        check.add(cf.createSelectedLayersMustNotBeLabelLayersCheck());
        check.add(cf.createSelectedWFSLayersMustBeTransactionalCheck());
        return check;
    }
}

