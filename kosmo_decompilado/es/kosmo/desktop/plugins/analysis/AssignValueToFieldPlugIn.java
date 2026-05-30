/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.analysis;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.controllers.analysis.AssignValueToFieldOptionsDialogController;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.model.feature.DummyFeatureIterator;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class AssignValueToFieldPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(AssignValueToFieldPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Assign-value-to-field");
    public static final Icon ICON = IconLoader.icon("assignValue.png");
    public AssignValueToFieldOptionsDialogController controller;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        int numFeaturesSelected = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(editableLayer);
        if (this.controller == null) {
            this.controller = new AssignValueToFieldOptionsDialogController();
        }
        this.controller.refresh(editableLayer, numFeaturesSelected);
        this.controller.show();
        return this.controller.wasOkPressed();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        String attrName = editableLayer.getFeatureCollectionWrapper().getFeatureSchema().getAttributeName(this.controller.getAttributeName());
        Object newAttributeValue = this.controller.getNewValue();
        boolean useSelectedOnly = this.controller.useSelectedOnly();
        FeatureCollectionWrapper fcToSave = null;
        Collection<Feature> selectedFeatures = null;
        if (useSelectedOnly) {
            selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
            context.getLayerViewPanel().getSelectionManager().unselectItems(editableLayer, selectedFeatures);
        } else {
            fcToSave = editableLayer.getFeatureCollectionWrapper();
        }
        FeatureIterator itFeatures = null;
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        int cont = 0;
        int totalFeatures = 0;
        try {
            try {
                if (!useSelectedOnly) {
                    itFeatures = fcToSave.iterator();
                    totalFeatures = fcToSave.size();
                } else {
                    itFeatures = selectedFeatures.isEmpty() ? new DummyFeatureIterator() : new CollectionIterator(selectedFeatures);
                    totalFeatures = selectedFeatures.size();
                }
                while (itFeatures.hasNext() && !monitor.isCancelRequested()) {
                    Feature feat = itFeatures.next();
                    Feature cloneFeature = (Feature)feat.clone();
                    cloneFeature.setAttribute(attrName, newAttributeValue);
                    featsToUpdate.add(cloneFeature);
                    if (++cont % 100 != 0) continue;
                    monitor.report(cont, totalFeatures, I18N.getString("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Modified-elements"));
                }
                if (!monitor.isCancelRequested()) {
                    monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updating-elements-for-layer-{0}", new Object[]{editableLayer.getTitle()}));
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                    editableLayer.setFeatureCollectionModified(true);
                    if (useSelectedOnly) {
                        context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(editableLayer, selectedFeatures);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-field-value-{0}-of-multiple-elements-from-layer-{1}", new Object[]{attrName, editableLayer.getTitle()}), I18N.getString("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Error-while-modifying-values"));
                if (itFeatures != null) {
                    itFeatures.close();
                }
                return;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.AssignValueToFieldPlugIn.Updated-{0}-elements-from-layer-{1}", new Object[]{Integer.toString(cont), editableLayer.getTitle()}));
            editableLayer.fireLayerChanged(LayerEventType.COMMITED);
        }
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return AssignValueToFieldPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createEditableLayerMustHaveAtLeastOneValidAttributeType(null));
        return check;
    }
}

