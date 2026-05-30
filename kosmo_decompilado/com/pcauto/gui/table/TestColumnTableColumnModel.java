/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.AbstractEntityTableColumnModel;
import com.pcauto.gui.table.EntityTableColumn;

public class TestColumnTableColumnModel
extends AbstractEntityTableColumnModel {
    private static final long serialVersionUID = 1L;

    public TestColumnTableColumnModel() {
        this.addColumn(new EntityTableColumn("Column Name", String.class));
        this.addColumn(new EntityTableColumn("Locked", Boolean.class));
        this.addColumn(new EntityTableColumn("Sortable", Boolean.class));
        this.addColumn(new EntityTableColumn("Hidden", Boolean.class));
        this.addColumn(new EntityTableColumn("Editable", Boolean.class));
    }

    @Override
    public void setCellValue(int col, Object entity, Object value) {
        EntityTableColumn model = (EntityTableColumn)entity;
        switch (col) {
            case 0: {
                model.setName((String)value);
                break;
            }
            case 1: {
                model.setLocked((Boolean)value);
                break;
            }
            case 2: {
                model.setSortable((Boolean)value);
                break;
            }
            case 3: {
                model.setHidden((Boolean)value);
                break;
            }
            case 4: {
                model.setEditable((Boolean)value);
            }
        }
    }

    @Override
    public Object getCellValue(int col, Object entity) {
        EntityTableColumn model = (EntityTableColumn)entity;
        Object retVal = null;
        switch (col) {
            case 0: {
                retVal = model.getName();
                break;
            }
            case 1: {
                retVal = new Boolean(model.isLocked());
                break;
            }
            case 2: {
                retVal = new Boolean(model.isSortable());
                break;
            }
            case 3: {
                retVal = new Boolean(model.isHidden());
                break;
            }
            case 4: {
                retVal = new Boolean(model.isEditable());
            }
        }
        return retVal;
    }
}

