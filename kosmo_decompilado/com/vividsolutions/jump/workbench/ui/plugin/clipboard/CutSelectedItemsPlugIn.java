/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MacroPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CopySelectedItemsPlugIn;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CutSelectedItemsPlugIn
extends MacroPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.CutSelectedItemsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("cut.gif");

    public CutSelectedItemsPlugIn() {
        super(new PlugIn[]{new CopySelectedItemsPlugIn(), new DeleteSelectedItemsPlugIn()});
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return DeleteSelectedItemsPlugIn.createEnableCheck(workbenchContext).add(checkFactory.createAttributeTabLayersMustNotBeHidden());
    }

    @Override
    public EnableCheck getCheck() {
        return CutSelectedItemsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

