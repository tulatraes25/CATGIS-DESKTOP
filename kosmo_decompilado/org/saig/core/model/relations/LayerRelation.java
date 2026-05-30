/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.model.relations;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;

public class LayerRelation
extends Relation<Feature> {
    private Layer targetLayer;

    public LayerRelation(String attributeSource, String attributeTarget, String relationName) {
        super(attributeSource, relationName, attributeTarget);
    }

    public Layer getTargetLayer() {
        return this.targetLayer;
    }

    public Layer getTargetLayerForXML() {
        Layer cloneLayer = new Layer();
        cloneLayer.setName(this.targetLayer.getName());
        return cloneLayer;
    }

    public void setTargetLayer(Layer targetLayer) {
        this.targetLayer = targetLayer;
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
        FeatureSchema schema = this.targetLayer.getFeatureSchema();
        int i = 0;
        while (i < this.relationFields.size()) {
            result.add(schema.getAttributeType((String)this.relationFields.get(i)));
            ++i;
        }
        return result;
    }

    @Override
    public void fillValues() throws Exception {
        if (!this.onDemmand && CollectionUtils.isNotEmpty((Collection)this.relationFields) && this.targetLayer.getFeatureCollectionWrapper() != null) {
            FeatureCollection fc = this.targetLayer.getFeatureCollectionWrapper().getUltimateWrappee();
            String[] fields = new String[this.relationFields.size()];
            int i = 0;
            while (i < fields.length) {
                fields[i] = (String)this.relationFields.get(i);
                ++i;
            }
            Map<Object, RelationAttribute> theValues = fc.getMapFieldsValues(fields, this.attributeTarget);
            this.createTemporalFile(theValues, this.targetLayer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
        }
    }

    @Override
    public AttributeType getAttributeType(String field) {
        FeatureSchema schema = this.targetLayer.getUltimateFeatureCollectionWrapper().getFeatureSchema();
        return schema.getAttributeType(field);
    }

    @Override
    public List<Feature> getRelationRecords(Object record) throws Exception {
        return this.targetLayer.getUltimateFeatureCollectionWrapper().getByAttribute(new String[]{this.attributeTarget}, new Object[]{record});
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
                    List<Feature> feats = this.targetLayer.getUltimateFeatureCollectionWrapper().getByAttribute(new String[]{this.attributeTarget}, new Object[]{record});
                    if (CollectionUtils.isEmpty(feats)) {
                        return null;
                    }
                    Feature featRecord = feats.get(0);
                    values = new Object[this.relationFields.size()];
                    int i = 0;
                    while (i < this.relationFields.size()) {
                        String field = (String)this.relationFields.get(i);
                        values[i] = featRecord.getAttribute(field);
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

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Map<Object, Object> getFieldValues(String field) {
        HashMap<Object, Object> result = new HashMap<Object, Object>();
        if (this.onDemmand) {
            FeatureCollectionWrapper fc = this.targetLayer.getFeatureCollectionWrapper();
            FeatureIterator it = null;
            try {
                try {
                    it = fc.iterator();
                    while (it.hasNext()) {
                        Feature feat = it.next();
                        Object key = feat.getAttribute(this.attributeTarget);
                        Object value = feat.getAttribute(field);
                        result.put(key, value);
                    }
                    return result;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (it == null) return result;
                    it.close();
                }
                return result;
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
                    result.put(key, values[this.relationFields.indexOf(field)]);
                    continue;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                return result;
            }
            return result;
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

    @Override
    public Set<Object> getKeysForFieldValue(String field, Object value) {
        TreeSet<Object> keys = new TreeSet<Object>();
        if (this.onDemmand) {
            FeatureCollectionWrapper fc = this.targetLayer.getFeatureCollectionWrapper();
            List<Feature> features = fc.getByAttribute(new String[]{field}, new Object[]{value});
            for (Feature feat : features) {
                keys.add(feat.getAttribute(this.attributeTarget));
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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LayerRelation)) {
            return false;
        }
        LayerRelation otherRel = (LayerRelation)other;
        return this.getRelationName().equals(otherRel.getRelationName()) && this.getTargetLayer().equals(otherRel.getTargetLayer()) && this.getAttributeTarget().equals(otherRel.getAttributeTarget()) && this.getSourceAttribute().equals(otherRel.getSourceAttribute());
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        result = result * 37 + this.getTargetLayer().hashCode();
        result = result * 37 + this.getAttributeTarget().hashCode();
        result = result * 37 + this.getSourceAttribute().hashCode();
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String fieldName) {
        TreeSet<Object> result = null;
        if (this.onDemmand) {
            result = this.targetLayer.getUltimateFeatureCollectionWrapper().getDistintsValues(fieldName);
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
                        result.add(values[this.relationFields.indexOf(fieldName)]);
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
    public Set<Object> getDistintsValues(String fieldName, int limit) {
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
}

