/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractSaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.project.OpenRecentProjectsPlugIn;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.util.project.ProjectEvent;
import org.saig.jump.widgets.util.project.ProjectEventType;

public class SaveProjectPlugIn
extends AbstractSaveProjectPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.SaveProjectPlugIn.name");
    public static final String LAYERS_RELATIVE_PATH = String.valueOf(SaveProjectPlugIn.class.getName()) + " - RELATIVE_PATH";
    private SaveProjectAsPlugIn saveProjectAsPlugIn;
    private File projectFile;

    public SaveProjectPlugIn(SaveProjectAsPlugIn saveProjectAsPlugIn) {
        this.saveProjectAsPlugIn = saveProjectAsPlugIn;
    }

    public File getProjectFile() {
        return this.projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.projectFile = context.getWorkbenchContext().getProjectFile();
        if (this.projectFile == null) {
            return this.saveProjectAsPlugIn.execute(context);
        }
        context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(context.getWorkbenchContext().getProject(), ProjectEventType.BEFORE_SAVE));
        return SaveProjectAsPlugIn.checkTemporalLayers();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(String.valueOf(I18N.getString("workbench.ui.plugin.SaveProjectPlugIn.saving-project")) + "...");
        SaveProjectAsPlugIn.saveTaskFrameStatus();
        if (this.projectFile == null) {
            this.projectFile = this.saveProjectAsPlugIn.getFile();
        }
        List<TaskFrame> taskFrames = context.getWorkbenchContext().getTaskManager().getTasks();
        List<ViewTableFrame> tableFrames = context.getWorkbenchContext().getDataManager().getTables();
        List<PrintLayoutFrame> layoutFrames = context.getWorkbenchContext().getPrintLayoutManager().getPrintLayouts();
        ArrayList<Task> tasksToSave = new ArrayList<Task>();
        for (TaskFrame taskFrame : taskFrames) {
            Envelope currentEnvelope = taskFrame.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
            Task currentTask = taskFrame.getTask();
            currentTask.setCurrentView(currentEnvelope);
            currentTask.setFrameLocationX(taskFrame.getLocation().x);
            currentTask.setFrameLocationY(taskFrame.getLocation().y);
            currentTask.setFrameWidth(taskFrame.getSize().width);
            currentTask.setFrameHeight(taskFrame.getSize().height);
            currentTask.setVisible(taskFrame.isVisible());
            currentTask.setMapFactor(taskFrame.getLayerViewPanel().getFactor());
            currentTask.setMapLengthUnit(taskFrame.getLayerViewPanel().getMapLengthUnit().toString());
            currentTask.setUserLengthUnit(taskFrame.getLayerViewPanel().getUserLengthUnit().toString());
            currentTask.setUserAreaUnit(taskFrame.getLayerViewPanel().getUserAreaUnit().toString());
            currentTask.setGraphicScale(taskFrame.getLayerViewPanel().getGraphicScale());
            currentTask.setLegends(taskFrame.getLayerViewPanel().getLegends());
            currentTask.setAngle(taskFrame.getLayerViewPanel().getAngle());
            currentTask.setNorth(taskFrame.getLayerViewPanel().getNorth());
            tasksToSave.add(currentTask);
        }
        ArrayList<Table> tablesToSave = new ArrayList<Table>();
        for (ViewTableFrame dataFrame : tableFrames) {
            Table tableRecord = dataFrame.getTable();
            if (tableRecord.isInternal()) continue;
            tableRecord.setFrameLocationX(dataFrame.getLocation().x);
            tableRecord.setFrameLocationY(dataFrame.getLocation().y);
            tableRecord.setFrameWidth(dataFrame.getSize().width);
            tableRecord.setFrameHeight(dataFrame.getSize().height);
            tableRecord.setVisible(dataFrame.isVisible());
            tablesToSave.add(tableRecord);
        }
        ArrayList<Page> layoutsToSave = new ArrayList<Page>();
        for (PrintLayoutFrame layoutFrame : layoutFrames) {
            Page currentPage = null;
            currentPage = layoutFrame.getPreviewPanel() == null ? new Page() : layoutFrame.getPage();
            currentPage.setPageFormat(layoutFrame.getPageFormat());
            currentPage.setActiveZoom(layoutFrame.getActiveZoom());
            currentPage.setName(layoutFrame.getName());
            currentPage.setTaskName(layoutFrame.getTaskFrame().getName());
            layoutsToSave.add(currentPage);
        }
        this.save(tasksToSave, tablesToSave, layoutsToSave, this.projectFile, context.getWorkbenchFrame());
        context.getWorkbenchContext().setProjectFile(this.projectFile);
        context.getWorkbenchFrame().warnUser(I18N.getString("workbench.ui.plugin.SaveProjectPlugIn.project-successfully-saved"));
        PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put("LAST_PROYECT", this.projectFile.getAbsolutePath());
        LinkedList recentProjectsLinkedList = null;
        Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(OpenRecentProjectsPlugIn.RECENT_PROJECTS_KEY);
        recentProjectsLinkedList = value == null || !(value instanceof LinkedList) ? new LinkedList() : (LinkedList)value;
        String path = this.projectFile.getAbsolutePath();
        recentProjectsLinkedList.remove(path);
        recentProjectsLinkedList.addFirst(path);
        while (recentProjectsLinkedList.size() > 5) {
            recentProjectsLinkedList.removeLast();
        }
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(OpenRecentProjectsPlugIn.RECENT_PROJECTS_KEY, recentProjectsLinkedList);
        PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(OpenProjectPlugIn.FILE_CHOOSER_DIRECTORY_KEY, this.projectFile.getParent());
        context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(context.getWorkbenchContext().getProject(), ProjectEventType.SAVED));
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return checkFactory.createProjectMustBeOpenedCheck();
    }

    public static void saveProject(String file) {
        SaveProjectPlugIn spp = new SaveProjectPlugIn(new SaveProjectAsPlugIn());
        spp.setProjectFile(new File(file));
        try {
            PlugInContext context = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
            context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(context.getWorkbenchContext().getProject(), ProjectEventType.BEFORE_SAVE));
            SaveProjectAsPlugIn.checkTemporalLayers();
            spp.run(new DummyTaskMonitor(), context);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

