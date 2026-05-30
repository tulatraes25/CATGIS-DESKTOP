/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MacroPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.RemoveSelectedLayersPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CopySelectedLayersPlugIn;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CutSelectedLayersPlugIn
extends MacroPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.CutSelectedLayersPlugIn.name");
    public static final Icon ICON = IconLoader.icon("cutLayer.gif");

    public CutSelectedLayersPlugIn() {
        super(new PlugIn[]{new CopySelectedLayersPlugIn(), new RemoveSelectedLayersPlugIn()});
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
        return CutSelectedLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(CopySelectedLayersPlugIn.createEnableCheck(workbenchContext));
        check.add(RemoveSelectedLayersPlugIn.createEnableCheck(workbenchContext));
        return check;
    }
}

