/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.collections.CollectionUtils
 *  org.postgis.PGbox2d
 *  org.postgis.PGbox3d
 */
package org.saig.core.dao.datasource.dbdatasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectGeometryTypeDialog;

public class ExtendPostGisDataSource
extends PostGisDataSource {
    public static final String METADATA_TABLE_NAME = "extended_geometry_columns";

    public ExtendPostGisDataSource() {
    }

    public ExtendPostGisDataSource(String host, int port, String databaseName, String tableName, String username, String password) {
        super(host, port, databaseName, tableName, username, password);
    }

    public ExtendPostGisDataSource(String host, int port, String databaseName, String username, String password) {
        super(host, port, databaseName, username, password);
    }

    @Override
    public void initialize(boolean ignoreGeometryType) throws SQLException {
        this.envelope = null;
        this.size = -1;
        Connection con = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                if (!this.checkIfCreateExtendedMetadataTable() && !this.checkIfHasPermisionOfSelectInsertOrUpdateOnExtendedMetadataTable()) {
                    throw new SQLException(I18N.getString(ExtendPostGisDataSource.class, "selected-user-has-not-necessaries-privileges-to-perform-optimized-load"));
                }
                this.buildFeatureSchema(con);
                if (!this.buildGeometryType(con) && !ignoreGeometryType) {
                    int option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined-do-you-want-to-select-it"), I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Select-geometry-type"));
                    if (option == 0) {
                        SelectGeometryTypeDialog selectDialog = new SelectGeometryTypeDialog(JUMPWorkbench.getFrameInstance(), true);
                        GUIUtil.centreOnWindow(selectDialog);
                        selectDialog.setVisible(true);
                        int geomType = selectDialog.getGeometryType();
                        if (geomType == 0) {
                            throw new SQLException(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined"));
                        }
                        this.schema.setGeometryType(geomType);
                    } else {
                        throw new SQLException(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined"));
                    }
                }
                this.buildGeometrySRID(con);
                this.geomColName = this.schema.getAttributeName(this.schema.getGeometryIndex());
                this.pkName = this.schema.getPrimaryKeyName();
                this.updateFeatures = new HashSet();
                this.newFeatures = new HashSet();
                this.deletedFeatures = new HashSet();
                this.lockedFeatures = new BitSet();
                this.inMemory = true;
                this.iniciado = true;
                if (!this.verifyAndCreateExtendMetadata() || !this.checkIfExitsRow()) {
                    this.insertMetadata();
                } else {
                    this.getCalculateMetadata();
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                throw new SQLException(e.getMessage());
            }
        }
        finally {
            this.closeConnection(con);
        }
        this.setPostGISFunctions();
    }

    @Override
    public Envelope getViewBox() {
        Envelope fullEnvelope = this.expandEnvelope(this.envelope, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }

    @Override
    public int size() throws Exception {
        int trueSize = this.size;
        trueSize += this.newFeatures.size();
        return trueSize -= this.deletedFeatures.size();
    }

    private boolean verifyAndCreateExtendMetadata() throws Exception {
        String verifyTableSQL = "SELECT count(*) from pg_tables WHERE tablename='extended_geometry_columns' AND schemaname='public'";
        List<Object[]> results = this.executeNonFeatureQuery(verifyTableSQL, 1);
        if (results != null && !results.isEmpty()) {
            boolean exists;
            boolean bl = exists = ((Number)results.get(0)[0]).intValue() > 0;
            if (exists) {
                return true;
            }
        }
        String consultaCreateTable = "CREATE TABLE \"public\".\"extended_geometry_columns\" (\"f_table_schema\" VARCHAR(256) NOT NULL, \"f_table_name\" VARCHAR(256) NOT NULL, \"x_min\" DOUBLE PRECISION, \"y_min\" DOUBLE PRECISION, \"x_max\" DOUBLE PRECISION, \"y_max\" DOUBLE PRECISION, \"size\" INTEGER, CONSTRAINT \"extended_geometry_columns_pkey\" PRIMARY KEY(\"f_table_schema\", \"f_table_name\") ) WITH OIDS";
        this.executeNonFeatureQuery(consultaCreateTable);
        return false;
    }

    private void getCalculateMetadata() throws SQLException {
        String sql = "SELECT x_min, y_min, x_max, y_max, size FROM \"public\".extended_geometry_columns WHERE f_table_name = '" + this.tableName + "' AND " + "f_table_schema = '" + this.dataBaseSchema + "'";
        List<Object[]> results = this.executeNonFeatureQuery(sql, 5);
        if (CollectionUtils.isNotEmpty(results)) {
            Object[] row = results.get(0);
            double xmin = ((Number)row[0]).doubleValue();
            double ymin = ((Number)row[1]).doubleValue();
            double xmax = ((Number)row[2]).doubleValue();
            double ymax = ((Number)row[3]).doubleValue();
            this.envelope = new Envelope(xmin, xmax, ymin, ymax);
            this.size = ((Number)row[4]).intValue();
        }
    }

    private boolean checkIfExitsRow() {
        String sql = "SELECT count(*) FROM \"public\".extended_geometry_columns WHERE f_table_schema='" + this.dataBaseSchema + "' and f_table_name='" + this.tableName + "'";
        List<Object[]> results = this.executeNonFeatureQuery(sql, 1);
        if (CollectionUtils.isNotEmpty(results)) {
            Object[] row = results.get(0);
            return ((Number)row[0]).intValue() > 0;
        }
        return false;
    }

    private void insertMetadata() throws Exception {
        this.envelope = this.getExactEnvelope();
        this.size = this.getExactSize();
        String sqlInsert = "INSERT INTO \"public\".\"extended_geometry_columns\" (f_table_schema, f_table_name, x_min, y_min, x_max, y_max, size) VALUES ('" + this.dataBaseSchema + "','" + this.tableName + "'," + this.envelope.getMinX() + "," + this.envelope.getMinY() + "," + this.envelope.getMaxX() + "," + this.envelope.getMaxY() + "," + this.size + ")";
        this.executeNonFeatureQuery(sqlInsert);
    }

    private Envelope getExactEnvelope() {
        Envelope envelope = new Envelope();
        Connection connection = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                Statement s = connection.createStatement();
                String sql = "SELECT extent(" + this.escapeAttributeName(this.getGeomColName()) + ") AS \"FullExtent\" FROM " + this.getTables();
                String sqlWhere = "";
                if (this.layerFilter != null) {
                    sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
                }
                if (this.schema.isVersionable()) {
                    sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                }
                sql = String.valueOf(sql) + sqlWhere;
                ResultSet r = s.executeQuery(sql);
                r.next();
                String strAux = r.getString(1);
                if (strAux == null) {
                    envelope = new Envelope();
                } else if (strAux.startsWith("BOX3D")) {
                    PGbox3d regeom = new PGbox3d(strAux);
                    envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
                } else {
                    PGbox2d regeom = new PGbox2d(strAux);
                    envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
                }
                r.close();
                s.close();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeConnection(connection);
            }
        }
        finally {
            this.closeConnection(connection);
        }
        return envelope;
    }

    public int getExactSize() throws Exception {
        int size = -1;
        Connection conn = null;
        Statement statement = null;
        ResultSet resultset = null;
        try {
            try {
                conn = DataBaseConnectionFactory.getConnection(this);
                statement = conn.createStatement();
                String sql = "SELECT COUNT(1) FROM " + this.getTables();
                String sqlWhere = "";
                if (this.layerFilter != null) {
                    sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
                }
                if (this.schema.isVersionable()) {
                    sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                }
                if ((resultset = statement.executeQuery(sql = String.valueOf(sql) + sqlWhere)).next()) {
                    size = resultset.getInt(1);
                }
                this.closeChannel(resultset, statement);
            }
            catch (SQLException ex) {
                this.closeChannel(resultset, statement);
                LOGGER.error((Object)"", (Throwable)ex);
                throw ex;
            }
        }
        finally {
            this.closeConnection(conn);
        }
        return size;
    }

    private boolean checkIfCreateExtendedMetadataTable() {
        String sql = "SELECT has_database_privilege('" + this.userName + "','" + this.dataBase + "','create')";
        return this.checkPermission(sql);
    }

    private boolean checkIfHasPermisionOfSelectInsertOrUpdateOnExtendedMetadataTable() {
        String sqlSelect = "SELECT has_table_privilege('\"public\".extended_geometry_columns', 'select')";
        String sqlInsert = "SELECT has_table_privilege('\"public\".extended_geometry_columns', 'insert')";
        String sqlUpdate = "SELECT has_table_privilege('\"public\".extended_geometry_columns', 'update')";
        return this.checkPermission(sqlSelect) && this.checkPermission(sqlInsert) && this.checkPermission(sqlUpdate);
    }

    private boolean checkPermission(String sql) {
        List<Object[]> results = this.executeNonFeatureQuery(sql, 1);
        if (results != null && !results.isEmpty()) {
            return (Boolean)results.get(0)[0] != false;
        }
        return false;
    }

    private void updateExtendedLayerMetadata() throws Exception {
        Envelope currentEnvelope = this.getViewBox();
        String sqlUpdate = "UPDATE \"public\".\"extended_geometry_columns\" SET x_min = " + currentEnvelope.getMinX() + ", y_min = " + currentEnvelope.getMinY() + "," + "x_max = " + currentEnvelope.getMaxX() + ", y_max = " + currentEnvelope.getMaxY() + "," + "size = " + this.size() + " WHERE f_table_schema = '" + this.dataBaseSchema + "' AND f_table_name = '" + this.tableName + "'";
        this.executeNonFeatureQuery(sqlUpdate);
    }

    @Override
    public void commit() throws Exception {
        this.updateExtendedLayerMetadata();
        super.commit();
    }
}

