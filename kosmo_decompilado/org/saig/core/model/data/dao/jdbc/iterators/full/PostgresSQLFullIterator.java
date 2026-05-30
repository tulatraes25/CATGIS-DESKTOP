/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.jdbc.iterators.full;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.iterators.full.ITableFullIterator;

public class PostgresSQLFullIterator
implements ITableFullIterator {
    public static final Logger LOGGER = Logger.getLogger(PostgresSQLFullIterator.class);
    protected Connection connection;
    protected Statement statement;
    protected String consultaBD;
    protected TableDBRecordDataSource dataSource;

    public PostgresSQLFullIterator(Filter filter, String[] fieldsToOrdered, boolean ascending, TableDBRecordDataSource dataSource) throws SQLException {
        this.consultaBD = dataSource.getSQLByFilter(filter, fieldsToOrdered, ascending);
        this.dataSource = dataSource;
        this.connection = DataBaseConnectionFactory.getConnection(dataSource);
        this.open();
    }

    @Override
    public Record absolute(int n) throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH ABSOLUTE " + n + " FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public Record backward() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public List<Record> backward(int n) throws SQLException {
        ArrayList<Record> records = new ArrayList<Record>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD " + n + " FROM my_cursor");
            while (resultset.next()) {
                records.add(this.dataSource.readRecord(resultset));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return records;
    }

    @Override
    public List<Record> backward_all() throws SQLException {
        ArrayList<Record> records = new ArrayList<Record>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD ALL FROM my_cursor");
            while (resultset.next()) {
                records.add(this.dataSource.readRecord(resultset));
            }
        }
        finally {
            resultset.close();
        }
        return records;
    }

    @Override
    public void close() throws SQLException {
        if (this.statement != null) {
            this.statement.execute("CLOSE my_cursor");
        }
        if (this.connection != null) {
            this.connection.close();
        }
    }

    @Override
    public Record first() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FIRST FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public Record forward() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public List<Record> forward(int n) throws SQLException {
        ArrayList<Record> records = new ArrayList<Record>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD " + n + " FROM my_cursor");
            while (resultset.next()) {
                records.add(this.dataSource.readRecord(resultset));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return records;
    }

    @Override
    public List<Record> forward_all() throws SQLException {
        ArrayList<Record> records = new ArrayList<Record>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD ALL FROM my_cursor");
            while (resultset.next()) {
                records.add(this.dataSource.readRecord(resultset));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return records;
    }

    @Override
    public Record last() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH LAST FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public Record next() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH NEXT FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public void open() throws SQLException {
        this.statement = this.connection.createStatement();
        this.statement.execute("BEGIN");
        this.statement.execute("DECLARE my_cursor SCROLL CURSOR for " + this.consultaBD);
    }

    @Override
    public Record prior() throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH PRIOR FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public Record relative(int n) throws SQLException {
        Record record = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH RELATIVE " + n + " FROM my_cursor");
            if (resultset.next()) {
                record = this.dataSource.readRecord(resultset);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return record;
    }

    @Override
    public long size() throws SQLException {
        long size = 0L;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("SELECT COUNT(*) FROM (" + this.consultaBD + ") a");
            if (resultset.next()) {
                size = resultset.getLong(1);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return size;
    }
}

