/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.ProxyColumnModel;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class RowHeaderColumnModel
extends ProxyColumnModel
implements TableColumnModel,
TableColumnModelListener {
    private static final long serialVersionUID = 1L;
    private TableColumn columnHeader = new TableColumn();
    private boolean virtualColumn = false;

    public RowHeaderColumnModel() {
        this.columnHeader.setModelIndex(0);
        this.columnHeader.setCellRenderer(new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            JButton renderer = new JButton();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                this.renderer.setText("");
                if (value instanceof String && value != null) {
                    this.renderer.setText((String)value);
                    this.renderer.setFont(this.renderer.getFont().deriveFont(1));
                    this.renderer.setMargin(new Insets(1, 1, 1, 1));
                }
                return this.renderer;
            }
        });
        this.columnHeader.setWidth(30);
        this.columnHeader.setPreferredWidth(30);
    }

    public RowHeaderColumnModel(EntityTableColumnModel m) {
        this();
        this.setModel(m);
    }

    @Override
    public void setModel(EntityTableColumnModel m) {
        if (this.origModel != null) {
            while (this.getColumnCount() > 0) {
                this.removeColumn(this.getColumn(0));
            }
            this.origModel.removeColumnModelListener(this);
        }
        this.origModel = m;
        if (this.origModel != null) {
            this.origModel.addColumnModelListener(this);
            int colOffset = 0;
            if (this.virtualColumn) {
                this.addColumn(this.columnHeader);
                ++colOffset;
            }
            int j = 0;
            int i = 0;
            while (i < this.origModel.getColumnCount()) {
                if (this.origModel.isLocked(i) && !this.origModel.isHidden(i)) {
                    this.addColumn(this.origModel.getColumn(i));
                    this.getColumn(j + colOffset).setModelIndex(i + colOffset);
                    this.getColumn(j + colOffset).setHeaderValue(((EntityTableColumn)this.origModel.getColumn(i)).getName());
                    this.getColumn(j + colOffset).setHeaderRenderer(((EntityTableColumn)this.origModel.getColumn(i)).getHeaderRenderer());
                    ++j;
                }
                ++i;
            }
        }
    }

    public boolean isVirtualColumnEnabled() {
        return this.virtualColumn;
    }

    public void setVirtualColumnEnabled(boolean v) {
        if (this.virtualColumn == v) {
            return;
        }
        this.virtualColumn = v;
        if (this.origModel != null) {
            if (this.virtualColumn) {
                this.addColumn(this.columnHeader);
                this.moveColumn(this.getColumnCount() - 1, 0);
            } else {
                this.removeColumn(this.columnHeader);
            }
        }
    }

    @Override
    protected int getRealColumnCount() {
        int retVal = this.getColumnCount();
        if (this.virtualColumn) {
            --retVal;
        }
        return retVal;
    }
}

