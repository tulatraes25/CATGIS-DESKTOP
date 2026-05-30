/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.jdbc.iterators;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;

public class JDBCTableIterator
implements ITableIterator {
    public static final Logger LOGGER = Logger.getLogger(JDBCTableIterator.class);
    protected ResultSet resultset;
    protected Connection connection;
    protected Statement statement;
    protected Iterator<Record> itExternals;
    protected boolean itExternalsExhausted = false;
    protected HashSet<Record> processed;
    protected String dbQuery;
    protected boolean close;
    protected TableDBRecordDataSource dataSource;
    protected Record dataBaseRecord;
    protected Record externalRecord;
    protected final int BLOCK = 100;
    protected Filter filter;

    public JDBCTableIterator(String query, TableDBRecordDataSource dataSource) {
        this(query, null, dataSource);
    }

    public JDBCTableIterator(String query, Filter filter, TableDBRecordDataSource dataSource) {
        this.dbQuery = query;
        this.processed = new HashSet<Record>(dataSource.getDeletedRecords());
        this.filter = filter;
        HashSet<Record> newMod = new HashSet<Record>();
        newMod.addAll(dataSource.getUpdatedRecords());
        newMod.addAll(dataSource.getNewRecords());
        this.itExternals = newMod.iterator();
        this.close = false;
        this.dataSource = dataSource;
    }

    @Override
    public void close() {
        this.close(false);
    }

    @Override
    public boolean hasNext() {
        Record record;
        if (this.close) {
            return false;
        }
        if (!this.itExternalsExhausted) {
            if (this.itExternals.hasNext()) {
                record = null;
                while (record == null && this.itExternals.hasNext()) {
                    Record object = this.itExternals.next();
                    if (object == null) continue;
                    Record recordToProcess = object;
                    if (!recordToProcess.isUnsaved()) {
                        this.processed.add(recordToProcess);
                    }
                    if (this.filter != null) {
                        RecordToFeatureWrapper wrapper = new RecordToFeatureWrapper(recordToProcess);
                        if (!this.filter.contains(wrapper)) continue;
                        record = recordToProcess;
                        continue;
                    }
                    record = recordToProcess;
                }
                this.externalRecord = record;
                if (record == null) {
                    this.itExternalsExhausted = true;
                    try {
                        this.executeSQLIterator();
                    }
                    catch (SQLException e) {
                        LOGGER.debug((Object)"", (Throwable)e);
                        return false;
                    }
                }
                return true;
            }
            this.itExternalsExhausted = true;
            try {
                this.executeSQLIterator();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                return false;
            }
        }
        record = null;
        try {
            while (record == null && this.resultset.next()) {
                Record recordToProcess = this.dataSource.readRecord(this.resultset);
                if (this.processed.contains(recordToProcess)) continue;
                record = recordToProcess;
            }
            this.dataBaseRecord = record;
            return record != null;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return false;
        }
    }

    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement();
        this.resultset = this.statement.executeQuery(this.dbQuery);
    }

    @Override
    public Record next() {
        if (!this.itExternalsExhausted) {
            return this.externalRecord;
        }
        return this.dataBaseRecord;
    }

    public void close(boolean isCancel) {
        if (this.close) {
            return;
        }
        this.close = true;
        try {
            if (this.resultset != null) {
                this.resultset.close();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.statement != null) {
                if (isCancel) {
                    this.statement.cancel();
                }
                this.statement.close();
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (Exception ex1) {
            LOGGER.error((Object)"", (Throwable)ex1);
        }
    }
}

