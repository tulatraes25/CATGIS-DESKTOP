/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.index.strtree.STRtree
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.MapUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import es.kosmo.core.model.relations.topology.OperationType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.memory.FeatureDatasetIterator;
import org.saig.core.dao.datasource.memory.MemoryFullIterator;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.model.data.trigger.IPostAddTrigger;
import org.saig.core.model.data.trigger.IPostDeleteTrigger;
import org.saig.core.model.data.trigger.IPostUpdateTrigger;
import org.saig.core.model.data.trigger.IPreAddTrigger;
import org.saig.core.model.data.trigger.IPreDeleteTrigger;
import org.saig.core.model.data.trigger.IPreUpdateTrigger;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.data.trigger.TriggerFactory;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.DummyDataSourceWrapper;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.stats.StatsOperatorsFactory;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class FeatureDataset
implements FeatureCollection {
    private static final Logger LOGGER = Logger.getLogger(FeatureDataset.class);
    protected static final Set<String> AVAILABLE_FEATURE_DATASET_OPERATORS = new HashSet<String>();
    protected FeatureSchema featureSchema;
    protected List<Feature> features;
    protected Envelope envelope = null;
    protected String name;
    protected Hashtable<Feature, Feature> updateFeatures;
    protected Set<Feature> newFeatures;
    protected Hashtable<Feature, Feature> deletedFeatures;
    protected boolean inMemory = true;
    private List<ITopologyRelation> topologyRelations = new ArrayList<ITopologyRelation>();
    protected Set<ITrigger> triggers = new LinkedHashSet<ITrigger>();
    protected boolean editable;
    protected boolean is3d;
    protected STRtree index;
    protected Filter layerFilter;

    static {
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_AVG");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_COUNT");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_COUNT_NO");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_COUNT_YES");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_FIRST");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_LAST");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_MAX");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_MIN");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_STANDARD_DEVIANCE");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_SUM");
        AVAILABLE_FEATURE_DATASET_OPERATORS.add("OP_VARIANCE");
    }

    public FeatureDataset(Collection<Feature> newFeatures, FeatureSchema featureSchema) {
        this.features = new ArrayList<Feature>();
        this.newFeatures = new HashSet<Feature>();
        this.deletedFeatures = new Hashtable();
        this.updateFeatures = new Hashtable();
        this.featureSchema = featureSchema;
        for (Feature feature : newFeatures) {
            feature.setAttribute(featureSchema.getPrimaryKeyIndex(), null);
            try {
                this.addWithNewKey(feature);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        if (CollectionUtils.isNotEmpty(newFeatures)) {
            this.createSpatialIndex();
        }
    }

    public FeatureDataset(FeatureSchema featureSchema) {
        this(new ArrayList<Feature>(), featureSchema);
    }

    public Feature getFeature(int index) {
        Feature feature = this.features.get(index);
        if (this.layerFilter == null || this.layerFilter.contains(feature)) {
            return feature;
        }
        return null;
    }

    public Feature getRealFeature(Feature feat) {
        Feature solucion = null;
        if (this.deletedFeatures.contains(feat)) {
            return null;
        }
        solucion = this.updateFeatures.contains(feat) ? this.updateFeatures.get(feat) : feat;
        return solucion;
    }

    @Override
    public FeatureSchema getFeatureSchema() {
        return this.featureSchema;
    }

    @Override
    public Envelope getEnvelope() {
        if (this.envelope == null) {
            FeatureIterator i = null;
            try {
                try {
                    i = this.iterator();
                    while (i.hasNext()) {
                        Feature feature = i.next();
                        if (feature.getGeometry() == null) continue;
                        if (this.envelope == null) {
                            this.envelope = new Envelope(feature.getGeometry().getEnvelopeInternal());
                            continue;
                        }
                        this.envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (i != null) {
                        i.close();
                    }
                }
            }
            finally {
                if (i != null) {
                    i.close();
                }
            }
            return this.envelope;
        }
        Envelope fullEnvelope = this.expandEnvelope(this.envelope, this.newFeatures);
        return this.expandEnvelope(fullEnvelope, this.updateFeatures.values());
    }

    @Override
    public Envelope getEnvelope(Filter filter) throws Exception {
        if (filter == null) {
            return this.getEnvelope();
        }
        Envelope envelope = new Envelope();
        FeatureIterator i = null;
        try {
            try {
                i = new FeatureDatasetIterator(this, this.getFullFilter(filter), null);
                while (i.hasNext()) {
                    Feature feature = i.next();
                    if (feature.getGeometry() == null) continue;
                    if (envelope == null) {
                        envelope = feature.getGeometry().getEnvelopeInternal();
                        continue;
                    }
                    envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (i != null) {
                    i.close();
                }
            }
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
        return envelope;
    }

    protected Envelope expandEnvelope(Envelope envelope, Collection<Feature> features) {
        if (features == null) {
            return envelope;
        }
        Envelope fullEnvelope = null;
        if (envelope != null) {
            fullEnvelope = envelope;
        }
        for (Feature element : features) {
            if (this.layerFilter != null && !this.layerFilter.contains(element) || element.getGeometry() == null) continue;
            if (fullEnvelope == null) {
                fullEnvelope = element.getGeometry().getEnvelopeInternal();
                continue;
            }
            fullEnvelope.expandToInclude(element.getGeometry().getEnvelopeInternal());
        }
        return fullEnvelope;
    }

    @Override
    public List<Feature> getFeatures() {
        if (this.layerFilter == null) {
            return this.features;
        }
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        for (Feature currentFeat : this.features) {
            if (!this.layerFilter.contains(currentFeat)) continue;
            resultado.add(currentFeat);
        }
        return resultado;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public FeatureIterator queryIterator(Envelope env) {
        return new FeatureDatasetIterator(this, this.getFullFilter(null), env);
    }

    @Override
    public List<Feature> query(Envelope envelope) {
        return this.query(envelope, null);
    }

    private Filter getFullFilter(Filter filter) {
        Filter filter_ = null;
        if (filter == null) {
            if (this.layerFilter != null) {
                filter_ = (Filter)((Cloneable)this.layerFilter).clone();
            }
        } else {
            filter_ = this.layerFilter != null ? filter.and(this.layerFilter) : filter;
        }
        return filter_;
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) {
        Envelope currentEnvelope;
        if (envelope == null) {
            envelope = this.envelope;
        }
        if ((currentEnvelope = this.getEnvelope()) == null || !envelope.intersects(currentEnvelope)) {
            return new ArrayList<Feature>();
        }
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        FeatureIterator iter = null;
        try {
            try {
                iter = this.queryIterator(filter, envelope);
                while (iter.hasNext()) {
                    resultado.add(iter.next());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (iter != null) {
                    iter.close();
                }
            }
        }
        finally {
            if (iter != null) {
                iter.close();
            }
        }
        return resultado;
    }

    public void addAllWithNewKey(Collection<Feature> featuresToAdd) throws Exception {
        this.checkTopologicRelations(featuresToAdd);
        if (this.launchPreAddTriggers(featuresToAdd)) {
            for (Feature element : featuresToAdd) {
                Integer key = new Integer(this.features.size());
                element.setAttribute(this.featureSchema.getPrimaryKeyIndex(), (Object)key);
                if (element.getGeometry() != null) {
                    if (this.envelope == null) {
                        this.envelope = new Envelope(element.getGeometry().getEnvelopeInternal());
                    } else {
                        this.envelope.expandToInclude(element.getGeometry().getEnvelopeInternal());
                    }
                }
                this.features.add(element);
            }
            this.launchPostAddTriggers(featuresToAdd);
        }
    }

    public void addWithNewKey(Feature feature) throws Exception {
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        featuresToAdd.add(feature);
        this.addAllWithNewKey(featuresToAdd);
    }

    @Override
    public void add(Feature feature) throws Exception {
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        featuresToAdd.add(feature);
        this.addAll(featuresToAdd);
    }

    public boolean contains(Feature feature) {
        return this.features.contains(feature);
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        List<Feature> features = this.query(env);
        this.removeAll(features);
        return features;
    }

    @Override
    public void remove(Feature feature) throws Exception {
        ArrayList<Feature> featuresToRemove = new ArrayList<Feature>();
        featuresToRemove.add(feature);
        this.removeAll(featuresToRemove);
    }

    @Override
    public void clear() {
        this.features.clear();
        this.newFeatures.clear();
        this.updateFeatures.clear();
        this.deletedFeatures.clear();
    }

    @Override
    public int size() {
        return this.features.size() - this.deletedFeatures.size() + this.newFeatures.size();
    }

    @Override
    public FeatureIterator iterator() {
        return new FeatureDatasetIterator(this, this.getFullFilter(null), null);
    }

    @Override
    public void addAll(Collection<Feature> featuresToAdd) throws Exception {
        if (this.envelope == null) {
            this.envelope = this.getEnvelope();
        }
        this.checkTopologicRelations(featuresToAdd);
        if (this.launchPreAddTriggers(featuresToAdd)) {
            if (this.inMemory) {
                for (Feature element : featuresToAdd) {
                    boolean wasDeleted = this.deletedFeatures.contains(element);
                    if (!wasDeleted) {
                        element.setAttribute(this.featureSchema.getPrimaryKeyIndex(), null);
                    }
                    if (element.isUnsaved()) {
                        this.newFeatures.add(element);
                    } else {
                        this.updateFeatures.put(element, element);
                    }
                    if (!wasDeleted) continue;
                    this.deletedFeatures.remove(element);
                }
            }
            this.launchPostAddTriggers(featuresToAdd);
        }
    }

    @Override
    public void removeAll(Collection<Feature> removingFeatures) throws Exception {
        this.checkTopologicRelations(removingFeatures, OperationType.REMOVE);
        if (this.launchPreRemoveTriggers(this.features)) {
            if (this.inMemory) {
                for (Feature element : removingFeatures) {
                    if (this.newFeatures.contains(element)) {
                        this.newFeatures.remove(element);
                        continue;
                    }
                    if (this.updateFeatures.containsKey(element)) {
                        this.updateFeatures.remove(element);
                    }
                    this.deletedFeatures.put(element, element);
                }
            }
            this.launchPostRemoveTriggers(this.features);
        }
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryIterator(Filter filter, Envelope envelope) {
        return new FeatureDatasetIterator(this, this.getFullFilter(filter), envelope);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Envelope envelope, List<String> labels) {
        return this.queryIterator(envelope);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Filter filter, Envelope envelope, List<String> labels) {
        return this.queryIterator(filter, envelope);
    }

    @Override
    public void commit() throws Exception {
        Integer key;
        this.inMemory = false;
        this.envelope = null;
        int index = 0;
        ArrayList<Feature> resultados = new ArrayList<Feature>(this.size());
        for (Feature feature : this.features) {
            if (this.updateFeatures.contains(feature)) {
                Feature feat = this.updateFeatures.get(feature);
                Integer key2 = new Integer(index);
                feat.setAttribute(this.featureSchema.getPrimaryKeyIndex(), (Object)key2);
                ++index;
                resultados.add(feat);
                if (feature.getGeometry() == null) continue;
                if (this.envelope == null) {
                    this.envelope = new Envelope(feature.getGeometry().getEnvelopeInternal());
                    continue;
                }
                this.envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
                continue;
            }
            if (this.deletedFeatures.contains(feature)) continue;
            key = new Integer(index);
            feature.setAttribute(this.featureSchema.getPrimaryKeyIndex(), (Object)key);
            ++index;
            resultados.add(feature);
            if (feature.getGeometry() == null) continue;
            if (this.envelope == null) {
                this.envelope = new Envelope(feature.getGeometry().getEnvelopeInternal());
                continue;
            }
            this.envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
        }
        for (Feature feature : this.newFeatures) {
            key = new Integer(index);
            feature.setAttribute(this.featureSchema.getPrimaryKeyIndex(), (Object)key);
            if (feature.getGeometry() != null) {
                if (this.envelope == null) {
                    this.envelope = new Envelope(feature.getGeometry().getEnvelopeInternal());
                } else {
                    this.envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
                }
            }
            ++index;
            resultados.add(feature);
        }
        this.updateFeatures.clear();
        this.deletedFeatures.clear();
        this.newFeatures.clear();
        this.features.clear();
        this.features = resultados;
        this.inMemory = true;
        this.createSpatialIndex();
    }

    @Override
    public void rollBack() {
        this.updateFeatures.clear();
        this.deletedFeatures.clear();
        this.newFeatures.clear();
    }

    @Override
    public void update(Feature feature) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        this.updateAll(features);
    }

    @Override
    public void updateAll(Collection<Feature> featuresToUpdate) throws Exception {
        this.checkTopologicRelations(featuresToUpdate);
        if (this.launchPreUpdateTriggers(featuresToUpdate)) {
            if (this.inMemory) {
                for (Feature object : featuresToUpdate) {
                    if (!object.isUnsaved()) {
                        this.updateFeatures.remove(object);
                        this.updateFeatures.put(object, object);
                        if (!this.deletedFeatures.contains(object)) continue;
                        this.deletedFeatures.remove(object);
                        continue;
                    }
                    this.newFeatures.remove(object);
                    this.newFeatures.add(object);
                }
            }
            this.launchPostUpdateTriggers(featuresToUpdate);
        }
    }

    @Override
    public List<Feature> query(Filter filter) {
        ArrayList<Feature> result = new ArrayList<Feature>();
        FeatureIterator iter = null;
        try {
            try {
                iter = this.queryIterator(filter, null);
                while (iter.hasNext()) {
                    Feature element = iter.next();
                    result.add(element);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (iter != null) {
                    iter.close();
                }
            }
        }
        finally {
            if (iter != null) {
                iter.close();
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public List<Feature> getFeaturesSamples(int n) {
        ArrayList<Feature> features = new ArrayList<Feature>();
        FeatureIterator it = null;
        try {
            try {
                it = this.iterator();
                int i = 0;
                while (i < n) {
                    if (!it.hasNext()) {
                        return features;
                    }
                    Feature obj = it.next();
                    if (obj != null) {
                        features.add(obj);
                    }
                    ++i;
                }
                return features;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it == null) return features;
                it.close();
                return features;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public List<Feature> getByAttribute(String[] campos, Object[] valores) {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        FeatureIterator it = null;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    boolean condition = true;
                    int i = 0;
                    while (i < campos.length && condition) {
                        String campo = campos[i];
                        Object valor = valores[i];
                        condition = feat.getAttribute(campo).equals(valor);
                        ++i;
                    }
                    if (!condition) continue;
                    resultado.add(feat);
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                ArrayList<Feature> arrayList = new ArrayList<Feature>();
                if (it != null) {
                    it.close();
                }
                return arrayList;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return resultado;
    }

    @Override
    public List<Object> getKeys() {
        ArrayList<Object> keys = new ArrayList<Object>();
        FeatureIterator it = null;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    Feature element = it.next();
                    if (element.isUnsaved()) {
                        keys.add(element);
                        continue;
                    }
                    keys.add(element.getPrimaryKey());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return keys;
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof Feature) {
            return (Feature)key;
        }
        int intKey = (Integer)key;
        if (intKey > this.features.size() - 1) {
            return null;
        }
        return this.getRealFeature(this.features.get(intKey));
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        TreeSet<SortedAttribute> sort;
        block16: {
            Object key;
            Object value;
            Feature element;
            boolean isString = this.featureSchema.getAttribute(column).getType().toJavaClass().equals(String.class);
            sort = new TreeSet<SortedAttribute>();
            if (values == null) {
                FeatureIterator iter = null;
                try {
                    try {
                        iter = this.iterator();
                        while (iter.hasNext()) {
                            element = iter.next();
                            value = element.getAttribute(column);
                            key = element.getPrimaryKey();
                            if (key == null) {
                                sort.add(new SortedAttribute(value, element, ascending, isString));
                                continue;
                            }
                            sort.add(new SortedAttribute(value, key, ascending, isString));
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (iter == null) break block16;
                        iter.close();
                        break block16;
                    }
                }
                catch (Throwable throwable) {
                    if (iter != null) {
                        iter.close();
                    }
                    throw throwable;
                }
                if (iter != null) {
                    iter.close();
                }
            } else {
                int i = 0;
                while (i < values.length) {
                    element = null;
                    element = values[i] instanceof Feature ? (Feature)values[i] : this.getByPrimaryKey(values[i]);
                    if (element != null) {
                        value = element.getAttribute(column);
                        key = element.getPrimaryKey();
                        if (key == null) {
                            sort.add(new SortedAttribute(value, element, ascending, isString));
                        } else {
                            sort.add(new SortedAttribute(value, key, ascending, isString));
                        }
                    }
                    ++i;
                }
            }
        }
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        sort = null;
        System.gc();
        return result;
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] keys) {
        ArrayList<Feature> result = new ArrayList<Feature>();
        int i = 0;
        while (i < keys.length) {
            if (keys[i] != null) {
                result.add(this.getByPrimaryKey(keys[i]));
            }
            ++i;
        }
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.getDistintsValues(field, Integer.MAX_VALUE);
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        if (!this.featureSchema.hasAttribute(field)) {
            return null;
        }
        Object result = null;
        Attribute attr = this.featureSchema.getAttribute(field);
        if (attr instanceof AttributeCalculate) {
            return ((AttributeCalculate)attr).getRelation().getFieldValue(field, value);
        }
        FeatureIterator it = null;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    result = feat.getAttribute(field);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return result;
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        if (!this.featureSchema.hasAttribute(fieldKey)) {
            return values;
        }
        int i = 0;
        while (i < fields.length) {
            String field = fields[i];
            if (!this.featureSchema.hasAttribute(field)) {
                return values;
            }
            ++i;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            String field = fields[i2];
            Attribute attr = this.featureSchema.getAttribute(field);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        FeatureIterator it = null;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    RelationAttribute ra = new RelationAttribute();
                    int i3 = 0;
                    while (i3 < attrNoCalculate.size()) {
                        ra.setFieldValue(((Attribute)attrNoCalculate.get(i3)).getName(), feat.getAttribute(((Attribute)attrNoCalculate.get(i3)).getName()));
                        ++i3;
                    }
                    values.put(feat.getAttribute(fieldKey), ra);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return values;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        FeatureIterator it = null;
        int cont = 0;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    if (cont >= limit) {
                        return values;
                    }
                    Object value = it.next().getAttribute(field);
                    if (value == null) continue;
                    values.add(value);
                    ++cont;
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it == null) return values;
                it.close();
                return values;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.getDistintsValues(expr, Integer.MAX_VALUE);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        FeatureIterator it = null;
        int cont = 0;
        try {
            try {
                it = this.iterator();
                while (it.hasNext()) {
                    if (cont >= limit) {
                        return values;
                    }
                    Object value = expr.getValue(it.next());
                    if (value == null) continue;
                    values.add(value);
                    ++cont;
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it == null) return values;
                it.close();
                return values;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public void setFeatureSchema(FeatureSchema schema) {
        this.featureSchema = schema;
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered) {
        return this.getByAttribute(fields, values, fieldOrdered, true);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, Filter filter) {
        return this.getByAttribute(fields, values, fieldOrdered);
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public boolean isCad() {
        return false;
    }

    public Set<Feature> getNewFeatures() {
        return this.newFeatures;
    }

    public Collection<Feature> getUpdatedFeatures() {
        return this.updateFeatures.values();
    }

    public Collection<Feature> getDeletedFeatures() {
        return this.deletedFeatures.values();
    }

    @Override
    public void createSpatialIndex() {
        long t1 = System.currentTimeMillis();
        this.index = new STRtree();
        Iterator<Feature> it = this.features.iterator();
        try {
            while (it.hasNext()) {
                Feature feat = it.next();
                if (feat.getGeometry() == null) continue;
                this.index.insert(feat.getGeometry().getEnvelopeInternal(), (Object)feat);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.index.build();
        LOGGER.debug((Object)("Construcci\u00f3n del \u00edndice en memoria STRTree " + (System.currentTimeMillis() - t1) + " ms"));
    }

    @Override
    public boolean isSpatialIndex() {
        return this.index != null;
    }

    @Override
    public void setTopologyRelations(List<ITopologyRelation> topologyRelations) {
        this.topologyRelations = topologyRelations;
    }

    protected void checkTopologicRelations(Collection<Feature> features) throws Exception {
        this.checkTopologicRelations(features, OperationType.UNSPECIFIED);
    }

    protected void checkTopologicRelations(Collection<Feature> features, OperationType type) throws Exception {
        if (this.topologyRelations.isEmpty()) {
            return;
        }
        boolean check = true;
        Iterator<Feature> itFeats = features.iterator();
        while (itFeats.hasNext() && check) {
            Feature currentFeat = itFeats.next();
            Iterator<ITopologyRelation> itRelations = this.topologyRelations.iterator();
            while (itRelations.hasNext() && check) {
                ITopologyRelation relation = itRelations.next();
                if (!relation.isEnabled()) continue;
                OperationType opType = relation.getOperationType();
                try {
                    relation.setOperationType(type);
                }
                finally {
                    relation.setOperationType(opType);
                }
                if (check &= relation.check(currentFeat, features)) continue;
                relation.failure(currentFeat, features);
            }
        }
        if (check) {
            Iterator<ITopologyRelation> itRelations = this.topologyRelations.iterator();
            while (itRelations.hasNext() && check) {
                ITopologyRelation currentRelation = itRelations.next();
                currentRelation.success(features);
            }
        }
    }

    protected boolean launchPreRemoveTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return true;
        }
        boolean ok = true;
        Collection<IPreDeleteTrigger> deletedTriggers = TriggerFactory.getInstance().filterPreDeleteTriggers(this.triggers);
        for (IPreDeleteTrigger deleteTrigger : deletedTriggers) {
            ok &= deleteTrigger.onDelete(features);
        }
        return ok;
    }

    protected void launchPostRemoveTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return;
        }
        Collection<IPostDeleteTrigger> deletedTriggers = TriggerFactory.getInstance().filterPostDeleteTriggers(this.triggers);
        for (IPostDeleteTrigger deleteTrigger : deletedTriggers) {
            deleteTrigger.onDelete(features);
        }
    }

    protected boolean launchPreAddTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return true;
        }
        boolean ok = true;
        Collection<IPreAddTrigger> addTriggers = TriggerFactory.getInstance().filterPreAddTriggers(this.triggers);
        for (IPreAddTrigger addTrigger : addTriggers) {
            ok &= addTrigger.onAdd(features);
        }
        return ok;
    }

    protected void launchPostAddTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return;
        }
        Collection<IPostAddTrigger> addTriggers = TriggerFactory.getInstance().filterPostAddTriggers(this.triggers);
        for (IPostAddTrigger addTrigger : addTriggers) {
            addTrigger.onAdd(features);
        }
    }

    protected boolean launchPreUpdateTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return true;
        }
        boolean ok = true;
        Collection<IPreUpdateTrigger> updateTriggers = TriggerFactory.getInstance().filterPreUpdateTriggers(this.triggers);
        for (IPreUpdateTrigger updateTrigger : updateTriggers) {
            ok &= updateTrigger.onUpdate(features);
        }
        return ok;
    }

    protected void launchPostUpdateTriggers(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.triggers)) {
            return;
        }
        Collection<IPostUpdateTrigger> updateTriggers = TriggerFactory.getInstance().filterPostUpdateTriggers(this.triggers);
        for (IPostUpdateTrigger updateTrigger : updateTriggers) {
            updateTrigger.onUpdate(features);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<int[]> getIntervalSelection() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void invertSelection() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Filter getLayerFilter() {
        return this.layerFilter;
    }

    @Override
    public void setLayerFilter(Filter layerFilter) {
        this.layerFilter = layerFilter;
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        throw new UnsupportedOperationException(I18N.getString("com.vividsolutions.jump.feature.FeatureDataset.Not-supported-operation"));
    }

    @Override
    public void setTriggers(Set<ITrigger> triggers) {
        this.triggers = triggers;
    }

    @Override
    public boolean isEditable() {
        return this.editable;
    }

    @Override
    public boolean is3d() {
        return this.is3d;
    }

    @Override
    public void set3d(boolean is3d) {
        this.is3d = is3d;
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        FeatureDataset resultDS = null;
        if (filter != null || envelope != null) {
            List<Feature> filtrados = this.query(envelope, filter);
            resultDS = new FeatureDataset(filtrados, this.getFeatureSchema());
        } else {
            resultDS = this;
        }
        MemoryFullIterator iterator = new MemoryFullIterator(resultDS);
        if (fieldsToOrdered != null) {
            iterator.sort(fieldsToOrdered[0], null);
        }
        return iterator;
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending) {
        return this.getByAttribute(fields, values, fieldOrdered, ascending, null);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        TreeSet<SortedAttribute> sort;
        block21: {
            Object key;
            Object valor;
            String campo;
            boolean condition;
            Feature element;
            boolean isString = this.featureSchema.getAttribute(fieldOrdered).getType().toJavaClass().equals(String.class);
            sort = new TreeSet<SortedAttribute>();
            if (values == null) {
                FeatureIterator iter = null;
                try {
                    try {
                        iter = this.iterator();
                        while (iter.hasNext()) {
                            element = iter.next();
                            if (filter != null && !filter.contains(element)) continue;
                            condition = true;
                            if (fields != null) {
                                int i = 0;
                                while (i < fields.length && condition) {
                                    campo = fields[i];
                                    valor = null;
                                    condition = element.getAttribute(campo).equals(valor);
                                    ++i;
                                }
                            }
                            if (!condition) continue;
                            Object value = element.getAttribute(fieldOrdered);
                            key = element.getPrimaryKey();
                            if (key == null) {
                                sort.add(new SortedAttribute(value, element, ascending, isString));
                                continue;
                            }
                            sort.add(new SortedAttribute(value, key, ascending, isString));
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (iter == null) break block21;
                        iter.close();
                        break block21;
                    }
                }
                catch (Throwable throwable) {
                    if (iter != null) {
                        iter.close();
                    }
                    throw throwable;
                }
                if (iter != null) {
                    iter.close();
                }
            } else {
                int i = 0;
                while (i < values.length) {
                    element = null;
                    element = values[i] instanceof Feature ? (Feature)values[i] : this.getByPrimaryKey(values[i]);
                    if (element != null && (filter == null || filter.contains(element))) {
                        condition = true;
                        if (fields != null) {
                            int j = 0;
                            while (i < fields.length && condition) {
                                campo = fields[j];
                                valor = values[j];
                                condition = element.getAttribute(campo).equals(valor);
                                ++j;
                            }
                        }
                        if (condition) {
                            Object value = element.getAttribute(fieldOrdered);
                            key = element.getPrimaryKey();
                            if (key == null) {
                                sort.add(new SortedAttribute(value, element, ascending, isString));
                            } else {
                                sort.add(new SortedAttribute(value, key, ascending, isString));
                            }
                        }
                    }
                    ++i;
                }
            }
        }
        ArrayList<Feature> result = new ArrayList<Feature>();
        for (SortedAttribute element : sort) {
            result.add(this.getByPrimaryKey(element.getRecordNumber()));
        }
        sort = null;
        System.gc();
        return result;
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        FeatureIterator itFeats = null;
        List<Object[]> results = new ArrayList<Object[]>();
        try {
            try {
                Filter filter = this.getFilterByPrimaryKey(keys);
                itFeats = this.queryIterator(filter, null);
                results = StatsOperatorsFactory.getInstance().queryStats(operatorsByFieldMap, groupByFields, resultStatPairs, AVAILABLE_FEATURE_DATASET_OPERATORS, this.size(), itFeats, new DummyDataSourceWrapper(this));
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itFeats != null) {
                    itFeats.close();
                }
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
        return results;
    }

    public Set<String> getAvailableStatOperators() {
        return AVAILABLE_FEATURE_DATASET_OPERATORS;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public Filter getFilterByPrimaryKey(Object[] values) {
        if (ArrayUtils.isEmpty((Object[])values)) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.the-key-set-is-null"));
            return null;
        }
        LogicFilter filter = null;
        try {
            if (AttributeType.isNumeric(this.featureSchema.getPrimaryKey().getType())) {
                List<long[]> ranges = this.getRanges(values);
                if (ranges.isEmpty()) {
                    return null;
                }
                for (long[] range : ranges) {
                    LogicFilterImpl currentFilter = new LogicFilterImpl(2);
                    AttributeExpressionImpl2 pkExpression1 = new AttributeExpressionImpl2(this.featureSchema.getPrimaryKeyName());
                    AttributeExpressionImpl2 pkExpression2 = new AttributeExpressionImpl2(this.featureSchema.getPrimaryKeyName());
                    CompareFilterImpl leftFilter = new CompareFilterImpl(18);
                    leftFilter.addLeftValue(pkExpression1);
                    leftFilter.addRightValue(new LiteralExpressionImpl(range[0]));
                    CompareFilterImpl rightFilter = new CompareFilterImpl(17);
                    rightFilter.addLeftValue(pkExpression2);
                    rightFilter.addRightValue(new LiteralExpressionImpl(range[1]));
                    currentFilter.addFilter(leftFilter);
                    currentFilter.addFilter(rightFilter);
                    if (filter == null) {
                        filter = new LogicFilterImpl(1);
                    }
                    filter.addFilter(currentFilter);
                }
            } else {
                int i = 0;
                while (i < values.length) {
                    AttributeExpressionImpl2 pkExpression1 = new AttributeExpressionImpl2(this.featureSchema.getPrimaryKeyName());
                    CompareFilterImpl currentFilter = new CompareFilterImpl(14);
                    currentFilter.addLeftValue(pkExpression1);
                    currentFilter.addRightValue(new LiteralExpressionImpl("'" + values[i] + "'"));
                    if (filter == null) {
                        filter = new LogicFilterImpl(1);
                    }
                    filter.addFilter(currentFilter);
                    ++i;
                }
            }
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return filter;
    }

    protected List<long[]> getRanges(Object[] values) {
        long value1;
        ArrayList<long[]> ranges = new ArrayList<long[]>();
        if (values.length == 0) {
            return ranges;
        }
        ArrayList<Object> valuesCopyList = new ArrayList<Object>();
        int i = 0;
        while (i < values.length) {
            if (!(values[i] instanceof Feature)) {
                valuesCopyList.add(values[i]);
            }
            ++i;
        }
        Object[] valuesCopy = new Object[valuesCopyList.size()];
        valuesCopyList.toArray(valuesCopy);
        Arrays.sort(valuesCopy);
        long value2 = value1 = ((Number)valuesCopy[0]).longValue();
        int i2 = 1;
        while (i2 < valuesCopy.length) {
            long value = ((Number)valuesCopy[i2]).longValue();
            if (value == value2 + 1L) {
                value2 = value;
            } else {
                ranges.add(new long[]{value1, value2});
                value1 = value;
                value2 = value;
            }
            ++i2;
        }
        ranges.add(new long[]{value1, value2});
        return ranges;
    }

    @Override
    public Object clone() {
        FeatureDataset fds = new FeatureDataset((FeatureSchema)this.featureSchema.clone());
        fds.name = this.name;
        ArrayList<Feature> features = new ArrayList<Feature>();
        Iterator<Feature> iterator = this.features.iterator();
        while (iterator.hasNext()) {
            Feature feature = iterator.next().clone(true);
            features.add(feature);
        }
        fds.features = features;
        if (this.envelope != null) {
            fds.envelope = new Envelope(this.envelope);
        }
        if (this.layerFilter != null) {
            fds.layerFilter = (Filter)((Cloneable)this.getLayerFilter()).clone();
        }
        fds.inMemory = this.inMemory;
        fds.is3d = this.is3d;
        fds.editable = this.editable;
        return fds;
    }

    public Iterator<Feature> getSpatialIndexCandidatesIterator(Envelope queryEnv) {
        if (!this.hasEditableFeatures() && this.index != null && queryEnv != null) {
            List candidates = this.index.query(queryEnv);
            return candidates.iterator();
        }
        return this.getFeatures().iterator();
    }

    private boolean hasEditableFeatures() {
        return CollectionUtils.isEmpty(this.newFeatures) && MapUtils.isEmpty(this.updateFeatures) && MapUtils.isEmpty(this.deletedFeatures);
    }

    @Override
    public void dispose() {
        if (this.features != null) {
            this.clear();
            this.features = null;
        }
    }
}

