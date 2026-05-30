/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.dao.iterators.full;

import java.sql.SQLException;
import java.util.List;
import org.saig.core.model.data.Record;

public interface ITableFullIterator {
    public Record next() throws SQLException;

    public Record prior() throws SQLException;

    public Record first() throws SQLException;

    public Record last() throws SQLException;

    public Record absolute(int var1) throws SQLException;

    public Record relative(int var1) throws SQLException;

    public Record forward() throws SQLException;

    public List<Record> forward(int var1) throws SQLException;

    public List<Record> forward_all() throws SQLException;

    public Record backward() throws SQLException;

    public List<Record> backward(int var1) throws SQLException;

    public List<Record> backward_all() throws SQLException;

    public void close() throws SQLException;

    public void open() throws SQLException;

    public long size() throws SQLException;
}

