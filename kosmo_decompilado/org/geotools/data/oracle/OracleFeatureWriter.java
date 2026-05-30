/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  oracle.jdbc.OracleConnection
 *  oracle.sql.STRUCT
 *  org.geotools.data.DataSourceException
 *  org.geotools.data.FeatureReader
 *  org.geotools.data.jdbc.JDBCTextFeatureWriter
 *  org.geotools.data.jdbc.MutableFIDFeature
 *  org.geotools.data.jdbc.QueryData
 *  org.geotools.feature.AttributeType
 *  org.geotools.feature.Feature
 *  org.geotools.feature.FeatureType
 *  org.geotools.feature.type.GeometricAttributeType
 */
package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.MutableFIDFeature;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.type.GeometricAttributeType;
import org.saig.jump.lang.I18N;

public class OracleFeatureWriter
extends JDBCTextFeatureWriter {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    GeometryConverter converter;

    public OracleFeatureWriter(FeatureReader fReader, QueryData queryData) throws IOException {
        super(fReader, queryData);
        this.converter = new GeometryConverter((Connection)((OracleConnection)queryData.getConnection()));
    }

    protected String getGeometryInsertText(Geometry geom, int srid) throws IOException {
        return "?";
    }

    protected void doInsert(MutableFIDFeature current) throws IOException, SQLException {
        LOGGER.fine(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleFeatureWriter.inserting-into-postgis-feature")) + " " + current);
        Statement statement = null;
        Connection conn = null;
        try {
            try {
                conn = this.queryData.getConnection();
                String sql = this.makeInsertSql((Feature)current);
                statement = conn.prepareStatement(sql);
                int position = 1;
                FeatureType schema = current.getFeatureType();
                int i = 0;
                while (i < current.getNumberOfAttributes()) {
                    AttributeType type = schema.getAttributeType(i);
                    if (type instanceof GeometricAttributeType) {
                        Geometry geometry = (Geometry)current.getAttribute(i);
                        STRUCT struct = this.converter.toSDO(geometry, geometry.getFactory().getSRID());
                        statement.setObject(position, struct);
                        ++position;
                    }
                    ++i;
                }
                LOGGER.fine(sql);
                statement.execute();
                if (this.mapper.getColumnCount() > 0 && this.mapper.hasAutoIncrementColumns()) {
                    current.setID(this.mapper.createID(conn, (Feature)current, statement));
                }
            }
            catch (SQLException sqle) {
                String msg = String.valueOf(I18N.getString("org.geotools.data.oracle.OracleFeatureWriter.sql-exception-writing-geometry-column")) + sqle.getLocalizedMessage();
                LOGGER.log(Level.SEVERE, msg, sqle);
                this.queryData.close(sqle);
                throw new DataSourceException(msg, (Throwable)sqle);
            }
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    String msg = I18N.getString("org.geotools.data.oracle.OracleFeatureWriter.error-closing-jdbc-statement");
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }
}

