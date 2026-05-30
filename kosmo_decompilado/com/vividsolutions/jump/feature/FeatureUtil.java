/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.deegree.framework.util.DateUtil;
import org.saig.core.geometry.Arc;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.NumberFormatManager;
import org.saig.jump.plugin.editing.ZManager;

public class FeatureUtil {
    private static int lastID = 0;

    public static Feature toFeature(Geometry g, FeatureSchema schema) {
        BasicFeature feature = new BasicFeature(schema);
        try {
            feature.setGeometry(g);
            if (g != null && schema.getGeometryType() == 15) {
                feature.setAttribute("Color", (Object)new Integer(0));
                feature.setAttribute("Layer", (Object)"Default");
                if (g instanceof Point || g instanceof MultiPoint) {
                    feature.setAttribute("HeightText", (Object)new Double(12.0));
                    feature.setAttribute("RotationText", (Object)new Double(0.0));
                    feature.setAttribute("Entity", (Object)"Point");
                    String shape = "Point2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Point3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                } else if (g instanceof Arc) {
                    feature.setAttribute("Entity", (Object)"Arc");
                    String shape = "Polyline2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Polyline3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                } else if (g instanceof Circle) {
                    feature.setAttribute("Entity", (Object)"Circle");
                    String shape = "Polygon2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Polygon3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                } else if (g instanceof Ellipse) {
                    feature.setAttribute("Entity", (Object)"Ellipse");
                    String shape = "Polygon2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Polygon3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                } else if (g instanceof Polygon || g instanceof MultiPolygon) {
                    feature.setAttribute("Entity", (Object)"LwPolyline");
                    String shape = "Polygon2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Polygon3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                } else if (g instanceof LineString || g instanceof MultiLineString) {
                    feature.setAttribute("Entity", (Object)"LwPolyline");
                    String shape = "Polyline2D";
                    if (FeatureUtil.is3D(g)) {
                        shape = "Polyline3D";
                    }
                    feature.setAttribute("FShape", (Object)shape);
                }
            }
            return feature;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean is3D(Geometry geometry) {
        if (geometry.isEmpty()) {
            return false;
        }
        return !Double.isNaN(geometry.getCoordinates()[0].z);
    }

    public static Feature toFeature(Geometry g, FeatureSchema schema, int geomType) {
        BasicFeature feature = new BasicFeature(schema);
        try {
            feature.setGeometry(g);
            if (schema.getGeometryType() == 15) {
                feature.setAttribute("Color", (Object)new Integer(0));
                feature.setAttribute("HeightText", (Object)new Double(12.0));
                feature.setAttribute("RotationText", (Object)new Double(0.0));
                if (geomType == 1) {
                    feature.setAttribute("Entity", (Object)"Point");
                }
            }
            return feature;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Geometry> toGeometries(Collection<Feature> features) {
        ArrayList<Geometry> list = new ArrayList<Geometry>();
        for (Feature feature : features) {
            if (feature == null) continue;
            list.add(feature.getGeometry());
        }
        return list;
    }

    public static List<LineString> toLineStrings(Collection<Feature> features) {
        ArrayList<LineString> list = new ArrayList<LineString>();
        for (Feature feature : features) {
            if (feature == null || !(feature.getGeometry() instanceof LineString)) continue;
            list.add((LineString)feature.getGeometry());
        }
        return list;
    }

    public static List<Geometry> toGeometries(FeatureIterator iterator) throws Exception {
        ArrayList<Geometry> list = new ArrayList<Geometry>();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();
            if (feature == null) continue;
            list.add(feature.getGeometry());
        }
        return list;
    }

    public static List<Geometry> toSimpleGeometries(FeatureIterator iterator) throws Exception {
        ArrayList<Geometry> list = new ArrayList<Geometry>();
        while (iterator.hasNext()) {
            Geometry geom;
            Feature feature = iterator.next();
            if (feature == null || (geom = feature.getGeometry()) == null) continue;
            int i = 0;
            while (i < geom.getNumGeometries()) {
                list.add(geom.getGeometryN(i));
                ++i;
            }
        }
        return list;
    }

    public static List<Geometry> toSimpleGeometries(Collection<Feature> features) throws Exception {
        ArrayList<Geometry> list = new ArrayList<Geometry>();
        for (Feature feature : features) {
            Geometry geom;
            if (feature == null || (geom = feature.getGeometry()) == null) continue;
            int i = 0;
            while (i < geom.getNumGeometries()) {
                list.add(geom.getGeometryN(i));
                ++i;
            }
        }
        return list;
    }

    public static Geometry[] toGeometryArray(FeatureIterator iterator) throws Exception {
        List<Geometry> geometries = FeatureUtil.toGeometries(iterator);
        return geometries.toArray(new Geometry[geometries.size()]);
    }

    public static Geometry[] toSimpleGeometryArray(FeatureIterator iterator) throws Exception {
        List<Geometry> geometries = FeatureUtil.toSimpleGeometries(iterator);
        return geometries.toArray(new Geometry[geometries.size()]);
    }

    public static Geometry[] toSimpleGeometryArray(Collection<Feature> features) throws Exception {
        List<Geometry> geometries = FeatureUtil.toSimpleGeometries(features);
        return geometries.toArray(new Geometry[geometries.size()]);
    }

    public static int nextID() {
        return ++lastID;
    }

    public static void copyAttributes(Feature a, Feature b) {
        int i = 0;
        while (i < a.getSchema().getAttributeCount()) {
            b.setAttribute(i, a.getAttribute(i));
            ++i;
        }
    }

    public static void copyOnlyExistentAttributes(Feature source, Feature target, boolean ignorePKAttr, boolean ignoreGeometryAttr) {
        FeatureSchema sourceSchema = source.getSchema();
        FeatureSchema targetSchema = target.getSchema();
        int i = 0;
        while (i < sourceSchema.getAttributeCount()) {
            Attribute attr = sourceSchema.getAttribute(i);
            if (!(ignorePKAttr && attr.isPrimaryKey() || ignoreGeometryAttr && attr.getType().equals(AttributeType.GEOMETRY) || !targetSchema.hasAttribute(attr.getName()))) {
                target.setAttribute(attr.getName(), source.getAttribute(attr.getName()));
            }
            ++i;
        }
    }

    public static Feature copyFeature(FeatureSchema schema, Feature source) {
        BasicFeature target = new BasicFeature(schema);
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Object value;
            String name = schema.getAttributeName(i);
            if (source.getSchema().hasAttribute(name) && (value = source.getAttribute(name)) != null) {
                AttributeType attrType = schema.getAttributeType(name);
                if (attrType.equals(AttributeType.DATE) || attrType.equals(AttributeType.TIMESTAMP) || attrType.equals(AttributeType.TIME) || attrType.equals(AttributeType.GEOMETRY)) {
                    target.setAttribute(name, value);
                } else {
                    target.setAttribute(name, FeatureUtil.getGoodAttribute(schema.getAttributeType(name), value));
                }
            }
            ++i;
        }
        return target;
    }

    public static Feature copyFeature(Feature source) {
        return FeatureUtil.copyFeature(source.getSchema(), source);
    }

    public static boolean areAllNonSpatialAttributesNull(Feature feature) {
        int i = 0;
        while (i < feature.getSchema().getAttributeCount()) {
            if (AttributeType.GEOMETRY != feature.getSchema().getAttributeType(i) && feature.getAttribute(i) != null) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static boolean fullEquals(Feature featureSelected, Feature featureClone) {
        boolean equals = true;
        FeatureSchema schema = featureSelected.getSchema();
        int i = 0;
        while (i < schema.getAttributeCount() && equals) {
            if (i != schema.getGeometryIndex()) {
                Object value1 = featureSelected.getAttribute(i);
                Object value2 = featureClone.getAttribute(i);
                equals = value1 != null && value2 != null ? equals && value1.equals(value2) : value1 == null && value2 == null;
            }
            ++i;
        }
        return equals;
    }

    public static Object getGoodAttribute(AttributeType type, Object value) {
        if (value == null || value.equals("null")) {
            return null;
        }
        if (type == AttributeType.STRING || type == AttributeType.CHAR || type == AttributeType.VARCHAR || type == AttributeType.LONGVARCHAR || type == AttributeType.TEXT) {
            return value.toString();
        }
        if (type == AttributeType.BOOLEAN || type == AttributeType.BIT) {
            if (StringUtils.isEmpty((String)value.toString())) {
                return null;
            }
            if (value.toString().equalsIgnoreCase("false") || value.toString().equalsIgnoreCase("f") || value.toString().equalsIgnoreCase("0") || value.toString().equalsIgnoreCase("n")) {
                return new Boolean(false);
            }
            return new Boolean(true);
        }
        if (type == AttributeType.TINYINT || type == AttributeType.SMALLINT) {
            Short shortValue;
            if (Number.class.isAssignableFrom(value.getClass())) {
                shortValue = ((Number)value).shortValue();
            } else if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                shortValue = null;
            } else {
                try {
                    shortValue = new Short(value.toString());
                }
                catch (NumberFormatException e) {
                    shortValue = new Short(FeatureUtil.convert(value.toString()));
                }
            }
            return shortValue;
        }
        if (type == AttributeType.INTEGER) {
            Integer integerValue = null;
            if (Number.class.isAssignableFrom(value.getClass())) {
                integerValue = ((Number)value).intValue();
            } else if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                integerValue = null;
            } else {
                try {
                    integerValue = Integer.valueOf(value.toString());
                }
                catch (NumberFormatException nfe) {
                    integerValue = new Integer(FeatureUtil.convert(value.toString()));
                }
            }
            return integerValue;
        }
        if (type == AttributeType.LONG || type == AttributeType.BIGINT) {
            Long longValue;
            if (Number.class.isAssignableFrom(value.getClass())) {
                longValue = ((Number)value).longValue();
            } else if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                longValue = null;
            } else {
                try {
                    longValue = new Long(value.toString());
                }
                catch (NumberFormatException nfe) {
                    longValue = new Long(FeatureUtil.convert(value.toString()));
                }
            }
            return longValue;
        }
        if (type == AttributeType.NUMERIC || type == AttributeType.BIGDECIMAL || type == AttributeType.DECIMAL) {
            BigDecimal bigDecimalValue;
            if (Number.class.isAssignableFrom(value.getClass())) {
                bigDecimalValue = new BigDecimal(((Number)value).doubleValue());
            } else if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                bigDecimalValue = null;
            } else {
                try {
                    bigDecimalValue = new BigDecimal(value.toString());
                }
                catch (NumberFormatException e) {
                    bigDecimalValue = new BigDecimal(FeatureUtil.convert(value.toString()));
                }
            }
            return bigDecimalValue;
        }
        if (type == AttributeType.FLOAT) {
            Float floatValue;
            if (Number.class.isAssignableFrom(value.getClass())) {
                floatValue = Float.valueOf(((Number)value).floatValue());
            } else if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                floatValue = null;
            } else {
                try {
                    floatValue = new Float(value.toString());
                }
                catch (NumberFormatException nfe) {
                    floatValue = new Float(FeatureUtil.convert(value.toString()));
                }
            }
            return floatValue;
        }
        if (type == AttributeType.DOUBLE || type == AttributeType.REAL) {
            Double doubleValue;
            if (value instanceof String && StringUtils.isEmpty((String)((String)value))) {
                doubleValue = null;
            } else {
                try {
                    doubleValue = Double.valueOf(value.toString());
                }
                catch (NumberFormatException nfe) {
                    doubleValue = Double.valueOf(FeatureUtil.convert(value.toString()));
                }
            }
            return doubleValue;
        }
        if (type == AttributeType.TIMESTAMP) {
            Timestamp timestampValue = null;
            if (value instanceof Timestamp) {
                timestampValue = (Timestamp)value;
            } else {
                try {
                    timestampValue = value instanceof java.util.Date ? new Timestamp(((java.util.Date)value).getTime()) : (Date.class.isAssignableFrom(value.getClass()) ? new Timestamp(((Date)value).getTime()) : new Timestamp(DateFormatManager.getDateTimeFormat().parse(value.toString()).getTime()));
                }
                catch (Exception e) {
                    try {
                        timestampValue = new Timestamp(DateFormatManager.getJDBCTimestampFormatter().parse(value.toString()).getTime());
                    }
                    catch (Exception ex) {
                        try {
                            timestampValue = new Timestamp(DateFormatManager.getDefaultDateTimeFormat().parse(value.toString()).getTime());
                        }
                        catch (Exception e2) {
                            try {
                                timestampValue = new Timestamp(DateUtil.parseISO8601Date(value.toString()).getTimeInMillis());
                            }
                            catch (Exception e3) {
                                timestampValue = null;
                            }
                        }
                    }
                }
            }
            return timestampValue;
        }
        if (type == AttributeType.TIME) {
            Time timeValue = null;
            if (value instanceof Time) {
                timeValue = (Time)value;
            } else {
                try {
                    timeValue = value instanceof java.util.Date ? new Time(((java.util.Date)value).getTime()) : (Date.class.isAssignableFrom(value.getClass()) ? new Time(((Date)value).getTime()) : new Time(DateFormatManager.getDateTimeFormat().parse(value.toString()).getTime()));
                }
                catch (Exception e) {
                    try {
                        timeValue = new Time(DateFormatManager.getDefaultDateTimeFormat().parse(value.toString()).getTime());
                    }
                    catch (Exception e2) {
                        try {
                            timeValue = new Time(DateUtil.parseISO8601Date(value.toString()).getTimeInMillis());
                        }
                        catch (Exception e3) {
                            timeValue = null;
                        }
                    }
                }
            }
            return timeValue;
        }
        if (type == AttributeType.DATE) {
            java.util.Date dateValue = null;
            if (value instanceof java.util.Date) {
                dateValue = (java.util.Date)value;
            } else {
                try {
                    dateValue = value instanceof Timestamp ? new java.util.Date(((Timestamp)value).getTime()) : (Date.class.isAssignableFrom(value.getClass()) ? new java.util.Date(((Date)value).getTime()) : new java.util.Date(DateFormatManager.getDateFormat().parse(value.toString()).getTime()));
                }
                catch (Exception e) {
                    try {
                        dateValue = new java.util.Date(DateFormatManager.getJDBCTimestampFormatter().parse(value.toString()).getTime());
                    }
                    catch (Exception ex) {
                        try {
                            dateValue = new java.util.Date(DateFormatManager.getDefaultDateFormat().parse(value.toString()).getTime());
                        }
                        catch (Exception e2) {
                            try {
                                dateValue = DateUtil.parseISO8601Date(value.toString()).getTime();
                            }
                            catch (Exception e3) {
                                dateValue = null;
                            }
                        }
                    }
                }
            }
            return dateValue;
        }
        return value;
    }

    public static Object getGoodAttribute(AttributeType type, Number value) {
        if (value == null) {
            return null;
        }
        if (type == AttributeType.STRING || type == AttributeType.CHAR || type == AttributeType.VARCHAR || type == AttributeType.LONGVARCHAR || type == AttributeType.TEXT) {
            return NumberFormatManager.getFormattedValue(value);
        }
        if (type == AttributeType.BOOLEAN || type == AttributeType.BIT) {
            if (value.doubleValue() == 0.0) {
                return new Boolean(false);
            }
            return new Boolean(true);
        }
        if (type == AttributeType.TINYINT || type == AttributeType.SMALLINT) {
            return new Short(value.shortValue());
        }
        if (type == AttributeType.INTEGER) {
            return new Integer(value.intValue());
        }
        if (type == AttributeType.LONG || type == AttributeType.BIGINT) {
            return new Long(value.longValue());
        }
        if (type == AttributeType.NUMERIC || type == AttributeType.BIGDECIMAL || type == AttributeType.DECIMAL) {
            return new BigDecimal(value.doubleValue());
        }
        if (type == AttributeType.FLOAT) {
            return new Float(value.floatValue());
        }
        if (type == AttributeType.DOUBLE || type == AttributeType.REAL) {
            return new Double(value.doubleValue());
        }
        return value;
    }

    public static String convert(String value) {
        String newValue = "";
        int lastIndex = value.lastIndexOf(".");
        newValue = value.replaceAll(",", ".");
        if (lastIndex != -1) {
            newValue = String.valueOf(newValue.substring(0, lastIndex)) + "," + newValue.substring(lastIndex + 1, newValue.length());
        }
        return newValue;
    }

    public static Object getDefaultAttributeValue(AttributeType type) {
        Object defaultValue = null;
        if (type == AttributeType.STRING || type == AttributeType.CHAR || type == AttributeType.VARCHAR || type == AttributeType.LONGVARCHAR || type == AttributeType.TEXT) {
            defaultValue = "";
        } else if (type == AttributeType.BOOLEAN || type == AttributeType.BIT) {
            defaultValue = new Boolean(true);
        } else if (type == AttributeType.TINYINT || type == AttributeType.SMALLINT) {
            defaultValue = new Short(0);
        } else if (type == AttributeType.INTEGER) {
            defaultValue = new Integer(0);
        } else if (type == AttributeType.BIGINT || type == AttributeType.LONG) {
            defaultValue = new Long(0L);
        } else if (type == AttributeType.NUMERIC || type == AttributeType.BIGDECIMAL || type == AttributeType.DECIMAL) {
            defaultValue = new BigDecimal(0.0);
        } else if (type == AttributeType.FLOAT) {
            defaultValue = new Float(0.0);
        } else if (type == AttributeType.DOUBLE || type == AttributeType.REAL) {
            defaultValue = new Double(0.0);
        } else if (type == AttributeType.DATE) {
            defaultValue = new java.util.Date();
        } else if (type == AttributeType.TIMESTAMP) {
            defaultValue = new Timestamp(System.currentTimeMillis());
        } else if (type == AttributeType.TIME) {
            defaultValue = new Time(System.currentTimeMillis());
        }
        return defaultValue;
    }

    public static void fillZs(List<Feature> feats) {
        if (ZManager.isZUseActive()) {
            double z = ZManager.getActiveZ();
            for (Feature feat : feats) {
                Geometry geom = feat.getGeometry();
                Coordinate[] coordinateArray = geom.getCoordinates();
                int n = coordinateArray.length;
                int n2 = 0;
                while (n2 < n) {
                    Coordinate cord = coordinateArray[n2];
                    if (Double.isNaN(cord.z)) {
                        cord.z = z;
                    }
                    ++n2;
                }
            }
        }
    }

    public static void convertAttributeNamesToLowerCase(Feature feature) {
        Map<String, Object> attrs = feature.getAttributes();
        HashMap<String, Object> attrsCopy = new HashMap<String, Object>();
        for (String attrName : attrs.keySet()) {
            attrsCopy.put(attrName.toLowerCase(), attrs.get(attrName));
        }
        feature.setAttributes(attrsCopy);
    }

    public static void changeGeometryAttributeName(Feature feature, String geomColumnAttrName) {
        Map<String, Object> attrs = feature.getAttributes();
        HashMap<String, Object> attrsCopy = new HashMap<String, Object>();
        String geometryAttrName = feature.getSchema().getAttribute(feature.getSchema().getGeometryIndex()).getName();
        for (String attrName : attrs.keySet()) {
            if (attrName.equals(geometryAttrName)) {
                attrsCopy.put(geomColumnAttrName, attrs.get(attrName));
                continue;
            }
            attrsCopy.put(attrName, attrs.get(attrName));
        }
        feature.setAttributes(attrsCopy);
    }

    public static class IDComparator
    implements Comparator<Feature> {
        @Override
        public int compare(Feature f1, Feature f2) {
            if (f1.getID() < f2.getID()) {
                return -1;
            }
            if (f1.getID() > f2.getID()) {
                return 1;
            }
            return 0;
        }
    }
}

