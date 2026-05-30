/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.relations;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.relations.RelationsConfigDialog;

public class ConfigRelationPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConfigRelationPlugIn.configure-relations")) + "...";
    public static final Icon ICON = IconLoader.icon("FlowGraph.gif");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        RelationsConfigDialog dialog = new RelationsConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, context.getWorkbenchContext(), context.getWorkbenchContext().getLayerManager(), (Layer)context.getLayerNamePanel().getSelectedLayers()[0]);
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

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustBeNoInternals());
        solucion.add(checkFactory.createSelectedLayersMustNotBeAppInternalSystemLayersCheck());
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return ConfigRelationPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

