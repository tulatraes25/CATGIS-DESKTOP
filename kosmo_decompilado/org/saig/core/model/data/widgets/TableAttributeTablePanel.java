/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooserCellEditor
 */
package org.saig.core.model.data.widgets;

import com.toedter.calendar.JDateChooserCellEditor;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.TableAttributePanel;
import org.saig.core.model.data.widgets.TableAttributeTab;
import org.saig.core.model.data.widgets.TableAttributeTablePanelListener;
import org.saig.core.model.data.widgets.TableTableModel;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DateNumberCellRenderer;

public class TableAttributeTablePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Icon TABLE_ICON = IconLoader.icon("Table.gif");
    private boolean columnWidthsInitialized = false;
    private int rightClickColumn;
    private MyTable guiTable = new MyTable();
    private TableCellRenderer headerRenderer = new TableCellRenderer(){
        private Icon clearIcon = IconLoader.icon("Clear.gif");
        private Icon downIcon = IconLoader.icon("Down.gif");
        private TableCellRenderer originalRenderer;
        private Icon upIcon;
        private Icon keyIcon;
        private Icon keyUpIcon;
        private Icon keyDownIcon;
        {
            this.originalRenderer = TableAttributeTablePanel.this.guiTable.getTableHeader().getDefaultRenderer();
            this.upIcon = IconLoader.icon("Up.gif");
            this.keyIcon = IconLoader.icon("key2.gif");
            this.keyUpIcon = IconLoader.icon("key2_up.gif");
            this.keyDownIcon = IconLoader.icon("key2_down.gif");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel)this.originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(label.getFont().deriveFont(3));
            boolean isKey = TableAttributeTablePanel.this.getModel().isPKName(label.getText());
            String sortedColumnName = TableAttributeTablePanel.this.getModel().getSortedColumnName();
            Attribute attr = null;
            if (sortedColumnName != null) {
                attr = TableAttributeTablePanel.this.getModel().getTable().getSchema().getAttribute(sortedColumnName);
            }
            if (attr == null || !attr.getPublicName().equals(table.getColumnName(column))) {
                if (isKey) {
                    label.setIcon(this.keyIcon);
                } else {
                    label.setIcon(this.clearIcon);
                }
            } else if (TableAttributeTablePanel.this.getModel().isSortAscending()) {
                if (isKey) {
                    label.setIcon(this.keyUpIcon);
                } else {
                    label.setIcon(this.upIcon);
                }
            } else if (isKey) {
                label.setIcon(this.keyDownIcon);
            } else {
                label.setIcon(this.downIcon);
            }
            label.setHorizontalTextPosition(2);
            label.setHorizontalAlignment(0);
            return label;
        }
    };
    private LayerNameRenderer layerNameRenderer = new LayerNameRenderer();
    private List<TableAttributeTablePanelListener> listeners = new ArrayList<TableAttributeTablePanelListener>();
    private WorkbenchContext workbenchContext;
    private TableAttributePanel panel;

    public Dimension getTableSize() {
        return this.guiTable.getTableHeader().getPreferredSize();
    }

    public TableAttributeTablePanel(final TableTableModel model, boolean addScrollPane, WorkbenchContext workbenchContext, TableAttributePanel panel) {
        this();
        this.panel = panel;
        if (addScrollPane) {
            this.remove(this.guiTable);
            this.remove(this.guiTable.getTableHeader());
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setMinimumSize(new Dimension(300, 200));
            scrollPane.setPreferredSize(new Dimension(300, 200));
            scrollPane.getViewport().add(this.guiTable);
            this.add((Component)scrollPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        }
        this.updateGrid(model.getTable());
        try {
            JList list = new JList();
            list.setBackground(new JLabel().getBackground());
            this.guiTable.setModel(model);
            model.addTableModelListener(new TableModelListener(){

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getFirstRow() == -1) {
                        TableAttributeTablePanel.this.initColumnWidths();
                    }
                }
            });
            this.layerNameRenderer.getLabel().setFont(this.layerNameRenderer.getLabel().getFont().deriveFont(1));
            model.addTableModelListener(new TableModelListener(){

                @Override
                public void tableChanged(TableModelEvent e) {
                    TableAttributeTablePanel.this.updateLabel();
                }
            });
            this.workbenchContext = workbenchContext;
            this.guiTable.setSelectionModel(new SelectionModelWrapper(this));
            this.guiTable.getTableHeader().setDefaultRenderer(this.headerRenderer);
            this.initColumnWidths();
            this.setToolTips();
            this.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.BLUE));
            this.guiTable.addKeyListener(new KeyAdapter(){

                @Override
                public void keyPressed(KeyEvent e) {
                    if (model.getTable().getDataSource().isEditable() && e.getKeyCode() == 127) {
                        int row = TableAttributeTablePanel.this.guiTable.getSelectedRow();
                        int column = TableAttributeTablePanel.this.guiTable.getSelectedColumn();
                        TableAttributeTablePanel.this.guiTable.getModel().setValueAt("", row, column);
                    }
                }
            });
            this.updateLabel();
        }
        catch (Throwable t) {
            workbenchContext.getErrorHandler().handleThrowable(t);
        }
    }

    private TableAttributeTablePanel() {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateGrid(Table table) {
        this.guiTable.setShowGrid(false);
    }

    public void updateLabel() {
        this.layerNameRenderer.getIconLabel().setIcon(TABLE_ICON);
        this.layerNameRenderer.getLabel().setText(this.getModel().getRowCount() == 1 ? I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTablePanel.{0}-1-records-1-selected", new Object[]{this.getModel().getTable().getName()}) : I18N.getMessage("org.saig.core.model.data.widgets.TableAttributeTablePanel.{0}-{1}-records-{2}-selected", new Object[]{this.getModel().getTable().getName(), new Integer(this.getModel().getRowCount()), new Integer(this.getTable().getSelectedRowCount())}));
    }

    public TableTableModel getModel() {
        return (TableTableModel)this.guiTable.getModel();
    }

    public JTable getTable() {
        return this.guiTable;
    }

    public void addListener(TableAttributeTablePanelListener listener) {
        this.listeners.add(listener);
    }

    void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
        this.add((Component)this.layerNameRenderer, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, 18, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.guiTable.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.guiTable, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 10, 1, new Insets(0, 0, 0, 200), 0, 0));
    }

    private void initColumnWidths() {
        GUIUtil.chooseGoodColumnWidths(this.guiTable);
        this.columnWidthsInitialized = true;
    }

    private void setToolTips() {
        this.guiTable.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseMoved(MouseEvent e) {
                int column = TableAttributeTablePanel.this.guiTable.columnAtPoint(e.getPoint());
                if (column == -1) {
                    return;
                }
                int row = TableAttributeTablePanel.this.guiTable.rowAtPoint(e.getPoint());
                if (row == -1) {
                    return;
                }
                Object obj = TableAttributeTablePanel.this.guiTable.getValueAt(row, column);
                StringBuilder toolTipSB = new StringBuilder("<HTML><b>");
                toolTipSB.append(TableAttributeTablePanel.this.guiTable.getColumnName(column));
                toolTipSB.append(" [");
                toolTipSB.append(TableAttributeTablePanel.this.getModel().getTable().getName());
                toolTipSB.append("]</b><br>");
                if (obj != null) {
                    toolTipSB.append("<pre>");
                    toolTipSB.append(obj.toString());
                    toolTipSB.append("</pre>");
                } else {
                    toolTipSB.append("<i><b>NULL</i></b>");
                }
                toolTipSB.append("</HTML>");
                TableAttributeTablePanel.this.guiTable.setToolTipText(StringUtil.formatTooltip(toolTipSB.toString(), 200, 15));
            }
        });
    }

    private void fireSelectionReplaced() {
        for (TableAttributeTablePanelListener listener : this.listeners) {
            listener.selectionReplaced(this);
        }
        this.updateLabel();
    }

    public Collection<Record> getSelectedRecords() {
        int[] rows = this.guiTable.getSelectedRows();
        TableTableModel model = (TableTableModel)this.guiTable.getModel();
        ArrayList<Object> realKeys = new ArrayList<Object>();
        ArrayList<Record> memoKeys = new ArrayList<Record>();
        int i = 0;
        while (i < rows.length) {
            Object k = model.getKey(rows[i]);
            if (k instanceof Record) {
                memoKeys.add((Record)k);
            } else {
                realKeys.add(k);
            }
            ++i;
        }
        ArrayList<Record> result = new ArrayList<Record>();
        if (!realKeys.isEmpty()) {
            result.addAll(model.getTable().getByPrimaryKeys(realKeys.toArray()));
        }
        result.addAll(memoKeys);
        return result;
    }

    public LayerNameRenderer getLayerNameRenderer() {
        return this.layerNameRenderer;
    }

    public void sortSelectedFeatures(boolean ascending) {
        TableTableModel model = (TableTableModel)this.guiTable.getModel();
        int[] rows = this.guiTable.getSelectedRows();
        if (model.getRowCount() == rows.length) {
            return;
        }
        Object[] keys = new Object[rows.length];
        int i = 0;
        while (i < keys.length) {
            keys[i] = model.getKey(rows[i]);
            ++i;
        }
        List<Record> selectedRecords = model.getTable().getByPrimaryKeys(keys);
        model.sortSelectedRows(ascending, rows);
        this.panel.selectRecords(selectedRecords, model.getTable());
        this.repaint();
    }

    public TableAttributePanel getPanel() {
        return this.panel;
    }

    public void setRightClickColumn(int column) {
        this.rightClickColumn = column;
    }

    public void explicitSortRight(boolean ascending) {
        try {
            TableTableModel model = (TableTableModel)this.guiTable.getModel();
            if (this.rightClickColumn == -1) {
                return;
            }
            Attribute att = model.getTable().getSchema().getAttribute(this.guiTable.getColumnName(this.rightClickColumn));
            ((TableAttributeTab)this.panel.getParent()).setLayerViewPanelUpdates(false);
            Collection<Record> col = this.getSelectedRecords();
            new SortWaitDialog(JUMPWorkbench.getFrameInstance(), true, model, this.guiTable, this.rightClickColumn, new Boolean(ascending)).setVisible(true);
            this.panel.selectRecords(col, model.getTable());
            ((TableAttributeTab)this.panel.getParent()).setLayerViewPanelUpdates(true);
        }
        catch (Throwable t) {
            this.workbenchContext.getErrorHandler().handleThrowable(t);
        }
    }

    public String getLastRightClickColumnName() {
        return this.guiTable.getColumnName(this.rightClickColumn);
    }

    private class MyTable
    extends JTable {
        private static final long serialVersionUID = 1L;
        private final Color LIGHT_GRAY = new Color(230, 230, 230);
        private DateNumberCellRenderer myTableCellRenderer = new DateNumberCellRenderer();

        public MyTable() {
            this.setAutoResizeMode(0);
            GUIUtil.doNotRoundDoubles(this);
            this.setDefaultEditor(Date.class, (TableCellEditor)new JDateChooserCellEditor());
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            Object valor;
            Class<?> columnClass = this.getModel().getColumnClass(column);
            if (columnClass != null && columnClass.equals(Boolean.class) && (valor = this.getModel().getValueAt(row, column)) != null && valor instanceof Boolean) {
                return new TableCellRenderer(){

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = table.getDefaultRenderer(Boolean.class).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (!table.isRowSelected(row)) {
                            comp.setBackground(row % 2 == 0 ? Color.white : MyTable.this.LIGHT_GRAY);
                        }
                        return comp;
                    }
                };
            }
            DateNumberCellRenderer renderer = this.myTableCellRenderer;
            if (this.isRowSelected(row)) {
                ((JComponent)renderer).setBackground(Color.YELLOW);
            } else {
                ((JComponent)renderer).setBackground(row % 2 == 0 ? Color.white : this.LIGHT_GRAY);
            }
            return renderer;
        }
    }

    private static class SelectionModelWrapper
    implements ListSelectionModel {
        private TableAttributeTablePanel panel;
        private ListSelectionModel selectionModel;

        public SelectionModelWrapper(TableAttributeTablePanel panel) {
            this.panel = panel;
            this.selectionModel = panel.guiTable.getSelectionModel();
        }

        @Override
        public void setAnchorSelectionIndex(int index) {
            this.selectionModel.setAnchorSelectionIndex(index);
        }

        @Override
        public void setLeadSelectionIndex(int index) {
            this.selectionModel.setLeadSelectionIndex(index);
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            this.selectionModel.setSelectionInterval(index0, index1);
            this.panel.fireSelectionReplaced();
        }

        @Override
        public void setSelectionMode(int selectionMode) {
            this.selectionModel.setSelectionMode(selectionMode);
        }

        @Override
        public void setValueIsAdjusting(boolean valueIsAdjusting) {
            this.selectionModel.setValueIsAdjusting(valueIsAdjusting);
        }

        @Override
        public int getAnchorSelectionIndex() {
            return this.selectionModel.getAnchorSelectionIndex();
        }

        @Override
        public int getLeadSelectionIndex() {
            return this.selectionModel.getLeadSelectionIndex();
        }

        @Override
        public int getMaxSelectionIndex() {
            return this.selectionModel.getMaxSelectionIndex();
        }

        @Override
        public int getMinSelectionIndex() {
            return this.selectionModel.getMinSelectionIndex();
        }

        @Override
        public int getSelectionMode() {
            return this.selectionModel.getSelectionMode();
        }

        @Override
        public boolean getValueIsAdjusting() {
            return this.selectionModel.getValueIsAdjusting();
        }

        @Override
        public boolean isSelectedIndex(int index) {
            return this.selectionModel.isSelectedIndex(index);
        }

        @Override
        public boolean isSelectionEmpty() {
            return this.selectionModel.isSelectionEmpty();
        }

        @Override
        public void addListSelectionListener(ListSelectionListener x) {
            this.selectionModel.addListSelectionListener(x);
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            this.selectionModel.addSelectionInterval(index0, index1);
        }

        @Override
        public void clearSelection() {
            this.selectionModel.clearSelection();
        }

        @Override
        public void insertIndexInterval(int index, int length, boolean before) {
            this.selectionModel.insertIndexInterval(index, length, before);
        }

        @Override
        public void removeIndexInterval(int index0, int index1) {
            this.selectionModel.removeIndexInterval(index0, index1);
        }

        @Override
        public void removeListSelectionListener(ListSelectionListener x) {
            this.selectionModel.removeListSelectionListener(x);
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
            this.selectionModel.removeSelectionInterval(index0, index1);
        }
    }

    private class SortWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        SortWaitDialog(JFrame parent, boolean modal, final TableTableModel model, final JTable table, final int column, final Boolean ascending) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("workbench.ui.AttributeTablePanel.sorting-rows")) + " ...");
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
                        if (ascending == null) {
                            model.sort(table.getColumnName(column));
                        } else {
                            model.explicitSort(table.getColumnName(TableAttributeTablePanel.this.rightClickColumn), ascending);
                        }
                        table.repaint();
                        table.getTableHeader().repaint();
                        return model;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        SortWaitDialog.this.dispose();
                        return null;
                    }
                }

                @Override
                public void finished() {
                    SortWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        void closeWindow() {
            this.dispose();
        }
    }
}

