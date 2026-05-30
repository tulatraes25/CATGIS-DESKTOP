/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.relations;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileNIO;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.RelationType;
import org.saig.core.util.LRUCache;
import org.saig.jump.lang.I18N;

public abstract class Relation<T> {
    protected static final Logger LOGGER = Logger.getLogger(Relation.class);
    protected String sourceAttribute;
    private String relationName;
    protected String attributeTarget;
    protected List<String> relationFields;
    protected Map<String, String> relationFieldsPublicNames;
    protected Map<String, Boolean> relationFieldsVisibility;
    protected Map<Object, Number> dbfKeyCache;
    protected AttributeType keyAttrType;
    protected DbaseFileNIO dbaseFileChannel;
    protected LRUCache cache;
    protected File file;
    protected RelationType relationType = RelationType.JOIN;
    protected boolean onDemmand;

    public Relation(String sourceAttribute, String relationName, String attributeTarget) {
        this.sourceAttribute = sourceAttribute;
        this.relationName = relationName;
        this.attributeTarget = attributeTarget;
        this.onDemmand = false;
        this.cache = new LRUCache(100);
    }

    public String getSourceAttribute() {
        return this.sourceAttribute;
    }

    public Object[] getRelationValues(Object record) {
        return this.getRelationValues(record, false);
    }

    public abstract Object[] getRelationValues(Object var1, boolean var2);

    public abstract List<T> getRelationRecords(Object var1) throws Exception;

    public Object getFieldValue(String field, Object value) {
        Object[] values = this.getRelationValues(value);
        int index = this.relationFields.indexOf(field);
        if (values == null || index == -1) {
            return null;
        }
        return values[index];
    }

    public abstract void fillValues() throws Exception;

    public abstract Set<Object> getDistintsValues(String var1);

    public abstract Set<Object> getDistintsValues(String var1, int var2);

    public String getRelationName() {
        return this.relationName;
    }

    public String getAttributeTarget() {
        return this.attributeTarget;
    }

    public List<String> getRelationFields() {
        return this.relationFields;
    }

    public void setRelationFields(List<String> relationFields) {
        this.relationFields = relationFields;
    }

    public abstract Map<Object, Object> getFieldValues(String var1);

    public abstract AttributeType getAttributeType(String var1);

    public void eraseCache() {
        this.dbfKeyCache.clear();
        this.dbfKeyCache = null;
    }

    public String toString() {
        return this.relationName;
    }

    protected void createTemporalFile(Map<Object, RelationAttribute> values, FeatureSchema schema) throws Exception {
        String fileName = FileUtil.uniqueTempFileName(this.getRelationName(), "dbf");
        DbfFileWriter dbfFileWriter = new DbfFileWriter(fileName);
        DbfFieldDef[] dbfFieldsDefs = new DbfFieldDef[this.relationFields.size()];
        int i = 0;
        while (i < this.relationFields.size()) {
            Attribute attr = schema.getAttribute(this.relationFields.get(i));
            if (attr.getType().equals(AttributeType.STRING) || attr.getType().equals(AttributeType.CHAR) || attr.getType().equals(AttributeType.VARCHAR) || attr.getType().equals(AttributeType.LONGVARCHAR) || attr.getType().equals(AttributeType.TEXT) || attr.getType().equals(AttributeType.OBJECT)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'C', 255, 0);
            } else if (attr.getType().equals(AttributeType.INTEGER) || attr.getType().equals(AttributeType.SMALLINT) || attr.getType().equals(AttributeType.TINYINT) || attr.getType().equals(AttributeType.BIT)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'N', 32, 0);
            } else if (attr.getType().equals(AttributeType.LONG) || attr.getType().equals(AttributeType.BIGINT)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'N', 33, 0);
            } else if (attr.getType().equals(AttributeType.DOUBLE) || attr.getType().equals(AttributeType.FLOAT) || attr.getType().equals(AttributeType.REAL) || attr.getType().equals(AttributeType.NUMERIC) || attr.getType().equals(AttributeType.BIGDECIMAL) || attr.getType().equals(AttributeType.DECIMAL)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'N', 33, 16);
            } else if (attr.getType().equals(AttributeType.DATE) || attr.getType().equals(AttributeType.TIME) || attr.getType().equals(AttributeType.TIMESTAMP)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'D', 8, 0);
            } else if (attr.getType().equals(AttributeType.BOOLEAN)) {
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'L', 1, 0);
            } else {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.model.relations.Relation.Attribute-{0}-of-type-{1}-is-not-supported-it-will-be-used-a-string-instead", new Object[]{attr.getName(), attr.getType()}));
                dbfFieldsDefs[i] = new DbfFieldDef(this.relationFields.get(i), 'C', 255, 0);
            }
            ++i;
        }
        this.dbfKeyCache = new HashMap<Object, Number>();
        dbfFileWriter.writeHeader(dbfFieldsDefs, values.size());
        Iterator<Object> iterator = values.keySet().iterator();
        long cont = 0L;
        while (iterator.hasNext()) {
            Object key = iterator.next();
            this.dbfKeyCache.put(key, cont);
            RelationAttribute rel = values.get(key);
            Vector<Object> values_ = new Vector<Object>();
            int i2 = 0;
            while (i2 < this.relationFields.size()) {
                values_.add(rel.getFieldValue(this.relationFields.get(i2)));
                ++i2;
            }
            dbfFileWriter.writeRecord(values_);
            ++cont;
        }
        Object firstKey = this.dbfKeyCache.keySet().iterator().next();
        this.keyAttrType = AttributeType.toAttributeType(firstKey.getClass());
        dbfFileWriter.close();
        this.dbaseFileChannel = new DbaseFileNIO();
        this.file = new File(fileName);
        this.dbaseFileChannel.setFile(this.file);
    }

    public void destroy() {
        if (this.file != null) {
            LOGGER.info((Object)I18N.getMessage("org.saig.core.model.relations.Relation.deleting-the-file-{0}", new Object[]{this.file.getAbsolutePath()}));
            boolean deleted = this.file.delete();
            if (!deleted) {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.model.relations.Relation.The-file-{0}-could-not-be-deleted", new Object[]{this.file.getAbsolutePath()}));
            }
        }
    }

    public Map<String, String> getRelationFieldsNames() {
        return this.relationFieldsPublicNames;
    }

    public void setRelationFieldsNames(Map<String, String> relationFieldsNames) {
        this.relationFieldsPublicNames = relationFieldsNames;
    }

    public Map<String, Boolean> getRelationFieldsVisibility() {
        return this.relationFieldsVisibility;
    }

    public void setRelationFieldsVisibility(Map<String, Boolean> relationFieldsVisibility) {
        this.relationFieldsVisibility = relationFieldsVisibility;
    }

    public void setRelationFieldNameValues(String fieldName, String publicName, boolean visibility) {
        if (this.relationFields == null) {
            this.relationFields = new ArrayList<String>();
        }
        if (!this.relationFields.contains(fieldName)) {
            this.relationFields.add(fieldName);
        }
        if (this.relationFieldsPublicNames == null) {
            this.relationFieldsPublicNames = new HashMap<String, String>();
        }
        if (this.relationFieldsVisibility == null) {
            this.relationFieldsVisibility = new HashMap<String, Boolean>();
        }
        if (this.relationFieldsPublicNames.containsKey(fieldName)) {
            this.relationFieldsPublicNames.remove(fieldName);
        }
        this.relationFieldsPublicNames.put(fieldName, publicName);
        if (this.relationFieldsVisibility.containsKey(fieldName)) {
            this.relationFieldsVisibility.remove(fieldName);
        }
        this.relationFieldsVisibility.put(fieldName, visibility);
    }

    public abstract Set<Object> getKeysForFieldValue(String var1, Object var2);

    public abstract List<AttributeType> getFieldsTypes();

    public boolean isOnDemmand() {
        return this.onDemmand;
    }

    public void setOnDemmand(boolean onDemmand) {
        this.onDemmand = onDemmand;
    }

    public RelationType getRelationType() {
        return this.relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}

