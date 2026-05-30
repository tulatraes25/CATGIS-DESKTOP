/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;

public class TemporalFeatureDataset
extends FeatureDataset {
    private static final Logger LOGGER = Logger.getLogger(TemporalFeatureDataset.class);

    public TemporalFeatureDataset(Collection<Feature> newFeatures, FeatureSchema featureSchema) {
        super(featureSchema);
        for (Feature feature : newFeatures) {
            try {
                this.add(feature);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    public TemporalFeatureDataset(FeatureSchema featureSchema) {
        this(new ArrayList<Feature>(), featureSchema);
    }

    @Override
    public void add(Feature feature) throws Exception {
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        featuresToAdd.add(feature);
        this.addAll(featuresToAdd);
    }

    @Override
    public void addAll(Collection<Feature> featuresToAdd) throws Exception {
        if (this.envelope == null) {
            this.envelope = this.getEnvelope();
        }
        for (Feature currentFeature : featuresToAdd) {
            this.features.add(currentFeature);
            if (this.envelope == null) {
                this.envelope = currentFeature.getGeometry().getEnvelopeInternal();
                continue;
            }
            this.envelope.expandToInclude(currentFeature.getGeometry().getEnvelopeInternal());
        }
    }

    @Override
    public void commit() {
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        Feature feature = null;
        boolean enc = false;
        Iterator it = this.features.iterator();
        while (it.hasNext() && !enc) {
            Feature aux = (Feature)it.next();
            if (!key.equals(aux.getPrimaryKey())) continue;
            enc = true;
            feature = aux;
        }
        return feature;
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] key) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getFeatures() {
        return this.features;
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        ArrayList<Feature> result = new ArrayList<Feature>();
        int i = 0;
        while (i < n && i < this.size()) {
            result.add((Feature)this.features.get(i));
            ++i;
        }
        return result;
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Object> getKeys() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] field, String fieldKey) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Object> getSortKeys(String colunmn, boolean ascending, Object[] values) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.features.isEmpty();
    }

    @Override
    public FeatureIterator iterator() {
        return super.iterator();
    }

    @Override
    public List<Feature> query(Envelope envelope) {
        ArrayList<Feature> list = new ArrayList<Feature>();
        for (Feature feature : this.features) {
            if (!envelope.contains(feature.getGeometry().getEnvelopeInternal())) continue;
            list.add(feature);
        }
        return list;
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> query(Filter filter) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryIterator(Envelope envelope) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryIterator(Filter filter, Envelope envelope) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Envelope envelope, List<String> labels) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Filter filter, Envelope envelope, List<String> labels) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void remove(Feature feature) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Collection<Feature> remove(Envelope env) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void removeAll(Collection<Feature> features) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void rollBack() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public int size() {
        return this.features.size();
    }

    @Override
    public void update(Feature feature) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void updateAll(Collection<Feature> features) {
        throw new I18NUnsupportedOperationException();
    }
}

