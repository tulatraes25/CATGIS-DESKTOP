/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityTableColumn;
import javax.swing.table.TableColumnModel;

public interface EntityTableColumnModel
extends TableColumnModel {
    public Object getCellValue(int var1, Object var2);

    public void setCellValue(int var1, Object var2, Object var3);

    public boolean isLocked(int var1);

    public void setLocked(int var1, boolean var2);

    public boolean isSortable(int var1);

    public void setSortable(int var1, boolean var2);

    public boolean isHidden(int var1);

    public void setHidden(int var1, boolean var2);

    public boolean isEditable(int var1);

    public void setEditable(int var1, boolean var2);

    public boolean isRightAlignmentSortable(int var1);

    public void setRightAlignmentSortable(int var1, boolean var2);

    public Class<?> getColumnClass(int var1);

    public String getColumnName(int var1);

    @Override
    public int getColumnCount();

    public int getRealColumnCount();

    public void addColumn(EntityTableColumn var1);

    public void removeColumn(int var1);

    public EntityTableColumn getByName(String var1);

    public boolean hasEditableColumns();
}

