/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  oracle.sql.ARRAY
 *  oracle.sql.Datum
 *  oracle.sql.STRUCT
 *  org.geotools.data.DataSourceException
 *  org.geotools.data.FeatureReader
 *  org.geotools.data.Transaction
 *  org.geotools.data.jdbc.ConnectionPool
 *  org.geotools.data.jdbc.DefaultSQLBuilder
 *  org.geotools.data.jdbc.FeatureTypeInfo
 *  org.geotools.data.jdbc.JDBCDataStore
 *  org.geotools.data.jdbc.JDBCDataStoreConfig
 *  org.geotools.data.jdbc.JDBCFeatureWriter
 *  org.geotools.data.jdbc.JDBCUtils
 *  org.geotools.data.jdbc.QueryData
 *  org.geotools.data.jdbc.SQLBuilder
 *  org.geotools.data.jdbc.attributeio.AttributeIO
 *  org.geotools.feature.AttributeType
 *  org.geotools.feature.AttributeTypeFactory
 *  org.geotools.filter.SQLEncoder
 *  org.geotools.geometry.JTS$ReferencedEnvelope
 *  org.geotools.referencing.CRS
 *  org.opengis.referencing.FactoryException
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 */
package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;
import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.oracle.OracleFeatureWriter;
import org.geotools.data.oracle.attributeio.SDOAttributeIO;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderOracle;
import org.geotools.geometry.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.saig.jump.lang.I18N;

public class OracleDataStore
extends JDBCDataStore {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");

    public OracleDataStore(ConnectionPool connectionPool, JDBCDataStoreConfig config) throws IOException {
        super(connectionPool, config);
    }

    public OracleDataStore(ConnectionPool connectionPool, String schemaName, Map fidGeneration) throws IOException {
        this(connectionPool, schemaName, schemaName, fidGeneration);
    }

    public OracleDataStore(ConnectionPool connectionPool, String namespace, String schemaName, Map fidGeneration) throws IOException {
        this(connectionPool, new JDBCDataStoreConfig(namespace, schemaName, null, fidGeneration));
    }

    protected boolean allowTable(String tablename) {
        LOGGER.finer(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleDataStore.checking-table-name")) + ": " + tablename);
        if (tablename.endsWith("$")) {
            return false;
        }
        if (tablename.startsWith("BIN$")) {
            return false;
        }
        if (tablename.startsWith("XDB$")) {
            return false;
        }
        if (tablename.startsWith("DR$")) {
            return false;
        }
        if (tablename.startsWith("DEF$")) {
            return false;
        }
        if (tablename.startsWith("SDO_")) {
            return false;
        }
        if (tablename.startsWith("WM$")) {
            return false;
        }
        if (tablename.startsWith("WK$")) {
            return false;
        }
        if (tablename.startsWith("AW$")) {
            return false;
        }
        if (tablename.startsWith("AQ$")) {
            return false;
        }
        if (tablename.startsWith("APPLY$")) {
            return false;
        }
        if (tablename.startsWith("REPCAT$")) {
            return false;
        }
        if (tablename.startsWith("CWM$")) {
            return false;
        }
        if (tablename.startsWith("CWM2$")) {
            return false;
        }
        if (tablename.startsWith("EXF$")) {
            return false;
        }
        if (tablename.startsWith("DM$")) {
            return false;
        }
        LOGGER.finer(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleDataStore.returning-true-for-tablename")) + ": " + tablename);
        return true;
    }

    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        int TABLE_NAME = 3;
        int COLUMN_NAME = 4;
        int TYPE_NAME = 6;
        int IS_NULLABLE = 18;
        try {
            if (rs.getString(6).equals("SDO_GEOMETRY")) {
                String tableName = rs.getString(3);
                String columnName = rs.getString(4);
                String isNullable = rs.getString(18);
                return this.getSDOGeometryAttribute(tableName, columnName, "YES".equals(isNullable));
            }
            return super.buildAttributeType(rs);
        }
        catch (SQLException e) {
            throw new DataSourceException(I18N.getString("org.geotools.data.oracle.OracleDataStore.sql-error-occurred"), (Throwable)e);
        }
    }

    private AttributeType getSDOGeometryAttribute(String tableName, String columnName, boolean isNullable) {
        int srid = 0;
        try {
            srid = this.determineSRID(tableName, columnName);
            CoordinateReferenceSystem crs = this.determineCRS(srid);
            if (crs != null) {
                return AttributeTypeFactory.newAttributeType((String)columnName, Geometry.class, (boolean)isNullable, (int)0, null, (Object)crs);
            }
        }
        catch (IOException e) {
            LOGGER.warning(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleDataStore.could-not-map-srid")) + " " + srid + " " + I18N.getString("org.geotools.data.oracle.OracleDataStore.to-crs") + e);
        }
        return AttributeTypeFactory.newAttributeType((String)columnName, Geometry.class, (boolean)isNullable);
    }

    protected CoordinateReferenceSystem determineCRS(int srid) throws IOException {
        ResultSet set;
        String wkt;
        block4: {
            Connection conn = this.getConnection(Transaction.AUTO_COMMIT);
            wkt = null;
            Statement st = conn.createStatement();
            st.execute("select wktext from cs_srs where srid = " + srid);
            set = st.getResultSet();
            if (set.next()) break block4;
            set.close();
            st.close();
            return null;
        }
        try {
            wkt = set.getString(1);
            return CRS.parseWKT((String)wkt);
        }
        catch (FactoryException parse) {
            throw (IOException)new IOException(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleDataStore.unabled-to-parse-wktext-into-a-crs")) + ": " + wkt).initCause(parse);
        }
        catch (SQLException sql) {
            throw (IOException)new IOException(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleDataStore.no-crs-for-srid")) + ": " + srid).initCause(sql);
        }
    }

    protected int determineSRID(String tableName, String geometryColumnName) throws IOException {
        Connection conn;
        block5: {
            conn = null;
            String sqlStatement = "SELECT SRID FROM ALL_SDO_GEOM_METADATA WHERE TABLE_NAME='" + tableName + "' AND COLUMN_NAME='" + geometryColumnName + "'";
            conn = this.getConnection(Transaction.AUTO_COMMIT);
            LOGGER.finer("the sql statement for srid is " + sqlStatement);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);
            if (!result.next()) break block5;
            int retSrid = result.getInt("srid");
            JDBCUtils.close((Statement)statement);
            int n = retSrid;
            JDBCUtils.close((Connection)conn, (Transaction)Transaction.AUTO_COMMIT, null);
            return n;
        }
        try {
            try {
                String mesg = I18N.getMessage("org.geotools.data.oracle.OracleDataStore.no-geometry-column-row-for-srid-in-table-{0}-{1}-{2}-geometry-column-{3}-{4}-be-sure-column-is-defined-in-user_sdogeommetadada", new Object[]{": ", tableName, ", ", geometryColumnName, ", "});
                throw new DataSourceException(mesg);
            }
            catch (SQLException sqle) {
                String message = sqle.getMessage();
                throw new DataSourceException(message, (Throwable)sqle);
            }
        }
        catch (Throwable throwable) {
            JDBCUtils.close(conn, (Transaction)Transaction.AUTO_COMMIT, null);
            throw throwable;
        }
    }

    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        FeatureTypeInfo info = this.typeHandler.getFeatureTypeInfo(typeName);
        SQLEncoderOracle encoder = new SQLEncoderOracle(info.getSRIDs());
        encoder.setFIDMapper(this.getFIDMapper(typeName));
        return new DefaultSQLBuilder((SQLEncoder)encoder);
    }

    protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) throws IOException {
        return new SDOAttributeIO(type, queryData);
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader, QueryData queryData) throws IOException {
        return new OracleFeatureWriter(fReader, queryData);
    }

    public Envelope getEnvelope(String typeName) {
        Connection conn = null;
        try {
            conn = this.getConnection(Transaction.AUTO_COMMIT);
            Statement st = conn.createStatement();
            st.execute("SELECT srid,diminfo FROM USER_SDO_GEOM_METADATA where TABLE_NAME = 'ORA_TEST_LINES'");
            ResultSet set = st.getResultSet();
            set.next();
            int srid = set.getInt(1);
            CoordinateReferenceSystem crs = this.determineCRS(srid);
            ARRAY array = (ARRAY)set.getObject(2);
            Datum[] data = array.getOracleArray();
            double minx = Double.NaN;
            double miny = Double.NaN;
            double maxx = Double.NaN;
            double maxy = Double.NaN;
            int i = 0;
            while (i < data.length) {
                Datum datum = data[i];
                System.out.println(datum.getClass());
                STRUCT diminfo = (STRUCT)datum;
                Datum[] info = diminfo.getOracleAttributes();
                String ord = info[0].stringValue();
                double min = info[1].doubleValue();
                double max = info[2].doubleValue();
                if ("X".equalsIgnoreCase(ord)) {
                    minx = min;
                    maxx = max;
                }
                if ("Y".equalsIgnoreCase(ord)) {
                    miny = min;
                    maxy = max;
                }
                ++i;
            }
            Envelope extent = new Envelope(minx, maxx, miny, maxy);
            JTS.ReferencedEnvelope ref = new JTS.ReferencedEnvelope(extent, crs);
            set.close();
            st.close();
            JTS.ReferencedEnvelope referencedEnvelope = ref;
            return referencedEnvelope;
        }
        catch (Exception erp) {
            LOGGER.warning(erp.toString());
            return null;
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException sQLException) {}
            }
        }
    }
}

