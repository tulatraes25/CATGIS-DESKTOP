/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.LayerTreeModel;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.CloneableInternalFrame;
import com.vividsolutions.jump.workbench.ui.DummyLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.PrimaryInfoFrame;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;
import org.saig.jump.widgets.util.ILeftTabTaskFrameComponent;

public class TaskFrame
extends JInternalFrame
implements TaskFrameProxy,
CloneableInternalFrame,
LayerViewPanelProxy,
LayerNamePanelProxy,
LayerManagerProxy,
SelectionManagerProxy,
Task.NameListener,
Comparable<TaskFrame> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TaskFrame.class);
    private LayerManager layerManager;
    protected JSplitPane splitPane = new JSplitPane();
    private String realTitle;
    @Deprecated
    private final int cloneIndex;
    private InfoFrame infoFrame = null;
    private LayerNamePanel layerNamePanel = new DummyLayerNamePanel();
    private LayerViewPanel layerViewPanel;
    private Task task;
    private WorkbenchContext workbenchContext;
    protected Component leftPanel;
    protected static Set<ILeftTabTaskFrameComponent> leftComponents = new HashSet<ILeftTabTaskFrameComponent>();
    protected Timer wmsUpdateTimer;
    protected Timer arcIMSUpdateTimer;
    protected InternalFrameAdapter internalFrameAdapter;
    protected LayerTreeModel layerTreeModel;
    protected TreeLayerNamePanel treeLayerNamePanel;

    public TaskFrame(Task task, WorkbenchContext workbenchContext) {
        this(task, 0, workbenchContext);
    }

    private TaskFrame(Task task, int cloneIndex, final WorkbenchContext workbenchContext) {
        this.setDefaultCloseOperation(1);
        this.task = task;
        this.layerManager = task.getLayerManager();
        this.cloneIndex = cloneIndex;
        this.workbenchContext = workbenchContext;
        this.internalFrameAdapter = new InternalFrameAdapter(){

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                if (TaskFrame.this.layerViewPanel != null) {
                    TaskFrame.this.layerViewPanel.setCurrentCursorTool(new DummyTool());
                }
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                TaskFrame.this.refreshTabbedPanel(true);
                TaskFrame.this.layerNamePanel.addListener(workbenchContext.getWorkbench().getFrame().getLayerNamePanelListener());
            }
        };
        this.addInternalFrameListener(this.internalFrameAdapter);
        this.layerViewPanel = new LayerViewPanel(task.getLayerManager(), workbenchContext.getWorkbench().getFrame());
        this.layerViewPanel.setAngle(task.getAngle());
        this.layerViewPanel.setNorth(task.getNorth());
        try {
            this.jbInit();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.layerViewPanel.addListener(workbenchContext.getWorkbench().getFrame().getLayerViewPanelListener());
        this.layerViewPanel.getViewport().addListener(workbenchContext.getWorkbench().getFrame());
        task.add(this);
        this.installWMSAnimator();
    }

    protected Component createLeftPanel() {
        Component comp = null;
        if (!leftComponents.isEmpty()) {
            JTabbedPane tabbedPane = new JTabbedPane(3);
            tabbedPane.addTab(I18N.getString("com.vividsolutions.jump.workbench.ui.TaskFrame.layers"), GUIUtil.toSmallIcon(IconLoader.icon("World2.gif")), (Component)((Object)this.layerNamePanel));
            for (ILeftTabTaskFrameComponent currentComponent : leftComponents) {
                tabbedPane.addTab(currentComponent.getName(), currentComponent.getIcon(), currentComponent.getComponent(this.layerManager));
            }
            comp = tabbedPane;
        } else {
            comp = (Component)((Object)this.layerNamePanel);
        }
        return comp;
    }

    @Override
    public TaskFrame getTaskFrame() {
        return this;
    }

    @Override
    public SelectionManager getSelectionManager() {
        return this.getLayerViewPanel().getSelectionManager();
    }

    protected LayerNamePanel createLayerNamePanel() {
        this.layerTreeModel = new LayerTreeModel(this);
        this.treeLayerNamePanel = new TreeLayerNamePanel(this, this.layerTreeModel, this.layerViewPanel.getRenderingManager(), new HashMap(), this.workbenchContext);
        this.layerViewPanel.getViewport().addListener(this.treeLayerNamePanel.getScalePanel());
        Map<Class<?>, JPopupMenu> nodeClassToPopupMenuMap = this.workbenchContext.getWorkbench().getFrame().getNodeClassToPopupMenuMap();
        for (Class<?> nodeClass : nodeClassToPopupMenuMap.keySet()) {
            this.treeLayerNamePanel.addPopupMenu(nodeClass, nodeClassToPopupMenuMap.get(nodeClass));
        }
        return this.treeLayerNamePanel;
    }

    @Override
    public LayerManager getLayerManager() {
        if (this.task != null) {
            return this.task.getLayerManager();
        }
        return null;
    }

    public InfoFrame getInfoFrame() {
        if (this.infoFrame == null || this.infoFrame.isClosed()) {
            this.infoFrame = new PrimaryInfoFrame(this.workbenchContext, this, this);
        }
        return this.infoFrame;
    }

    @Override
    public LayerNamePanel getLayerNamePanel() {
        return this.layerNamePanel;
    }

    @Override
    public LayerViewPanel getLayerViewPanel() {
        return this.layerViewPanel;
    }

    public Task getTask() {
        return this.task;
    }

    public String getRealTitle() {
        return this.realTitle;
    }

    private int nextCloneIndex() {
        String key = String.valueOf(this.getClass().getName()) + " - LAST_CLONE_INDEX";
        this.task.getLayerManager().getBlackboard().put(key, 1 + this.task.getLayerManager().getBlackboard().get(key, 0));
        return this.task.getLayerManager().getBlackboard().getInt(key);
    }

    @Override
    public JInternalFrame internalFrameClone() {
        TaskFrame clone = new TaskFrame(this.task, this.nextCloneIndex(), this.workbenchContext);
        clone.splitPane.setDividerLocation(0);
        clone.setSize(300, 300);
        if (this.task.getLayerManager().size() > 0) {
            clone.getLayerViewPanel().getViewport().initialize(this.getLayerViewPanel().getViewport().getScale(), this.getLayerViewPanel().getViewport().getOriginInModelCoordinates());
            clone.getLayerViewPanel().setViewportInitialized(true);
        }
        return clone;
    }

    @Override
    public void taskNameChanged(String name) {
        this.updateTitle();
    }

    private void jbInit() throws Exception {
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.setSize(680, 380);
        this.getContentPane().setLayout(new BorderLayout());
        this.splitPane.setBorder(null);
        this.getContentPane().add((Component)this.splitPane, "Center");
        this.leftPanel = (Component)((Object)this.layerNamePanel);
        this.splitPane.add(this.leftPanel, "left");
        this.splitPane.add((Component)this.layerViewPanel, "right");
        this.splitPane.setDividerLocation(200);
        this.splitPane.setDividerSize(7);
        this.splitPane.setOneTouchExpandable(true);
        this.splitPane.setContinuousLayout(false);
        this.updateTitle();
    }

    public void updateTitle() {
        String title = this.task.getTitle(LocaleManager.getActiveLocale());
        IProjection proj = this.task.getProjection();
        if (this.cloneIndex > 0) {
            title = String.valueOf(title) + " (" + I18N.getString("workbench.ui.TaskFrame.view") + " " + (this.cloneIndex + 1) + ")";
        }
        this.realTitle = title;
        title = proj != null ? String.valueOf(title) + " < " + GUITranslationsUtils.getCRSDescription(proj) + " >" : String.valueOf(title) + " < " + EPSGSelectionDialog.NO_SRS_DEFINED + " >";
        this.setTitle(title);
    }

    public JSplitPane getSplitPane() {
        return this.splitPane;
    }

    private void installWMSAnimator() {
        this.wmsUpdateTimer = new Timer(500, new ActionListener(){
            private boolean showingClocks = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.wmsRenderingInProgress()) {
                    TaskFrame.this.repaint();
                    this.showingClocks = true;
                } else if (this.showingClocks) {
                    TaskFrame.this.repaint();
                    this.showingClocks = false;
                }
            }

            private boolean wmsRenderingInProgress() {
                for (WMSLayer wmsLayer : TaskFrame.this.layerManager.getWMSLayers()) {
                    if (TaskFrame.this.layerViewPanel == null || TaskFrame.this.layerViewPanel.getRenderingManager() == null) {
                        return false;
                    }
                    AbstractRenderer renderer = TaskFrame.this.layerViewPanel.getRenderingManager().getRenderer(wmsLayer);
                    if (renderer == null || !renderer.isRendering()) continue;
                    return true;
                }
                return false;
            }
        });
        this.wmsUpdateTimer.setCoalesce(true);
        this.wmsUpdateTimer.start();
    }

    @Override
    public String getName() {
        if (this.task == null) {
            return "";
        }
        return this.task.getTitle(LocaleManager.getActiveLocale());
    }

    @Override
    public String toString() {
        return this.getTitle();
    }

    @Override
    public int compareTo(TaskFrame other) {
        Collator col = Collator.getInstance(I18N.getLocale());
        return col.compare(this.getTask().getName(), other.getTask().getName());
    }

    public void setPrintMode(Dimension d, Dimension dmm) {
        this.setResizable(false);
        this.setMaximizable(false);
        this.layerViewPanel.setPrintMode(d, dmm);
    }

    public void unsetPrintMode() {
        this.layerViewPanel.unsetPrintMode();
        this.setResizable(true);
        this.setMaximizable(true);
    }

    public static void registerTabbedPanel(ILeftTabTaskFrameComponent newPanel) {
        leftComponents.add(newPanel);
    }

    public static void unregisterTabbedPanel(ILeftTabTaskFrameComponent panel) {
        leftComponents.remove(panel);
    }

    public void refreshTabbedPanel(boolean regenerateLayerNamePanel) {
        this.splitPane.remove((Component)((Object)this.layerNamePanel));
        if (regenerateLayerNamePanel) {
            this.layerNamePanel = this.createLayerNamePanel();
        }
        this.leftPanel = this.createLeftPanel();
        this.splitPane.add(this.leftPanel, "left");
        this.splitPane.setDividerLocation(200);
    }

    public Component getLeftPanel() {
        return this.leftPanel;
    }

    public Set<ILeftTabTaskFrameComponent> getLeftComponents() {
        return leftComponents;
    }

    @Override
    public void dispose() {
        try {
            PropertyChangeListener[] propertyChangeListeners;
            InternalFrameListener[] listeners;
            this.layerViewPanel.dispose();
            this.layerNamePanel.dispose();
            this.getLayerManager().dispose();
            this.workbenchContext.getWorkbench().getTaskManager().remove(this);
            if (this.layerTreeModel != null) {
                this.layerTreeModel.dispose();
            }
            if (this.treeLayerNamePanel != null) {
                this.treeLayerNamePanel.dispose();
            }
            this.task = null;
            this.splitPane = null;
            if (this.infoFrame != null) {
                this.infoFrame.dispose();
                this.infoFrame = null;
            }
            if (this.wmsUpdateTimer != null) {
                this.wmsUpdateTimer.stop();
                this.wmsUpdateTimer = null;
            }
            if (this.arcIMSUpdateTimer != null) {
                this.arcIMSUpdateTimer.stop();
                this.arcIMSUpdateTimer = null;
            }
            this.layerViewPanel = null;
            this.layerNamePanel = null;
            this.layerManager = null;
            this.layerTreeModel = null;
            this.treeLayerNamePanel = null;
            InternalFrameListener[] internalFrameListenerArray = listeners = this.getInternalFrameListeners();
            int n = listeners.length;
            int n2 = 0;
            while (n2 < n) {
                InternalFrameListener internalFrameListener = internalFrameListenerArray[n2];
                this.removeInternalFrameListener(internalFrameListener);
                ++n2;
            }
            PropertyChangeListener[] propertyChangeListenerArray = propertyChangeListeners = this.getPropertyChangeListeners();
            int n3 = propertyChangeListeners.length;
            n = 0;
            while (n < n3) {
                PropertyChangeListener propertyChangeListener = propertyChangeListenerArray[n];
                this.removePropertyChangeListener(propertyChangeListener);
                ++n;
            }
            this.internalFrameAdapter = null;
            this.workbenchContext = null;
            JUMPWorkbench.getFrameInstance().removeInternalFrame(this);
            this.setDesktopIcon(null);
            this.setBorder(null);
        }
        catch (Throwable t) {
            this.workbenchContext.getWorkbench().getFrame().handleThrowable(t);
        }
        super.dispose();
    }

    public TreeLayerNamePanel getTreeLayerNamePanel() {
        return this.treeLayerNamePanel;
    }

    public void showLeftComponent(String name) {
        JTabbedPane tabbedPane;
        int tabIndex;
        if (StringUtils.isNotEmpty((String)name) && this.leftPanel instanceof JTabbedPane && (tabIndex = (tabbedPane = (JTabbedPane)this.leftPanel).indexOfTab(name)) != -1) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }
}

