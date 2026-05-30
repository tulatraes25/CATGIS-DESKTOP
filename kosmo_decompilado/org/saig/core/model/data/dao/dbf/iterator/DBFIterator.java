/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.dbf.iterator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.iterators.JDBCTableIterator;

public class DBFIterator
implements ITableIterator {
    public static final Logger LOGGER = Logger.getLogger(JDBCTableIterator.class);
    private Iterator<Record> itExternos;
    private boolean itExternoAgotado = false;
    private Set<Record> procesados;
    private boolean close;
    private DBFRecordDataSource dataSource;
    private Record dataBaseRecord;
    private Record elementRecord;
    private int total = 0;

    public DBFIterator(DBFRecordDataSource dbfDataSource) {
        this.dataSource = dbfDataSource;
        this.procesados = new HashSet<Record>();
        this.procesados.addAll(this.dataSource.getDeletedRecords());
        HashSet<Record> newMod = new HashSet<Record>();
        newMod.addAll(this.dataSource.getUpdatedRecords());
        newMod.addAll(this.dataSource.getNewRecords());
        this.itExternos = newMod.iterator();
        this.close = false;
    }

    @Override
    public void close() {
        this.close(false);
    }

    @Override
    public boolean hasNext() {
        Record recordToProcess;
        Record record;
        if (this.close) {
            return false;
        }
        if (!this.itExternoAgotado) {
            if (this.itExternos.hasNext()) {
                record = null;
                while (record == null && this.itExternos.hasNext()) {
                    recordToProcess = this.itExternos.next();
                    if (recordToProcess == null) continue;
                    if (!recordToProcess.isUnsaved()) {
                        this.procesados.add(recordToProcess);
                    }
                    record = recordToProcess;
                }
                this.elementRecord = record;
                if (record == null) {
                    this.itExternoAgotado = true;
                    try {
                        this.dataSource.open();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        return false;
                    }
                }
                return true;
            }
            this.itExternoAgotado = true;
            try {
                this.dataSource.open();
            }
            catch (IOException e) {
                LOGGER.error((Object)"", (Throwable)e);
                return false;
            }
        }
        record = null;
        try {
            while (record == null && this.total < this.dataSource.getNumReg()) {
                recordToProcess = this.dataSource.readRecord(this.total);
                ++this.total;
                if (this.procesados.contains(recordToProcess)) continue;
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

    @Override
    public Record next() {
        if (!this.itExternoAgotado) {
            return this.elementRecord;
        }
        return this.dataBaseRecord;
    }

    public void close(boolean isCancel) {
        try {
            this.dataSource.close();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }
}

