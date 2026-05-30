/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.collections.MapUtils
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.Unmarshaller
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.desktop.widgets.table.DummyViewTableFrame;
import java.awt.Frame;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFileChooser;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.gvsig.crs.ICrs;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.TableRelation;
import org.saig.core.model.task.TaskManager;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.TranslationWrapper;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.project.OpenRecentProjectsPlugIn;
import org.saig.jump.plugin.utils.project.ViewProjectPlugIn;
import org.saig.jump.plugin.utils.window.CloseAllTasksPlugIn;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.project.ProjectEvent;
import org.saig.jump.widgets.util.project.ProjectEventType;

public class OpenProjectPlugIn
extends AbstractLoadProjectPlugIn
implements ThreadedPlugIn {
    public static final String FILE_CHOOSER_DIRECTORY_KEY = String.valueOf(OpenProjectPlugIn.class.getName()) + " - FILE_PROJECT_CHOOSER_DIRECTORY_KEY";
    public static final String LAST_PROJECT_KEY = "LAST_PROYECT";
    public static final String RELATIVE_PATH_MARK = "$USER_DIR_PATH";
    public static final String KEY_RESOLVER_KEY = "KEY_RESOLVER";
    public static final String NAME = I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.name");
    private static final Logger LOGGER = Logger.getLogger((String)"com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn");
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    private Mapping projectMappings = null;
    private String directAbsolutePath;

    public OpenProjectPlugIn() {
        this.directAbsolutePath = null;
    }

    public OpenProjectPlugIn(Mapping mappings) {
        this.projectMappings = mappings;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        this.fileChooser.setDialogType(0);
        this.fileChooser.setFileSelectionMode(0);
        this.fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SaveProjectAsPlugIn.SAIG_PROJECT_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SaveProjectAsPlugIn.SAIG_PROJECT_FILE_FILTER);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (this.directAbsolutePath != null && context.getWorkbenchContext().getProject() != null && context.getWorkbenchContext().getProject().getProjectFile() != null && context.getWorkbenchContext().getProject().getProjectFile().getAbsolutePath().equals(this.directAbsolutePath)) {
            return false;
        }
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.PROJECTS_PATH_KEY);
        if (defaultPath != null && !defaultPath.equals("")) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.canRead()) {
                this.fileChooser.setCurrentDirectory(defaultPathFile);
            }
        } else if (PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(FILE_CHOOSER_DIRECTORY_KEY) != null) {
            this.fileChooser.setCurrentDirectory(new File((String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(FILE_CHOOSER_DIRECTORY_KEY)));
        }
        if (context.getWorkbenchContext().getProject() != null) {
            int res = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.do-you-want-to-save-the-current-project"), I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.save-current-project"));
            if (res == 0) {
                SaveProjectPlugIn saveProjectPlugIn = new SaveProjectPlugIn(new SaveProjectAsPlugIn());
                saveProjectPlugIn.initialize(context);
                if (!saveProjectPlugIn.execute(context)) return false;
                saveProjectPlugIn.run(new TaskMonitorDialog((Frame)context.getWorkbenchFrame(), context.getErrorHandler()), context);
            } else if (res == 2) {
                return false;
            }
            if (res != 0 && res != 1) return false;
            if (this.directAbsolutePath == null) return this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) == 0;
            return true;
        }
        if (this.directAbsolutePath == null) return this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) == 0;
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        if (this.directAbsolutePath != null) {
            File file = new File(this.directAbsolutePath);
            if (file.exists()) {
                this.directAbsolutePath = null;
                this.open(file, context, monitor);
            }
            return;
        }
        PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(FILE_CHOOSER_DIRECTORY_KEY, this.fileChooser.getCurrentDirectory().toString());
        this.open(this.fileChooser.getSelectedFile(), context, monitor);
    }

    public void open(File file, PlugInContext contexto, TaskMonitor monitor) throws Exception {
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            LOGGER.info((Object)I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.loading-project-{0}", new Object[]{file.getAbsolutePath()}));
            Project project = this.open(reader, contexto, monitor);
            project.setProjectFile(file);
            PersistentBlackboardPlugIn.get(contexto.getWorkbenchFrame().getContext()).put(LAST_PROJECT_KEY, file.getAbsolutePath());
            LinkedList recentProjectsLinkedList = null;
            Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(OpenRecentProjectsPlugIn.RECENT_PROJECTS_KEY);
            recentProjectsLinkedList = value == null || !(value instanceof LinkedList) ? new LinkedList() : (LinkedList)value;
            String path = file.getAbsolutePath();
            recentProjectsLinkedList.remove(path);
            recentProjectsLinkedList.addFirst(path);
            while (recentProjectsLinkedList.size() > 5) {
                recentProjectsLinkedList.removeLast();
            }
            PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(OpenRecentProjectsPlugIn.RECENT_PROJECTS_KEY, recentProjectsLinkedList);
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.error-loading-the-project-file-{0}", new Object[]{file.getName()}), (Throwable)e);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.there-are-errors-opening-the-project-file-{0}", new Object[]{file.getName()}), I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.error-loading-the-project-file-{0}", new Object[]{file.getName()}));
        }
    }

    public Project open(Reader reader, PlugInContext context, TaskMonitor monitor) throws Exception {
        Project project = null;
        CloseAllTasksPlugIn closeAllTask = new CloseAllTasksPlugIn(true);
        closeAllTask.execute(context);
        ViewProjectPlugIn.setInitialized(false);
        monitor.report(String.valueOf(I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.creating-objects")) + "...");
        WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
        HashMap<String, List<SummaryMessage>> messageMap = new HashMap<String, List<SummaryMessage>>();
        try {
            if (this.projectMappings == null) {
                this.projectMappings = LoadXMLMappings.loadProjectMappings();
            }
            Unmarshaller unmar = new Unmarshaller(this.projectMappings);
            unmar.setClassLoader(context.getWorkbenchContext().getWorkbench().getPlugInManager().getClassLoader());
            unmar.setWhitespacePreserve(true);
            project = (Project)unmar.unmarshal(reader);
            JUMPWorkbenchContext wContext = (JUMPWorkbenchContext)workbenchFrame.getContext();
            String loadingProjectMessage = I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.loading-project-{0}", new Object[]{project.getName()});
            monitor.report(String.valueOf(loadingProjectMessage) + "...");
            LOGGER.info((Object)loadingProjectMessage);
            wContext.setProject(project);
            wContext.getDataManager().clear();
            wContext.getPrintLayoutManager().clear();
            wContext.getTaskManager().clear();
            List<Table> tables = project.getTables();
            List<SummaryMessage> tableExceptions = this.loadTables(tables, wContext, monitor);
            TaskManager taskManager = wContext.getTaskManager();
            List<Task> tasks = project.getTasks();
            int totalTareas = tasks.size();
            int contador = 1;
            for (Task sourceTask : tasks) {
                monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn.Loading-the-views-associated-to-the-project")) + "...");
                monitor.report(contador, totalTareas, I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.loading-view-{0}-from-project-{1}", new Object[]{sourceTask.getName(), project.getName()}));
                Task newTask = new Task();
                newTask.setName(sourceTask.getName());
                newTask.setProperties(sourceTask.getProperties());
                newTask.setCurrentView(sourceTask.getCurrentView());
                newTask.setFrameLocationX(sourceTask.getFrameLocationX());
                newTask.setFrameLocationY(sourceTask.getFrameLocationY());
                newTask.setFrameWidth(sourceTask.getFrameWidth());
                newTask.setFrameHeight(sourceTask.getFrameHeight());
                newTask.setVisible(sourceTask.isVisible());
                newTask.setGraphicScale(sourceTask.getGraphicScale());
                newTask.setLegends(sourceTask.getLegends());
                newTask.setAngle(sourceTask.getAngle());
                newTask.setNorth(sourceTask.getNorth());
                newTask.setTitleByLang(sourceTask.getTitleByLang());
                double mapFactor = sourceTask.getMapFactor();
                if (mapFactor == 0.0) {
                    mapFactor = 1.0;
                }
                newTask.setMapFactor(mapFactor);
                newTask.setMapLengthUnit(UnitsManager.getMapLengthUnit(sourceTask).toString());
                newTask.setUserLengthUnit(UnitsManager.getUserLengthUnit(sourceTask).toString());
                newTask.setUserAreaUnit(UnitsManager.getUserAreaUnit(sourceTask).toString());
                ICrs proj = null;
                if (sourceTask.getCrsWKT() == null) {
                    if (sourceTask.getCrsDescription() != null && !sourceTask.getCrsDescription().equals("")) {
                        proj = CrsRepositoryManager.getInstance().getCRS(sourceTask.getCrsDescription());
                    }
                } else {
                    proj = CrsRepositoryManager.getInstance().getCRS(sourceTask.getCrsCode(), sourceTask.getCrsWKT());
                    proj.setTransParam(sourceTask.getNadGrid());
                    proj.setTransInTarget(sourceTask.isTargetNad());
                }
                newTask.setProjection(proj);
                TaskFrame taskFrame = workbenchFrame.addTaskFrame(newTask, newTask.isVisible());
                taskFrame.setLocation(newTask.getFrameLocationX(), newTask.getFrameLocationY());
                taskFrame.setSize(newTask.getFrameWidth(), newTask.getFrameHeight());
                taskFrame.getLayerViewPanel().setFactor(newTask.getMapFactor());
                taskFrame.getLayerViewPanel().setMapLengthUnit(UnitsManager.getMapLengthUnit(newTask));
                taskFrame.getLayerViewPanel().setUserLengthUnit(UnitsManager.getUserLengthUnit(newTask));
                taskFrame.getLayerViewPanel().setUserAreaUnit(UnitsManager.getUserAreaUnit(newTask));
                taskFrame.getLayerViewPanel().setGraphicScale(newTask.getGraphicScale());
                taskFrame.getLayerViewPanel().setLegends(newTask.getLegends());
                this.zoomToEnvelope(taskFrame);
                taskFrame.getLayerViewPanel().getRenderingManager().setPaintingEnabled(false);
                List<SummaryMessage> taskMessages = this.loadLayers(sourceTask, newTask, monitor, context);
                if (taskMessages.size() != 0) {
                    messageMap.put(newTask.getName(), taskMessages);
                }
                taskManager.addTask(taskFrame);
                LayerManager layerManager = taskFrame.getLayerManager();
                DataManager dataManager = wContext.getDataManager();
                List<Layer> layers = layerManager.getLayers();
                for (Layer layer : layers) {
                    this.manageLayer(layer, layerManager, dataManager);
                }
                taskFrame.getLayerViewPanel().getRenderingManager().setPaintingEnabled(true);
                taskFrame.getLayerViewPanel().repaint(true);
                ++contador;
            }
            DataManager dataManager = wContext.getDataManager();
            for (Table table : dataManager.getRealTables()) {
                if (!table.hasRelations()) continue;
                HashMap relations = new HashMap();
                for (Relation<?> element : table.getAllRelations()) {
                    LOGGER.debug((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn.Processing-the-relation-{0}", new Object[]{element.getRelationName()}));
                    if (element instanceof LayerRelation) {
                        LayerRelation layerRelation = (LayerRelation)element;
                        String layerName = layerRelation.getTargetLayer().getName();
                        List layers = wContext.getAllLayers();
                        Layer selectedLayer = null;
                        Iterator iterator2 = layers.iterator();
                        while (iterator2.hasNext() && selectedLayer == null) {
                            Layer layer = (Layer)iterator2.next();
                            if (!layer.getName().equals(layerName)) continue;
                            selectedLayer = layer;
                        }
                        layerRelation.setTargetLayer(selectedLayer);
                    } else if (element instanceof TableRelation) {
                        TableRelation tableRelation = (TableRelation)element;
                        Table tableRelation_ = tableRelation.getTable();
                        if (tableRelation_ == null) {
                            LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn.It-is-not-possible-to-restore-the-table-of-the-relation-{0}", new Object[]{tableRelation.getRelationName()}));
                        } else {
                            tableRelation.setTable(dataManager.getTable(tableRelation.getTable().getName()));
                        }
                    }
                    relations.put(element.getRelationName(), element);
                }
                table.setRelations(relations);
            }
            if (tableExceptions.size() > 0) {
                messageMap.put(I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.tables"), tableExceptions);
            }
            if (project.getActiveLocale() == null) {
                LOGGER.info((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn.Establishing-the-default-language"));
                project.setActiveLocale(I18N.getLocale());
            }
            PrintLayoutManager layoutManager = wContext.getPrintLayoutManager();
            List<Page> layouts = project.getLayouts();
            ArrayList<SummaryMessage> layoutMessages = new ArrayList<SummaryMessage>();
            for (Page page : layouts) {
                TaskFrame associatedTaskFrame = taskManager.getTask(page.getTaskName());
                if (associatedTaskFrame != null) {
                    PrintLayoutFrame frame = new PrintLayoutFrame(associatedTaskFrame, wContext.getWorkbench().getFrame(), false);
                    frame.setPage(page);
                    frame.setPageFormat(page.getPageFormat());
                    frame.setActiveZoom(page.getActiveZoom());
                    frame.setName(page.getName());
                    frame.setTitle(page.getName());
                    page.initPage();
                    PreviewPanel preview = new PreviewPanel(frame, page);
                    frame.getPrintLayoutPreviewPanel().setPreview(preview);
                    int i = 1;
                    while (i <= page.getGraphicElements().size()) {
                        frame.setGraphic(page.getGraphicElements().get(i - 1));
                        frame.getPage().posGraphicElement(frame);
                        ++i;
                    }
                    page.resize(frame.getPageFormat());
                    layoutManager.addLayout(frame);
                    frame.changeUnits(associatedTaskFrame.getTask().getMapFactor(), UnitsManager.getMapLengthUnit(associatedTaskFrame.getTask()), UnitsManager.getUserLengthUnit(associatedTaskFrame.getTask()), UnitsManager.getUserAreaUnit(associatedTaskFrame.getTask()));
                    continue;
                }
                layoutMessages.add(this.buildLayoutErrorMessage(page.getName(), new Exception(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.the-associated-view-{0}-can-not-be-found", new Object[]{page.getTaskName()}))));
            }
            if (layoutMessages.size() > 0) {
                messageMap.put(I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.layouts"), layoutMessages);
            }
            if (messageMap.size() != 0) {
                this.showErrorSummary(messageMap, project.getName());
            }
            wContext.fireProjectChanged(new ProjectEvent(project, ProjectEventType.OPENED));
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        this.loadTaskFrameStatus();
        System.gc();
        return project;
    }

    protected void manageLayer(Layer layer, LayerManager layerManager, DataManager dataManager) {
        if (layer.getLayerFilter() != null) {
            layer.getUltimateFeatureCollectionWrapper().setLayerFilter(layer.getLayerFilter());
        }
        if (layer.getTransactionalDataSource() != null && layer.getProperty(KEY_RESOLVER_KEY) != null) {
            layer.getTransactionalDataSource().setKeyResolver((IDBKeyResolver)layer.getProperty(KEY_RESOLVER_KEY));
        }
        Map<String, Boolean> attributesVisibilities = layer.getAttributeVisibility();
        Map<String, String> attributesPublicNames = layer.getAttributePublicNames();
        Map<String, Map<Locale, String>> attributesTranslationsMap = layer.getAttributeTranslationsMap();
        if (MapUtils.isEmpty(attributesTranslationsMap)) {
            attributesTranslationsMap = new HashMap<String, Map<Locale, String>>();
            Locale currentLocale = LocaleManager.getActiveLocale();
            for (String attrName : attributesPublicNames.keySet()) {
                HashMap<Locale, String> translations = new HashMap<Locale, String>();
                translations.put(currentLocale, attributesPublicNames.get(attrName));
                attributesTranslationsMap.put(attrName, (Map<Locale, String>)((Object)new TranslationWrapper(translations)));
            }
        }
        if (layer.isEnabled()) {
            FeatureSchema schema = layer.getFeatureSchema();
            if (layer.isVersionable()) {
                layer.setVersionable(true);
                layer.setStartDateField(layer.getStartDateField());
                layer.setEndDateField(layer.getEndDateField());
                layer.setHistoryField(layer.getHistoryField());
                layer.setVersionableViewDate(layer.getVersionableViewDate());
            }
            int i = 0;
            while (i < schema.getAttributeCount()) {
                String name = schema.getAttributeName(i);
                if (attributesTranslationsMap.containsKey(name)) {
                    schema.changeTranslations(name, ((TranslationWrapper)((Object)attributesTranslationsMap.get(name))).getTranslationMap());
                }
                if (attributesVisibilities.containsKey(name)) {
                    schema.changeVisibility(name, attributesVisibilities.get(name));
                }
                ++i;
            }
        }
        if (layer.hasRelations()) {
            HashMap relations = new HashMap();
            for (Relation<?> element : layer.getAllRelations()) {
                if (element instanceof LayerRelation) {
                    LayerRelation layerRelation = (LayerRelation)element;
                    layerRelation.setTargetLayer(layerManager.getLayer(layerRelation.getTargetLayer().getName()));
                } else if (element instanceof TableRelation) {
                    TableRelation tableRelation = (TableRelation)element;
                    tableRelation.setTable(dataManager.getTable(tableRelation.getTable().getName()));
                }
                relations.put(element.getRelationName(), element);
            }
            layer.setRelations(relations);
        }
        if (layer.hashiperLink() && layer.getHiperLink() instanceof HiperLinkCompound) {
            HiperLinkCompound hiper = (HiperLinkCompound)layer.getHiperLink();
            hiper.setTable(dataManager.getTable(hiper.getTable().getName()));
        }
    }

    protected void showErrorSummary(Map<String, List<SummaryMessage>> messageMap, String projectName) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.error-loading-the-project-{0}", new Object[]{projectName}), messageMap);
        dialog.setVisible(true);
    }

    protected List<SummaryMessage> loadTables(List<Table> tables, JUMPWorkbenchContext context, TaskMonitor monitor) {
        DataManager dataManager = context.getDataManager();
        ArrayList<SummaryMessage> messageList = new ArrayList<SummaryMessage>();
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn.Loading-tables")) + "...");
        int total = tables.size();
        int cont = 0;
        for (Table table : tables) {
            monitor.report(cont++, total, I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.loading-table-{0}", new Object[]{table.getName()}));
            try {
                ViewTableFrame frame;
                Map<String, Object> properties = table.getProperties();
                TableRecordDataSource trds = TableRecordDataSource.buildTableRecordDataSourceFromProperties(properties);
                table.setDataSource(trds);
                if (table.isVersionable()) {
                    table.setVersionable(true);
                    table.setStartDateField(table.getStartDateField());
                    table.setEndDateField(table.getEndDateField());
                    table.setHistoryField(table.getHistoryField());
                    table.setVersionableViewDate(table.getVersionableViewDate());
                }
                if (table.isEnabled()) {
                    frame = new ViewTableFrame(table, context.createPlugInContext());
                    frame.setLocation(table.getFrameLocationX(), table.getFrameLocationY());
                    frame.setSize(table.getFrameWidth(), table.getFrameHeight());
                    frame.setVisible(table.isVisible());
                    dataManager.addTable(frame);
                    continue;
                }
                frame = new DummyViewTableFrame(table, context.createPlugInContext());
                dataManager.addTable(frame);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-table-{0}", new Object[]{table.getName()})) + " - " + e.getMessage()));
                messageList.add(this.buildTableErrorMessage(table.getName(), e));
            }
        }
        return messageList;
    }

    protected void zoomToEnvelope(TaskFrame taskFrame) throws NoninvertibleTransformException {
        Envelope savedEnvelope = taskFrame.getTask().getCurrentView();
        if (savedEnvelope != null) {
            taskFrame.getLayerViewPanel().getViewport().zoom(savedEnvelope, true);
            taskFrame.getLayerViewPanel().setViewportInitialized(true);
        }
    }

    protected List<SummaryMessage> loadLayers(Task sourceTask, Task newTask, TaskMonitor monitor, PlugInContext context) throws Exception {
        ArrayList<SummaryMessage> messageList = new ArrayList<SummaryMessage>();
        LayerManager sourceLayerManager = sourceTask.getLayerManager();
        for (Category sourceLayerCategory : sourceLayerManager.getCategories()) {
            this.loadCategory(newTask, sourceLayerCategory, monitor, messageList);
        }
        return messageList;
    }

    protected void loadTaskFrameStatus() {
        TaskFrame[] taskWindows = JUMPWorkbench.getFrameInstance().getTaskFrames();
        int i = 0;
        while (i < taskWindows.length) {
            TaskFrame tf = taskWindows[i];
            if (tf.getLayerNamePanel() != null) {
                tf.getLayerNamePanel().loadStatus();
            }
            ++i;
        }
    }

    public void setDirectAbsolutePath(String path) {
        this.directAbsolutePath = path;
    }
}

