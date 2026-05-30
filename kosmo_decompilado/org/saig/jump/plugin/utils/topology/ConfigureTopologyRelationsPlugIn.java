/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.topology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.topology.TopologyRelationManagerDialog;

public class ConfigureTopologyRelationsPlugIn
extends AbstractPlugIn {
    public static String NAME = I18N.getString(ConfigureTopologyRelationsPlugIn.class, "configure-topologic-relations");
    public static Icon ICON = IconLoader.icon("topologia.gif");

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        Layerable[] layers = context.getLayerNamePanel().getSelectedLayers();
        Layer layer = (Layer)layers[0];
        new TopologyRelationManagerDialog(JUMPWorkbench.getFrameInstance(), true, layer.getName());
        return true;
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
        return ConfigureTopologyRelationsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(cf.createTaskWindowMustBeActiveCheck());
        check.add(cf.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(cf.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(cf.createSelectedLayersMustNotBeRasterCheck());
        check.add(cf.createSelectedLayerMustBeActiveCheck());
        check.add(cf.createSelectedLayersMustBeNoInternals());
        check.add(cf.createSelectedLayersMustNotBeAppInternalSystemLayersCheck());
        return check;
    }
}

