/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.jdbc.OracleDataSource;
import org.saig.core.model.data.dao.jdbc.PostgreSQLDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.Relation;
import org.saig.jump.lang.I18N;

public class FeatureSchema
implements Cloneable {
    public static final Logger LOGGER = Logger.getLogger(FeatureSchema.class);
    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final int DESCONOCIDA = 0;
    public static final int POINT = 1;
    public static final int MULTILINESTRING = 2;
    public static final int LINESTRING = 3;
    public static final int MULTIPOLYGON = 4;
    public static final int POLYGON = 5;
    public static final int MULTIPOINT = 8;
    public static final int ARC = 9;
    public static final int CIRCLE = 10;
    public static final int ELLIPSE = 11;
    public static final int MULTIPLE = 15;
    private int geometryType = 0;
    private int geometryIndex = -1;
    private int attributeCount = 0;
    private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
    private Map<Integer, String> indexAttributes = new HashMap<Integer, String>();
    private Map<String, Integer> attributesIndex = new HashMap<String, Integer>();
    private Map<String, Relation<?>> relations = new HashMap();
    private boolean versionable = false;
    private String fieldStartDate;
    private String fieldEndDate;
    private Timestamp versionableViewDate;
    private String historyField;

    public void addAttribute(String attributeName, AttributeType attributeType) {
        this.addAttribute(attributeName, attributeName, Boolean.TRUE, attributeType, Boolean.FALSE);
    }

    public void addAttribute(String attributeName, AttributeType attributeType, Boolean isPrimaryKey) {
        this.addAttribute(attributeName, attributeName, Boolean.TRUE, attributeType, isPrimaryKey);
    }

    public void addAttributeCalculate(String attributeName, AttributeType attributeType, Relation<?> relation) {
        this.addAttributeCalculate(attributeName, attributeName, attributeName, Boolean.TRUE, attributeType, relation, Boolean.FALSE);
    }

    public Attribute addAttribute(String attributeName, String publicName, Boolean visibility, AttributeType attributeType, Boolean isPrimaryKey) {
        if (AttributeType.GEOMETRY == attributeType) {
            this.geometryIndex = this.attributeCount;
        }
        Attribute atribute = new Attribute(attributeName, publicName, visibility, attributeType);
        atribute.setPrimaryKey(isPrimaryKey);
        Integer index = this.attributeCount;
        this.attributes.put(attributeName, atribute);
        this.indexAttributes.put(index, attributeName);
        this.attributesIndex.put(attributeName, index);
        ++this.attributeCount;
        return atribute;
    }

    public Attribute addAttribute(String attributeName, String publicName, Boolean visibility, AttributeType attributeType, Boolean isPrimaryKey, Map<Locale, String> titleByLang) {
        Attribute attr = this.addAttribute(attributeName, publicName, visibility, attributeType, isPrimaryKey);
        attr.setTitleByLang(titleByLang);
        return attr;
    }

    public void addAttributeCalculate(String attributeName, String publicName, String relationFieldName, Boolean visibility, AttributeType attributeType, Relation<?> relation, Boolean isPrimaryKey) {
        if (AttributeType.GEOMETRY == attributeType) {
            this.geometryIndex = this.attributeCount;
        }
        AttributeCalculate atribute = new AttributeCalculate(attributeName, publicName, relationFieldName, visibility, attributeType, relation);
        atribute.setPrimaryKey(isPrimaryKey);
        Integer index = this.attributeCount;
        this.attributes.put(attributeName, atribute);
        this.indexAttributes.put(index, attributeName);
        this.attributesIndex.put(attributeName, index);
        ++this.attributeCount;
    }

    public String toString() {
        String result = "";
        for (String attName : this.attributes.keySet()) {
            Integer index = this.attributesIndex.get(attName);
            result = String.valueOf(result) + "[" + attName + "=" + index + "]" + ",";
        }
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public void addRelation(Relation<?> relation) {
        this.relations.put(relation.getRelationName(), relation);
        List<String> fields = relation.getRelationFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            Map<String, String> fieldsPublicNames = relation.getRelationFieldsNames();
            Map<String, Boolean> fieldsVisibility = relation.getRelationFieldsVisibility();
            int i = 0;
            while (i < fields.size()) {
                String field;
                String relationFieldName = field = fields.get(i);
                String fieldPublicName = field;
                Boolean fieldVisibility = Boolean.TRUE;
                int cont = 1;
                while (this.hasAttribute(field)) {
                    field = String.valueOf(fields.get(i)) + "_" + cont;
                    ++cont;
                }
                if (fieldsPublicNames != null && fieldsPublicNames.containsKey(relationFieldName)) {
                    fieldPublicName = fieldsPublicNames.get(relationFieldName);
                } else {
                    fieldPublicName = field;
                    relation.setRelationFieldNameValues(relationFieldName, fieldPublicName, fieldVisibility);
                }
                if (fieldsVisibility != null && fieldsVisibility.containsKey(relationFieldName)) {
                    fieldVisibility = fieldsVisibility.get(relationFieldName);
                }
                this.addAttributeCalculate(field, fieldPublicName, relationFieldName, fieldVisibility, relation.getAttributeType(fields.get(i)), relation, Boolean.FALSE);
                ++i;
            }
        }
    }

    public void removeRelation(Relation<?> relation) {
        this.relations.remove(relation.getRelationName());
        ArrayList<AttributeCalculate> attrRemoveList = new ArrayList<AttributeCalculate>();
        for (Attribute attribute : this.attributes.values()) {
            AttributeCalculate attrCalculate;
            if (!(attribute instanceof AttributeCalculate) || !(attrCalculate = (AttributeCalculate)attribute).getRelation().equals(relation)) continue;
            attrRemoveList.add(attrCalculate);
        }
        for (AttributeCalculate attributeCalculate : attrRemoveList) {
            this.attributes.remove(attributeCalculate.getName());
            Integer index = this.attributesIndex.get(attributeCalculate.getName());
            this.indexAttributes.remove(index);
            this.attributesIndex.remove(attributeCalculate.getName());
            --this.attributeCount;
        }
    }

    public int buildGeometryType(Geometry geom) {
        this.geometryType = geom == null ? 0 : (geom.getClass().equals(Point.class) || geom.getClass().equals(MultiPoint.class) ? 1 : (geom.getClass().equals(LineString.class) || geom.getClass().equals(MultiLineString.class) ? 3 : (geom.getClass().equals(Polygon.class) || geom.getClass().equals(MultiPolygon.class) ? 5 : 0)));
        return this.geometryType;
    }

    public boolean changeTranslations(String attributeName, Map<Locale, String> translations) {
        if (this.hasAttribute(attributeName)) {
            Attribute attribute = this.attributes.get(attributeName);
            attribute.setTitleByLang(translations);
            return true;
        }
        return false;
    }

    public boolean changeInternalName(String attributeName, String newAttributeName) {
        if (this.hasAttribute(attributeName)) {
            Attribute attribute = this.attributes.get(attributeName);
            attribute.setName(newAttributeName);
            int index = this.getAttributeIndex(attributeName);
            this.attributes.remove(attributeName);
            this.attributes.put(newAttributeName, attribute);
            this.indexAttributes.put(index, newAttributeName);
            this.attributesIndex.put(newAttributeName, index);
            return true;
        }
        return false;
    }

    public boolean changeVisibility(String attributeName, Boolean visibility) {
        if (this.hasAttribute(attributeName)) {
            Attribute attribute = this.attributes.get(attributeName);
            attribute.setVisibility(visibility);
            return true;
        }
        return false;
    }

    public boolean hasAttribute(String attributeName) {
        return this.attributes.containsKey(attributeName);
    }

    public boolean isAttributeCalculate(String attributeName) {
        return this.getAttribute(attributeName) instanceof AttributeCalculate;
    }

    public boolean isAttributeCalculate(int index) {
        return this.getAttribute(index).isCalculated();
    }

    public int getAttributeIndex(String attributeName) {
        Integer index = this.attributesIndex.get(attributeName);
        if (index == null) {
            throw new IllegalArgumentException(String.valueOf(I18N.getString("com.vividsolutions.jump.feature.FeatureSchema.unreconigzed-attribute-name")) + ": " + attributeName);
        }
        return index;
    }

    public Object clone() {
        FeatureSchema newSchema = new FeatureSchema();
        int i = 0;
        while (i < this.getAttributeCount()) {
            Attribute attribute = this.getAttribute(i);
            if (!(attribute instanceof AttributeCalculate)) {
                newSchema.addAttribute(attribute.getName(), attribute.getPublicName(), attribute.isVisibility(), attribute.getType(), attribute.isPrimaryKey());
            }
            ++i;
        }
        newSchema.addRelations(this.recoverRelations());
        newSchema.setGeometryType(this.getGeometryType());
        return newSchema;
    }

    public boolean equals(Object other) {
        return this.equals(other, false);
    }

    public boolean equals(Object other, boolean orderMatters) {
        if (!(other instanceof FeatureSchema)) {
            return false;
        }
        FeatureSchema otherFeatureSchema = (FeatureSchema)other;
        if (this.attributes.size() != otherFeatureSchema.attributes.size()) {
            return false;
        }
        if (!orderMatters) {
            for (Attribute attribute : this.attributes.values()) {
                Attribute otherAttribute = otherFeatureSchema.getAttribute(attribute.getName());
                if (otherAttribute == null) {
                    return false;
                }
                if (otherAttribute.equals(attribute)) continue;
                return false;
            }
        } else {
            int i = 0;
            while (i < this.getAttributeCount()) {
                Attribute attr2;
                Attribute attr1 = this.getAttribute(i);
                if (!attr1.equals(attr2 = otherFeatureSchema.getAttribute(i))) {
                    return false;
                }
                ++i;
            }
        }
        return true;
    }

    public Attribute getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Attribute getAttribute(int index) {
        String name = this.indexAttributes.get(new Integer(index));
        return this.attributes.get(name);
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeName(int attributeIndex) {
        return this.indexAttributes.get(new Integer(attributeIndex));
    }

    public List<String> getAttributeNames() {
        return new ArrayList<String>(this.indexAttributes.values());
    }

    public AttributeType getAttributeType(int attributeIndex) {
        return this.attributes.get(this.indexAttributes.get(new Integer(attributeIndex))).getType();
    }

    public AttributeType getAttributeType(String attributeName) {
        if (this.hasAttribute(attributeName)) {
            return this.getAttributeType(this.getAttributeIndex(attributeName));
        }
        if (this.hasPublicAttribute(attributeName)) {
            return this.getPublicAttribute(attributeName).getType();
        }
        return null;
    }

    public Set<AttributeType> getAttributeTypes() {
        HashSet<AttributeType> types = new HashSet<AttributeType>();
        for (Attribute element : this.attributes.values()) {
            types.add(element.getType());
        }
        return types;
    }

    public int getGeometryIndex() {
        return this.geometryIndex;
    }

    public int getGeometryType() {
        return this.geometryType;
    }

    public static int getGeometryType(Geometry geom) {
        return FeatureSchema.getGeometryType(geom.getClass());
    }

    public static int getGeometryType(Class<? extends Geometry> geomClass) {
        if (geomClass.equals(Point.class)) {
            return 1;
        }
        if (geomClass.equals(MultiPoint.class)) {
            return 8;
        }
        if (geomClass.equals(LineString.class)) {
            return 3;
        }
        if (geomClass.equals(MultiLineString.class)) {
            return 2;
        }
        if (geomClass.equals(Polygon.class)) {
            return 5;
        }
        if (geomClass.equals(MultiPolygon.class)) {
            return 4;
        }
        return 0;
    }

    public int getPublicAttributeIndex(String attributeName) {
        for (Attribute element : this.attributes.values()) {
            if (!element.getPublicName().equals(attributeName)) continue;
            return this.attributesIndex.get(element.getName());
        }
        throw new IllegalArgumentException(String.valueOf(I18N.getString("com.vividsolutions.jump.feature.FeatureSchema.unreconigzed-attribute-name")) + ": " + attributeName);
    }

    public Attribute getPublicAttribute(String attributeName) {
        int index = this.getPublicAttributeIndex(attributeName);
        return this.getAttribute(index);
    }

    public String getPublicName(int attributeIndex) {
        return this.attributes.get(this.indexAttributes.get(new Integer(attributeIndex))).getPublicName();
    }

    public List<String> getPublicNames() {
        ArrayList<String> publicNames = new ArrayList<String>();
        for (Attribute element : this.attributes.values()) {
            publicNames.add(element.getPublicName());
        }
        return publicNames;
    }

    public Collection<Relation<?>> recoverRelations() {
        return this.relations.values();
    }

    public void addRelations(Collection<Relation<?>> col) {
        for (Relation<?> element : col) {
            this.addRelation(element);
        }
    }

    public Boolean getVisibility(int attributeIndex) {
        return this.attributes.get(this.indexAttributes.get(new Integer(attributeIndex))).isVisibility();
    }

    public boolean hasPublicAttribute(String attributeName) {
        Iterator<String> iterator = this.attributes.keySet().iterator();
        while (iterator.hasNext()) {
            Attribute attr = this.attributes.get(iterator.next());
            if (!attr.getPublicName().equals(attributeName)) continue;
            return true;
        }
        return false;
    }

    public void setGeometryType(int geomType) {
        this.geometryType = geomType;
    }

    public Attribute getPrimaryKey() {
        for (Attribute element : this.attributes.values()) {
            if (!element.isPrimaryKey()) continue;
            return element;
        }
        return null;
    }

    public int getPrimaryKeyIndex() {
        Attribute att = this.getPrimaryKey();
        if (att != null) {
            return this.attributesIndex.get(att.getName());
        }
        return -1;
    }

    public String getPrimaryKeyName() {
        Attribute att = this.getPrimaryKey();
        if (att != null) {
            return att.getName();
        }
        return null;
    }

    public Map<String, Attribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, Attribute> attrMap) {
        this.attributes = attrMap;
    }

    public Map<String, Integer> getAttributesIndex() {
        return this.attributesIndex;
    }

    public void setAttributesIndex(Map<String, Integer> attrIndexMap) {
        this.attributesIndex = attrIndexMap;
    }

    public Map<Integer, String> getIndexAttributes() {
        return this.indexAttributes;
    }

    public void setIndexAttributes(Map<Integer, String> indexAttrMap) {
        this.indexAttributes = indexAttrMap;
    }

    public void setGeometryIndex(int geometryIndex) {
        this.geometryIndex = geometryIndex;
    }

    public boolean hasCalculatedAttributes() {
        boolean hasCalculated = false;
        int i = 0;
        while (i < this.attributes.size() && !hasCalculated) {
            hasCalculated = this.getAttribute(i) instanceof AttributeCalculate;
            ++i;
        }
        return hasCalculated;
    }

    public boolean isVersionable() {
        return this.versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
        if (!versionable) {
            this.fieldStartDate = null;
            this.fieldEndDate = null;
        }
    }

    public void setVersionableViewDate(Timestamp versionableViewDate) {
        this.versionableViewDate = versionableViewDate;
    }

    public Timestamp getVersionableViewDate() {
        return this.versionableViewDate;
    }

    public String getFieldStartDate() {
        return this.fieldStartDate;
    }

    public void setFieldStartDate(String fieldStartDate) {
        this.fieldStartDate = fieldStartDate;
    }

    public String getFieldEndDate() {
        return this.fieldEndDate;
    }

    public void setFieldEndDate(String fieldEndDate) {
        this.fieldEndDate = fieldEndDate;
    }

    public String getHistoryField() {
        return this.historyField;
    }

    public void setHistoryField(String historyField) {
        this.historyField = historyField;
    }

    public String getEndDateFilter(AbstractJDBCDataSource dataSource) {
        boolean isQuote = dataSource instanceof PostGisDataSource;
        String consulta = "";
        if (this.isVersionable()) {
            Relation<?> rel;
            String tableName;
            if (this.versionableViewDate != null) {
                String fechaToString = "";
                fechaToString = dataSource instanceof OracleSpatialDataSource || dataSource instanceof PostGisDataSource ? "to_timestamp('" + DATE_FORMATER.format(this.versionableViewDate) + "','yyyy-mm-dd hh24:mi:ss')" : "date_format('" + DATE_FORMATER.format(this.versionableViewDate) + "','%Y-%m-%d %H:%i:%s')";
                consulta = !isQuote ? String.valueOf(dataSource.getFullTableName()) + "." + this.getFieldStartDate() + " <= " + fechaToString + " AND (" + dataSource.getFullTableName() + "." + this.getFieldEndDate() + " IS null OR " + dataSource.getFullTableName() + "." + this.getFieldEndDate() + " > " + fechaToString + ")" : String.valueOf(dataSource.getFullTableName()) + ".\"" + this.getFieldStartDate() + "\" <= " + fechaToString + " AND (" + dataSource.getFullTableName() + ".\"" + this.getFieldEndDate() + "\" IS null OR " + dataSource.getFullTableName() + ".\"" + this.getFieldEndDate() + "\" > " + fechaToString + ")";
            } else {
                consulta = !isQuote ? String.valueOf(dataSource.getFullTableName()) + "." + this.getFieldEndDate() + " IS null" : String.valueOf(dataSource.getFullTableName()) + ".\"" + this.getFieldEndDate() + "\" IS null";
            }
            Attribute attrEndDate = this.getAttribute(this.getFieldEndDate());
            Attribute attrStartDate = this.getAttribute(this.getFieldStartDate());
            if (attrStartDate.isCalculated() && !(tableName = dataSource.getTableNameOfRelation(rel = ((AttributeCalculate)attrStartDate).getRelation())).equals("")) {
                consulta = !isQuote ? dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + "." + ((AttributeCalculate)attrStartDate).getName(), ((AttributeCalculate)attrStartDate).getRelationFieldName(), tableName) : dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + ".\"" + ((AttributeCalculate)attrStartDate).getName() + "\"", ((AttributeCalculate)attrStartDate).getRelationFieldName(), tableName);
            }
            if (attrEndDate.isCalculated() && !(tableName = dataSource.getTableNameOfRelation(rel = ((AttributeCalculate)attrEndDate).getRelation())).equals("")) {
                consulta = !isQuote ? dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + "." + ((AttributeCalculate)attrEndDate).getName(), ((AttributeCalculate)attrEndDate).getRelationFieldName(), tableName) : dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + ".\"" + ((AttributeCalculate)attrEndDate).getName() + "\"", ((AttributeCalculate)attrEndDate).getRelationFieldName(), tableName);
            }
        }
        return consulta;
    }

    public String getEndDateFilter(TableDBRecordDataSource dataSource) {
        String consulta = "";
        if (this.isVersionable()) {
            Relation<?> rel;
            String tableName;
            if (this.versionableViewDate != null) {
                String fechaToString = "";
                fechaToString = dataSource instanceof OracleDataSource || dataSource instanceof PostgreSQLDataSource ? "to_timestamp('" + DATE_FORMATER.format(this.versionableViewDate) + "','yyyy-mm-dd hh24:mi:ss')" : "date_format('" + DATE_FORMATER.format(this.versionableViewDate) + "','%Y-%m-%d %H:%i:%s')";
                consulta = !(dataSource instanceof PostgreSQLDataSource) ? String.valueOf(dataSource.getFullTableName()) + "." + this.getFieldStartDate() + " <= " + fechaToString + " AND (" + dataSource.getFullTableName() + "." + this.getFieldEndDate() + " IS null OR " + dataSource.getFullTableName() + "." + this.getFieldEndDate() + " > " + fechaToString + ")" : String.valueOf(dataSource.getFullTableName()) + ".\"" + this.getFieldStartDate() + "\" <= " + fechaToString + " AND (" + dataSource.getFullTableName() + ".\"" + this.getFieldEndDate() + "\" IS null OR " + dataSource.getFullTableName() + ".\"" + this.getFieldEndDate() + "\" > " + fechaToString + ")";
            } else {
                consulta = !(dataSource instanceof PostgreSQLDataSource) ? String.valueOf(dataSource.getFullTableName()) + "." + this.getFieldEndDate() + " IS NULL" : String.valueOf(dataSource.getFullTableName()) + ".\"" + this.getFieldEndDate() + "\" IS NULL";
            }
            Attribute attrEndDate = this.getAttribute(this.getFieldEndDate());
            Attribute attrStartDate = this.getAttribute(this.getFieldStartDate());
            if (attrStartDate.isCalculated() && !(tableName = dataSource.getTableNameOfRelation(rel = ((AttributeCalculate)attrStartDate).getRelation())).equals("")) {
                consulta = dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + "." + ((AttributeCalculate)attrStartDate).getName(), ((AttributeCalculate)attrStartDate).getRelationFieldName(), tableName);
            }
            if (attrEndDate.isCalculated() && !(tableName = dataSource.getTableNameOfRelation(rel = ((AttributeCalculate)attrEndDate).getRelation())).equals("")) {
                consulta = dataSource.processSQLExpressionSinComillas(consulta, String.valueOf(dataSource.getFullTableName()) + "." + ((AttributeCalculate)attrEndDate).getName(), ((AttributeCalculate)attrEndDate).getRelationFieldName(), tableName);
            }
        }
        return consulta;
    }

    public Map<Locale, String> getTranslations(int attributeIndex) {
        return this.attributes.get(this.indexAttributes.get(new Integer(attributeIndex))).getTitleByLang();
    }

    public String getAttributeName(String publicName) {
        boolean found = false;
        String attrName = null;
        Iterator<Attribute> iter = this.attributes.values().iterator();
        while (iter.hasNext() && !found) {
            Attribute element = iter.next();
            if (!element.getPublicName().equals(publicName)) continue;
            attrName = element.getName();
            found = true;
        }
        return attrName;
    }
}

