/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.context.GenericContext;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.widgets.tables.management.definition.Column;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.Manager;
import org.saig.core.model.data.widgets.tables.management.operations.MandatoryFieldsException;

public class TableDataSourceManager
implements Manager {
    private static final Logger LOGGER = Logger.getLogger(TableDataSourceManager.class);
    private TableDBRecordDataSource table;
    TableDef tableDef;

    public TableDataSourceManager(String tableName, String pk, TableDef tableDef) {
        try {
            this.table = GenericContext.getGenericContext().getTableDataSource(tableName, pk);
            this.tableDef = tableDef;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public void doOperations(List dirtyInsert, List dirtyUpdate, List dirtyDelete) throws Exception {
        boolean valid;
        boolean bl = valid = this.checkMandatory(dirtyInsert) && this.checkMandatory(dirtyUpdate);
        if (!valid) {
            throw new MandatoryFieldsException(this.getMandatoryListAsString());
        }
        try {
            this.table.addAll(dirtyInsert);
            this.table.updateAll(dirtyUpdate);
            this.table.removeAll(dirtyDelete);
            this.table.commit();
        }
        catch (Exception ex) {
            this.table.rollback();
            throw ex;
        }
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
        return new ArrayList();
    }

    public List getVersionableFieldsNames() {
        ArrayList<String> list = new ArrayList<String>();
        if (this.getSchema().isVersionable()) {
            list.add(this.getSchema().getFieldEndDate());
            list.add(this.getSchema().getHistoryField());
            list.add(this.getSchema().getFieldStartDate());
        }
        return list;
    }
}

