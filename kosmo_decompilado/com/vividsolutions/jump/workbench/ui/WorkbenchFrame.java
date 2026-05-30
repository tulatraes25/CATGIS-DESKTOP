/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.UndoableEditReceiver;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.ApplicationExitHandler;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.ErrorDialog;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.InternalFrameCloseHandler;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.PrimaryInfoFrame;
import com.vividsolutions.jump.workbench.ui.RecursiveKeyListener;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TitledPopupMenu;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.model.data.widgets.TableAttributeTab;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.core.model.relations.Relation;
import org.saig.core.styling.Rule;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.ApplicationExitListener;
import org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel;
import org.saig.jump.widgets.scale.ScalePanel;
import org.saig.jump.widgets.util.DialogFactory;

public class WorkbenchFrame
extends JFrame
implements LayerViewPanelContext,
ViewportListener {
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel statusPanel = new JPanel();
    private JLabel coordinateLabel = new JLabel();
    private JLabel messageLabel = new JLabel();
    private JLabel timeLabel = new JLabel();
    private JLabel memoryLabel = new JLabel();
    private JLabel wmsLabel = new JLabel();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu();
    private JMenu windowMenu = new JMenu();
    private JMenuItem exitMenuItem = new JMenuItem();
    private TitledPopupMenu categoryPopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(Category.class).size() != 1 ? "(" + panel.selectedNodes(Category.class).size() + " " + I18N.getString("workbench.ui.WorkbenchFrame.categories-selected") + ")" : panel.selectedNodes(Category.class).iterator().next().getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu layerNamePopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(Layer.class).size() != 1 ? "(" + panel.selectedNodes(Layer.class).size() + " " + I18N.getString("workbench.ui.WorkbenchFrame.layers-selected") + ")" : ((Layerable)panel.selectedNodes(Layer.class).iterator().next()).getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu wmsLayerNamePopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(WMSLayer.class).size() != 1 ? "(" + panel.selectedNodes(WMSLayer.class).size() + " " + I18N.getString("workbench.ui.WorkbenchFrame.wms-layers-selected") + ")" : ((Layerable)panel.selectedNodes(WMSLayer.class).iterator().next()).getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu textBalloonLayerNamePopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(TextBalloonLayer.class).size() != 1 ? "(" + panel.selectedNodes(TextBalloonLayer.class).size() + " " + I18N.getString("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.selected-balloon-layers") + ")" : ((Layerable)panel.selectedNodes(TextBalloonLayer.class).iterator().next()).getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu attributeTabLayerNamePopupMenu = new TitledPopupMenu();
    private TitledPopupMenu tableAttributeTabLayerNamePopupMenu = new TitledPopupMenu();
    private TitledPopupMenu ruleNamePopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(Rule.class).size() != 1 ? "(" + panel.selectedNodes(Rule.class).size() + " " + I18N.getString("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.selected-rules") + ")" : String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.rule")) + " - " + panel.selectedNodes(Rule.class).iterator().next().getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu layerablePopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    LayerNamePanel panel = ((LayerNamePanelProxy)((Object)WorkbenchFrame.this.getActiveInternalFrame())).getLayerNamePanel();
                    this.setTitle(panel.selectedNodes(Layerable.class).size() != 1 ? "(" + panel.selectedNodes(Layerable.class).size() + " " + I18N.getString("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.selected-layers") + ")" : panel.selectedNodes(Layerable.class).iterator().next().getTitle(LocaleManager.getActiveLocale()));
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private WorkbenchToolBar toolBar;
    List<ToolboxDialog> toolbarDialogs = new ArrayList<ToolboxDialog>();
    private String taskName = "";
    private boolean viewLinked = false;
    private TaskMonitorManager taskMonitor = new TaskMonitorManager();
    private Map<JInternalFrame, LayerListener> internalFrameToLayerListenersMap = new HashMap<JInternalFrame, LayerListener>();
    private JDesktopPane desktopPane = new MyDesktopPane();
    private HTMLFrame outputFrame = new HTMLFrame(this){
        private static final long serialVersionUID = 1L;

        @Override
        public void setTitle(String title) {
            super.setTitle(I18N.getString("workbench.ui.WorkbenchFrame.output"));
        }
    };
    private ImageIcon icon;
    private LayerNamePanelListener layerNamePanelListener = new LayerNamePanelListener(){

        @Override
        public void layerSelectionChanged() {
            WorkbenchFrame.this.updateAllToolbars();
        }
    };
    private LayerViewPanelListener layerViewPanelListener = new LayerViewPanelListener(){
        private StringBuffer positionStatusBuf = new StringBuffer("(");

        @Override
        public synchronized void cursorPositionChanged(String x, String y) {
            this.positionStatusBuf.setLength(1);
            this.positionStatusBuf.append(x).append(" ; ").append(y).append(")");
            WorkbenchFrame.this.coordinateLabel.setText(this.positionStatusBuf.toString());
        }

        @Override
        public void selectionChanged() {
            WorkbenchFrame.this.updateAllToolbars();
        }

        public void fenceChanged() {
            WorkbenchFrame.this.updateAllToolbars();
        }

        @Override
        public void painted(Graphics graphics) {
        }

        @Override
        public void renderingFinished() {
        }

        @Override
        public void renderingStarted() {
        }
    };
    private Map<Class<?>, JPopupMenu> nodeClassToLayerNamePopupMenuMap = CollectionUtil.createMap(new Object[]{Layer.class, this.layerNamePopupMenu, WMSLayer.class, this.wmsLayerNamePopupMenu, TextBalloonLayer.class, this.textBalloonLayerNamePopupMenu, Category.class, this.categoryPopupMenu, Layerable.class, this.layerablePopupMenu, Rule.class, this.ruleNamePopupMenu, AttributeTab.class, this.attributeTabLayerNamePopupMenu, TableAttributeTab.class, this.tableAttributeTabLayerNamePopupMenu});
    private StringBuffer log = new StringBuffer();
    private int taskSequence = 1;
    private WorkbenchContext workbenchContext;
    private String lastStatusMessage = "";
    private Set swappableStyleClasses = new HashSet();
    private List<KeyListener> easyKeyListeners = new ArrayList<KeyListener>();
    private KeyListener keyListener = new KeyAdapter(){

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 27 && WorkbenchFrame.this.getContext() != null && WorkbenchFrame.this.getContext().getLayerViewPanel() != null && WorkbenchFrame.this.getContext().getLayerViewPanel().getSelectionManager() != null) {
                LayerViewPanel panel = WorkbenchFrame.this.getContext().getLayerViewPanel();
                Collection<AbstractSelection> selections = panel.getSelectionManager().getSelections();
                for (AbstractSelection currentSelection : selections) {
                    AbstractRenderer renderer = panel.getRenderingManager().getRenderer(currentSelection.getRendererContentID());
                    if (!renderer.isRendering()) continue;
                    renderer.cancel();
                }
                SwingUtilities.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        WorkbenchFrame.this.getContext().getLayerViewPanel().getSelectionManager().clear();
                    }
                });
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            this.keyStateChanged(e);
        }

        private void keyStateChanged(KeyEvent e) {
        }
    };
    private int positionIndex = -1;
    private int primaryInfoFrameIndex = -1;
    private InternalFrameCloseHandler internalFrameCloseHandler = new DefaultInternalFrameCloser();
    private ApplicationExitHandler applicationExitHandler = new DefaultApplicationExitHandler();

    public WorkbenchFrame(String title, ImageIcon icon, WorkbenchContext workbenchContext) throws Exception {
        this.setTitle(title);
        this.workbenchContext = workbenchContext;
        this.icon = icon;
        this.toolBar = new WorkbenchToolBar(workbenchContext);
        this.toolBar.setTaskMonitorManager(this.taskMonitor);
        try {
            this.jbInit();
            this.configureStatusLabel(this.coordinateLabel, 250);
            this.configureStatusLabel(this.timeLabel, 200);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        new RecursiveKeyListener(this){

            @Override
            public void keyTyped(KeyEvent e) {
                for (KeyListener l : WorkbenchFrame.this.easyKeyListeners) {
                    l.keyTyped(e);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                for (KeyListener l : new ArrayList(WorkbenchFrame.this.easyKeyListeners)) {
                    l.keyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                for (KeyListener l : new ArrayList(WorkbenchFrame.this.easyKeyListeners)) {
                    l.keyReleased(e);
                }
            }
        };
        this.easyKeyListeners.add(this.keyListener);
    }

    public void addToolbarDialog(ToolboxDialog dialog) {
        this.toolbarDialogs.add(dialog);
    }

    public void addEasyKeyListener(KeyListener l) {
        if (!this.easyKeyListeners.contains(l)) {
            this.easyKeyListeners.add(l);
        }
    }

    public void removeEasyKeyListener(KeyListener l) {
        this.easyKeyListeners.remove(l);
    }

    public void log(String message) {
        this.log.append(new Date() + "  " + message + System.getProperty("line.separator"));
    }

    public JLabel getTimeLabel() {
        return this.timeLabel;
    }

    public String getLog() {
        return this.log.toString();
    }

    public void displayLastStatusMessage() {
        this.setStatusMessage(this.lastStatusMessage);
    }

    @Override
    public void setStatusMessage(String message) {
        this.lastStatusMessage = message;
        this.setStatusBarText(message);
        this.setStatusBarTextHighlighted(false);
    }

    private void setStatusBarText(String message) {
        this.messageLabel.setText(StringUtils.isEmpty((String)message) ? " " : message);
        this.messageLabel.setToolTipText(message);
    }

    private void setStatusBarTextHighlighted(boolean highlighted) {
        this.messageLabel.setForeground(highlighted ? Color.black : this.coordinateLabel.getForeground());
        this.messageLabel.setBackground(highlighted ? Color.yellow : this.coordinateLabel.getBackground());
    }

    public void setTimeMessage(String message) {
        this.timeLabel.setText(message.equals("") ? " " : message);
    }

    public JInternalFrame getActiveInternalFrame() {
        return this.desktopPane.getSelectedFrame();
    }

    public JInternalFrame[] getInternalFrames() {
        return this.desktopPane.getAllFrames();
    }

    public TitledPopupMenu getCategoryPopupMenu() {
        return this.categoryPopupMenu;
    }

    public TitledPopupMenu getLayerablePopupmenu() {
        return this.layerablePopupMenu;
    }

    public WorkbenchContext getContext() {
        return this.workbenchContext;
    }

    public JDesktopPane getDesktopPane() {
        return this.desktopPane;
    }

    public TitledPopupMenu getLayerNamePopupMenu() {
        return this.layerNamePopupMenu;
    }

    public TitledPopupMenu getWMSLayerNamePopupMenu() {
        return this.wmsLayerNamePopupMenu;
    }

    public TitledPopupMenu getTextBalloonLayerNamePopupMenu() {
        return this.textBalloonLayerNamePopupMenu;
    }

    public TitledPopupMenu getAttributeTabLayerNamePopupMenu() {
        return this.attributeTabLayerNamePopupMenu;
    }

    public JPopupMenu getTableAttributeTabLayerNamePopupMenu() {
        return this.tableAttributeTabLayerNamePopupMenu;
    }

    public TitledPopupMenu getRulePopupMenu() {
        return this.ruleNamePopupMenu;
    }

    public LayerViewPanelListener getLayerViewPanelListener() {
        return this.layerViewPanelListener;
    }

    public Map<Class<?>, JPopupMenu> getNodeClassToPopupMenuMap() {
        return this.nodeClassToLayerNamePopupMenuMap;
    }

    public LayerNamePanelListener getLayerNamePanelListener() {
        return this.layerNamePanelListener;
    }

    public HTMLFrame getOutputFrame() {
        return this.outputFrame;
    }

    public WorkbenchToolBar getToolBar() {
        return this.toolBar;
    }

    public void activateFrame(JInternalFrame frame) {
        this.activateFrame(frame, true);
    }

    public void activateFrame(JInternalFrame frame, boolean changeVisibility) {
        if (frame instanceof ViewTableFrame) {
            ViewTableFrame dataFrame = (ViewTableFrame)frame;
            if (changeVisibility) {
                dataFrame.getTable().setVisible(true);
            }
            frame.setVisible(dataFrame.getTable().isEnabled() && dataFrame.getTable().isVisible());
        } else {
            frame.setVisible(true);
        }
        frame.moveToFront();
        frame.requestFocus();
        try {
            frame.setSelected(true);
        }
        catch (PropertyVetoException e) {
            this.warnUser(StringUtil.stackTrace(e));
        }
    }

    public void addInternalFrame(JInternalFrame internalFrame) {
        this.addInternalFrame(internalFrame, false, true);
    }

    public void addInternalFrame(JInternalFrame internalFrame, boolean visible) {
        this.addInternalFrame(internalFrame, false, true, visible);
    }

    public void addInternalFrame(final JInternalFrame internalFrame, boolean alwaysOnTop, boolean autoUpdateToolBar, boolean visible) {
        if (internalFrame instanceof LayerManagerProxy) {
            this.setClosingBehaviour((LayerManagerProxy)((Object)internalFrame));
            this.installTitleBarModifiedIndicator((LayerManagerProxy)((Object)internalFrame));
        }
        internalFrame.setFrameIcon(this.icon);
        internalFrame.setVisible(visible);
        this.desktopPane.add((Component)internalFrame, alwaysOnTop ? JDesktopPane.PALETTE_LAYER : JDesktopPane.DEFAULT_LAYER);
        internalFrame.putClientProperty("JInternalFrame.frameType", "optionDialog");
        if (autoUpdateToolBar) {
            internalFrame.addInternalFrameListener(new InternalFrameListener(){

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                    WorkbenchFrame.this.toolBar.reClickSelectedCursorToolButton();
                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                }

                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    if (internalFrame instanceof LayerManagerProxy && WorkbenchFrame.this.internalFrameToLayerListenersMap.containsKey(internalFrame)) {
                        LayerListener listener = (LayerListener)WorkbenchFrame.this.internalFrameToLayerListenersMap.get(internalFrame);
                        LayerManager layerManager = ((LayerManagerProxy)((Object)internalFrame)).getLayerManager();
                        if (layerManager != null) {
                            layerManager.removeLayerListener(listener);
                        }
                    }
                    WorkbenchFrame.this.updateAllToolbars();
                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                }

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {
                    WorkbenchFrame.this.updateAllToolbars();
                }
            });
            if (visible) {
                this.activateFrame(internalFrame, false);
            }
            if (internalFrame.getClass().equals(TaskFrame.class)) {
                internalFrame.setSize(this.desktopPane.getSize());
            }
        }
    }

    public void addInternalFrame(JInternalFrame internalFrame, boolean alwaysOnTop, boolean autoUpdateToolBar) {
        this.addInternalFrame(internalFrame, alwaysOnTop, autoUpdateToolBar, true);
    }

    private void installTitleBarModifiedIndicator(final LayerManagerProxy internalFrame) {
        final JInternalFrame i = (JInternalFrame)((Object)internalFrame);
        new Block(){
            private boolean updatingTitle = false;

            private void updateTitle() {
                if (this.updatingTitle) {
                    return;
                }
                this.updatingTitle = true;
                try {
                    String newTitle = i.getTitle();
                    if (newTitle.trim().length() == 0) {
                        return;
                    }
                    if (newTitle.charAt(0) == '*') {
                        newTitle = newTitle.substring(1);
                    }
                    if (!internalFrame.getLayerManager().getLayersWithModifiedFeatureCollections().isEmpty()) {
                        newTitle = String.valueOf('*') + newTitle;
                    }
                    i.setTitle(newTitle);
                }
                finally {
                    this.updatingTitle = false;
                }
            }

            @Override
            public Object yield() {
                LayerListener layerListener = new LayerListener(){

                    @Override
                    public void layerChanged(LayerEvent e) {
                        if (e.getType() == LayerEventType.METADATA_CHANGED || e.getType() == LayerEventType.REMOVED) {
                            this.updateTitle();
                        }
                    }

                    @Override
                    public void categoryChanged(CategoryEvent e) {
                    }

                    @Override
                    public void featuresChanged(FeatureEvent e) {
                    }
                };
                internalFrame.getLayerManager().addLayerListener(layerListener);
                WorkbenchFrame.this.internalFrameToLayerListenersMap.put(i, layerListener);
                i.addPropertyChangeListener("title", new PropertyChangeListener(){

                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        this.updateTitle();
                    }
                });
                return null;
            }
        }.yield();
    }

    private void setClosingBehaviour(LayerManagerProxy internalFrame) {
        final JInternalFrame i = (JInternalFrame)((Object)internalFrame);
        i.setDefaultCloseOperation(0);
        i.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                i.setVisible(false);
                if (!(i instanceof TaskFrame)) {
                    i.dispose();
                }
            }
        });
    }

    private Collection getInternalFramesAssociatedWith(LayerManager layerManager) {
        ArrayList<JInternalFrame> internalFramesAssociatedWithLayerManager = new ArrayList<JInternalFrame>();
        JInternalFrame[] internalFrames = this.getInternalFrames();
        int i = 0;
        while (i < internalFrames.length) {
            if (internalFrames[i] instanceof LayerManagerProxy && ((LayerManagerProxy)((Object)internalFrames[i])).getLayerManager() == layerManager) {
                internalFramesAssociatedWithLayerManager.add(internalFrames[i]);
            }
            ++i;
        }
        return internalFramesAssociatedWithLayerManager;
    }

    public TaskFrame addTaskFrame() {
        TaskFrame f = this.addTaskFrame(this.createTask());
        return f;
    }

    public Task createTask() {
        Task task = new Task();
        String newTaskName = String.valueOf(this.taskName) + " - " + this.taskSequence++;
        while (this.getContext().getTaskManager().getTask(newTaskName) != null) {
            newTaskName = String.valueOf(this.taskName) + " - " + this.taskSequence++;
        }
        task.setName(newTaskName);
        String mapUnits = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigDefaultViewOptionsPanel.DEFAULT_MAP_UNITS_KEY, UnitsManager.DEFAULT_LENGTH_UNIT.toString());
        String lengthMeasureUnits = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigDefaultViewOptionsPanel.DEFAULT_LENGTH_MEASSURE_UNITS_KEY, UnitsManager.DEFAULT_LENGTH_UNIT.toString());
        String areaMeasureUnits = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigDefaultViewOptionsPanel.DEFAULT_AREA_MEASSURE_UNITS_KEY, UnitsManager.DEFAULT_AREA_UNIT.toString());
        task.setMapLengthUnit(mapUnits);
        task.setUserLengthUnit(lengthMeasureUnits);
        task.setUserAreaUnit(areaMeasureUnits);
        return task;
    }

    public TaskFrame addTaskFrame(Task task) {
        return this.addTaskFrame(new TaskFrame(task, this.workbenchContext), true);
    }

    public TaskFrame addTaskFrame(Task task, boolean visible) {
        return this.addTaskFrame(new TaskFrame(task, this.workbenchContext), visible);
    }

    public TaskFrame addTaskFrame(TaskFrame taskFrame, boolean visible) {
        taskFrame.getTask().getLayerManager().addLayerListener(new LayerListener(){

            @Override
            public void featuresChanged(FeatureEvent e) {
            }

            @Override
            public void categoryChanged(CategoryEvent e) {
                WorkbenchFrame.this.updateAllToolbars();
            }

            @Override
            public void layerChanged(LayerEvent layerEvent) {
                WorkbenchFrame.this.updateAllToolbars();
            }
        });
        this.addInternalFrame(taskFrame, visible);
        taskFrame.getLayerViewPanel().getLayerManager().getUndoableEditReceiver().add(new UndoableEditReceiver.Listener(){

            @Override
            public void undoHistoryChanged() {
                WorkbenchFrame.this.updateAllToolbars();
            }

            @Override
            public void undoHistoryTruncated() {
                WorkbenchFrame.this.updateAllToolbars();
                WorkbenchFrame.this.log(I18N.getString("workbench.ui.WorkbenchFrame.undo-history-was-truncated"));
            }
        });
        return taskFrame;
    }

    public void flash(final HTMLFrame frame) {
        final Color originalColor = frame.getBackgroundColor();
        new Timer(100, new ActionListener(){
            private int tickCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ++this.tickCount;
                    frame.setBackgroundColor(this.tickCount % 2 == 0 ? originalColor : Color.yellow);
                    if (this.tickCount == 2) {
                        Timer timer = (Timer)e.getSource();
                        timer.stop();
                    }
                }
                catch (Throwable t) {
                    WorkbenchFrame.this.handleThrowable(t);
                }
            }
        }).start();
    }

    private void flashStatusMessage(final String message) {
        new Timer(100, new ActionListener(){
            private int tickCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                ++this.tickCount;
                WorkbenchFrame.this.setStatusBarText(message);
                WorkbenchFrame.this.setStatusBarTextHighlighted(this.tickCount % 2 == 0);
                if (this.tickCount == 4) {
                    Timer timer = (Timer)e.getSource();
                    timer.stop();
                }
            }
        }).start();
    }

    @Override
    public void handleThrowable(final Throwable t) {
        t.printStackTrace(System.err);
        this.log(StringUtil.stackTrace(t));
        Window parent = this;
        Window[] ownedWindows = this.getOwnedWindows();
        int i = 0;
        while (i < ownedWindows.length) {
            if (ownedWindows[i] instanceof Dialog && ownedWindows[i].isVisible() && ((Dialog)ownedWindows[i]).isModal()) {
                parent = ownedWindows[i];
                break;
            }
            ++i;
        }
        final WorkbenchFrame finalParent = parent;
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                ErrorDialog.show(finalParent, StringUtil.toFriendlyName(t.getClass().getName()), WorkbenchFrame.this.toMessage(t), StringUtil.stackTrace(t));
            }
        });
    }

    private String toMessage(Throwable t) {
        String message = t.getLocalizedMessage() == null ? I18N.getString("workbench.ui.WorkbenchFrame.no-description-was-provided") : (t.getLocalizedMessage().toLowerCase().indexOf(I18N.getString("workbench.ui.WorkbenchFrame.side-location-conflict")) > -1 ? String.valueOf(t.getLocalizedMessage()) + " -- " + I18N.getString("workbench.ui.WorkbenchFrame.check-for-invalid-geometries") : t.getLocalizedMessage());
        return String.valueOf(message) + " (" + StringUtil.toFriendlyName(t.getClass().getName()) + ")";
    }

    public boolean hasInternalFrame(JInternalFrame internalFrame) {
        JInternalFrame[] frames = this.desktopPane.getAllFrames();
        int i = 0;
        while (i < frames.length) {
            if (frames[i] == internalFrame) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public void removeInternalFrame(JInternalFrame internalFrame) {
        this.desktopPane.getDesktopManager().closeFrame(internalFrame);
        this.internalFrameToLayerListenersMap.remove(internalFrame);
    }

    @Override
    public void warnUser(String warning) {
        this.log(String.valueOf(I18N.getString("workbench.ui.WorkbenchFrame.warning")) + ": " + warning);
        this.flashStatusMessage(warning);
    }

    @Override
    public void zoomChanged(Envelope modelEnvelope) {
        this.updateAllToolbars();
        if (this.viewLinked) {
            JInternalFrame[] frames = this.getInternalFrames();
            int i = 0;
            while (i < frames.length) {
                JInternalFrame frame = frames[i];
                if (!frame.equals(this.getActiveInternalFrame()) && frame instanceof TaskFrame) {
                    TaskFrame taskFrame = (TaskFrame)frame;
                    Viewport viewport = taskFrame.getLayerViewPanel().getViewport();
                    try {
                        viewport.zoom(modelEnvelope, false);
                        for (ViewportListener currentListener : new CopyOnWriteArrayList<ViewportListener>(viewport.getListeners())) {
                            if (!(currentListener instanceof ScalePanel)) continue;
                            ScalePanel scalePanel = (ScalePanel)currentListener;
                            scalePanel.zoomChanged(viewport.getEnvelopeInModelCoordinates());
                        }
                    }
                    catch (NoninvertibleTransformException noninvertibleTransformException) {
                        // empty catch block
                    }
                }
                ++i;
            }
        }
    }

    void exitMenuItem_actionPerformed(ActionEvent e) {
        this.closeApplication();
    }

    void this_componentShown(ComponentEvent e) {
        try {
            this.updateAllToolbars();
        }
        catch (Throwable t) {
            this.handleThrowable(t);
        }
    }

    void this_windowClosing(WindowEvent e) {
        this.closeApplication();
    }

    void windowMenu_menuSelected(MenuEvent e) {
        int numItems = this.windowMenu.getItemCount();
        JMenuItem[] menuItems = new JMenuItem[numItems];
        int i = 0;
        while (i < menuItems.length) {
            menuItems[i] = this.windowMenu.getItem(i);
            ++i;
        }
        this.windowMenu.removeAll();
        i = 0;
        while (i < menuItems.length) {
            JMenuItem element = menuItems[i];
            if (element == null) break;
            this.windowMenu.add(element);
            ++i;
        }
        this.windowMenu.addSeparator();
        JInternalFrame[] frames = this.desktopPane.getAllFrames();
        int i2 = 0;
        while (i2 < frames.length) {
            ViewTableFrame dataFrame;
            if (!(frames[i2] instanceof ViewTableFrame) || (dataFrame = (ViewTableFrame)frames[i2]).getTable().isEnabled()) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
                menuItem.setText(GUIUtil.truncateString(frames[i2].getTitle(), 50));
                menuItem.setSelected(frames[i2].isSelected());
                this.associate(menuItem, frames[i2]);
                this.windowMenu.add(menuItem);
            }
            ++i2;
        }
    }

    private void associate(JMenuItem menuItem, final JInternalFrame frame) {
        menuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WorkbenchFrame.this.activateFrame(frame);
                }
                catch (Throwable t) {
                    WorkbenchFrame.this.handleThrowable(t);
                }
            }
        });
    }

    private void closeApplication() {
        this.applicationExitHandler.exitApplication(this);
    }

    private Collection<Layer> getLayersWithModifiedFeatureCollections() {
        ArrayList<Layer> layersWithModifiedFeatureCollections = new ArrayList<Layer>();
        for (LayerManager layerManager : this.getLayerManagers()) {
            layersWithModifiedFeatureCollections.addAll(layerManager.getLayersWithModifiedFeatureCollections());
        }
        return layersWithModifiedFeatureCollections;
    }

    private Collection<LayerManager> getLayerManagers() {
        HashSet<LayerManager> layerManagers = new HashSet<LayerManager>();
        JInternalFrame[] internalFrames = this.getInternalFrames();
        int i = 0;
        while (i < internalFrames.length) {
            if (internalFrames[i] instanceof LayerManagerProxy) {
                layerManagers.add(((LayerManagerProxy)((Object)internalFrames[i])).getLayerManager());
            }
            ++i;
        }
        return layerManagers;
    }

    private void configureStatusLabel(JLabel label, int width) {
        label.setMinimumSize(new Dimension(width, (int)label.getMinimumSize().getHeight()));
        label.setMaximumSize(new Dimension(width, (int)label.getMaximumSize().getHeight()));
        label.setPreferredSize(new Dimension(width, (int)label.getPreferredSize().getHeight()));
    }

    private void jbInit() throws Exception {
        this.setDefaultCloseOperation(0);
        this.setIconImage(this.icon.getImage());
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                WorkbenchFrame.this.this_componentShown(e);
            }
        });
        this.getContentPane().setLayout(this.borderLayout1);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                WorkbenchFrame.this.this_windowClosing(e);
            }
        });
        this.setJMenuBar(this.menuBar);
        this.setSize(900, 645);
        this.desktopPane.setDragMode(1);
        this.messageLabel.setOpaque(true);
        this.wmsLabel.setHorizontalAlignment(2);
        this.wmsLabel.setText(" ");
        this.getContentPane().add((Component)this.statusPanel, "South");
        this.fileMenu.setText(MainMenuNames.FILE);
        this.exitMenuItem.setText(I18N.getString("workbench.ui.WorkbenchFrame.exit"));
        this.exitMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WorkbenchFrame.this.exitMenuItem_actionPerformed(e);
            }
        });
        this.windowMenu.setText(MainMenuNames.WINDOW);
        this.windowMenu.addMenuListener(new MenuListener(){

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                WorkbenchFrame.this.windowMenu_menuSelected(e);
            }
        });
        this.coordinateLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.wmsLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.coordinateLabel.setText(" ");
        this.statusPanel.setLayout(this.gridBagLayout1);
        this.statusPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        this.messageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.messageLabel.setText(" ");
        this.timeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.timeLabel.setText(" ");
        this.memoryLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.memoryLabel.setText(" ");
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.windowMenu);
        this.getContentPane().add((Component)this.toolBar, "North");
        this.getContentPane().add((Component)this.desktopPane, "Center");
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.exitMenuItem);
        this.statusPanel.add((Component)this.coordinateLabel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.statusPanel.add((Component)this.timeLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.statusPanel.add((Component)this.messageLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
    }

    public JPanel getStatusPanel() {
        return this.statusPanel;
    }

    private void position(JInternalFrame internalFrame) {
        int STEP = 5;
        GUIUtil.Location location = null;
        if (internalFrame instanceof PrimaryInfoFrame) {
            ++this.primaryInfoFrameIndex;
            int offset = this.primaryInfoFrameIndex % 3 * 5;
            location = new GUIUtil.Location(offset, true, offset, true);
        } else {
            ++this.positionIndex;
            int offset = this.positionIndex % 5 * 5;
            location = new GUIUtil.Location(offset, false, offset, false);
        }
        GUIUtil.setLocation(internalFrame, location, this.desktopPane);
    }

    public Set getSwappableStyleClasses() {
        return Collections.unmodifiableSet(this.swappableStyleClasses);
    }

    public void addSwappableStyleClass(Class<?> styleClass) {
        Assert.isTrue((boolean)Style.class.isAssignableFrom(styleClass));
        this.swappableStyleClasses.add(styleClass);
    }

    public void setCurrentProjectTitle(String projectTitle) {
        String currentTitle = this.getTitle();
        int pos = currentTitle.lastIndexOf("[");
        currentTitle = pos == -1 ? String.valueOf(currentTitle) + " [ " + projectTitle + " ]" : String.valueOf(currentTitle.substring(0, pos + 1)) + projectTitle + "]";
        this.setTitle(currentTitle);
        this.taskName = projectTitle;
        this.taskSequence = 1;
        this.workbenchContext.getProjectManagerFrame().setTitle(projectTitle);
    }

    public boolean isViewLinked() {
        return this.viewLinked;
    }

    public void setViewLinked(boolean viewLinked) {
        this.viewLinked = viewLinked;
    }

    public void updateAllToolbars() {
        this.toolBar.updateEnabledState();
        if (!this.toolbarDialogs.isEmpty()) {
            for (ToolboxDialog element : this.toolbarDialogs) {
                if (!element.isVisible()) continue;
                element.updateEnabledState();
            }
        }
    }

    public ToolboxDialog getToolboxDialog(String className) {
        ToolboxDialog selectedToolboxDialog = null;
        if (!this.toolbarDialogs.isEmpty()) {
            Iterator<ToolboxDialog> iter = this.toolbarDialogs.iterator();
            while (iter.hasNext() && selectedToolboxDialog == null) {
                ToolboxDialog element = iter.next();
                if (!element.getClass().getName().equals(className)) continue;
                selectedToolboxDialog = element;
            }
        }
        return selectedToolboxDialog;
    }

    public InternalFrameCloseHandler getInternalFrameCloseHandler() {
        return this.internalFrameCloseHandler;
    }

    public void setInternalFrameCloseHandler(InternalFrameCloseHandler value) {
        this.internalFrameCloseHandler = value;
    }

    public ApplicationExitHandler getApplicationExitHandler() {
        return this.applicationExitHandler;
    }

    public void setApplicationExitHandler(ApplicationExitHandler value) {
        this.applicationExitHandler = value;
    }

    private void closeTaskFrame(TaskFrame taskFrame) {
        LayerManager layerManager = taskFrame.getLayerManager();
    }

    public boolean confirmClose(String action, Collection<Layer> modifiedLayers) {
        if (CollectionUtils.isEmpty(modifiedLayers)) {
            return true;
        }
        StringBuffer messBuf = new StringBuffer();
        if (modifiedLayers.size() == 1) {
            messBuf.append(I18N.getMessage("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.The-layer-{0}-has-modified-data-without-saving", new Object[]{modifiedLayers.iterator().next().getTitle()}));
        } else {
            String layerNames = "";
            for (Layer layer : modifiedLayers) {
                layerNames = String.valueOf(layerNames) + layer.getTitle() + ", ";
            }
            if (StringUtils.isNotEmpty((String)layerNames)) {
                layerNames = layerNames.substring(0, layerNames.length() - 2);
            }
            messBuf.append(I18N.getMessage("com.vividsolutions.jump.workbench.ui.WorkbenchFrame.The-layers-{0}-have-modified-changes-without-saving", new Object[]{layerNames}));
        }
        messBuf.append(". " + I18N.getString("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Do-you-want-to-continue"));
        Object value = DialogFactory.showWarningOptionDialog(this, messBuf.toString(), I18N.getString("workbench.ui.WorkbenchFrame.close-application"), new String[]{action, I18N.getString("workbench.ui.WorkbenchFrame.cancel")}, action);
        return (Integer)value == 0;
    }

    public TaskFrame[] getTaskFrames() {
        ArrayList<TaskFrame> taskFrames = new ArrayList<TaskFrame>();
        JInternalFrame[] internalFrames = this.getInternalFrames();
        int i = 0;
        while (i < internalFrames.length) {
            if (internalFrames[i] instanceof TaskFrame) {
                taskFrames.add((TaskFrame)internalFrames[i]);
            }
            ++i;
        }
        return taskFrames.toArray(new TaskFrame[0]);
    }

    private class DefaultApplicationExitHandler
    implements ApplicationExitHandler {
        protected Set<ApplicationExitListener> exitListeners = new HashSet<ApplicationExitListener>();
        private final Logger LOGGER = Logger.getLogger(DefaultApplicationExitHandler.class);

        private DefaultApplicationExitHandler() {
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public void exitApplication(JFrame mainFrame) {
            if (!WorkbenchFrame.this.confirmClose(I18N.getString("workbench.ui.WorkbenchFrame.exit-application"), WorkbenchFrame.this.getLayersWithModifiedFeatureCollections())) return;
            try {
                int res = DialogFactory.showYesNoCancelDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.do-you-want-to-save-the-current-project"), I18N.getString("workbench.ui.plugin.OpenProjectPlugIn.save-current-project"));
                if (res == 0) {
                    PlugInContext context = WorkbenchFrame.this.getContext().createPlugInContext();
                    SaveProjectPlugIn saveProjectPlugIn = new SaveProjectPlugIn(new SaveProjectAsPlugIn());
                    saveProjectPlugIn.initialize(context);
                    if (!saveProjectPlugIn.execute(context)) return;
                    saveProjectPlugIn.run(new TaskMonitorDialog((Frame)context.getWorkbenchFrame(), context.getErrorHandler()), context);
                } else if (res == 2) {
                    return;
                }
            }
            catch (Exception e) {
                this.LOGGER.error((Object)"", (Throwable)e);
            }
            if (!this.fireExitingApplication()) {
                return;
            }
            WorkbenchFrame.this.setVisible(false);
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    List<TaskFrame> tasks = WorkbenchFrame.this.getContext().getTaskManager().getTasks();
                    for (TaskFrame element : tasks) {
                        List<Layer> layers = element.getLayerManager().getLayers();
                        for (Layer layer : layers) {
                            layer.clearTransaction();
                            Map<String, Relation<?>> relations = layer.getRelations();
                            for (Relation<?> relation : relations.values()) {
                                relation.destroy();
                            }
                        }
                    }
                    DataBaseConnectionFactory.clearDataBaseConnections();
                    System.exit(0);
                }
            });
        }

        @Override
        public void addExitListener(ApplicationExitListener listener) {
            this.exitListeners.add(listener);
        }

        @Override
        public void removeExitListener(ApplicationExitListener listener) {
            this.exitListeners.remove(listener);
        }

        @Override
        public boolean fireExitingApplication() {
            boolean exitOk = true;
            Iterator<ApplicationExitListener> iterator = this.exitListeners.iterator();
            while (iterator.hasNext() && exitOk) {
                ApplicationExitListener currentListener = iterator.next();
                exitOk &= currentListener.exitingApplication();
            }
            return exitOk;
        }
    }

    private class DefaultInternalFrameCloser
    implements InternalFrameCloseHandler {
        private DefaultInternalFrameCloser() {
        }

        @Override
        public void close(JInternalFrame internalFrame) {
            if (internalFrame instanceof TaskFrame) {
                WorkbenchFrame.this.closeTaskFrame((TaskFrame)internalFrame);
            } else {
                GUIUtil.dispose(internalFrame, WorkbenchFrame.this.desktopPane);
            }
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    System.runFinalization();
                    System.gc();
                }
            });
        }
    }

    class MyDesktopPane
    extends JDesktopPane {
        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (JUMPWorkbench.DESKTOP_IMAGE != null) {
                g.drawImage(JUMPWorkbench.DESKTOP_IMAGE, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }
}

