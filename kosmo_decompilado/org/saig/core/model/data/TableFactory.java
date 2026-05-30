/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data;

import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableRecordDataSource;

public class TableFactory {
    public static Table getRecordCollection(TableRecordDataSource dataSource) {
        if (dataSource == null) {
            return null;
        }
        Table recordCollection = new Table(dataSource);
        return recordCollection;
    }
}

