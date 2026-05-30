/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.dao.jdbc.iterators;

import java.sql.SQLException;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.jdbc.iterators.JDBCTableIterator;

public class MySQLTableIterator
extends JDBCTableIterator {
    public MySQLTableIterator(String query, TableDBRecordDataSource dataSource) {
        super(query, dataSource);
    }

    public MySQLTableIterator(String query, Filter filter, TableDBRecordDataSource dataSource) {
        super(query, filter, dataSource);
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement(1003, 1007);
        this.statement.setFetchSize(Integer.MIN_VALUE);
        this.resultset = this.statement.executeQuery(this.dbQuery);
    }
}

