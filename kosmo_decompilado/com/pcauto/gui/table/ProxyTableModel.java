/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListEvent;
import com.pcauto.gui.table.EntityListListener;
import com.pcauto.gui.table.EntityTableColumnModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

class ProxyTableModel
extends AbstractTableModel
implements TableModel,
EntityListListener {
    private static final long serialVersionUID = 1L;
    protected EntityList entityList = null;
    protected EntityTableColumnModel columnModel = null;
    protected Object virtualEntity = null;
    protected boolean readOnly = false;
    protected boolean virtualRow = false;
    private boolean cellEditing = true;

    public ProxyTableModel() {
    }

    public ProxyTableModel(EntityList e, EntityTableColumnModel c) {
        this.entityList = e;
        this.columnModel = c;
    }

    public EntityList getEntityList() {
        return this.entityList;
    }

    public void setEntityList(EntityList e) {
        if (this.entityList != null) {
            this.entityList.removeEntityListListener(this);
        }
        this.entityList = e;
        this.entityList.addEntityListListener(this);
        super.fireTableDataChanged();
    }

    public EntityTableColumnModel getColumnModel() {
        return this.columnModel;
    }

    public void setColumnModel(EntityTableColumnModel c) {
        if (this.columnModel == c) {
            return;
        }
        this.columnModel = c;
        super.fireTableStructureChanged();
    }

    public boolean isVirtualRowEnabled() {
        return this.virtualRow;
    }

    public void setVirtualRowEnabled(boolean v) {
        if (this.virtualRow == v) {
            return;
        }
        this.virtualRow = v;
        super.fireTableDataChanged();
    }

    public boolean isCellEditingEnabled() {
        return this.cellEditing;
    }

    public void setCellEditingEnabled(boolean b) {
        this.cellEditing = b;
    }

    public Object getVirtualEntity() {
        if (this.virtualEntity == null && this.entityList != null) {
            this.virtualEntity = this.entityList.getNewEntity();
            int i = 0;
            while (i < this.getColumnCount()) {
                this.columnModel.setCellValue(i, this.virtualEntity, this.columnModel.getCellValue(i, this.entityList.getDefaultEntity()));
                ++i;
            }
        }
        return this.virtualEntity;
    }

    public void setVirtualEntity(Object v) {
        this.virtualEntity = v;
    }

    @Override
    public boolean isCellEditable(int p1, int col) {
        if (this.readOnly) {
            return false;
        }
        if (!this.cellEditing) {
            return false;
        }
        if (this.columnModel == null) {
            return false;
        }
        return this.columnModel.isEditable(col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (this.entityList == null || this.columnModel == null) {
            return null;
        }
        Object entity = null;
        entity = row >= 0 && row < this.entityList.getCount() ? this.entityList.getEntity(row) : this.getVirtualEntity();
        return this.columnModel.getCellValue(col, entity);
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        if (this.entityList == null || this.columnModel == null) {
            return;
        }
        Object entity = null;
        if (row >= 0 && row < this.entityList.getCount()) {
            entity = this.entityList.getEntity(row);
        } else if (row == this.entityList.getCount()) {
            entity = this.getVirtualEntity();
        }
        this.columnModel.setCellValue(col, entity, val);
        this.fireTableRowsUpdated(row, row);
    }

    @Override
    public int getRowCount() {
        int retVal = 0;
        if (this.entityList != null) {
            retVal = this.entityList.getCount();
        }
        if (this.virtualRow) {
            ++retVal;
        }
        return retVal;
    }

    @Override
    public int getColumnCount() {
        int retVal = 0;
        if (this.columnModel != null) {
            retVal = this.columnModel.getColumnCount();
        }
        return retVal;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return this.columnModel.getColumnClass(col);
    }

    @Override
    public String getColumnName(int col) {
        return this.columnModel.getColumnName(col);
    }

    public void refresh() {
        super.fireTableStructureChanged();
        super.fireTableDataChanged();
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean r) {
        this.readOnly = r;
        super.fireTableStructureChanged();
    }

    @Override
    public void listChanged(EntityListEvent e) {
        switch (e.getType()) {
            case 0: {
                super.fireTableRowsInserted(e.getFirstEntity(), e.getLastEntity());
                break;
            }
            case 1: {
                super.fireTableRowsDeleted(e.getFirstEntity(), e.getLastEntity());
                break;
            }
            case 2: {
                super.fireTableRowsUpdated(e.getFirstEntity(), e.getLastEntity());
                break;
            }
            default: {
                super.fireTableDataChanged();
            }
        }
    }

    public void revalidate() {
        this.setEntityList(this.entityList);
        this.setColumnModel(this.columnModel);
        super.fireTableStructureChanged();
        super.fireTableDataChanged();
    }
}

