/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.JumpJDBCDataSource;
import org.saig.jump.widgets.datasource.JDBCPropertiesPanel;
import org.saig.jump.widgets.util.DialogFactory;

public class JDBCLoadDataSourceQueryChooser
implements DataSourceQueryChooser {
    public static final String NAME = I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.database");
    protected JDBCPropertiesPanel panel = new JDBCPropertiesPanel(true);

    @Override
    public Component getComponent() {
        return this.panel;
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        ArrayList<DataSourceQuery> result = new ArrayList<DataSourceQuery>();
        List<AbstractJDBCDataSource> dataSources = this.panel.getLayerDataSources();
        if (CollectionUtils.isNotEmpty(dataSources)) {
            for (AbstractJDBCDataSource currentDS : dataSources) {
                result.add(this.toDataSourceQuery(currentDS));
            }
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public boolean isInputValid() {
        if (this.panel.checkParameters()) {
            try {
                this.panel.initializeTable();
            }
            catch (Exception e) {
                DialogFactory.showErrorDialog(this.panel, String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error-check-your-parameters")) + ":\n" + e.getMessage(), I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error"));
                return false;
            }
            return true;
        }
        DialogFactory.showErrorDialog(this.panel, I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error-check-your-parameters"), I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error"));
        return false;
    }

    @Override
    public String toString() {
        return NAME;
    }

    private DataSourceQuery toDataSourceQuery(AbstractJDBCDataSource datasource) {
        JumpJDBCDataSource dataSource = new JumpJDBCDataSource(datasource);
        return new DataSourceQuery(dataSource, null, datasource.getTableName());
    }
}

