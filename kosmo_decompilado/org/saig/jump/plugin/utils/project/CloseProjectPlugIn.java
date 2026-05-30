/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.project;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.Frame;
import java.util.Collection;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.project.ViewProjectPlugIn;
import org.saig.jump.plugin.utils.window.CloseAllTasksPlugIn;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.project.ProjectEvent;
import org.saig.jump.widgets.util.project.ProjectEventType;

public class CloseProjectPlugIn
extends AbstractPlugIn {
    private String lastProjectFilePath = null;
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.project.CloseProjectPlugIn.name");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        if (context.getWorkbenchContext().getProjectFile() != null) {
            this.lastProjectFilePath = context.getWorkbenchContext().getProjectFile().getAbsolutePath();
        }
        if (context.getWorkbenchContext().getProject() == null) return false;
        Collection<Layer> modifiedItems = null;
        if (context.getWorkbenchContext().getLayerManager() != null) {
            modifiedItems = context.getWorkbenchContext().getLayerManager().getLayersWithModifiedFeatureCollections();
        }
        if (!context.getWorkbenchFrame().confirmClose(I18N.getString("workbench.ui.WorkbenchFrame.close-project"), modifiedItems)) return false;
        int res = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.utils.project.CloseProjectPlugIn.do-you-want-to-save-the-current-project"), I18N.getString("org.saig.jump.plugin.utils.project.CloseProjectPlugIn.save-current-project"));
        if (res == 0) {
            SaveProjectPlugIn saveProjectPlugIn = new SaveProjectPlugIn(new SaveProjectAsPlugIn());
            saveProjectPlugIn.initialize(context);
            boolean exec = saveProjectPlugIn.execute(context);
            if (!exec) return false;
            saveProjectPlugIn.run(new TaskMonitorDialog((Frame)context.getWorkbenchFrame(), context.getErrorHandler()), context);
        } else if (res == 2) {
            return false;
        }
        if (context.getWorkbenchContext().getProjectFile() != null) {
            this.lastProjectFilePath = context.getWorkbenchContext().getProjectFile().getAbsolutePath();
        }
        if (res != 0 && res != 1) return false;
        CloseAllTasksPlugIn closeAllTask = new CloseAllTasksPlugIn(true);
        closeAllTask.execute(context);
        context.getWorkbenchContext().getDataManager().clear();
        context.getWorkbenchContext().getPrintLayoutManager().clear();
        context.getWorkbenchContext().getTaskManager().clear();
        Project oldProject = context.getWorkbenchContext().getProject();
        context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(oldProject, ProjectEventType.CLOSED));
        Project proyecto = new Project();
        proyecto.setName(FirstTaskFramePlugIn.UNTITLED_PROJECT);
        proyecto.setAuthor(System.getProperty("user.name"));
        proyecto.setVersion("3.0 RC1 (20130528)");
        context.getWorkbenchContext().setProject(proyecto);
        context.getWorkbenchContext().getProjectManagerFrame().setTitle(FirstTaskFramePlugIn.UNTITLED_PROJECT);
        ViewProjectPlugIn.setInitialized(false);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return checkFactory.createProjectMustBeOpenedCheck();
    }

    public String getLastProjectFilePath() {
        return this.lastProjectFilePath;
    }
}

