/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.feature.Feature;
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
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn;
import java.util.Collection;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class CopySelectedItemsToEditableLayerPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.copy-selected-features-to-the-editable-layer");
    public static final Icon ICON = IconLoader.icon("copyToEditable.png");
    private String editableLayerName;
    private boolean excludeElementsFromEditableLayer;

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
        return CopySelectedItemsToEditableLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1)).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        this.editableLayerName = editableLayer.getName();
        int numFeaturesFromEditableLayer = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(editableLayer);
        if (numFeaturesFromEditableLayer > 0) {
            int res = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.you-have-selected-{0}-features-from-the-editable-layer-do-you-want-to-copy-them-too", new Object[]{numFeaturesFromEditableLayer}), NAME);
            if (res == 2) {
                return false;
            }
            this.excludeElementsFromEditableLayer = res == 1;
        } else {
            this.excludeElementsFromEditableLayer = false;
        }
        return true;
    }

    @Override
    public void run(final TaskMonitor monitor, final PlugInContext context) throws Exception {
        final Layer editableLayer = JUMPWorkbench.getLayer(this.editableLayerName);
        Collection<Feature> featuresToCopy = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
        if (this.excludeElementsFromEditableLayer) {
            featuresToCopy.removeAll(context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer));
        }
        final Collection<Feature> selectedFeatures = PasteItemsPlugIn.conform(featuresToCopy, editableLayer.getFeatureSchema());
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.{0}-elements", new Object[]{selectedFeatures.size()})){

            @Override
            public void execute() throws Exception {
                monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.copying-{0}-elements-to-the-layer-{1}", new Object[]{selectedFeatures.size(), editableLayer.getName()}));
                editableLayer.getFeatureCollectionWrapper().addAll(selectedFeatures);
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.{0}-elements-have-been-copied-to-the-layer-{1}", new Object[]{selectedFeatures.size(), editableLayer.getName()}));
            }

            @Override
            public void unexecute() throws Exception {
                monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn.removing-{0}-elements-from-the-layer-{1}", new Object[]{selectedFeatures.size(), editableLayer.getName()}));
                editableLayer.getFeatureCollectionWrapper().removeAll(selectedFeatures);
            }
        }, context);
    }
}

