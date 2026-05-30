/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.info;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.info.ViewInfoDialog;

public class ViewInfoPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.info.ViewInfoPlugIn.View-properties");
    public static final Icon ICON = IconLoader.icon("Info.gif");

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        TaskFrame[] frames = context.getWorkbenchFrame().getTaskFrames();
        TaskFrame frameToChange = null;
        int i = 0;
        while (i < frames.length) {
            TaskFrame frame = frames[i];
            if (frame.getRealTitle().equals(context.getTask().getTitle(LocaleManager.getActiveLocale()))) {
                frameToChange = frame;
            }
            ++i;
        }
        if (frameToChange == null) {
            return false;
        }
        ViewInfoDialog dialog = new ViewInfoDialog(JUMPWorkbench.getFrameInstance(), true, frameToChange, context.getLayerViewPanel());
        GUIUtil.centreOnScreen(dialog);
        dialog.setVisible(true);
        dialog.dispose();
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    @Override
    public EnableCheck getCheck() {
        return ViewInfoPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

