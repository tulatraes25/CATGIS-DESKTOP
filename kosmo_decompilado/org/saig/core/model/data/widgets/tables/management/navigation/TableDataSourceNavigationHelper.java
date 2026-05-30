/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.filter.SQLEncoder;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;

public class TableDataSourceNavigationHelper
extends AbstractNavigationHelper {
    Logger LOGGER = Logger.getLogger(TableDataSourceNavigationHelper.class);
    TableDBRecordDataSource table;

    public TableDataSourceNavigationHelper(TableDBRecordDataSource table) {
        this.table = table;
        this.type = 2;
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
        try {
            ResultSet rs;
            String sqlWhere = null;
            if (this.filter != null) {
                SQLEncoder encoder = new SQLEncoder();
                sqlWhere = encoder.encode(this.filter);
            }
            if (sqlWhere != null && sqlWhere.startsWith("WHERE")) {
                sqlWhere = sqlWhere.substring(5);
            }
            Connection connection = this.table.getConnection();
            Statement createStatement = connection.createStatement();
            String tableName = this.table.getFullTableName();
            String sqlCount = "select count(*) from " + tableName;
            if (sqlWhere != null && !sqlWhere.isEmpty()) {
                sqlCount = String.valueOf(sqlCount) + " where " + sqlWhere;
            }
            if ((rs = createStatement.executeQuery(sqlCount)).next()) {
                size = rs.getInt(1);
            }
            rs.close();
            connection.close();
        }
        catch (Exception ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
        }
        return size;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public List<Object> getElements(int startIndex, int nElements) {
        ArrayList<Object> result = new ArrayList<Object>();
        ITableIterator it = null;
        try {
            String sqlOrderBy = "";
            int i = 0;
            while (i < this.orderBy.length) {
                sqlOrderBy = String.valueOf(sqlOrderBy) + this.orderBy[i] + ", ";
                ++i;
            }
            if (sqlOrderBy.endsWith(", ")) {
                sqlOrderBy = sqlOrderBy.substring(0, sqlOrderBy.length() - 2);
            }
            String sqlWhere = null;
            if (this.filter != null) {
                SQLEncoder encoder = new SQLEncoder();
                sqlWhere = encoder.encode(this.filter);
            }
            if (sqlWhere != null && sqlWhere.startsWith("WHERE")) {
                sqlWhere = sqlWhere.substring(5);
            }
            it = this.table.getIterator(sqlWhere, sqlOrderBy);
            int i2 = 1;
            while (i2 < startIndex && it.hasNext()) {
                it.next();
                ++i2;
            }
            if (i2 < startIndex) {
                ArrayList<Object> arrayList = result;
                return arrayList;
            }
            int processedElements = 0;
            while (processedElements < nElements) {
                if (!it.hasNext()) {
                    return result;
                }
                result.add(it.next());
                ++processedElements;
            }
            return result;
        }
        catch (Exception ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
            return result;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }
}

