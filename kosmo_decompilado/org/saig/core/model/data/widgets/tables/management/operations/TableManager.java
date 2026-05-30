/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.tables.management.definition.Column;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.Manager;
import org.saig.core.model.data.widgets.tables.management.operations.MandatoryFieldsException;

public class TableManager
implements Manager {
    protected Table table;
    TableDef tableDef;

    public TableManager(String tableName, TableDef tableDef) {
        this.table = JUMPWorkbench.getTable(tableName);
        this.tableDef = tableDef;
    }

    @Override
    public void doOperations(List dirtyInsert, List dirtyUpdate, List dirtyDelete) throws Exception {
        boolean valid;
        boolean bl = valid = this.checkMandatory(dirtyInsert) && this.checkMandatory(dirtyUpdate);
        if (!valid) {
            throw new MandatoryFieldsException(this.getMandatoryListAsString());
        }
        this.table.addAll(dirtyInsert);
        this.table.updateAll(dirtyUpdate);
        this.table.removeAll(dirtyDelete);
        this.table.commit();
    }

    private String getMandatoryListAsString() {
        String mandatoryFields = "";
        if (this.tableDef.isPkEditable()) {
            String publicName = this.getSchema().getPublicName(this.getSchema().getPrimaryKeyIndex());
            mandatoryFields = mandatoryFields.concat(String.valueOf(publicName) + ", ");
        }
        Collection<Column> columns = this.tableDef.getColumns().values();
        for (Column column : columns) {
            String publicName = this.getSchema().getPublicName(this.getSchema().getAttributeIndex(column.getName()));
            mandatoryFields = mandatoryFields.concat(String.valueOf(publicName) + ", ");
        }
        return mandatoryFields.substring(0, mandatoryFields.length() - 2);
    }

    private boolean checkMandatory(List dirtyList) {
        boolean valid = true;
        if (this.tableDef == null) {
            valid = true;
        } else {
            if (this.tableDef.isPkEditable()) {
                int pkIndex = this.getSchema().getPrimaryKeyIndex();
                valid = this.isNotNullField(pkIndex, dirtyList);
            }
            Collection<Column> columns = this.tableDef.getColumns().values();
            Iterator<Column> it = columns.iterator();
            while (it.hasNext() && valid) {
                Column column = it.next();
                int fieldIndex = this.getSchema().getAttributeIndex(column.getName());
                valid = this.isNotNullField(fieldIndex, dirtyList);
            }
        }
        return valid;
    }

    private boolean isNotNullField(int i, List dirtyList) {
        boolean valid = true;
        for (Record r : dirtyList) {
            Object field = r.getAttribute(i);
            if (field != null && !"".equals(field)) continue;
            valid = false;
        }
        return valid;
    }

    @Override
    public FeatureSchema getSchema() {
        return this.table.getSchema();
    }

    @Override
    public Object getValue(int col, Object entity) {
        Record record = (Record)entity;
        return record.getAttribute(col);
    }

    @Override
    public void setValue(int col, Object entity, Object newValue) {
        Record record = (Record)entity;
        record.setAttribute(col, newValue);
    }

    @Override
    public Object getNewEntity() {
        return new Record(this.getSchema());
    }

    @Override
    public List getDataList() {
        return this.table.getRecords();
    }

    @Override
    public List getDataList(String fieldOrdered, Filter filter) {
        return this.table.getRecords(fieldOrdered, filter);
    }

    @Override
    public Collection getRelations() {
        return this.table.getAllRelations();
    }

    public List getVersionableFieldsNames() {
        ArrayList<String> list = new ArrayList<String>();
        if (this.table.isVersionable()) {
            list.add(this.table.getEndDateField());
            list.add(this.table.getHistoryField());
            list.add(this.table.getStartDateField());
        }
        return list;
    }
}

