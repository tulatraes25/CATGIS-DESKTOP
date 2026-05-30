/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.model.relations;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;

public class TableRelation
extends Relation<Record> {
    private Table table;

    public TableRelation(String attributeSource, String attributeTarget, String relationName) {
        super(attributeSource, relationName, attributeTarget);
    }

    public Table getTable() {
        return this.table;
    }

    public Table getTableForXML() {
        Table newTable = new Table();
        newTable.setName(this.table.getName());
        return newTable;
    }

    public void setTable(Table table) {
        this.table = table;
        try {
            if (!this.onDemmand) {
                this.fillValues();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public List<AttributeType> getFieldsTypes() {
        ArrayList<AttributeType> result = new ArrayList<AttributeType>();
        FeatureSchema schema = this.table.getSchema();
        int i = 0;
        while (i < this.relationFields.size()) {
            result.add(schema.getAttributeType((String)this.relationFields.get(i)));
            ++i;
        }
        return result;
    }

    @Override
    public AttributeType getAttributeType(String field) {
        return this.table.getSchema().getAttributeType(field);
    }

    @Override
    public void fillValues() {
        String[] fields = null;
        if (this.relationFields != null) {
            fields = new String[this.relationFields.size()];
            int i = 0;
            while (i < fields.length) {
                fields[i] = (String)this.relationFields.get(i);
                ++i;
            }
            Map<Object, RelationAttribute> theValues = this.table.getMapFieldsValues(fields, this.attributeTarget);
            if (theValues != null) {
                try {
                    this.createTemporalFile(theValues, this.table.getSchema());
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
    }

    @Override
    public Object[] getRelationValues(Object record, boolean ignoreCache) {
        Object[] values;
        block22: {
            Number pos;
            block19: {
                block21: {
                    values = null;
                    if (record == null) {
                        return values;
                    }
                    if (!this.onDemmand) break block21;
                    List<Record> records = this.table.getByAttribute(new String[]{this.attributeTarget}, new Object[]{record});
                    if (CollectionUtils.isEmpty(records)) {
                        return null;
                    }
                    Record tableRecord = records.get(0);
                    values = new Object[this.relationFields.size()];
                    int i = 0;
                    while (i < this.relationFields.size()) {
                        String field = (String)this.relationFields.get(i);
                        values[i] = tableRecord.getAttribute(field);
                        ++i;
                    }
                    break block22;
                }
                if (Number.class.isAssignableFrom(record.getClass()) && !record.getClass().equals(this.keyAttrType.toJavaClass())) {
                    record = FeatureUtil.getGoodAttribute(this.keyAttrType, (Number)record);
                }
                if (!ignoreCache && (values = (Object[])this.cache.get(record)) != null) {
                    return values;
                }
                this.dbaseFileChannel.open();
                pos = (Number)this.dbfKeyCache.get(record);
                if (pos != null) break block19;
                try {
                    this.dbaseFileChannel.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                return null;
            }
            try {
                try {
                    values = this.dbaseFileChannel.getRecord(pos.longValue());
                }
                catch (IOException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    try {
                        this.dbaseFileChannel.close();
                    }
                    catch (Exception e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                }
            }
            catch (Throwable throwable) {
                try {
                    this.dbaseFileChannel.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                throw throwable;
            }
            try {
                this.dbaseFileChannel.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            if (!ignoreCache) {
                this.cache.add(record, values);
            }
        }
        return values;
    }

    @Override
    public Map<Object, Object> getFieldValues(String fieldName) {
        HashMap<Object, Object> result = new HashMap<Object, Object>();
        if (this.onDemmand) {
            ITableIterator it = null;
            try {
                it = this.table.getDataSource().getIterator();
                while (it.hasNext()) {
                    Record record = it.next();
                    Object key = record.getAttribute(this.attributeTarget);
                    Object value = record.getAttribute(fieldName);
                    result.put(key, value);
                }
            }
            finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        try {
            this.dbaseFileChannel.open();
            for (Object key : this.dbfKeyCache.keySet()) {
                Number pos = (Number)this.dbfKeyCache.get(key);
                if (pos == null) {
                    return null;
                }
                try {
                    Object[] values = this.dbaseFileChannel.getRecord(pos.longValue());
                    result.put(key, values[this.relationFields.indexOf(fieldName)]);
                    continue;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                break;
            }
        }
        finally {
            try {
                this.dbaseFileChannel.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return result;
    }

    @Override
    public Set<Object> getKeysForFieldValue(String field, Object value) {
        TreeSet<Object> keys = new TreeSet<Object>();
        if (this.onDemmand) {
            List<Record> records = this.table.getByAttribute(new String[]{field}, new Object[]{value});
            for (Record record : records) {
                keys.add(record.getAttribute(this.attributeTarget));
            }
        } else {
            try {
                this.dbaseFileChannel.open();
                for (Object key : this.dbfKeyCache.keySet()) {
                    Number pos = (Number)this.dbfKeyCache.get(key);
                    if (pos == null) {
                        return null;
                    }
                    try {
                        Object[] values = this.dbaseFileChannel.getRecord(pos.longValue());
                        if (!values[this.relationFields.indexOf(field)].equals(value)) continue;
                        keys.add(key);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
            finally {
                try {
                    this.dbaseFileChannel.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        return keys;
    }

    @Override
    public List<Record> getRelationRecords(Object key) {
        return this.table.getByAttribute(new String[]{this.attributeTarget}, new Object[]{key});
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TableRelation)) {
            return false;
        }
        TableRelation otherRel = (TableRelation)other;
        return this.getRelationName().equals(otherRel.getRelationName()) && this.getTable().equals(otherRel.getTable()) && this.getAttributeTarget().equals(otherRel.getAttributeTarget()) && this.getSourceAttribute().equals(otherRel.getSourceAttribute());
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        result = result * 37 + this.getTable().hashCode();
        result = result * 37 + this.getAttributeTarget().hashCode();
        result = result * 37 + this.getSourceAttribute().hashCode();
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        TreeSet<Object> result = null;
        if (this.onDemmand) {
            result = this.table.getDistintsValues(field);
        } else {
            result = new TreeSet();
            try {
                this.dbaseFileChannel.open();
                for (Object key : this.dbfKeyCache.keySet()) {
                    Number pos = (Number)this.dbfKeyCache.get(key);
                    if (pos == null) {
                        return null;
                    }
                    try {
                        Object[] values = this.dbaseFileChannel.getRecord(pos.longValue());
                        result.add(values[this.relationFields.indexOf(field)]);
                        continue;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                    break;
                }
            }
            finally {
                try {
                    this.dbaseFileChannel.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        return result;
    }

    /*
     * Exception decompiling
     */
    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [14[DOLOOP]], but top level block is 16[SIMPLE_IF_TAKEN]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public List<Object> getFieldValue(String field, String fieldKey, Object value) {
        ArrayList<Object> result = null;
        if (this.onDemmand) {
            result = this.table.getRelationValues(field, fieldKey, value);
        } else {
            result = new ArrayList();
            try {
                this.dbaseFileChannel.open();
                for (Object key : this.dbfKeyCache.keySet()) {
                    Number pos = (Number)this.dbfKeyCache.get(key);
                    if (pos == null) {
                        return null;
                    }
                    try {
                        Object[] values = this.dbaseFileChannel.getRecord(pos.longValue());
                        Object fieldValue = values[this.relationFields.indexOf(fieldKey)];
                        if (!fieldValue.equals(value)) continue;
                        result.add(values[this.relationFields.indexOf(field)]);
                        continue;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                    break;
                }
            }
            finally {
                try {
                    this.dbaseFileChannel.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        return result;
    }
}

