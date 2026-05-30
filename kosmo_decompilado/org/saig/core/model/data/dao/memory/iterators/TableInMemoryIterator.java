/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.memory.iterators;

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.memory.MemoryRecordDataSource;

public class TableInMemoryIterator
implements ITableIterator {
    private static final Logger LOGGER = Logger.getLogger(TableInMemoryIterator.class);
    private MemoryRecordDataSource ds;
    private Record readObject;
    private Iterator<Record> recordIterator;
    private Iterator<Record> newRecordIterator;
    private final Filter filter;
    private final RecordToFeatureWrapper wrapper;

    public TableInMemoryIterator(MemoryRecordDataSource datasource) {
        this(datasource, null);
    }

    public TableInMemoryIterator(MemoryRecordDataSource datasource, Filter filter) {
        this.ds = datasource;
        this.filter = filter;
        this.wrapper = new RecordToFeatureWrapper(null);
        this.recordIterator = this.ds.getInternalRecords().iterator();
        this.newRecordIterator = this.ds.getNewRecords().iterator();
    }

    @Override
    public void close() {
        this.wrapper.setInternalRecord(null);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hasNext() {
        block5: {
            this.readObject = null;
            try {
                if (!this.recordIterator.hasNext()) ** GOTO lbl19
                while (this.recordIterator.hasNext() && this.readObject == null) {
                    this.readObject = this.recordIterator.next();
                    if (this.filterContainsRecord()) continue;
                    this.readObject = null;
                }
                if (this.readObject != null) break block5;
                while (this.newRecordIterator.hasNext() && this.readObject == null) {
                    this.readObject = this.newRecordIterator.next();
                    if (this.filterContainsRecord()) continue;
                    this.readObject = null;
                }
                break block5;
lbl-1000:
                // 1 sources

                {
                    this.readObject = this.newRecordIterator.next();
                    if (this.filterContainsRecord()) continue;
                    this.readObject = null;
lbl19:
                    // 3 sources

                    ** while (this.newRecordIterator.hasNext() && this.readObject == null)
                }
lbl20:
                // 1 sources

            }
            catch (Exception e) {
                TableInMemoryIterator.LOGGER.error((Object)"", (Throwable)e);
                return false;
            }
        }
        return this.readObject != null;
    }

    @Override
    public Record next() {
        return this.readObject;
    }

    private boolean filterContainsRecord() {
        if (this.filter != null) {
            this.wrapper.setInternalRecord(this.readObject);
            return this.filter.contains(this.wrapper);
        }
        return true;
    }
}

