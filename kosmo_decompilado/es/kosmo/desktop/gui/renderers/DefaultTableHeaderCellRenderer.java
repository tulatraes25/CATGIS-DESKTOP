/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.renderers;

import java.awt.Component;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public class DefaultTableHeaderCellRenderer
extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    public DefaultTableHeaderCellRenderer() {
        this.setHorizontalAlignment(0);
        this.setHorizontalTextPosition(2);
        this.setVerticalAlignment(3);
        this.setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JTableHeader tableHeader = table.getTableHeader();
        if (tableHeader != null) {
            this.setForeground(tableHeader.getForeground());
        }
        this.setIcon(this.getIcon(table, column));
        this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return this;
    }

    protected Icon getIcon(JTable table, int column) {
        RowSorter.SortKey sortKey = this.getSortKey(table, column);
        if (sortKey != null && table.convertColumnIndexToView(sortKey.getColumn()) == column) {
            switch (sortKey.getSortOrder()) {
                case ASCENDING: {
                    return UIManager.getIcon("Table.ascendingSortIcon");
                }
                case DESCENDING: {
                    return UIManager.getIcon("Table.descendingSortIcon");
                }
            }
            return null;
        }
        return null;
    }

    protected RowSorter.SortKey getSortKey(JTable table, int column) {
        RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
        if (rowSorter == null) {
            return null;
        }
        List<RowSorter.SortKey> sortedColumns = rowSorter.getSortKeys();
        if (sortedColumns.size() > 0) {
            return sortedColumns.get(0);
        }
        return null;
    }
}

