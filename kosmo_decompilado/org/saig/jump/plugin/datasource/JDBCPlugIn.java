/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.core.dao.datasource.dbdatasource.DataBaseDataSourceFactory;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.model.data.dao.jdbc.OracleDataSource;
import org.saig.core.model.data.dao.jdbc.PostgreSQLDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser;
import org.saig.jump.widgets.datasource.SaveJDBCDataSourceQueryChooser;

public class JDBCPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.JDBCPlugIn.name");

    @Override
    public void initialize(PlugInContext context) {
        this.registerDefaultDatasources();
        JDBCLoadDataSourceQueryChooser loadQueryChooser = new JDBCLoadDataSourceQueryChooser();
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
        dataSourceManager.addSaveDataSourceQueryChooser(new SaveJDBCDataSourceQueryChooser());
    }

    private void registerDefaultDatasources() {
        DataBaseDataSourceFactory.registerLayerJDBCDataSource(new PostGisDataSource());
        DataBaseDataSourceFactory.registerLayerJDBCDataSource(new MySQLDataSource());
        DataBaseDataSourceFactory.registerLayerJDBCDataSource(new OracleSpatialDataSource());
        DataBaseDataSourceFactory.registerTableJDBCDataSource(new PostgreSQLDataSource());
        DataBaseDataSourceFactory.registerTableJDBCDataSource(new org.saig.core.model.data.dao.jdbc.MySQLDataSource());
        DataBaseDataSourceFactory.registerTableJDBCDataSource(new OracleDataSource());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

