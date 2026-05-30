/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.datatypes.UnknownTypeException
 *  org.deegree.framework.util.BootLogger
 *  org.deegree.ogcbase.CommonNamespaces
 */
package org.deegree.datatypes;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.util.BootLogger;
import org.deegree.ogcbase.CommonNamespaces;
import org.saig.jump.lang.I18N;

public final class Types {
    private static Logger LOGGER = Logger.getLogger(Types.class);
    private static URI GMLNS = CommonNamespaces.GMLNS;
    public static final int ARRAY = 2003;
    public static final int BIGINT = -5;
    public static final int BINARY = -2;
    public static final int BIT = -7;
    public static final int BLOB = 2004;
    public static final int BOOLEAN = 16;
    public static final int CHAR = 1;
    public static final int CLOB = 2005;
    public static final int DATALINK = 70;
    public static final int DATE = 91;
    public static final int DECIMAL = 3;
    public static final int DISTINCT = 2001;
    public static final int DOUBLE = 8;
    public static final int LONG = -5;
    public static final int FLOAT = 6;
    public static final int INTEGER = 4;
    public static final int JAVA_OBJECT = 2000;
    public static final int LONGVARBINARY = -4;
    public static final int LONGVARCHAR = -1;
    public static final int NULL = 0;
    public static final int NUMERIC = 2;
    public static final int OTHER = 1111;
    public static final int REAL = 7;
    public static final int REF = 2006;
    public static final int SMALLINT = 5;
    public static final int STRUCT = 2002;
    public static final int TIME = 92;
    public static final int TIMESTAMP = 93;
    public static final int TINYINT = -6;
    public static final int VARBINARY = -3;
    public static final int VARCHAR = 12;
    public static final int GEOMETRY = 10012;
    public static final int MULTIGEOMETRY = 10013;
    public static final int FEATURE = 10014;
    public static final int FEATURECOLLECTION = 10016;
    public static final int POINT = 11012;
    public static final int CURVE = 11013;
    public static final int SURFACE = 11014;
    public static final int MULTIPOINT = 11015;
    public static final int MULTICURVE = 11016;
    public static final int MULTISURFACE = 11017;
    public static final int ENVELOPE = 11018;
    public static final int ANYTYPE = 11019;
    public static final QualifiedName GEOMETRY_PROPERTY_NAME = new QualifiedName("GeometryPropertyType", GMLNS);
    public static final QualifiedName MULTI_GEOMETRY_PROPERTY_NAME = new QualifiedName("MultiGeometryPropertyType", GMLNS);
    public static final QualifiedName FEATURE_PROPERTY_NAME = new QualifiedName("FeaturePropertyType", GMLNS);
    public static final QualifiedName FEATURE_ARRAY_PROPERTY_NAME = new QualifiedName("FeatureArrayPropertyType", GMLNS);
    private static Map<Integer, String> typeNameMap = new HashMap<Integer, String>();
    private static Map<String, Integer> typeCodeMap = new HashMap<String, Integer>();

    static {
        try {
            Field[] fields = java.sql.Types.class.getFields();
            int i = 0;
            while (i < fields.length) {
                String typeName = fields[i].getName();
                Integer typeCode = (Integer)fields[i].get(null);
                typeNameMap.put(typeCode, typeName);
                typeCodeMap.put(typeName, typeCode);
                ++i;
            }
        }
        catch (Exception e) {
            BootLogger.logError((String)("Error populating sql type code maps: " + e.getMessage()), (Throwable)e);
        }
    }

    public static int getTypeCodeForSQLType(String typeName) throws UnknownTypeException {
        Integer typeCode = typeCodeMap.get(typeName);
        if (typeCode == null) {
            throw new UnknownTypeException(String.valueOf(I18N.getString("org.deegree.datatypes.Types.type-name")) + " '" + typeName + "' " + I18N.getString("org.deegree.datatypes.Types.does-not-denote-an-sql-type") + ".");
        }
        return typeCode;
    }

    public static String getTypeNameForSQLTypeCode(int typeCode) throws UnknownTypeException {
        String typeName = typeNameMap.get(new Integer(typeCode));
        if (typeName == null) {
            throw new UnknownTypeException(String.valueOf(I18N.getString("org.deegree.datatypes.Types.type-code")) + " '" + typeCode + "' " + I18N.getString("org.deegree.datatypes.Types.does-not-denote-an-sql-type") + ".");
        }
        return typeName;
    }

    public static int getJavaTypeForGMLType(String gmlTypeName) throws UnknownTypeException {
        if ("GeometryPropertyType".equals(gmlTypeName)) {
            return 10012;
        }
        if ("MultiGeometryPropertyType".equals(gmlTypeName)) {
            return 10013;
        }
        if ("PointPropertyType".equals(gmlTypeName)) {
            return 11012;
        }
        if ("MultiPointPropertyType".equals(gmlTypeName)) {
            return 11015;
        }
        if ("LineStringPropertyType".equals(gmlTypeName)) {
            return 11013;
        }
        if ("MultiLineStringPropertyType".equals(gmlTypeName)) {
            return 11016;
        }
        if ("CurvePropertyType".equals(gmlTypeName)) {
            return 11013;
        }
        if ("MultiCurvePropertyType".equals(gmlTypeName)) {
            return 11016;
        }
        if ("PolygonPropertyType".equals(gmlTypeName)) {
            return 11014;
        }
        if ("MultiPolygonPropertyType".equals(gmlTypeName)) {
            return 11017;
        }
        if ("SurfacePropertyType".equals(gmlTypeName)) {
            return 11014;
        }
        if ("MultiSurfacePropertyType".equals(gmlTypeName)) {
            return 11017;
        }
        throw new UnknownTypeException(String.valueOf(I18N.getString("org.deegree.datatypes.Types.unsupported-type")) + ": '" + gmlTypeName + "'");
    }

    public static int getJavaTypeForXSDType(String schemaTypeName) throws UnknownTypeException {
        if ("integer".equalsIgnoreCase(schemaTypeName) || "int".equalsIgnoreCase(schemaTypeName)) {
            return 4;
        }
        if ("string".equalsIgnoreCase(schemaTypeName) || "character".equalsIgnoreCase(schemaTypeName)) {
            return 12;
        }
        if ("date".equalsIgnoreCase(schemaTypeName)) {
            return 91;
        }
        if ("boolean".equalsIgnoreCase(schemaTypeName)) {
            return 16;
        }
        if ("float".equalsIgnoreCase(schemaTypeName)) {
            return 6;
        }
        if ("long".equalsIgnoreCase(schemaTypeName)) {
            return -5;
        }
        if ("double".equalsIgnoreCase(schemaTypeName) || "real".equalsIgnoreCase(schemaTypeName)) {
            return 8;
        }
        if ("decimal".equalsIgnoreCase(schemaTypeName)) {
            return 3;
        }
        if ("dateTime".equalsIgnoreCase(schemaTypeName)) {
            return 93;
        }
        if ("time".equalsIgnoreCase(schemaTypeName)) {
            return 92;
        }
        if ("date".equalsIgnoreCase(schemaTypeName)) {
            return 91;
        }
        if ("short".equalsIgnoreCase(schemaTypeName)) {
            return 5;
        }
        if ("object".equalsIgnoreCase(schemaTypeName) || "base64Binary".equalsIgnoreCase(schemaTypeName) || "hexBinary".equalsIgnoreCase(schemaTypeName)) {
            return 2000;
        }
        throw new UnknownTypeException(String.valueOf(I18N.getString("org.deegree.datatypes.Types.unsupported-type")) + ":" + schemaTypeName);
    }

    public static String getXSDTypeForSQLType(int type, int precision) {
        String s = null;
        switch (type) {
            case 1: 
            case 12: {
                s = "string";
                break;
            }
            case 2: {
                if (precision <= 1) {
                    s = "integer";
                    break;
                }
                s = "double";
                break;
            }
            case 3: {
                s = "decimal";
                break;
            }
            case 7: 
            case 8: {
                s = "double";
                break;
            }
            case 6: {
                s = "float";
                break;
            }
            case -5: 
            case 4: 
            case 5: {
                s = "integer";
                break;
            }
            case 93: {
                s = "dateTime";
                break;
            }
            case 92: {
                s = "time";
                break;
            }
            case 91: {
                s = "date";
                break;
            }
            case 2005: {
                s = "string";
                break;
            }
            case -7: 
            case 16: {
                s = "boolean";
                break;
            }
            case 1111: 
            case 2002: 
            case 10012: {
                s = "gml:GeometryPropertyType";
                break;
            }
            case 10014: {
                s = "gml:FeaturePropertyType";
                break;
            }
            default: {
                LOGGER.warn((Object)(String.valueOf(I18N.getString("org.deegree.datatypes.Types.could-not-determine-xsdttype-for-sqltype")) + "; " + I18N.getString("org.deegree.datatypes.Types.using") + " 'XXX': " + type));
                s = "code: " + type;
            }
        }
        return s;
    }
}

