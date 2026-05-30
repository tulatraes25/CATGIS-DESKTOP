/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateList
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  oracle.sql.ARRAY
 *  oracle.sql.ArrayDescriptor
 *  oracle.sql.CHAR
 *  oracle.sql.CharacterSet
 *  oracle.sql.Datum
 *  oracle.sql.NUMBER
 *  oracle.sql.STRUCT
 *  oracle.sql.StructDescriptor
 */
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.CHAR;
import oracle.sql.CharacterSet;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import org.geotools.data.oracle.sdo.SDO;

public class GeometryConverter {
    protected Connection connection;
    GeometryFactory geometryFactory;
    public static final String DATATYPE = "MDSYS.SDO_GEOMETRY";

    public GeometryConverter(Connection connection) {
        this(connection, new GeometryFactory());
    }

    public GeometryConverter(Connection connection, GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.connection = connection;
    }

    public String getDataTypeName() {
        return DATATYPE;
    }

    public boolean isCapable(Geometry geom) {
        if (geom == null) {
            return true;
        }
        if (geom instanceof Point || geom instanceof MultiPoint || geom instanceof LineString || geom instanceof MultiLineString || geom instanceof Polygon || geom instanceof MultiPolygon || geom instanceof GeometryCollection) {
            int d = SDO.D(geom);
            int l = SDO.L(geom);
            return l == 0 && (d == 2 || d == 3);
        }
        return false;
    }

    public Geometry asGeometry(STRUCT sdoGeometry) throws SQLException {
        if (sdoGeometry == null) {
            return null;
        }
        ResultSetMetaData meta = sdoGeometry.getDescriptor().getMetaData();
        Datum[] data = sdoGeometry.getOracleAttributes();
        int GTYPE = this.asInteger(data[0], 0);
        int SRID = this.asInteger(data[1], -1);
        double[] POINT = this.asDoubleArray((STRUCT)data[2], Double.NaN);
        int[] ELEMINFO = this.asIntArray((ARRAY)data[3], 0);
        double[] ORDINATES = this.asDoubleArray((ARRAY)data[4], Double.NaN);
        return SDO.create(this.geometryFactory, GTYPE, SRID, POINT, ELEMINFO, ORDINATES);
    }

    public STRUCT toSDO(Geometry geom, int srid) throws SQLException {
        ARRAY SDO_ORDINATES;
        ARRAY SDO_ELEM_INFO;
        STRUCT SDO_POINT;
        if (geom == null || geom.isEmpty()) {
            return this.asEmptyDataType();
        }
        int gtype = SDO.gType(geom);
        NUMBER SDO_GTYPE = new NUMBER(gtype);
        NUMBER SDO_SRID = srid == -1 ? null : new NUMBER(srid);
        double[] point = SDO.point(geom);
        if (point == null) {
            int[] elemInfo = SDO.elemInfo(geom);
            double[] ordinates = SDO.ordinates(geom);
            SDO_POINT = null;
            SDO_ELEM_INFO = this.toARRAY(elemInfo, "MDSYS.SDO_ELEM_INFO_ARRAY");
            SDO_ORDINATES = this.toARRAY(ordinates, "MDSYS.SDO_ORDINATE_ARRAY");
        } else {
            Datum[] data = new Datum[]{this.toNUMBER(point[0]), this.toNUMBER(point[1]), this.toNUMBER(point[2])};
            SDO_POINT = this.toSTRUCT(data, "MDSYS.SDO_POINT_TYPE");
            SDO_ELEM_INFO = null;
            SDO_ORDINATES = null;
        }
        Datum[] attributes = new Datum[]{SDO_GTYPE, SDO_SRID, SDO_POINT, SDO_ELEM_INFO, SDO_ORDINATES};
        return this.toSTRUCT(attributes, DATATYPE);
    }

    protected STRUCT asEmptyDataType() throws SQLException {
        return this.toSTRUCT(null, DATATYPE);
    }

    protected final STRUCT toSTRUCT(Datum[] attributes, String dataType) throws SQLException {
        if (dataType.startsWith("*.")) {
            dataType = "DRA." + dataType.substring(2);
        }
        StructDescriptor descriptor = StructDescriptor.createDescriptor((String)dataType, (Connection)this.connection);
        return new STRUCT(descriptor, this.connection, (Object[])attributes);
    }

    protected final ARRAY toARRAY(double[] doubles, String dataType) throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor((String)dataType, (Connection)this.connection);
        return new ARRAY(descriptor, this.connection, (Object)doubles);
    }

    protected final ARRAY toORDINATE(CoordinateList list, double[][] measures, int D) throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor((String)"MDSYS.SDO_ORDINATE_ARRAY", (Connection)this.connection);
        int LENGTH = measures != null ? measures.length : 0;
        int LEN = D + LENGTH;
        Datum[] data = new Datum[list.size() * LEN];
        int offset = 0;
        int index = 0;
        for (Coordinate coord : list) {
            data[offset++] = this.toNUMBER(coord.x);
            data[offset++] = this.toNUMBER(coord.y);
            if (D == 3) {
                data[offset++] = this.toNUMBER(coord.x);
            }
            int j = 0;
            while (j < LENGTH) {
                data[offset++] = this.toNUMBER(measures[j][index]);
                ++j;
            }
            ++index;
        }
        return new ARRAY(descriptor, this.connection, (Object)data);
    }

    protected final ARRAY toORDINATE(double[] ords) throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor((String)"MDSYS.SDO_ORDINATE_ARRAY", (Connection)this.connection);
        int LENGTH = ords.length;
        Datum[] data = new Datum[LENGTH];
        int i = 0;
        while (i < LENGTH) {
            data[i] = this.toNUMBER(ords[i]);
            ++i;
        }
        return new ARRAY(descriptor, this.connection, (Object)data);
    }

    protected final ARRAY toATTRIBUTE(double[] ords, String desc) throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor((String)desc, (Connection)this.connection);
        int LENGTH = ords.length;
        Datum[] data = new Datum[LENGTH];
        int i = 0;
        while (i < LENGTH) {
            data[i] = this.toNUMBER(ords[i]);
            ++i;
        }
        return new ARRAY(descriptor, this.connection, (Object)data);
    }

    protected final NUMBER toNUMBER(double number) throws SQLException {
        if (Double.isNaN(number)) {
            return null;
        }
        return new NUMBER(number);
    }

    protected final ARRAY toARRAY(int[] ints, String dataType) throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor((String)dataType, (Connection)this.connection);
        return new ARRAY(descriptor, this.connection, (Object)ints);
    }

    protected final NUMBER toNUMBER(int number) {
        return new NUMBER(number);
    }

    protected final CHAR toCHAR(String s) {
        if (s.length() > 1) {
            s = new String(new Character(s.charAt(0)).toString());
        }
        try {
            return new CHAR(s, CharacterSet.make((int)31));
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected int asInteger(Datum datum, int DEFAULT) throws SQLException {
        if (datum == null) {
            return DEFAULT;
        }
        return ((NUMBER)datum).intValue();
    }

    protected double asDouble(Datum datum, double DEFAULT) throws SQLException {
        if (datum == null) {
            return DEFAULT;
        }
        return ((NUMBER)datum).doubleValue();
    }

    protected double[] asDoubleArray(STRUCT struct, double DEFAULT) throws SQLException {
        if (struct == null) {
            return null;
        }
        return this.asDoubleArray(struct.getOracleAttributes(), DEFAULT);
    }

    protected double[] asDoubleArray(ARRAY array, double DEFAULT) throws SQLException {
        if (array == null) {
            return null;
        }
        if (DEFAULT == 0.0) {
            return array.getDoubleArray();
        }
        return this.asDoubleArray(array.getOracleArray(), DEFAULT);
    }

    protected double[] asDoubleArray(Datum[] data, double DEFAULT) throws SQLException {
        if (data == null) {
            return null;
        }
        double[] array = new double[data.length];
        int i = 0;
        while (i < data.length) {
            array[i] = this.asDouble(data[i], DEFAULT);
            ++i;
        }
        return array;
    }

    protected int[] asIntArray(ARRAY array, int DEFAULT) throws SQLException {
        if (array == null) {
            return null;
        }
        if (DEFAULT == 0) {
            return array.getIntArray();
        }
        return this.asIntArray(array.getOracleArray(), DEFAULT);
    }

    protected int[] asIntArray(Datum[] data, int DEFAULT) throws SQLException {
        if (data == null) {
            return null;
        }
        int[] array = new int[data.length];
        int i = 0;
        while (i < data.length) {
            array[i] = this.asInteger(data[i], DEFAULT);
            ++i;
        }
        return array;
    }
}

