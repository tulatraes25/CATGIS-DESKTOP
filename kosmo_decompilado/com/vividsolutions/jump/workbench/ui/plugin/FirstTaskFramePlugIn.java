/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.project.ProjectManagerDialog;

public class FirstTaskFramePlugIn
extends AbstractPlugIn {
    public static final String UNTITLED_PROJECT = I18N.getString("workbench.ui.plugin.FirstTaskFramePlugIn.untitled");
    private ComponentListener componentListener;

    @Override
    public void initialize(final PlugInContext context) throws Exception {
        this.componentListener = new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                ProjectManagerDialog dialog = new ProjectManagerDialog(context.getWorkbenchFrame(), true, context);
                if (JUMPWorkbench.PROJECT_DIRECT_LOAD_PATH != null) {
                    File directLoadProjectFile = new File(JUMPWorkbench.PROJECT_DIRECT_LOAD_PATH);
                    if (directLoadProjectFile.canRead()) {
                        PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put("LAST_PROYECT", directLoadProjectFile.getAbsolutePath());
                        ProjectManagerDialog.initProjectFrame();
                        dialog.loadLastOpenProject(directLoadProjectFile.getName());
                    } else {
                        dialog.setVisible(true);
                    }
                } else {
                    dialog.setVisible(true);
                }
                context.getWorkbenchFrame().removeComponentListener(FirstTaskFramePlugIn.this.componentListener);
            }
        };
        context.getWorkbenchFrame().addComponentListener(this.componentListener);
    }
}

