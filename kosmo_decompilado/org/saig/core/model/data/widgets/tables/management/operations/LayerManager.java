/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.widgets.tables.management.definition.Column;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.Manager;
import org.saig.core.model.data.widgets.tables.management.operations.MandatoryFieldsException;
import org.saig.core.model.feature.FeatureCollectionOnDemand;

public class LayerManager
implements Manager {
    private Layer layer;
    private TableDef tableDef;

    public LayerManager(String name, TableDef tableDef) {
        this.layer = JUMPWorkbench.getLayer(name);
        this.tableDef = tableDef;
    }

    public LayerManager(Layer layer, TableDef tableDef) {
        this.layer = layer;
        this.tableDef = tableDef;
    }

    @Override
    public void doOperations(List dirtyInsert, List dirtyUpdate, List dirtyDelete) throws Exception {
        boolean valid;
        boolean bl = valid = this.checkMandatory(dirtyInsert) && this.checkMandatory(dirtyUpdate);
        if (!valid) {
            throw new MandatoryFieldsException(this.getMandatoryListAsString());
        }
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        if (fc instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcOnDemmand = (FeatureCollectionOnDemand)fc;
            fcOnDemmand.getDataAccesor().setInMemory(false);
            fcOnDemmand.addAll(dirtyInsert);
            fcOnDemmand.updateAll(dirtyUpdate);
            fcOnDemmand.removeAll(dirtyDelete);
            fcOnDemmand.getDataAccesor().setInMemory(true);
        } else {
            fc.addAll(dirtyInsert);
            fc.updateAll(dirtyUpdate);
            fc.removeAll(dirtyDelete);
            fc.commit();
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
        for (Feature r : dirtyList) {
            Object field = r.getAttribute(i);
            if (field != null && !"".equals(field)) continue;
            valid = false;
        }
        return valid;
    }

    @Override
    public FeatureSchema getSchema() {
        return this.layer.getFeatureSchema();
    }

    @Override
    public Object getValue(int col, Object entity) {
        Feature feature = (Feature)entity;
        return feature.getAttribute(col);
    }

    @Override
    public void setValue(int col, Object entity, Object newValue) {
        Feature feature = (Feature)entity;
        feature.setAttribute(col, newValue);
    }

    @Override
    public Object getNewEntity() {
        return new BasicFeature(this.getSchema());
    }

    @Override
    public List getDataList() {
        return this.layer.getFeatureCollectionWrapper().getFeatures();
    }

    @Override
    public List getDataList(String fieldOrdered, Filter filter) throws Exception {
        return this.layer.getFeatureCollectionWrapper().query(filter);
    }

    @Override
    public Collection getRelations() {
        return this.layer.getAllRelations();
    }

    public List getVersionableFieldsNames() {
        ArrayList<String> list = new ArrayList<String>();
        if (this.layer.isVersionable()) {
            list.add(this.layer.getEndDateField());
            list.add(this.layer.getHistoryField());
            list.add(this.layer.getStartDateField());
        }
        return list;
    }
}

