/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import java.util.Map;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.jump.plugin.datasource.JumpJDBCConnection;

public class JumpJDBCDataSource
extends DataSource {
    private AbstractJDBCDataSource datasource;

    public JumpJDBCDataSource() {
    }

    public JumpJDBCDataSource(AbstractJDBCDataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public Connection getConnection() {
        JumpJDBCConnection con = new JumpJDBCConnection();
        con.setDataSource(this.datasource);
        if (this.getProperties() != null) {
            Map<String, Object> properties = this.getProperties();
            if (properties.get("ADD_RESTRICTIONS") != null) {
                con.setAddRestrictions((Boolean)properties.get("ADD_RESTRICTIONS"));
            }
            if (properties.get("IGNORE_3D") != null) {
                con.setIgnore3d((Boolean)properties.get("IGNORE_3D"));
            }
            if (properties.get("FORCE_MULTI_GEOM") != null) {
                con.setForceMultiGeometry((Boolean)properties.get("FORCE_MULTI_GEOM"));
            }
            if (properties.get("USE_LOWER_CASE_FIELD_NAMES") != null) {
                con.setUseLowerCaseFieldNames((Boolean)properties.get("USE_LOWER_CASE_FIELD_NAMES"));
            }
            if (properties.get("SPECIFIC_GEOMETRY_COLUMN_NAME") != null) {
                con.setGeometryColumnName((String)properties.get("SPECIFIC_GEOMETRY_COLUMN_NAME"));
            }
        }
        return con;
    }

    public void setConnection(Connection con) {
        this.datasource = con.getDataSources() != null && !con.getDataSources().isEmpty() ? (AbstractJDBCDataSource)con.getDataSources().get(0) : null;
    }

    protected AbstractJDBCDataSource getDatasource() {
        return this.datasource;
    }

    public void setDatasource(AbstractJDBCDataSource datasource) {
        this.datasource = datasource;
    }
}

