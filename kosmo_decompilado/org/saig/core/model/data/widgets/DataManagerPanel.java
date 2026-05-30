/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import es.kosmo.desktop.widgets.datasource.LoadTableDialog;
import es.kosmo.desktop.widgets.table.DummyViewTableFrame;
import es.kosmo.desktop.widgets.task.TaskManagerPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.TableFactory;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.utils.relations.RelationsConfigDialog;

public class DataManagerPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DataManagerPanel.class);
    private JScrollPane tableListScrollPane;
    private JList tableList;
    private JPanel actionPanel;
    private JButton loadTableButton;
    private JButton removeTableButton;
    private JButton viewTableButton;
    private JButton changeNameButton;
    private JButton configureRelationsButton;
    private static LoadTableDialog dialog;
    private JPopupMenu popupMenu;
    private JMenuItem changeNameItem;
    private JMenuItem viewItem;
    private JMenuItem deleteItem;
    private JMenuItem configureRelationItem;
    private JMenuItem enabledItem;

    public DataManagerPanel() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getTableListScrollPane());
        FormUtils.addRowInGBL(this, 2, 0, this.getActionPanel());
        this.getPopupMenu();
        this.refreshActions();
    }

    private JScrollPane getTableListScrollPane() {
        if (this.tableListScrollPane == null) {
            this.tableListScrollPane = new JScrollPane();
            this.tableListScrollPane.setHorizontalScrollBarPolicy(31);
            this.tableListScrollPane.setMinimumSize(new Dimension(200, 120));
            this.tableListScrollPane.setPreferredSize(new Dimension(200, 120));
            this.tableListScrollPane.setViewportView(this.getTableList());
            this.tableListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.tableListScrollPane;
    }

    private JList getTableList() {
        this.tableList = new JList();
        this.tableList.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.loaded-table-list"));
        List<ViewTableFrame> tablas = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getTables();
        this.tableList.setListData(tablas.toArray());
        this.tableList.setCellRenderer(new MyListCellRenderer());
        this.tableList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) && !DataManagerPanel.this.tableList.isSelectionEmpty() && DataManagerPanel.this.tableList.isSelectedIndex(DataManagerPanel.this.tableList.locationToIndex(me.getPoint()))) {
                    DataManagerPanel.this.popupMenu.show(DataManagerPanel.this.tableList, me.getX(), me.getY());
                }
            }
        });
        this.tableList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    DataManagerPanel.this.refreshActions();
                }
            }
        });
        return this.tableList;
    }

    private void enabled(boolean value) {
        this.viewTableButton.setEnabled(value);
        this.removeTableButton.setEnabled(value);
        this.changeNameButton.setEnabled(value);
        this.configureRelationsButton.setEnabled(value);
        this.viewItem.setEnabled(value);
        this.deleteItem.setEnabled(value);
        this.changeNameItem.setEnabled(value);
        this.configureRelationItem.setEnabled(value);
    }

    private JPanel getActionPanel() {
        this.actionPanel = new JPanel();
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.actionPanel.setLayout(gridLayout1);
        this.loadTableButton = new JButton();
        this.loadTableButton.setText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.open"));
        this.loadTableButton.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.load-table"));
        this.loadTableButton.setIcon(IconLoader.icon("folder_open.png"));
        this.loadTableButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.loadTableButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DataManagerPanel.this.loadTable();
            }
        });
        this.removeTableButton = new JButton();
        this.removeTableButton.setText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.delete"));
        this.removeTableButton.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.delete-tables"));
        this.removeTableButton.setIcon(IconLoader.icon("error_obj.gif"));
        this.removeTableButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.removeTableButton.addActionListener(new DeleteActionListener());
        this.viewTableButton = new JButton();
        this.viewTableButton.setText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.view"));
        this.viewTableButton.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.view-tables"));
        this.viewTableButton.setIcon(IconLoader.icon("view.gif"));
        this.viewTableButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.viewTableButton.addActionListener(new ViewActionListener());
        this.changeNameButton = new JButton();
        this.changeNameButton.setText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.rename"));
        this.changeNameButton.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.change-selected-table-title"));
        this.changeNameButton.setIcon(IconLoader.icon("changeName.gif"));
        this.changeNameButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.changeNameButton.addActionListener(new ChangeNameActionListener());
        this.configureRelationsButton = new JButton();
        this.configureRelationsButton.setText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.relations"));
        this.configureRelationsButton.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.configure-relations"));
        this.configureRelationsButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("FlowGraph.gif")));
        this.configureRelationsButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.configureRelationsButton.addActionListener(new ConfigureRelationsActionListener());
        this.actionPanel.add((Component)this.loadTableButton, null);
        this.actionPanel.add((Component)this.changeNameButton, null);
        this.actionPanel.add((Component)this.viewTableButton, null);
        this.actionPanel.add((Component)this.removeTableButton, null);
        this.actionPanel.add((Component)this.configureRelationsButton, null);
        return this.actionPanel;
    }

    protected void loadTable() {
        final DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
        final LoadTableDialog dialog = DataManagerPanel.getDialog();
        dialog.refresh();
        dialog.setVisible(true);
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        progressDialog.setTitle(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.load-table"));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    /*
                     * Enabled aggressive block sorting
                     * Enabled unnecessary exception pruning
                     * Enabled aggressive exception aggregation
                     */
                    @Override
                    public void run() {
                        try {
                            try {
                                List<? extends TableRecordDataSource> dataSources = dialog.getTableDataSources();
                                int cont = 0;
                                int totalDS = dataSources.size();
                                Iterator<? extends TableRecordDataSource> itDS = dataSources.iterator();
                                while (itDS.hasNext()) {
                                    if (progressDialog.isCancelRequested()) {
                                        return;
                                    }
                                    TableRecordDataSource currentDS = itDS.next();
                                    progressDialog.report(cont++, totalDS, currentDS.getName());
                                    Table recordCollection = TableFactory.getRecordCollection(currentDS);
                                    ViewTableFrame dataFrame = new ViewTableFrame(recordCollection, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
                                    dataManager.addTable(dataFrame);
                                }
                                return;
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                                progressDialog.setExceptionMessage(e.getMessage());
                                progressDialog.setVisible(false);
                                return;
                            }
                        }
                        finally {
                            progressDialog.setVisible(false);
                        }
                    }
                }).start();
            }
        });
        GUIUtil.centre(progressDialog, this.getRootPane().getParent());
        progressDialog.setVisible(true);
    }

    public void refresh() {
        List<ViewTableFrame> tablas = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getTables();
        Collections.sort(tablas);
        this.tableList.setListData(tablas.toArray());
    }

    private JPopupMenu getPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.enabledItem = new JMenuItem(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.Enable-disable"));
        this.enabledItem.addActionListener(new EnabledActionListener());
        this.popupMenu.add(this.enabledItem);
        this.changeNameItem = new JMenuItem(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.change-name"), IconLoader.icon("changeName.gif"));
        this.changeNameItem.addActionListener(new ChangeNameActionListener());
        this.popupMenu.add(this.changeNameItem);
        this.viewItem = new JMenuItem(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.view"), IconLoader.icon("view.gif"));
        this.viewItem.addActionListener(new ViewActionListener());
        this.popupMenu.add(this.viewItem);
        this.deleteItem = new JMenuItem(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.delete"), IconLoader.icon("error_obj.gif"));
        this.deleteItem.addActionListener(new DeleteActionListener());
        this.popupMenu.add(this.deleteItem);
        this.configureRelationItem = new JMenuItem(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.relations"), GUIUtil.toSmallIcon(IconLoader.icon("FlowGraph.gif")));
        this.configureRelationItem.addActionListener(new ConfigureRelationsActionListener());
        this.popupMenu.add(this.configureRelationItem);
        this.popupMenu.setSize(new Dimension(100, 100));
        this.popupMenu.pack();
        return this.popupMenu;
    }

    public void hidePopUpMenu() {
        this.popupMenu.setVisible(false);
    }

    @Override
    public void repaint() {
        if (this.viewTableButton != null) {
            if (this.tableList.getSelectedValue() != null) {
                Table table = ((ViewTableFrame)this.tableList.getSelectedValue()).getTable();
                if (table.isEnabled()) {
                    this.enabled(true);
                } else {
                    this.enabled(false);
                }
            } else {
                this.enabled(false);
            }
        }
        super.repaint();
    }

    public static LoadTableDialog getDialog() {
        if (dialog == null) {
            dialog = new LoadTableDialog(JUMPWorkbench.getFrameInstance(), true);
        }
        return dialog;
    }

    private void refreshActions() {
        this.changeNameButton.setEnabled(this.tableList.getSelectedIndices().length == 1);
        this.viewTableButton.setEnabled(this.tableList.getSelectedIndices().length > 0 && this.isEnabledSelected());
        this.removeTableButton.setEnabled(this.tableList.getSelectedIndices().length > 0);
        this.configureRelationsButton.setEnabled(this.tableList.getSelectedIndices().length == 1 && this.isEnabledSelected());
        this.enabledItem.setEnabled(this.tableList.getSelectedIndices().length > 0);
        this.changeNameItem.setEnabled(this.changeNameButton.isEnabled());
        this.viewItem.setEnabled(this.viewTableButton.isEnabled());
        this.deleteItem.setEnabled(this.removeTableButton.isEnabled());
        this.configureRelationItem.setEnabled(this.configureRelationsButton.isEnabled());
    }

    private boolean isEnabledSelected() {
        boolean anyEnabled = false;
        Object[] selectedTables = this.tableList.getSelectedValues();
        int i = 0;
        while (i < selectedTables.length && !anyEnabled) {
            ViewTableFrame frame = (ViewTableFrame)selectedTables[i];
            anyEnabled = frame.getTable().isEnabled();
            ++i;
        }
        return anyEnabled;
    }

    private class ChangeNameActionListener
    implements ActionListener {
        private ChangeNameActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Object newName;
            DataManagerPanel.this.popupMenu.setVisible(false);
            ViewTableFrame dataFrame = (ViewTableFrame)DataManagerPanel.this.tableList.getSelectedValue();
            if (dataFrame != null && (newName = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn.insert-the-new-window-name"), TaskManagerPanel.NAME, dataFrame.getTitle())) != null) {
                String name = (String)newName;
                dataFrame.setTitle(name);
                dataFrame.getTable().setName(name);
                DataManagerPanel.this.refresh();
            }
        }
    }

    private class ConfigureRelationsActionListener
    implements ActionListener {
        private ConfigureRelationsActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataManagerPanel.this.popupMenu.setVisible(false);
            ViewTableFrame dataFrame = (ViewTableFrame)DataManagerPanel.this.tableList.getSelectedValue();
            LayerManager manager = null;
            if (JUMPWorkbench.getFrameInstance().getContext().getTaskManager().size() > 0) {
                manager = JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTask(0).getLayerManager();
            }
            new RelationsConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext(), manager, dataFrame.getTable());
        }
    }

    private class DeleteActionListener
    implements ActionListener {
        private DeleteActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DataManagerPanel.this.popupMenu.setVisible(false);
            DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
            Object[] selectedValues = DataManagerPanel.this.tableList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                ViewTableFrame selectedTableGUI = (ViewTableFrame)selectedValues[i];
                dataManager.remove(selectedTableGUI);
                ++i;
            }
            DataManagerPanel.this.tableList.removeAll();
            DataManagerPanel.this.refresh();
        }
    }

    private class EnabledActionListener
    implements ActionListener {
        private EnabledActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataManagerPanel.this.popupMenu.setVisible(false);
            Object[] selectedValues = DataManagerPanel.this.tableList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                boolean value;
                ViewTableFrame selectedFrame = (ViewTableFrame)selectedValues[i];
                boolean bl = value = !selectedFrame.getTable().isEnabled();
                if (value && selectedFrame instanceof DummyViewTableFrame) {
                    ViewTableFrame newFrame = new ViewTableFrame(selectedFrame.getTable(), JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
                    newFrame.setLocation(selectedFrame.getTable().getFrameLocationX(), selectedFrame.getTable().getFrameLocationY());
                    newFrame.setSize(selectedFrame.getTable().getFrameWidth(), selectedFrame.getTable().getFrameHeight());
                    newFrame.setVisible(selectedFrame.getTable().isVisible());
                    JUMPWorkbench.getFrameInstance().getContext().getDataManager().replace(selectedFrame, newFrame);
                    JUMPWorkbench.getFrameInstance().getContext().getDataManager().addTable(newFrame);
                    selectedFrame = newFrame;
                }
                selectedFrame.getTable().setEnabled(value);
                selectedFrame.makeEnabled(value);
                ++i;
            }
            DataManagerPanel.this.tableList.removeAll();
            DataManagerPanel.this.refresh();
        }
    }

    private class MyListCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private Font font = this.getFont().deriveFont(1);
        private Font fontDisabled = this.getFont().deriveFont(2);

        private MyListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            Table tabla = ((ViewTableFrame)value).getTable();
            if (index % 2 == 0) {
                this.setBackground(new Color(227, 254, 221));
            } else {
                this.setBackground(new Color(214, 236, 238));
            }
            if (isSelected) {
                this.setBackground(Color.BLUE);
                this.setForeground(Color.WHITE);
            } else {
                this.setBorder(BorderFactory.createLineBorder(this.getBackground(), 2));
                this.setForeground(Color.BLACK);
            }
            if (!tabla.isEnabled()) {
                this.setFont(this.fontDisabled);
            } else {
                this.setFont(this.font);
            }
            return this;
        }
    }

    private class ViewActionListener
    implements ActionListener {
        private ViewActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DataManagerPanel.this.popupMenu.setVisible(false);
            Object[] selectedValues = DataManagerPanel.this.tableList.getSelectedValues();
            int i = 0;
            while (i < selectedValues.length) {
                ViewTableFrame selectedTable = (ViewTableFrame)selectedValues[i];
                if (!selectedTable.isVisible() && selectedTable.isClosed()) {
                    JUMPWorkbench.getFrameInstance().getContext().getDataManager().addTable(selectedTable);
                } else {
                    selectedTable.setVisible(true);
                    selectedTable.moveToFront();
                }
                ++i;
            }
        }
    }
}

