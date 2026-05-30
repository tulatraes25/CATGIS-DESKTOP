/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;
import org.saig.core.model.task.TaskManager;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;

public class NewTaskPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.NewTaskPlugIn.name");
    private boolean showEPSGDialog;

    public NewTaskPlugIn() {
        this.showEPSGDialog = true;
    }

    public NewTaskPlugIn(boolean showEPSGDialog) {
        this.showEPSGDialog = showEPSGDialog;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        if (context.getWorkbenchContext().getProject() == null) {
            Project proyecto = new Project();
            proyecto.setName(FirstTaskFramePlugIn.UNTITLED_PROJECT);
            proyecto.setAuthor(System.getProperty("user.name"));
            proyecto.setVersion("3.0 RC1 (20130528)");
            ((JUMPWorkbenchContext)context.getWorkbenchContext()).setProject(proyecto);
        }
        TaskManager taskManager = context.getWorkbenchContext().getTaskManager();
        TaskFrame taskFrame = context.getWorkbenchFrame().addTaskFrame();
        Task task = taskFrame.getTask();
        if (this.showEPSGDialog) {
            EPSGSelectionDialog csDialog = new EPSGSelectionDialog(JUMPWorkbench.getFrameInstance(), true, true);
            if (csDialog.isOk()) {
                task.setProjection(csDialog.getProjection());
            } else {
                task.setProjection(null);
            }
        }
        taskFrame.getLayerViewPanel().setMapLengthUnit(UnitsManager.getLengthUnitFromName(task.getMapLengthUnit()));
        taskFrame.getLayerViewPanel().setUserLengthUnit(UnitsManager.getLengthUnitFromName(task.getUserLengthUnit()));
        taskFrame.getLayerViewPanel().setUserAreaUnit(UnitsManager.getAreaUnitFromName(task.getUserAreaUnit()));
        taskFrame.updateTitle();
        taskManager.addTask(taskFrame);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

