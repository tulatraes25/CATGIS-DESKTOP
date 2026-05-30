/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.PString;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;

public abstract class AbstractEntityTableColumnModel
extends DefaultTableColumnModel
implements EntityTableColumnModel {
    private static final long serialVersionUID = 1L;

    @Override
    public abstract Object getCellValue(int var1, Object var2);

    @Override
    public abstract void setCellValue(int var1, Object var2, Object var3);

    @Override
    public Class<?> getColumnClass(int col) {
        if (col >= 0 && col < this.getColumnCount()) {
            return ((EntityTableColumn)this.getColumn(col)).getColumnClass();
        }
        return null;
    }

    @Override
    public boolean isLocked(int col) {
        return ((EntityTableColumn)this.getColumn(col)).isLocked();
    }

    @Override
    public void setLocked(int col, boolean newValue) {
        ((EntityTableColumn)this.getColumn(col)).setLocked(newValue);
    }

    @Override
    public boolean isSortable(int col) {
        return ((EntityTableColumn)this.getColumn(col)).isSortable();
    }

    @Override
    public void setSortable(int col, boolean newValue) {
        ((EntityTableColumn)this.getColumn(col)).setSortable(newValue);
    }

    @Override
    public boolean isRightAlignmentSortable(int col) {
        return ((EntityTableColumn)this.getColumn(col)).isRightAlignmentSortable();
    }

    @Override
    public void setRightAlignmentSortable(int col, boolean newValue) {
        ((EntityTableColumn)this.getColumn(col)).setRightAlignmentSortable(newValue);
    }

    @Override
    public boolean isHidden(int col) {
        return ((EntityTableColumn)this.getColumn(col)).isHidden();
    }

    @Override
    public void setHidden(int col, boolean newValue) {
        ((EntityTableColumn)this.getColumn(col)).setHidden(newValue);
        this.fireColumnRemoved(new TableColumnModelEvent(this, col, col));
    }

    @Override
    public boolean isEditable(int col) {
        return ((EntityTableColumn)this.getColumn(col)).isEditable();
    }

    @Override
    public void setEditable(int col, boolean newValue) {
        ((EntityTableColumn)this.getColumn(col)).setEditable(newValue);
    }

    @Override
    public String getColumnName(int col) {
        return ((EntityTableColumn)this.getColumn(col)).getName();
    }

    @Override
    public void addColumn(EntityTableColumn c) {
        super.addColumn(c);
    }

    @Override
    public void removeColumn(int col) {
        super.removeColumn(this.getColumn(col));
    }

    @Override
    public EntityTableColumn getByName(String name) {
        EntityTableColumn retVal = null;
        int i = 0;
        while (i < this.getColumnCount()) {
            EntityTableColumn colModel = (EntityTableColumn)this.getColumn(i);
            if (PString.areEqual(colModel.getName(), name)) {
                retVal = colModel;
                break;
            }
            ++i;
        }
        return retVal;
    }

    @Override
    public int getRealColumnCount() {
        int retVal = this.getColumnCount();
        int i = 0;
        while (i < this.getColumnCount()) {
            if (this.isHidden(i)) {
                --retVal;
            }
            ++i;
        }
        return retVal;
    }

    @Override
    public boolean hasEditableColumns() {
        boolean retVal = false;
        int i = 0;
        while (i < this.getColumnCount()) {
            if (this.isEditable(i)) {
                retVal = true;
                break;
            }
            ++i;
        }
        return retVal;
    }
}

