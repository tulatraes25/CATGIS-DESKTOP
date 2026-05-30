/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.map.LinkedMap
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Geometry;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.apache.commons.collections.map.LinkedMap;

public class AttributeType {
    private static Map nameToAttributeTypeMap = new LinkedMap();
    public static final AttributeType STRING = new AttributeType("STRING", String.class);
    public static final AttributeType CHAR = new AttributeType("CHAR", String.class);
    public static final AttributeType VARCHAR = new AttributeType("VARCHAR", String.class);
    public static final AttributeType LONGVARCHAR = new AttributeType("LONGVARCHAR", String.class);
    public static final AttributeType TEXT = new AttributeType("TEXT", String.class);
    public static final AttributeType BOOLEAN = new AttributeType("BOOLEAN", Boolean.class);
    public static final AttributeType BIT = new AttributeType("BIT", Boolean.class);
    public static final AttributeType SMALLINT = new AttributeType("SMALLINT", Short.class);
    public static final AttributeType TINYINT = new AttributeType("TINYINT", Short.class);
    public static final AttributeType INTEGER = new AttributeType("INTEGER", Integer.class);
    public static final AttributeType LONG = new AttributeType("LONG", Long.class);
    public static final AttributeType BIGINT = new AttributeType("BIGINT", Long.class);
    public static final AttributeType DECIMAL = new AttributeType("DECIMAL", BigDecimal.class);
    public static final AttributeType NUMERIC = new AttributeType("NUMERIC", BigDecimal.class);
    public static final AttributeType BIGDECIMAL = new AttributeType("BIGDECIMAL", BigDecimal.class);
    public static final AttributeType FLOAT = new AttributeType("FLOAT", Float.class);
    public static final AttributeType DOUBLE = new AttributeType("DOUBLE", Double.class);
    public static final AttributeType REAL = new AttributeType("REAL", Double.class);
    public static final AttributeType DATE = new AttributeType("DATE", java.sql.Date.class);
    public static final AttributeType TIME = new AttributeType("TIME", Time.class);
    public static final AttributeType TIMESTAMP = new AttributeType("TIMESTAMP", Timestamp.class);
    public static final AttributeType GEOMETRY = new AttributeType("GEOMETRY", Geometry.class);
    public static final AttributeType OBJECT = new AttributeType("OBJECT", Object.class);
    private String name;
    private Class<?> javaClass;

    public static Collection<AttributeType> allTypes() {
        return nameToAttributeTypeMap.values();
    }

    public static Collection<AttributeType> basicTypes() {
        ArrayList<AttributeType> basicTypes = new ArrayList<AttributeType>();
        basicTypes.add(STRING);
        basicTypes.add(INTEGER);
        basicTypes.add(LONG);
        basicTypes.add(FLOAT);
        basicTypes.add(DOUBLE);
        basicTypes.add(DATE);
        basicTypes.add(TIMESTAMP);
        basicTypes.add(BOOLEAN);
        return basicTypes;
    }

    private AttributeType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
        nameToAttributeTypeMap.put(name, this);
    }

    public AttributeType(String name) {
        this.name = name;
        this.javaClass = ((AttributeType)nameToAttributeTypeMap.get(name)).toJavaClass();
    }

    public String toString() {
        return this.name;
    }

    public static final AttributeType toAttributeType(String name) {
        AttributeType type = (AttributeType)nameToAttributeTypeMap.get(name);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return type;
    }

    public Class<?> toJavaClass() {
        return this.javaClass;
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(AttributeType.class)) {
            return false;
        }
        AttributeType tipo = (AttributeType)obj;
        return this.name.equals(tipo.getName());
    }

    public static AttributeType toAttributeType(Class<?> javaClass) {
        for (AttributeType type : AttributeType.allTypes()) {
            if (type.toJavaClass() != javaClass) continue;
            return type;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public static boolean areCompatibleTypes(AttributeType attrType1, AttributeType attrType2) {
        boolean compatible;
        boolean bl = compatible = attrType1.equals(attrType2) || attrType1.toJavaClass().equals(attrType2.toJavaClass());
        if (!compatible) {
            Class<?> javaClass1 = attrType1.toJavaClass();
            Class<?> javaClass2 = attrType2.toJavaClass();
            if (Number.class.isAssignableFrom(javaClass1) && Number.class.isAssignableFrom(javaClass2) || javaClass1.equals(String.class) && javaClass2.equals(Boolean.class) || javaClass1.equals(Boolean.class) && javaClass2.equals(String.class) || Number.class.isAssignableFrom(javaClass1) && javaClass2.equals(Boolean.class) || javaClass1.equals(Boolean.class) && Number.class.isAssignableFrom(javaClass2)) {
                compatible = true;
            }
        }
        return compatible;
    }

    public static boolean isNumeric(AttributeType type) {
        return Number.class.isAssignableFrom(type.toJavaClass());
    }

    public static boolean isNumericWithoutDecimal(AttributeType type) {
        return Long.class.isAssignableFrom(type.toJavaClass());
    }

    public static boolean isDate(AttributeType type) {
        return Date.class.isAssignableFrom(type.toJavaClass());
    }

    public static boolean isString(AttributeType type) {
        return String.class.isAssignableFrom(type.toJavaClass());
    }

    public static boolean isBoolean(AttributeType type) {
        return Boolean.class.isAssignableFrom(type.toJavaClass());
    }
}

