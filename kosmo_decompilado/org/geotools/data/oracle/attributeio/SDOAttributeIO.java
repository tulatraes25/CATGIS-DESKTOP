/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.PrecisionModel
 *  oracle.jdbc.OracleConnection
 *  oracle.sql.STRUCT
 *  org.geotools.data.DataSourceException
 *  org.geotools.data.jdbc.QueryData
 *  org.geotools.data.jdbc.attributeio.AttributeIO
 *  org.geotools.feature.AttributeType
 */
package org.geotools.data.oracle.attributeio;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.geotools.feature.AttributeType;
import org.saig.jump.lang.I18N;

public class SDOAttributeIO
implements AttributeIO {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    GeometryConverter converter;
    private QueryData queryData;

    public SDOAttributeIO(AttributeType attributeType, QueryData queryData) throws DataSourceException {
        this.queryData = queryData;
        GeometryFactory geometryFactory = null;
        String tableName = queryData.getFeatureTypeInfo().getFeatureTypeName();
        String columnName = attributeType.getName();
        LOGGER.fine(I18N.getMessage("org.geotools.data.oracle.attributeio.SDOAttributeIO.about-to-create-geometry-convertor-for-{0}-{1}-{2}", new Object[]{tableName, ".", columnName}));
        OracleConnection oracleConnection = (OracleConnection)queryData.getConnection();
        int srid = queryData.getFeatureTypeInfo().getSRID(columnName);
        if (srid != -1) {
            PrecisionModel pm = new PrecisionModel();
            geometryFactory = new GeometryFactory(pm, srid);
        } else {
            geometryFactory = new GeometryFactory();
        }
        this.converter = new GeometryConverter((Connection)oracleConnection, geometryFactory);
    }

    public Object read(ResultSet rs, int position) throws IOException {
        try {
            Geometry geom = null;
            Object struct = rs.getObject(position);
            geom = this.converter.asGeometry((STRUCT)struct);
            return geom;
        }
        catch (SQLException e) {
            String msg = I18N.getString("org.geotools.data.oracle.attributeio.SDOAttributeIO.sql-exception-reading-geometry-column");
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, (Throwable)e);
        }
    }

    public void write(ResultSet rs, int position, Object value) throws IOException {
        try {
            Geometry geom = (Geometry)value;
            STRUCT struct = this.converter.toSDO(geom, geom.getFactory().getSRID());
            rs.updateObject(position, (Object)struct);
        }
        catch (SQLException sqlException) {
            String msg = I18N.getString("org.geotools.data.oracle.attributeio.SDOAttributeIO.sql-exception-writing-geometry-column");
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, (Throwable)sqlException);
        }
    }

    public void write(PreparedStatement ps, int position, Object value) throws IOException {
        try {
            Geometry geom = (Geometry)value;
            STRUCT struct = this.converter.toSDO(geom, geom.getFactory().getSRID());
            ps.setObject(position, struct);
        }
        catch (SQLException sqlException) {
            String msg = I18N.getString("org.geotools.data.oracle.attributeio.SDOAttributeIO.sql-exception-writing-geometry-column");
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, (Throwable)sqlException);
        }
    }
}

