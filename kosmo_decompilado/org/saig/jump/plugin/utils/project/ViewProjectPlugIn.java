/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.project;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.jump.lang.I18N;

public class ViewProjectPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.project.ViewProyectPlugIn.name");
    public static final Icon ICON = IconLoader.icon("ver_cartografia_no_edit.gif");
    private static boolean initialized = false;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        ProjectManagerFrame frame = context.getWorkbenchContext().getProjectManagerFrame();
        frame.setVisible(true);
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    public static void setInitialized(boolean initialized) {
        ViewProjectPlugIn.initialized = initialized;
    }
}

