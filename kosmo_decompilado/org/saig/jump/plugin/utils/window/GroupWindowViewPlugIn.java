/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.window;

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

public class GroupWindowViewPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.window.GroupWindowViewPlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getWorkbenchFrame().setViewLinked(!context.getWorkbenchFrame().isViewLinked());
        return false;
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
        return GroupWindowViewPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createNTasksMustBeExistsCheck());
    }
}

