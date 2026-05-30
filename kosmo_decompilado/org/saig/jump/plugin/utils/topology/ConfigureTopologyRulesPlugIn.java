/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.topology;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.TopologyRelationsRepository;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.topology.ConfigureTopologyRulesDialog;

public class ConfigureTopologyRulesPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = String.valueOf(I18N.getString(ConfigureTopologyRulesPlugIn.class, "configure-topological-rules")) + "...";
    public static final Icon ICON = GUIUtil.resize(IconLoader.icon("topologia.gif"), 20);
    private ConfigureTopologyRulesDialog dialog;
    private boolean selectedLayerOnly = false;

    public ConfigureTopologyRulesPlugIn() {
        this(false);
    }

    public ConfigureTopologyRulesPlugIn(boolean selectedLayerOnly) {
        this.selectedLayerOnly = selectedLayerOnly;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        boolean onlyOneLayerMode;
        List<ITopologyRelation> relations = ConfigureTopologyRulesPlugIn.copyRelations(ConfigureTopologyRulesPlugIn.recoverCurrentRelations(context.getLayerManager()));
        String selectedLayerName = null;
        boolean bl = onlyOneLayerMode = context.getLayerManager().getNoRasterLayers().size() == 1;
        if (this.selectedLayerOnly) {
            selectedLayerName = context.getSelectedLayer(0).getName();
        }
        if (this.dialog == null) {
            this.dialog = new ConfigureTopologyRulesDialog(context.getWorkbenchFrame(), true, relations, selectedLayerName, onlyOneLayerMode);
        } else {
            this.dialog.refresh(selectedLayerName, onlyOneLayerMode, context.getLayerManager(), relations);
        }
        GUIUtil.centreOnScreen(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    public static List<ITopologyRelation> copyRelations(List<ITopologyRelation> relationsToCopy) throws Exception {
        ArrayList<ITopologyRelation> copyOfRelations = new ArrayList<ITopologyRelation>(relationsToCopy.size());
        for (ITopologyRelation relation : relationsToCopy) {
            ITopologyRelation newRelation = TopologyRelationsRepository.getTopologyRelation(relation.getId());
            newRelation.setSourceLayerName(relation.getSourceLayerName());
            newRelation.setAlphanumericFilter(relation.getAlphanumericFilter());
            newRelation.setEntrySourceFilter(relation.getEntrySourceFilter());
            newRelation.setEnabled(relation.isEnabled());
            if (relation instanceof ITopologyBinaryRelation && newRelation instanceof ITopologyBinaryRelation) {
                ((ITopologyBinaryRelation)newRelation).setTargetLayerName(((ITopologyBinaryRelation)relation).getTargetLayerName());
                ((ITopologyBinaryRelation)newRelation).setEntryTargetFilter(((ITopologyBinaryRelation)relation).getEntryTargetFilter());
            }
            copyOfRelations.add(newRelation);
        }
        return copyOfRelations;
    }

    public static List<ITopologyRelation> recoverCurrentRelations(LayerManager layerManager) {
        ArrayList<ITopologyRelation> relations = new ArrayList<ITopologyRelation>();
        if (layerManager != null) {
            List<Layer> layers = layerManager.getNoRasterLayers();
            for (Layer currentLayer : layers) {
                relations.addAll(currentLayer.getTopologyRelations());
            }
        }
        return relations;
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
        EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench.getFrameInstance().getContext());
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(checkFactory.createAtLeastNLayersMustNotBeRasterCheck(1));
        if (this.selectedLayerOnly) {
            check.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
            check.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        }
        return check;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(I18N.getString(this.getClass(), "updating-configured-topological-rules"));
        List<ITopologyRelation> topologyRules = this.dialog.getTopologyRules();
        HashMap topologyToLayerMap = new HashMap();
        for (ITopologyRelation currentTopologyRelation : topologyRules) {
            String sourceLayerName = currentTopologyRelation.getSourceLayerName();
            if (!topologyToLayerMap.containsKey(sourceLayerName)) {
                topologyToLayerMap.put(sourceLayerName, new ArrayList());
            }
            List currentSourceLayerRelations = (List)topologyToLayerMap.get(sourceLayerName);
            currentSourceLayerRelations.add(currentTopologyRelation);
        }
        List<Layer> layers = context.getLayerManager().getNoRasterLayers();
        for (Layer currentLayer : layers) {
            List relations = (List)topologyToLayerMap.get(currentLayer.getName());
            if (relations == null) {
                currentLayer.removeAllTopologiesRelations();
                continue;
            }
            currentLayer.setTopologyRelations(relations);
        }
        context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "the-update-of-topological-rules-has-finished-successfully"));
    }
}

