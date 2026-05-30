/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.TitledPopupMenu;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.export.ExportUtils;
import org.saig.core.model.data.dao.export.OpenOfficeLibLoader;
import org.saig.core.model.data.widgets.TableAttributePanel;
import org.saig.core.model.data.widgets.TableAttributeTablePanel;
import org.saig.core.model.data.widgets.TableInfoModel;
import org.saig.core.model.data.widgets.TableInfoModelListener;
import org.saig.core.model.data.widgets.TableTableModel;
import org.saig.core.model.relations.Relation;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.window.AlwaysOnTopPlugIn;

public class TableAttributeTab
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TableAttributeTab.class);
    protected boolean layerViewPanelUpdates = true;
    private Table lastRightClickTable;
    private ErrorHandler errorHandler;
    private EnableCheck tablesEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return TableAttributeTab.this.panel.getModel().getTable() == null ? I18N.getString("workbench.ui.AttributeTab.one-or-more-layers-must-be-present") : null;
        }
    };
    private EnableCheck rowsSelectedEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return !TableAttributeTab.this.panel.hasSelectedRecords() ? I18N.getString("workbench.ui.AttributeTab.one-or-more-rows-must-be-selected") : null;
        }
    };
    private TableAttributePanel panel;
    private JScrollPane scrollPane = new JScrollPane();
    private EnableableToolBar toolBar = new EnableableToolBar();
    private TableInfoModel model;
    private Table[] selectedTables = new Table[0];
    private Table[] lastSelectedTables = new Table[0];
    private Dimension tableSize;
    private JFileChooser excelFileChooser;
    private JFileChooser calcFileChooser;

    public TableAttributeTab(TableInfoModel model, final WorkbenchContext workbenchContext, JInternalFrame parentInternalFrame, boolean addScrollPanesToChildren) {
        this.model = model;
        this.panel = new TableAttributePanel(model, workbenchContext, addScrollPanesToChildren){
            private static final long serialVersionUID = 1L;

            @Override
            public void tableAdded(TableTableModel tableTableModel) {
                super.tableAdded(tableTableModel);
                final TableAttributeTablePanel tablePanel = this.getTablePanel(tableTableModel.getTable());
                if (TableAttributeTab.this.tableSize == null || tablePanel.getTableSize().getWidth() > TableAttributeTab.this.tableSize.getWidth()) {
                    TableAttributeTab.this.tableSize = tablePanel.getTableSize();
                }
                MouseAdapter mouseListener = new MouseAdapter(){

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (!SwingUtilities.isRightMouseButton(e)) {
                            return;
                        }
                        int column = tablePanel.getTable().columnAtPoint(e.getPoint());
                        if (column != -1) {
                            tablePanel.setRightClickColumn(column);
                            TableAttributeTab.this.setLastRightClickTable(tablePanel.getModel().getTable());
                            workbenchContext.getWorkbench();
                            JPopupMenu popupMenu = JUMPWorkbench.getFrameInstance().getTableAttributeTabLayerNamePopupMenu();
                            ((TitledPopupMenu)popupMenu).setTitle(String.valueOf(tablePanel.getModel().getTable().getName()) + " - " + tablePanel.getTable().getColumnName(column));
                            TableAttributeTab.this.lastSelectedTables = new Table[]{tablePanel.getModel().getTable()};
                            TableAttributeTab.setEnableLastSelectedTables(true, TableAttributeTab.this);
                            try {
                                popupMenu.show(tablePanel.getLayerNameRenderer(), e.getX(), e.getY());
                            }
                            finally {
                                TableAttributeTab.setEnableLastSelectedTables(false, TableAttributeTab.this);
                            }
                        }
                    }
                };
                tablePanel.addMouseListener(mouseListener);
                tablePanel.getTable().addMouseListener(mouseListener);
                tablePanel.getTable().getTableHeader().addMouseListener(mouseListener);
                tablePanel.getLayerNameRenderer().addMouseListener(mouseListener);
            }
        };
        this.panel.setBackground(Color.BLUE);
        model.addListener(new TableInfoModelListener(){

            @Override
            public void tableAdded(TableTableModel tableTableModel) {
                TableAttributeTab.this.panel.getTablePanel(tableTableModel.getTable()).getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        TableAttributeTab.this.toolBar.updateEnabledState();
                    }
                });
                TableAttributeTab.this.toolBar.updateEnabledState();
            }

            @Override
            public void tableRemoved(TableTableModel tableTableModel) {
                TableAttributeTab.this.toolBar.updateEnabledState();
            }
        });
        this.errorHandler = workbenchContext.getErrorHandler();
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.initScrollPane();
        if (addScrollPanesToChildren) {
            this.remove(this.scrollPane);
            this.add((Component)this.panel, "Center");
        }
        this.installToolBarButtons(workbenchContext, parentInternalFrame);
        this.toolBar.updateEnabledState();
    }

    public TableInfoModel getModel() {
        return this.model;
    }

    public void setLastRightClickTable(Table table) {
        if (this.panel.getModel().getTable().equals(table)) {
            this.lastRightClickTable = table;
        }
    }

    public Dimension getTableSize() {
        return this.tableSize;
    }

    private void installToolBarButtons(WorkbenchContext workbenchContext, JInternalFrame parentInternalFrame) {
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.inverse"), IconLoader.icon("inverse.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new InverseSelectionWaitDialog(JUMPWorkbench.getFrameInstance(), true).setVisible(true);
                }
                catch (Throwable t) {
                    TableAttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.tablesEnableCheck));
        this.toolBar.add(new JButton(), ClearSelectionPlugIn.NAME, GUIUtil.toSmallIcon(ClearSelectionPlugIn.ICON), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TableAttributeTab.this.panel.clearSelection();
                }
                catch (Throwable t) {
                    TableAttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.tablesEnableCheck).add(this.rowsSelectedEnableCheck));
        final AlwaysOnTopPlugIn alwaysOnTopPlugin = new AlwaysOnTopPlugIn(parentInternalFrame);
        JButton alwaysOnTopButton = new JButton();
        alwaysOnTopButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ((JButton)arg0.getSource()).setIcon(GUIUtil.toSmallIcon((ImageIcon)alwaysOnTopPlugin.getIcon()));
            }
        });
        this.toolBar.add(alwaysOnTopButton, alwaysOnTopPlugin.getName(), GUIUtil.toSmallIcon(alwaysOnTopPlugin.getIcon()), AlwaysOnTopPlugIn.toActionListener(alwaysOnTopPlugin, workbenchContext, null), new MultiEnableCheck());
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.AttributeTab.move-selection-to-the-top"), IconLoader.icon("SelectionUp.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TableAttributeTab.this.setLayerViewPanelUpdates(false);
                    int i = 0;
                    while (i < TableAttributeTab.this.getSelectedTables().length) {
                        TableAttributeTab.this.panel.getTablePanel(TableAttributeTab.this.getSelectedTables()[i]).sortSelectedFeatures(true);
                        ++i;
                    }
                    TableAttributeTab.this.setLayerViewPanelUpdates(true);
                }
                catch (Throwable t) {
                    TableAttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.tablesEnableCheck).add(this.rowsSelectedEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.AttributeTab.move-selection-to-the-bottom"), IconLoader.icon("SelectionDown.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TableAttributeTab.this.setLayerViewPanelUpdates(false);
                    int i = 0;
                    while (i < TableAttributeTab.this.getSelectedTables().length) {
                        TableAttributeTab.this.panel.getTablePanel(TableAttributeTab.this.getSelectedTables()[i]).sortSelectedFeatures(false);
                        ++i;
                    }
                    TableAttributeTab.this.setLayerViewPanelUpdates(true);
                }
                catch (Throwable t) {
                    TableAttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.tablesEnableCheck).add(this.rowsSelectedEnableCheck));
        this.excelFileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        FileNameExtensionFilter excelFilter = new FileNameExtensionFilter(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Excel-files"), "xls");
        this.excelFileChooser.setFileFilter(excelFilter);
        this.toolBar.add(new JButton(), I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Export-to-Excel-sheet"), GUIUtil.toSmallIcon(IconLoader.icon("exportToExcel.png")), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                TableAttributeTab.this.saveTableToExcelSheet();
            }
        }, new MultiEnableCheck());
        this.calcFileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        FileNameExtensionFilter calcFilter = new FileNameExtensionFilter(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Calc-files"), "ods");
        this.calcFileChooser.setFileFilter(calcFilter);
        this.toolBar.add(new JButton(), I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Export-to-Calc-sheet"), GUIUtil.toSmallIcon(IconLoader.icon("exportToCalc.png")), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                TableAttributeTab.this.saveTableToCalcSheet();
            }
        }, new MultiEnableCheck());
    }

    protected void saveTableToExcelSheet() {
        this.excelFileChooser.showSaveDialog(JUMPWorkbench.getFrameInstance());
        File file = this.excelFileChooser.getSelectedFile();
        if (file == null) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Operation-cancelled-by-the-user"));
            return;
        }
        String path = file.getPath();
        if (!path.endsWith(".xls")) {
            path = path.concat(".xls");
        }
        final String filePath = path;
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        progressDialog.setTitle(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Export-to-Excel-sheet"));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            try {
                                progressDialog.report(I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTab.Exporting-the-table-to-the-Excel-sheet-at-{0}", new Object[]{filePath}));
                                ExportUtils.exportTableToExcelSpreadsheet(TableAttributeTab.this.getTable(), filePath, true);
                                JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTab.Excel-file-{0}-successfully-generated", new Object[]{filePath}));
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

    protected void saveTableToCalcSheet() {
        this.calcFileChooser.showSaveDialog(JUMPWorkbench.getFrameInstance());
        File file = this.calcFileChooser.getSelectedFile();
        if (file == null) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Operation-cancelled-by-the-user"));
            return;
        }
        String path = file.getPath();
        if (!path.endsWith(".ods")) {
            path = path.concat(".ods");
        }
        final String filePath = path;
        try {
            OpenOfficeLibLoader.loadLibs();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            JUMPWorkbench.getFrameInstance().warnUser(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.The-OpenOffice-libraries-could-not-be-loaded")) + ": " + e.getMessage());
            return;
        }
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        progressDialog.setTitle(I18N.getString("org.saig.core.model.data.widgets.TableAttributeTab.Export-to-Calc-sheet"));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            try {
                                progressDialog.report(I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTab.Exporting-the-table-to-the-Calc-sheet-at-{0}", new Object[]{filePath}));
                                ExportUtils.exportTableToCalcSpreadsheet(TableAttributeTab.this.getTable(), filePath, true);
                                JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTab.Calc-file-{0}-successfully-generated", new Object[]{filePath}));
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
        if (StringUtils.isNotEmpty((String)progressDialog.getExceptionMessage())) {
            JUMPWorkbench.getFrameInstance().warnUser(progressDialog.getExceptionMessage());
        }
    }

    void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        this.toolBar.setOrientation(0);
        this.scrollPane.getViewport().add((Component)this.panel, null);
        this.add((Component)this.scrollPane, "Center");
        this.add((Component)this.toolBar, "North");
    }

    private void initScrollPane() {
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(new JTable().getRowHeight());
    }

    private static void setEnableLastSelectedTables(boolean enabled, TableAttributeTab attributeTab) {
        attributeTab.selectedTables = enabled ? attributeTab.lastSelectedTables : new Table[]{};
    }

    public Table[] getSelectedTables() {
        if (this.model.getTable() != null) {
            return new Table[]{this.model.getTable()};
        }
        return this.selectedTables;
    }

    public void selectRecords(Collection<Record> col, Table table) {
        this.panel.selectRecords(col, table);
    }

    public TableAttributePanel getPanel() {
        return this.panel;
    }

    public EnableableToolBar getToolBar() {
        return this.toolBar;
    }

    public void dispose() {
        this.lastRightClickTable = null;
    }

    public void setLayerViewPanelUpdates(boolean b) {
        this.layerViewPanelUpdates = b;
    }

    public boolean isLayerViewPanelUpdates() {
        return this.layerViewPanelUpdates;
    }

    public void explicitSort(boolean ascending) {
        TableAttributeTablePanel atp = this.panel.getTablePanel(this.lastRightClickTable);
        if (atp != null) {
            atp.explicitSortRight(ascending);
        }
    }

    public Table getLastRightClickTable() {
        return this.lastRightClickTable;
    }

    public String getLastRightClickColumnName() {
        TableAttributeTablePanel atp = this.panel.getTablePanel(this.lastRightClickTable);
        if (atp != null) {
            return atp.getLastRightClickColumnName();
        }
        return null;
    }

    public static EnableCheck createNotGeometryRightClickEnableCheck(final WorkbenchContext workbenchContext) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanelProxy panelProxy = (LayerNamePanelProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame());
                AttributeTab attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
                if (attributeTab.getLastRightClickLayer() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-table");
                }
                String columnName = attributeTab.getLastRightClickColumnName();
                if (columnName == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-column");
                }
                if (columnName.equals("....")) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.it-is-not-applicable-over-geometric-field");
                }
                return null;
            }
        };
    }

    public static EnableCheck createNotEditableRightClickEnableCheck(final WorkbenchContext workbenchContext) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanelProxy panelProxy = (LayerNamePanelProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame());
                AttributeTab attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
                if (attributeTab.getLastRightClickLayer() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-table");
                }
                if (attributeTab.getLastRightClickLayer().isEditable()) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.layer-must-not-be-in-edition");
                }
                return null;
            }
        };
    }

    public void updateRelationSelection(Table table, Collection<Record> selectedRecords) {
        Collection<Relation<?>> relations = table.getAllRelations();
        if (CollectionUtils.isNotEmpty(relations)) {
            this.panel.updateRelationSelection(table, relations, selectedRecords, true);
        }
    }

    public TableAttributeTab setTable(Table table) {
        if (this.getModel().getTable() != null) {
            this.getModel().clear();
        }
        this.getModel().addKeys(table, table.getKeys());
        return this;
    }

    public Table getTable() {
        return this.getTableTableModel() != null ? this.getTableTableModel().getTable() : null;
    }

    public TableTableModel getTableTableModel() {
        return this.getModel().getTableTableModel();
    }

    private class InverseSelectionWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        InverseSelectionWaitDialog(JFrame parent, boolean modal) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("workbench.ui.AttributeTab.inverse")) + " ...");
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        TableAttributeTab.this.panel.inverseSelection();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        InverseSelectionWaitDialog.this.dispose();
                    }
                    return null;
                }

                @Override
                public void finished() {
                    InverseSelectionWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        void closeWindow() {
            this.dispose();
        }
    }
}

