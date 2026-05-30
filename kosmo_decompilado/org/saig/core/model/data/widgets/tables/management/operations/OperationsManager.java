/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.LayerManager;
import org.saig.core.model.data.widgets.tables.management.operations.Manager;
import org.saig.core.model.data.widgets.tables.management.operations.ManagerFactory;
import org.saig.core.model.data.widgets.tables.management.operations.TableDataSourceManager;
import org.saig.core.model.data.widgets.tables.management.operations.TableManager;
import org.saig.jump.lang.I18N;

public class OperationsManager {
    private List dirtyInsert;
    private List dirtyDelete;
    private List dirtyUpdate;
    protected Manager manager;
    protected static final Logger LOGGER = Logger.getLogger(OperationsManager.class);

    public OperationsManager() {
    }

    public OperationsManager(String tableName, TableDef tableDef) throws Exception {
        this.clearCollections();
        this.manager = ManagerFactory.getManager(tableName, tableDef);
        if (this.manager == null) {
            throw new Exception(I18N.getMessage("org.saig.core.model.data.widgets.tables.management.operations.OperationsManager.table-not-found-{0}", new Object[]{tableName}));
        }
    }

    public OperationsManager(Layer layer, TableDef tableDef) throws Exception {
        this.clearCollections();
        this.manager = ManagerFactory.getManager(layer, tableDef);
        if (this.manager == null) {
            throw new Exception(I18N.getMessage(this.getClass(), "layer-{0}-was-not-found", new Object[]{layer.getName()}));
        }
    }

    public void addInsert(Object operation) {
        this.dirtyInsert.add(operation);
    }

    public void addDelete(Object operation) {
        this.dirtyDelete.add(operation);
        try {
            this.doOperations();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void addUpdate(Object operation) {
        this.dirtyUpdate.add(operation);
    }

    public void doOperations() throws Exception {
        this.manager.doOperations(this.dirtyInsert, this.dirtyUpdate, this.dirtyDelete);
        this.clearCollections();
    }

    public void clearOperations() {
        this.clearCollections();
    }

    protected void clearCollections() {
        this.dirtyInsert = new ArrayList();
        this.dirtyDelete = new ArrayList();
        this.dirtyUpdate = new ArrayList();
    }

    public boolean hasOperations() {
        boolean operations = false;
        if (!(this.dirtyInsert.isEmpty() && this.dirtyDelete.isEmpty() && this.dirtyUpdate.isEmpty())) {
            operations = true;
        }
        return operations;
    }

    public FeatureSchema getSchema() {
        return this.manager.getSchema();
    }

    public Object getValue(int col, Object entity) {
        return this.manager.getValue(col, entity);
    }

    public void setValue(int col, Object entity, Object newValue) {
        this.manager.setValue(col, entity, newValue);
        this.addUpdate(entity);
    }

    public Object getNewEntity() {
        return this.manager.getNewEntity();
    }

    public List getDataList() {
        return this.manager.getDataList();
    }

    public List getDataList(String fieldOrdered, Filter filter) throws Exception {
        return this.manager.getDataList(fieldOrdered, filter);
    }

    public Collection getRelations() {
        return this.manager.getRelations();
    }

    public List getVersionableFieldsNames() {
        List list = null;
        if (this.manager instanceof LayerManager) {
            LayerManager layerManager = (LayerManager)this.manager;
            list = layerManager.getVersionableFieldsNames();
        } else if (this.manager instanceof TableManager) {
            TableManager tableManager = (TableManager)this.manager;
            list = tableManager.getVersionableFieldsNames();
        } else if (this.manager instanceof TableDataSourceManager) {
            TableDataSourceManager tableDataSourceManager = (TableDataSourceManager)this.manager;
            list = tableDataSourceManager.getVersionableFieldsNames();
        }
        return list;
    }

    public List getVersionableFieldsPositions() {
        List listNames = this.getVersionableFieldsNames();
        ArrayList<Integer> listPos = new ArrayList<Integer>();
        for (String fieldName : listNames) {
            listPos.add(this.manager.getSchema().getAttributeIndex(fieldName));
        }
        return listPos;
    }
}

