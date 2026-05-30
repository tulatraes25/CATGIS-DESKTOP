/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;

public class OutputWindowPlugIn
extends AbstractPlugIn {
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getWorkbenchFrame().getOutputFrame().surface();
        return true;
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("Frame.gif");
    }
}

