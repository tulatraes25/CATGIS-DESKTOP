/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListListener;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.OrderTranslatorEntityList;
import com.pcauto.gui.table.ProxyTableModel;
import javax.swing.table.TableModel;

public class RowHeaderModel
extends ProxyTableModel
implements TableModel,
EntityListListener {
    private static final long serialVersionUID = 1L;
    private ProxyTableModel mainTableModel = null;
    private boolean virtualColumn = false;
    private boolean rowNumbers = false;
    private boolean entityIndex = false;

    public RowHeaderModel() {
    }

    public RowHeaderModel(EntityList e, EntityTableColumnModel c) {
        this.entityList = e;
        this.columnModel = c;
    }

    public RowHeaderModel(ProxyTableModel m) {
        this.setMainTableModel(m);
        if (this.mainTableModel != null) {
            this.entityList = this.mainTableModel.getEntityList();
            this.columnModel = this.mainTableModel.getColumnModel();
        }
    }

    public ProxyTableModel getMainTableModel() {
        return this.mainTableModel;
    }

    public void setMainTableModel(ProxyTableModel m) {
        this.mainTableModel = m;
    }

    public boolean isVirtualColumnEnabled() {
        return this.virtualColumn;
    }

    public void setVirtualColumnEnabled(boolean v) {
        if (this.virtualColumn == v) {
            return;
        }
        this.virtualColumn = v;
        super.fireTableStructureChanged();
    }

    public boolean isRowNumberDisplayed() {
        return this.rowNumbers;
    }

    public void setRowNumberDisplayed(boolean d) {
        this.rowNumbers = d;
        super.fireTableDataChanged();
    }

    public boolean isEntityIndexDisplayed() {
        return this.entityIndex;
    }

    public void setEntityIndexDisplayed(boolean d) {
        this.entityIndex = d;
        super.fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int p1, int p2) {
        if (this.readOnly) {
            return false;
        }
        if (this.columnModel == null) {
            return false;
        }
        if (!this.isCellEditingEnabled()) {
            return false;
        }
        int columnIndex = p2;
        if (this.virtualColumn) {
            if (p2 == 0) {
                return false;
            }
            --columnIndex;
        }
        return this.columnModel.isEditable(columnIndex);
    }

    @Override
    public Object getValueAt(int p1, int p2) {
        if (this.entityList == null || this.columnModel == null) {
            return null;
        }
        Object entity = null;
        entity = p1 >= 0 && p1 < this.entityList.getCount() ? this.entityList.getEntity(p1) : this.mainTableModel.getVirtualEntity();
        int columnIndex = p2;
        if (this.virtualColumn) {
            if (p2 == 0) {
                if (p1 == this.entityList.getCount()) {
                    return "*";
                }
                if (p1 < this.entityList.getCount()) {
                    if (this.rowNumbers) {
                        return new Integer(p1 + 1).toString();
                    }
                    if (this.entityIndex) {
                        if (this.entityList instanceof OrderTranslatorEntityList) {
                            return new Integer(((OrderTranslatorEntityList)this.entityList).getEntityIndex(p1) + 1).toString();
                        }
                        return new Integer(p1 + 1).toString();
                    }
                }
            }
            --columnIndex;
        }
        return this.columnModel.getCellValue(columnIndex, entity);
    }

    @Override
    public void setValueAt(Object p1, int p2, int p3) {
        if (this.entityList == null || this.columnModel == null) {
            return;
        }
        Object entity = null;
        if (p2 >= 0 && p2 < this.entityList.getCount()) {
            entity = this.entityList.getEntity(p2);
        } else if (p2 == this.entityList.getCount()) {
            entity = this.mainTableModel.getVirtualEntity();
        }
        int columnIndex = p3;
        if (this.virtualColumn) {
            --columnIndex;
        }
        this.columnModel.setCellValue(columnIndex, entity, p1);
    }

    @Override
    public int getColumnCount() {
        int retVal = 0;
        if (this.columnModel != null) {
            int i = 0;
            while (i < this.columnModel.getColumnCount()) {
                if (this.columnModel.isLocked(i)) {
                    ++retVal;
                }
                ++i;
            }
        }
        if (this.virtualColumn) {
            ++retVal;
        }
        return retVal;
    }

    @Override
    public Class<?> getColumnClass(int p1) {
        int columnIndex = p1;
        if (this.virtualColumn) {
            if (columnIndex == 0) {
                return String.class;
            }
            --columnIndex;
        }
        return this.columnModel.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int p1) {
        int columnIndex = p1;
        if (this.virtualColumn) {
            if (columnIndex == 0) {
                return "";
            }
            --columnIndex;
        }
        return this.columnModel.getColumnName(columnIndex);
    }
}

