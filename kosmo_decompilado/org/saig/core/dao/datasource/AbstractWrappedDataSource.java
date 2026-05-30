/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.core.dao.datasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class AbstractWrappedDataSource
extends AbstractDataSource {
    protected AbstractDataSource internalDataSource;

    public AbstractWrappedDataSource(AbstractDataSource datasource) {
        Assert.isTrue((datasource != null ? 1 : 0) != 0);
        this.internalDataSource = datasource;
    }

    @Override
    public void add(Feature feature) throws Exception {
        this.internalDataSource.add(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        this.internalDataSource.addAll(features);
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void createSpatialIndex() throws Exception {
        this.internalDataSource.createSpatialIndex();
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues) {
        return this.internalDataSource.getByAttribute(attributeNames, attributeValues);
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues, String fieldOrdered) {
        return this.internalDataSource.getByAttribute(attributeNames, attributeValues, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues, String fieldOrdered, boolean ascending) {
        return this.internalDataSource.getByAttribute(attributeNames, attributeValues, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, Filter filter) {
        return this.internalDataSource.getByAttribute(names, values, fieldOrdered, filter);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.internalDataSource.getByAttribute(names, values, fieldOrdered, ascending, filter);
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.internalDataSource.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys) {
        return this.internalDataSource.getByPrimaryKey(keys);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoreUpdated) {
        return this.internalDataSource.getByPrimaryKey(keys, ignoreUpdated);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.internalDataSource.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.internalDataSource.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.internalDataSource.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.internalDataSource.getDistintsValues(expr, limit);
    }

    @Override
    public List<Feature> getFeatures() {
        return this.internalDataSource.getFeatures();
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.internalDataSource.getFeaturesIterator();
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.internalDataSource.getFeaturesSamples(n);
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        return this.internalDataSource.getFieldValue(field, fieldKey, value);
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.internalDataSource.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        return this.internalDataSource.getHistoryOfElement(pkId, filter);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.internalDataSource.getMapFieldsValues(fields, fieldKey);
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        return this.internalDataSource.getOrderedPrimaryKeyList();
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.internalDataSource.getSortKeys(column, ascending, values);
    }

    @Override
    public Envelope getViewBox() throws Exception {
        return this.internalDataSource.getViewBox();
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        return this.internalDataSource.getViewBox(filter);
    }

    @Override
    public List<Feature> query(Envelope rectangle) throws Exception {
        return this.internalDataSource.query(rectangle);
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
        return this.internalDataSource.query(filter);
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) throws Exception {
        return this.internalDataSource.query(view, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.internalDataSource.queryGeometryIterator(rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> labels) {
        return this.internalDataSource.queryGeometryIterator(rectangle, filter, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, List<String> labels) {
        return this.internalDataSource.queryGeometryIterator(rectangle, filter, orderByFields, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        return this.internalDataSource.queryGeometryIterator(rectangle, filter, orderByFields, ascending, labels);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.internalDataSource.queryIterator(rectangle);
    }

    @Override
    public FeatureIterator queryIterator(Envelope view, Filter filter) {
        return this.internalDataSource.queryIterator(view, filter);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.internalDataSource.queryIterator(rectangle, filtro, orderByFields);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.internalDataSource.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        this.internalDataSource.removeAll(features);
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.internalDataSource.removeByPKs(pks);
    }

    @Override
    public int size() throws Exception {
        return this.internalDataSource.size();
    }

    @Override
    public void update(Feature feature) throws Exception {
        this.internalDataSource.update(feature);
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        this.internalDataSource.updateAll(features);
    }

    @Override
    public FeatureSchema getSchema() {
        return this.internalDataSource.getSchema();
    }
}

