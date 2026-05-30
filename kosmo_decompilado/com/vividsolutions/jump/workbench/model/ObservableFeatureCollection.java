/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class ObservableFeatureCollection
extends FeatureCollectionWrapper {
    protected List<Listener> listeners = new ArrayList<Listener>();
    protected String name;

    public ObservableFeatureCollection(FeatureCollection fc) {
        super(fc);
    }

    public void add(Listener listener) {
        this.listeners.add(listener);
    }

    public void remove(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void add(Feature feature) throws Exception {
        super.add(feature);
        this.fireFeaturesAdded(Arrays.asList(feature));
    }

    @Override
    public void remove(Feature feature) throws Exception {
        super.remove(feature);
        this.fireFeaturesRemoved(Arrays.asList(feature));
    }

    private void fireFeaturesAdded(Collection<Feature> features) {
        for (Listener listener : this.listeners) {
            listener.featuresAdded(features);
        }
    }

    private void fireFeaturesRemoved(Collection<Feature> features) {
        for (Listener listener : this.listeners) {
            listener.featuresRemoved(features);
        }
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        super.addAll(features);
        this.fireFeaturesAdded(features);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        super.removeAll(features);
        this.fireFeaturesRemoved(features);
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        Collection<Feature> features = super.remove(env);
        this.fireFeaturesRemoved(features);
        return features;
    }

    @Override
    public FeatureIterator queryIterator(Filter filter, Envelope envelope) {
        return this.fc.queryIterator(filter, envelope);
    }

    @Override
    public void commit() throws Exception {
        this.fc.commit();
    }

    @Override
    public void rollBack() {
        this.fc.rollBack();
    }

    @Override
    public void update(Feature feature) throws Exception {
        this.fc.update(feature);
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        this.fc.updateAll(features);
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
        return this.fc.query(filter);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.fc.getFeaturesSamples(n);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values) {
        return this.fc.getByAttribute(fields, values);
    }

    @Override
    public List<Object> getKeys() {
        return this.fc.getKeys();
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.fc.getByPrimaryKey(key);
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.fc.getSortKeys(column, ascending, values);
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] keys) {
        return this.fc.getByPrimaryKeys(keys);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.fc.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.fc.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.fc.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.fc.getDistintsValues(expr, limit);
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        return this.fc.getFieldsValues(field, fieldKey, value);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.fc.getMapFieldsValues(fields, fieldKey);
    }

    @Override
    public void setFeatureSchema(FeatureSchema schema) {
        this.fc.setFeatureSchema(schema);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered) {
        return this.fc.getByAttribute(fields, values, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, Filter filter) {
        return this.fc.getByAttribute(fields, values, fieldOrdered, filter);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Envelope envelope, List<String> labels) {
        return this.fc.queryOnlyGeometryIterator(envelope, labels);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Filter filter, Envelope envelope, List<String> labels) {
        return this.fc.queryOnlyGeometryIterator(filter, envelope, labels);
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.fc.setEnvelope(envelope);
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) throws Exception {
        return this.fc.query(envelope, filter);
    }

    @Override
    public boolean isCad() {
        return this.fc.isCad();
    }

    @Override
    public void createSpatialIndex() {
        this.fc.createSpatialIndex();
    }

    @Override
    public boolean isSpatialIndex() {
        return this.fc.isSpatialIndex();
    }

    @Override
    public void setTopologyRelations(List<ITopologyRelation> topologyRelations) {
        this.fc.setTopologyRelations(topologyRelations);
    }

    @Override
    public void setEditable(boolean editable) {
        this.fc.setEditable(editable);
    }

    @Override
    public boolean isEditable() {
        return this.fc.isEditable();
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        this.fc.refreshSelection(features);
    }

    @Override
    public List<int[]> getIntervalSelection() {
        return this.fc.getIntervalSelection();
    }

    @Override
    public void invertSelection() {
        this.fc.invertSelection();
    }

    @Override
    public Filter getLayerFilter() {
        return this.fc.getLayerFilter();
    }

    @Override
    public void setLayerFilter(Filter layerFilter) {
        this.fc.setLayerFilter(layerFilter);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        return this.fc.getHistoryOfElement(pkId, filter);
    }

    @Override
    public void setTriggers(Set<ITrigger> triggers) {
        this.fc.setTriggers(triggers);
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.fc.removeByPKs(pks);
    }

    @Override
    public boolean is3d() {
        return this.fc.is3d();
    }

    @Override
    public void set3d(boolean is3d) {
        this.fc.set3d(is3d);
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.fc.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending) {
        return this.fc.getByAttribute(fields, values, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.fc.getByAttribute(fields, values, fieldOrdered, ascending, filter);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.fc.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public Envelope getEnvelope(Filter filter) throws Exception {
        return this.fc.getEnvelope(filter);
    }

    @Override
    public Object clone() {
        return this.fc.clone();
    }

    public static interface Listener {
        public void featuresAdded(Collection<Feature> var1);

        public void featuresRemoved(Collection<Feature> var1);
    }
}

