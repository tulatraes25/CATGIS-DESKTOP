/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.iterators;

import java.sql.SQLException;
import java.util.List;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.filter.Filter;

public class MySQLEditableIterator
extends JDBCIterator {
    public MySQLEditableIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels) {
        this(consultaBD, filter, dataSource, labels, false);
    }

    public MySQLEditableIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels, boolean checkFilter) {
        super(consultaBD, filter, dataSource, labels, checkFilter);
        if (dataSource.getLayerFilter() != null) {
            this.filter = this.filter != null ? this.filter.and(dataSource.getLayerFilter()) : dataSource.getLayerFilter();
        }
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement(1003, 1007);
        this.statement.setFetchSize(Integer.MIN_VALUE);
        this.resultset = this.statement.executeQuery(this.consultaBD);
    }
}

