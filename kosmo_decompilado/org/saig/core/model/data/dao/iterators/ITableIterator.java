/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.dao.iterators;

import org.saig.core.model.data.Record;

public interface ITableIterator {
    public void close();

    public boolean hasNext();

    public Record next();
}

