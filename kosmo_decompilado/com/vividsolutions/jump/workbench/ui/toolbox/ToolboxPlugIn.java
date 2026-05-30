/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.toolbox;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

public abstract class ToolboxPlugIn
extends AbstractPlugIn {
    protected ToolboxDialog toolbox;

    public ToolboxDialog getToolbox(WorkbenchContext context) {
        return this.getToolbox(context, true);
    }

    public ToolboxDialog getToolbox(WorkbenchContext context, boolean initilize) {
        if (this.toolbox == null) {
            this.toolbox = new ToolboxDialog(context);
            this.toolbox.setTitle(this.getName());
            if (initilize) {
                this.initializeToolbox(this.toolbox);
            }
            this.toolbox.finishAddingComponents();
        }
        return this.toolbox;
    }

    protected abstract void initializeToolbox(ToolboxDialog var1);

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.getToolbox(context.getWorkbenchContext()).setVisible(!this.getToolbox(context.getWorkbenchContext()).isVisible());
        this.getToolbox(context.getWorkbenchContext()).updateEnabledState();
        return true;
    }

    public void createMainMenuItem(String[] menuPath, Icon icon, final WorkbenchContext context, EnableCheck check) throws Exception {
        new FeatureInstaller(context).addMainMenuItem(this, menuPath, String.valueOf(this.getName()) + "...", true, icon, ((MultiEnableCheck)check).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(ToolboxPlugIn.this.getToolbox(context).isVisible());
                return null;
            }
        }));
    }

    public void createMainMenuItem(String[] menuPath, Icon icon, final WorkbenchContext context) throws Exception {
        new FeatureInstaller(context).addMainMenuItem(this, menuPath, String.valueOf(this.getName()) + "...", true, icon, new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(ToolboxPlugIn.this.getToolbox(context).isVisible());
                return null;
            }
        });
    }
}

