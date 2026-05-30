/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import org.saig.jump.plugin.datasource.IndexedShapeFileConnection;

public class IndexedShapeFileDataSource
extends DataSource {
    @Override
    public Connection getConnection() {
        return new IndexedShapeFileConnection();
    }
}

