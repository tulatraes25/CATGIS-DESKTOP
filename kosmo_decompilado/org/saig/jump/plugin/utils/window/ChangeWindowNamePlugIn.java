/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.plugin.utils.window;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import org.apache.commons.lang.StringUtils;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ChangeWindowNamePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.name");
    public static final Icon ICON = IconLoader.icon("Attribute.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        JInternalFrame frame = context.getWorkbenchFrame().getActiveInternalFrame();
        String frameTitle = "";
        frameTitle = frame instanceof TaskFrame ? ((TaskFrame)frame).getRealTitle() : frame.getTitle();
        Object newName = null;
        boolean repeated = false;
        do {
            if ((newName = DialogFactory.showInputDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.insert-the-new-window-name"), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.Change-name"), frameTitle)) == null) continue;
            String selectedName = (String)newName;
            if (frame instanceof TaskFrame) {
                if (StringUtils.isEmpty((String)selectedName)) {
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.the-name-can-not-be-blank"), I18N.getString("org.saig.core.model.task.widgets.TaskManagerPanel.error"));
                    continue;
                }
                boolean bl = repeated = context.getWorkbenchContext().getTaskManager().getTask(selectedName) != null;
                if (repeated) {
                    DialogFactory.showWarningDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.The-selected-task-name-{0}-already-exists", new Object[]{selectedName}), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.Repeated-name"));
                    continue;
                }
                ((TaskFrame)frame).getTask().setTitle(selectedName, LocaleManager.getActiveLocale());
                continue;
            }
            frame.setTitle((String)newName);
        } while (newName != null && repeated);
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

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
    }

    @Override
    public EnableCheck getCheck() {
        return ChangeWindowNamePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

