/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.DataPanelTable;
import org.saig.core.model.data.widgets.RecordSchemaTableModel;
import org.saig.core.model.data.widgets.RecordTableModel;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;

public class DataPanel
extends JPanel
implements TableModelListener {
    private static final Logger LOGGER = Logger.getLogger(DataPanel.class);
    private Table recordCollection;
    private DataPanelTable table;
    private RecordSchemaTableModel model;

    public DataPanel(Table recordCollection) {
        super(new BorderLayout());
        this.recordCollection = recordCollection;
        this.model = new RecordSchemaTableModel(recordCollection);
        this.table = new DataPanelTable(this.model);
        TableCellRenderer headerRenderer = new TableCellRenderer(){
            private Icon clearIcon = IconLoader.icon("Clear.gif");
            private Icon downIcon = IconLoader.icon("Down.gif");
            private TableCellRenderer originalRenderer;
            private Icon upIcon;
            private Icon keyIcon;
            private Icon keyUpIcon;
            private Icon keyDownIcon;
            {
                this.originalRenderer = DataPanel.this.table.getTableHeader().getDefaultRenderer();
                this.upIcon = IconLoader.icon("Up.gif");
                this.keyIcon = IconLoader.icon("key2.gif");
                this.keyUpIcon = IconLoader.icon("key2_up.gif");
                this.keyDownIcon = IconLoader.icon("key2_down.gif");
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel)this.originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(3));
                boolean isKey = DataPanel.this.getModel().isPKName(label.getText());
                if (DataPanel.this.model.getSortedColumnName() == null || !DataPanel.this.model.getSortedColumnName().equals(table.getColumnName(column))) {
                    if (isKey) {
                        label.setIcon(this.keyIcon);
                    } else {
                        label.setIcon(this.clearIcon);
                    }
                } else if (DataPanel.this.model.isSortAscending()) {
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
        this.table.getTableHeader().setDefaultRenderer(headerRenderer);
        this.table.getTableHeader().addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int column = DataPanel.this.table.columnAtPoint(e.getPoint());
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        new SortWaitDialog(JUMPWorkbench.getFrameInstance(), true, DataPanel.this.model, DataPanel.this.table, column).setVisible(true);
                    }
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        this.table.getModel().addTableModelListener(this);
        GUIUtil.chooseGoodColumnWidths(this.table);
        this.add((Component)this.table, "Center");
    }

    public void addSelectionListener(ListSelectionListener selectionListener) {
        this.table.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void removeSelectionListener(ListSelectionListener selectionListener) {
        this.table.getSelectionModel().removeListSelectionListener(selectionListener);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        if (row == -1) {
            ((RecordTableModel)this.table.getModel()).initialize();
        } else {
            int column = e.getColumn();
            if (column == -1) {
                return;
            }
            RecordTableModel model = (RecordTableModel)this.table.getModel();
            String columnName = model.getColumnName(column);
            Object data = model.getValueAt(row, column);
            int index = model.getPrimaryKeyIndex();
            Object key = model.getValueAt(row, index);
            Record record = this.recordCollection.getByPrimaryKey(key);
            record.setAttribute(columnName, data);
        }
    }

    public void refresh() {
        ((RecordSchemaTableModel)this.table.getModel()).initialize();
        this.table.getSelectionModel().clearSelection();
        this.table.repaint();
    }

    public int getTableSize() {
        return this.table.getTableHeader().getPreferredSize().width;
    }

    public JTableHeader getTableHeather() {
        return this.table.getTableHeader();
    }

    public String getTitle() {
        return String.valueOf(this.recordCollection.getName()) + ":" + this.recordCollection.size() + " " + I18N.getString("org.saig.core.model.data.widgets.DataPanel.elements");
    }

    public void moveRowUp(Object value) {
        if (this.table.getSelectedRow() > 0) {
            this.model.moveRowUp(value);
            this.table.changeSelection(this.table.getSelectedRow() - 1, 1, false, false);
            this.table.repaint();
            this.table.getTableHeader().repaint();
        }
    }

    public void moveRowDown(Object value) {
        if (this.table.getSelectedRow() < this.table.getRowCount() - 1) {
            this.model.moveRowDown(value);
            this.table.changeSelection(this.table.getSelectedRow() + 1, 1, false, false);
            this.table.repaint();
            this.table.getTableHeader().repaint();
        }
    }

    public RecordSchemaTableModel getModel() {
        return this.model;
    }

    public List<Record> getSelectedRecords() {
        return this.model.getSelectedRecords(this.table.getSelectedRows());
    }

    public void clearSelection() {
        this.table.clearSelection();
    }

    public int getSelectedRow() {
        return this.table.getSelectedRow();
    }

    public int getRowCount() {
        return this.table.getRowCount();
    }

    public Record getRow(int row) {
        return (Record)this.model.getRecordValueAt(row);
    }

    public Collection<Record> getAllRecords() {
        return this.model.getAllRecords();
    }

    public JTable getTable() {
        return this.table;
    }

    private class SortWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        SortWaitDialog(JFrame parent, boolean modal, final RecordSchemaTableModel model, final DataPanelTable table, final int column) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(I18N.getString("org.saig.core.model.data.widgets.DataPanel.sorting-rows"));
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
                        model.sort(table.getColumnName(column));
                        table.repaint();
                        table.getTableHeader().repaint();
                        return model;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
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

