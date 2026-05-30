/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
import es.kosmo.desktop.images.DesktopIconLoader;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import org.saig.jump.lang.I18N;

public class OptionsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.OptionsPlugIn.name");
    public static final Icon ICON = DesktopIconLoader.icon("config.png");
    public static final String BUFFER_KEY = "SNAP_BUFFER";

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.dialog(context).addTab(new EditOptionsPanel());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        GUIUtil.centreOnWindow(this.dialog(context));
        this.dialog(context).setVisible(true);
        if (this.dialog(context).wasOKPressed()) {
            JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
            int i = 0;
            while (i < frames.length) {
                if (frames[i] instanceof LayerViewPanelProxy) {
                    ((LayerViewPanelProxy)((Object)frames[i])).getLayerViewPanel().getRenderingManager().render("GRID", true);
                }
                ++i;
            }
        }
        return this.dialog(context).wasOKPressed();
    }

    private OptionsDialog dialog(PlugInContext context) {
        return OptionsDialog.instance(context.getWorkbenchContext().getWorkbench());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}

