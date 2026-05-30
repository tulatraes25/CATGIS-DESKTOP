/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JInternalFrame;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.task.TaskManager;
import org.saig.jump.widgets.util.project.IProjectListener;
import org.saig.jump.widgets.util.project.ProjectEvent;

public class JUMPWorkbenchContext
extends WorkbenchContext {
    private JUMPWorkbench workbench;
    private Project proyecto;
    private LayerViewPanel viewPanel;
    private boolean zEditionActive = false;
    private Set<IProjectListener> projectListeners = new HashSet<IProjectListener>();

    public JUMPWorkbenchContext(JUMPWorkbench workbench) {
        this.workbench = workbench;
    }

    @Override
    public JUMPWorkbench getWorkbench() {
        return this.workbench;
    }

    @Override
    public DriverManager getDriverManager() {
        return this.workbench.getDriverManager();
    }

    public boolean isZEditionActive() {
        return this.zEditionActive;
    }

    public void setZEditionActive(boolean activatedZEdition) {
        this.zEditionActive = activatedZEdition;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.workbench.getFrame();
    }

    @Override
    public Task getTask() {
        if (!(this.activeInternalFrame() instanceof TaskFrame)) {
            return null;
        }
        return ((TaskFrame)this.activeInternalFrame()).getTask();
    }

    @Override
    public LayerNamePanel getLayerNamePanel() {
        if (!(this.activeInternalFrame() instanceof LayerNamePanelProxy)) {
            return null;
        }
        return ((LayerNamePanelProxy)((Object)this.activeInternalFrame())).getLayerNamePanel();
    }

    @Override
    public LayerManager getLayerManager() {
        if (!(this.activeInternalFrame() instanceof LayerManagerProxy)) {
            return null;
        }
        return ((LayerManagerProxy)((Object)this.activeInternalFrame())).getLayerManager();
    }

    @Override
    public LayerViewPanel getLayerViewPanel() {
        if (!(this.activeInternalFrame() instanceof LayerViewPanelProxy)) {
            return null;
        }
        return ((LayerViewPanelProxy)((Object)this.activeInternalFrame())).getLayerViewPanel();
    }

    private JInternalFrame activeInternalFrame() {
        return this.workbench.getFrame().getActiveInternalFrame();
    }

    @Override
    public Project getProject() {
        return this.proyecto;
    }

    @Override
    public void setProject(Project newProject) {
        this.proyecto = newProject;
        if (this.proyecto != null) {
            this.getWorkbench().getFrame().setCurrentProjectTitle(this.proyecto.getName());
        } else {
            this.getWorkbench().getFrame().setCurrentProjectTitle(FirstTaskFramePlugIn.UNTITLED_PROJECT);
        }
        ProjectManagerFrame pmf = JUMPWorkbench.getFrameInstance().getContext().getProjectManagerFrame();
        if (pmf != null) {
            pmf.setProject(this.proyecto);
        }
    }

    @Override
    public DataManager getDataManager() {
        return this.workbench.getDataManager();
    }

    @Override
    public Blackboard getBlackboard() {
        return JUMPWorkbench.getBlackboard();
    }

    @Override
    public TaskManager getTaskManager() {
        return this.workbench.getTaskManager();
    }

    @Override
    public PrintLayoutManager getPrintLayoutManager() {
        return this.workbench.getPrintLayoutManager();
    }

    @Override
    public ProjectManagerFrame getProjectManagerFrame() {
        return this.workbench.getProjectManagerFrame();
    }

    @Override
    public LayerViewPanel getLastClickedLayerViewPanel() {
        return this.viewPanel;
    }

    @Override
    public void setLastClickedLayerViewPanel(LayerViewPanel layerViewPanel) {
        this.viewPanel = layerViewPanel;
    }

    @Override
    public List getAllLayers() {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        TaskManager taskManager = this.getTaskManager();
        Iterator<TaskFrame> iter = taskManager.getTasks().iterator();
        while (iter.hasNext()) {
            Task element = iter.next().getTask();
            layers.addAll(element.getLayerManager().getLayers());
        }
        return layers;
    }

    @Override
    public List<Category> getAllCategories() {
        ArrayList<Category> categories = new ArrayList<Category>();
        TaskManager taskManager = this.getTaskManager();
        Iterator<TaskFrame> iter = taskManager.getTasks().iterator();
        while (iter.hasNext()) {
            Task element = iter.next().getTask();
            categories.addAll(element.getLayerManager().getCategories());
        }
        return categories;
    }

    @Override
    public File getProjectFile() {
        if (this.proyecto != null) {
            return this.proyecto.getProjectFile();
        }
        return null;
    }

    @Override
    public void setProjectFile(File projectFile) {
        if (this.proyecto != null && projectFile != null) {
            this.proyecto.setProjectFile(projectFile);
        }
    }

    @Override
    public void addProjectListener(IProjectListener listener) {
        this.projectListeners.add(listener);
    }

    @Override
    public void fireProjectChanged(ProjectEvent event) {
        for (IProjectListener listener : this.projectListeners) {
            listener.projectChanged(event);
        }
    }

    @Override
    public void removeProjectListener(IProjectListener listener) {
        this.projectListeners.remove(listener);
    }
}

