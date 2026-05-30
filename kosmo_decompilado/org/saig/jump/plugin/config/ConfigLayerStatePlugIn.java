/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.config;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigLayerStateDialog;

public class ConfigLayerStatePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.config.ConfigLayerStatePlugIn.Enable-disable-layers");
    public static final Icon ICON = IconLoader.icon("Hammer.gif");
    private static ConfigLayerStateDialog dialog;

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        dialog = new ConfigLayerStateDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext());
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
        MultiEnableCheck multiCheck = new MultiEnableCheck();
        multiCheck.add(checkFactory.createTaskWindowMustBeActiveCheck());
        multiCheck.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        return multiCheck;
    }
}

