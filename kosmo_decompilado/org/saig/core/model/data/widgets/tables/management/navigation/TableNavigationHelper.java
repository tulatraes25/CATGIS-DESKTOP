/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.filter.SQLEncoder;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;

public class TableNavigationHelper
extends AbstractNavigationHelper {
    Logger LOGGER = Logger.getLogger(TableNavigationHelper.class);
    Table table;

    public TableNavigationHelper(Table table) {
        this.table = table;
        this.type = 0;
        this.setAscendingOrdering(true);
        this.setFilter(null);
        this.setOrderBy(null);
    }

    @Override
    public void setOrderBy(String[] names) {
        if (names == null || names.length == 0) {
            String[] orderAttrs = new String[]{this.table.getSchema().getPrimaryKey().getName()};
            super.setOrderBy(orderAttrs);
        } else {
            super.setOrderBy(names);
        }
    }

    @Override
    public int getNumElements() {
        int size = 0;
        TableRecordDataSource dataSource = this.table.getDataSource();
        ITableIterator it = null;
        try {
            try {
                if (dataSource instanceof TableDBRecordDataSource) {
                    String sqlWhere = null;
                    if (this.filter != null) {
                        SQLEncoder encoder = new SQLEncoder();
                        sqlWhere = encoder.encode(this.filter);
                    }
                    if (sqlWhere != null && sqlWhere.startsWith("WHERE")) {
                        sqlWhere = sqlWhere.substring(5);
                    }
                    it = ((TableDBRecordDataSource)dataSource).getIterator(sqlWhere, null);
                    while (it.hasNext()) {
                        it.next();
                        ++size;
                    }
                } else {
                    it = dataSource.getIterator();
                    while (it.hasNext()) {
                        Record rec = it.next();
                        RecordToFeatureWrapper wrapper = new RecordToFeatureWrapper(rec);
                        if (this.filter != null && !this.filter.contains(wrapper)) continue;
                        ++size;
                    }
                }
            }
            catch (Exception ex) {
                this.LOGGER.error((Object)"", (Throwable)ex);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return size;
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public List<Object> getElements(int startIndex, int nElements) {
        result = new ArrayList<Object>();
        dataSource = this.table.getDataSource();
        it = null;
        try {
            block19: {
                if (!(dataSource instanceof TableDBRecordDataSource)) break block19;
                sqlOrderBy = "";
                i = 0;
                while (i < this.orderBy.length) {
                    sqlOrderBy = String.valueOf(sqlOrderBy) + this.orderBy[i] + ", ";
                    ++i;
                }
                if (sqlOrderBy.endsWith(", ")) {
                    sqlOrderBy = sqlOrderBy.substring(0, sqlOrderBy.length() - 2);
                }
                sqlWhere = null;
                if (this.filter != null) {
                    encoder = new SQLEncoder();
                    sqlWhere = encoder.encode(this.filter);
                }
                if (sqlWhere != null && sqlWhere.startsWith("WHERE")) {
                    sqlWhere = sqlWhere.substring(5);
                }
                it = ((TableDBRecordDataSource)dataSource).getIterator(sqlWhere, sqlOrderBy);
                i = 1;
                while (i < startIndex && it.hasNext()) {
                    it.next();
                    ++i;
                }
                if (i < startIndex) {
                    var11_15 = result;
                    return var11_15;
                }
                processedElements = 0;
                if (true) ** GOTO lbl36
                do {
                    result.add(it.next());
                    ++processedElements;
lbl36:
                    // 2 sources

                    if (processedElements >= nElements) return result;
                } while (it.hasNext());
                return result;
            }
            it = dataSource.getIterator();
            i = 1;
            while (i < startIndex && it.hasNext()) {
                it.next();
                ++i;
            }
            if (i < startIndex) {
                var11_16 = result;
                return var11_16;
            }
            try {
                processedElements = 0;
                if (true) ** GOTO lbl60
                do {
                    rec = it.next();
                    wrapper = new RecordToFeatureWrapper(rec);
                    if (this.filter == null || this.filter.contains(wrapper)) {
                        result.add(rec);
                        ++processedElements;
                    }
lbl60:
                    // 4 sources

                    if (processedElements >= nElements) return result;
                } while (it.hasNext());
                return result;
            }
            catch (Exception ex) {
                this.LOGGER.error((Object)"", (Throwable)ex);
            }
            return result;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }
}

