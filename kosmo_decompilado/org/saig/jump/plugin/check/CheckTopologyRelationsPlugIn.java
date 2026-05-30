/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.check;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;
import org.saig.jump.widgets.util.DialogFactory;

public class CheckTopologyRelationsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Check-topology-Rules");
    public static final Icon ICON = IconLoader.icon("checkTopologyRules.png");
    public static final Logger LOGGER = Logger.getLogger(CheckTopologyRelationsPlugIn.class);

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
        this.reportNothingToUndoYet(context);
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return CheckTopologyRelationsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createAtLeastNEnabledTopologyRulesMustExistCheck(1));
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        HashMap errors = new HashMap();
        List<Layer> layers = context.getLayerManager().getNoRasterLayers();
        for (Layer currentLayer : layers) {
            List<ITopologyRelation> topologyRelations = currentLayer.getTopologyRelations();
            if (CollectionUtils.isEmpty(topologyRelations)) continue;
            ArrayList<Feature> errorFeats = new ArrayList<Feature>();
            int contador = 1;
            int toProcess = topologyRelations.size();
            monitor.report(I18N.getMessage(this.getClass(), "checking-layer-{0}", new Object[]{currentLayer.getName()}));
            for (ITopologyRelation topologyRelation : topologyRelations) {
                monitor.report(contador++, toProcess, I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.checking-topology-relation-{0}", new Object[]{topologyRelation.getName()}));
                if (!topologyRelation.isEnabled() || topologyRelation.checkAll()) continue;
                errorFeats.addAll(topologyRelation.obtainErrors());
            }
            if (errorFeats.isEmpty()) continue;
            errors.put(currentLayer, errorFeats);
        }
        if (errors.size() > 0) {
            int option = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.topology-errors-have-been-found-do-you-want-to-generate-an-error-layer"), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.topology-errors"));
            if (option == 0) {
                for (Layer currentLayer : errors.keySet()) {
                    CheckTopologyRelationsPlugIn.loadIncidentCategory(context, (List)errors.get(currentLayer), currentLayer.getName(), currentLayer.getGeometryType());
                }
            }
        } else {
            DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.topology-errors-have-not-been-found"), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.No-topology-errors-found"));
        }
    }

    public static void loadIncidentCategory(PlugInContext context, List<Feature> errores, String layerName, int geomType) throws Exception {
        String categoryName = CheckTopologyRelationsPlugIn.getNameForIncidentCategory();
        FeatureSchema schema = (FeatureSchema)AbstractTopologyRelation.errorSchema.clone();
        schema.setGeometryType(geomType);
        FeatureDataset fc = new FeatureDataset(schema);
        if (errores != null) {
            fc.addAllWithNewKey(errores);
        }
        Layer layer = context.getLayerManager().addLayer(categoryName, I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn.topology-errors-{0}", new Object[]{layerName}), fc);
        layer.setProjection(context.getTask().getProjection());
    }

    public static String getNameForIncidentCategory() {
        return I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Incidences-{0}", new Object[]{DateFormatManager.getDateTimeFormat().format(new Date())});
    }

    protected void showCheckSummary(Map<String, List<SummaryMessage>> messageMap, String checksFile) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Checks-summary-for-file-{0}", new Object[]{checksFile}), messageMap);
        dialog.setVisible(true);
    }
}

