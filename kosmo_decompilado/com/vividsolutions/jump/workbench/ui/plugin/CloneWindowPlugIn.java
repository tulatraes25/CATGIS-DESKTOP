/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.CloneableInternalFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import org.saig.jump.lang.I18N;

public class CloneWindowPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.CloneWindowPlugIn.name");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        JInternalFrame frame = ((CloneableInternalFrame)((Object)context.getActiveInternalFrame())).internalFrameClone();
        context.getWorkbenchFrame().addInternalFrame(frame);
        if (frame instanceof LayerViewPanelProxy) {
            ((LayerViewPanelProxy)((Object)frame)).getLayerViewPanel().repaint();
        }
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof CloneableInternalFrame) ? I18N.getString("workbench.ui.plugin.CloneWindowPlugIn.not-available-for-the-current-window") : null;
            }
        };
    }
}

