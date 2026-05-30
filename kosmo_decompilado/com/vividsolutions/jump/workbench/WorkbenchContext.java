/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import java.io.File;
import java.util.List;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.task.TaskManager;
import org.saig.jump.widgets.util.project.IProjectListener;
import org.saig.jump.widgets.util.project.ProjectEvent;

public abstract class WorkbenchContext
implements LayerViewPanelProxy,
LayerNamePanelProxy,
LayerManagerProxy {
    private String title;

    public abstract DriverManager getDriverManager();

    public abstract JUMPWorkbench getWorkbench();

    public abstract ErrorHandler getErrorHandler();

    @Override
    public abstract LayerNamePanel getLayerNamePanel();

    @Override
    public abstract LayerViewPanel getLayerViewPanel();

    public abstract Blackboard getBlackboard();

    @Override
    public abstract LayerManager getLayerManager();

    public abstract DataManager getDataManager();

    public abstract TaskManager getTaskManager();

    public abstract PrintLayoutManager getPrintLayoutManager();

    public abstract Task getTask();

    public abstract ProjectManagerFrame getProjectManagerFrame();

    public abstract File getProjectFile();

    public abstract void setProjectFile(File var1);

    public abstract void setProject(Project var1);

    public abstract Project getProject();

    public PlugInContext createPlugInContext() {
        return new PlugInContext(this, this.getTask(), this, this.getLayerNamePanel(), this.getLayerViewPanel());
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public abstract LayerViewPanel getLastClickedLayerViewPanel();

    public abstract void setLastClickedLayerViewPanel(LayerViewPanel var1);

    public abstract List getAllLayers();

    public abstract List<Category> getAllCategories();

    public abstract void addProjectListener(IProjectListener var1);

    public abstract void removeProjectListener(IProjectListener var1);

    public abstract void fireProjectChanged(ProjectEvent var1);
}

