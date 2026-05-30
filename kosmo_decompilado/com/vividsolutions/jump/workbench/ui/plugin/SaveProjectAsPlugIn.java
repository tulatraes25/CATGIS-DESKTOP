/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractSaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.project.OpenRecentProjectsPlugIn;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.util.project.ProjectEvent;
import org.saig.jump.widgets.util.project.ProjectEventType;
import org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog;

public class SaveProjectAsPlugIn
extends AbstractSaveProjectPlugIn
implements ThreadedPlugIn {
    public static final String SAIG_FILE_EXTENSION = "spr";
    public static final FileFilter SAIG_PROJECT_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString("workbench.ui.plugin.SaveProjectAsPlugIn.project-file"), new String[]{"spr"});
    private File projectFile;
    private File oldProjectFile;
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
    public static final String NAME = I18N.getString("workbench.ui.plugin.SaveProjectAsPlugIn.name");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SAIG_PROJECT_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SAIG_PROJECT_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (!SaveProjectAsPlugIn.checkTemporalLayers()) {
            return false;
        }
        this.oldProjectFile = JUMPWorkbench.getFrameInstance().getContext().getProjectFile();
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.PROJECTS_PATH_KEY);
        if (context.getWorkbenchContext().getProjectFile() != null) {
            this.fileChooser.setSelectedFile(context.getWorkbenchContext().getProjectFile());
        } else if (defaultPath != null && !defaultPath.equals("")) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.canRead()) {
                this.fileChooser.setCurrentDirectory(defaultPathFile);
            }
        } else if (PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(OpenProjectPlugIn.FILE_CHOOSER_DIRECTORY_KEY) != null) {
            this.fileChooser.setCurrentDirectory(new File((String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(OpenProjectPlugIn.FILE_CHOOSER_DIRECTORY_KEY)));
        }
        if (this.fileChooser.showSaveDialog(context.getWorkbenchFrame()) != 0) {
            return false;
        }
        this.projectFile = this.fileChooser.getSelectedFile();
        this.projectFile = FileUtil.addValidExtension(this.projectFile, SAIG_FILE_EXTENSION);
        context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(context.getWorkbenchContext().getProject(), ProjectEventType.BEFORE_SAVE));
        return true;
    }

    public static boolean checkTemporalLayers() {
        List<TaskFrame> taskFrames = JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks();
        List<Table> tables = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRealTables();
        ArrayList<String> temporalLayerNames = new ArrayList<String>();
        ArrayList<String> temporalTableNames = new ArrayList<String>();
        for (TaskFrame currentTask : taskFrames) {
            List<Layer> layers = currentTask.getLayerManager().getLayers();
            for (Layer currentLayer : layers) {
                if (currentLayer.hasReadableDataSource() && !LayerUtil.isSystemLayer(currentLayer) || LayerUtil.isAppInternalSystemLayer(currentLayer)) continue;
                temporalLayerNames.add(currentLayer.getName());
            }
        }
        for (Table currentTable : tables) {
            if (!currentTable.isInternal() && currentTable.hasReadableDataSource()) continue;
            temporalTableNames.add(currentTable.getName());
        }
        if (!temporalLayerNames.isEmpty() || !temporalTableNames.isEmpty()) {
            TemporalLayersInProjectDialog dialog = new TemporalLayersInProjectDialog(JUMPWorkbench.getFrameInstance(), true, temporalLayerNames, temporalTableNames);
            dialog.setVisible(true);
            return dialog.wasOkPressed();
        }
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        if (monitor != null) {
            monitor.report(String.valueOf(I18N.getString("workbench.ui.plugin.SaveProjectAsPlugIn.saving-project")) + "...");
        }
        SaveProjectAsPlugIn.saveTaskFrameStatus();
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
            if (tableRecord.isInternal() || !tableRecord.hasReadableDataSource()) continue;
            tableRecord.setFrameLocationX(dataFrame.getLocation().x);
            tableRecord.setFrameLocationY(dataFrame.getLocation().y);
            tableRecord.setFrameWidth(dataFrame.getSize().width);
            tableRecord.setFrameHeight(dataFrame.getSize().height);
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
        context.getWorkbenchFrame().warnUser(I18N.getMessage("workbench.ui.plugin.SaveProjectAsPlugIn.project-{0}-successfully-saved", new Object[]{GUIUtil.nameWithoutExtension(this.projectFile)}));
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
        String oldFileProjectPath = null;
        if (this.oldProjectFile != null) {
            oldFileProjectPath = this.oldProjectFile.getAbsolutePath();
        }
        context.getWorkbenchContext().fireProjectChanged(new ProjectEvent(context.getWorkbenchContext().getProject(), oldFileProjectPath, ProjectEventType.SAVED_AS));
    }

    public File getFile() {
        return this.projectFile;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return checkFactory.createProjectMustBeOpenedCheck();
    }

    public static void saveTaskFrameStatus() {
        TaskFrame[] taskWindows = JUMPWorkbench.getFrameInstance().getTaskFrames();
        int i = 0;
        while (i < taskWindows.length) {
            TaskFrame tf = taskWindows[i];
            if (tf.getLayerNamePanel() != null) {
                tf.getLayerNamePanel().saveStatus();
            }
            ++i;
        }
    }
}

