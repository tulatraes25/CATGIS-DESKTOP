/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.dao.jdbc.iterators;

import java.sql.SQLException;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.jdbc.iterators.JDBCTableIterator;
import org.saig.jump.lang.I18N;

public class PostgreSQLTableIterator
extends JDBCTableIterator {
    private int total = 0;

    public PostgreSQLTableIterator(String query, TableDBRecordDataSource dataSource) {
        this(query, null, dataSource);
    }

    public PostgreSQLTableIterator(String query, Filter filter, TableDBRecordDataSource dataSource) {
        super(query, filter, dataSource);
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement();
        this.statement.execute("BEGIN");
        this.statement.execute("DECLARE my_cursor CURSOR for " + this.dbQuery);
        this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
        ++this.total;
    }

    @Override
    public boolean hasNext() {
        Record record;
        block17: {
            Record recordToProcess;
            if (this.close) {
                return false;
            }
            if (!this.itExternalsExhausted) {
                if (this.itExternals.hasNext()) {
                    record = null;
                    while (record == null && this.itExternals.hasNext()) {
                        recordToProcess = (Record)this.itExternals.next();
                        if (recordToProcess == null) continue;
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
                            LOGGER.error((Object)"", (Throwable)e);
                            this.close(false);
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
                    this.close(false);
                    return false;
                }
            }
            record = null;
            try {
                while (record == null && this.resultset.next()) {
                    recordToProcess = this.dataSource.readRecord(this.resultset);
                    if (this.processed.contains(recordToProcess)) continue;
                    record = recordToProcess;
                }
                if (record != null) break block17;
                this.resultset.close();
                this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
                ++this.total;
                if (this.resultset.next()) {
                    recordToProcess = this.dataSource.readRecord(this.resultset);
                    if (recordToProcess != null && !this.processed.contains(recordToProcess)) {
                        record = recordToProcess;
                        break block17;
                    }
                    return this.hasNext();
                }
                return false;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                return false;
            }
        }
        this.dataBaseRecord = record;
        return record != null;
    }

    @Override
    public void close(boolean isCancel) {
        if (this.close) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.The-connection-was-already-closed"));
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
                this.statement.execute("CLOSE my_cursor");
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.statement != null) {
                this.statement.execute("END");
                this.statement.close();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }
}

