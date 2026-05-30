/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.util.Date;
import org.saig.jump.lang.I18N;

public class GenerateLogPlugIn
extends AbstractPlugIn {
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getOutputFrame().createNewDocument();
        context.getOutputFrame().addHeader(1, I18N.getString("workbench.ui.plugin.GenerateLogPlugIn.log"));
        context.getOutputFrame().addHeader(2, String.valueOf(I18N.getString("workbench.ui.GenerateLogPlugIn.generated")) + new Date());
        context.getOutputFrame().addText(context.getWorkbenchFrame().getLog());
        context.getOutputFrame().surface();
        return true;
    }
}

