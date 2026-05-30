/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.JumpJDBCDataSource;
import org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel;
import org.saig.jump.widgets.util.DialogFactory;

public class SaveJDBCDataSourceQueryChooser
implements DataSourceQueryChooser {
    public static final String NAME = I18N.getString("org.saig.jump.widgets.datasource.SaveJDBCDataSourceQueryChooser.database");
    public static final String ADD_RESTRICTIONS_KEY = "ADD_RESTRICTIONS";
    public static final String IGNORE_3D_KEY = "IGNORE_3D";
    public static final String FORCE_MULTI_GEOM = "FORCE_MULTI_GEOM";
    public static final String USE_LOWER_CASE_FIELD_NAMES = "USE_LOWER_CASE_FIELD_NAMES";
    public static final String SPECIFIC_GEOMETRY_COLUMN_NAME = "SPECIFIC_GEOMETRY_COLUMN_NAME";
    private JDBCSavePropertiesPanel panel = new JDBCSavePropertiesPanel();

    @Override
    public Component getComponent() {
        return this.panel;
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        ArrayList<DataSourceQuery> solucion = new ArrayList<DataSourceQuery>();
        AbstractJDBCDataSource dataSource = this.panel.getDataSource();
        boolean addRestrictions = this.panel.addRestrictions();
        boolean ignore3d = this.panel.ignore3d();
        boolean forceMultiGeometry = this.panel.forceMultiGeometry();
        boolean useLowerCaseFieldNames = this.panel.useLowerCaseFieldNames();
        String geometryColumnName = this.panel.getGeometryColumnName();
        if (dataSource != null) {
            DataSourceQuery query = this.toDataSourceQuery(dataSource);
            if (query.getDataSource().getProperties() == null) {
                query.getDataSource().setProperties(new HashMap<String, Object>());
            }
            query.getDataSource().getProperties().put(ADD_RESTRICTIONS_KEY, addRestrictions);
            query.getDataSource().getProperties().put(IGNORE_3D_KEY, ignore3d);
            query.getDataSource().getProperties().put(FORCE_MULTI_GEOM, forceMultiGeometry);
            query.getDataSource().getProperties().put(USE_LOWER_CASE_FIELD_NAMES, useLowerCaseFieldNames);
            query.getDataSource().getProperties().put(SPECIFIC_GEOMETRY_COLUMN_NAME, geometryColumnName);
            solucion.add(query);
        }
        return solucion;
    }

    @Override
    public boolean isInputValid() {
        if (this.panel.checkParameters() && this.panel.checkConnectionWithSchema()) {
            return true;
        }
        DialogFactory.showErrorDialog(this.panel, String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error-check-your-parameters")) + ".\n" + I18N.getMessage("org.saig.jump.widgets.datasource.SaveJDBCDataSourceQueryChooser.the-error-description-is-{0}", new Object[]{this.panel.getError()}), I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error"));
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

