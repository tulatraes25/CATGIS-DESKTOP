/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.window;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.jump.lang.I18N;

public class CloseAllTasksPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.window.CloseAllTasksPlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private boolean disposeFrames = false;

    public CloseAllTasksPlugIn(boolean shouldDisposeFrames) {
        this.disposeFrames = shouldDisposeFrames;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
        int i = 0;
        while (i < frames.length) {
            JInternalFrame frame = frames[i];
            if (!(frame instanceof ProjectManagerFrame)) {
                if (this.disposeFrames) {
                    frame.dispose();
                } else {
                    frame.setVisible(false);
                }
            }
            ++i;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck();
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return CloseAllTasksPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

