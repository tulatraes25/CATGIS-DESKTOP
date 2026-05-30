/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource;

import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;

public interface IPoolableDBDataSource {
    public ConnectionPoolDataSource createConnectionPool() throws SQLException;

    public String getDataBaseName();
}

