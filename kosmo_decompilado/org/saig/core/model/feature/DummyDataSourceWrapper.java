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
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.workbench.model.IQueryable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.jump.plugin.stats.StatsOperatorsFactory;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class DummyDataSourceWrapper
extends AbstractDataSource
implements IQueryable {
    private static final Logger LOGGER = Logger.getLogger(DummyDataSourceWrapper.class);
    protected FeatureDataset dataset;

    public DummyDataSourceWrapper(FeatureDataset featDataset) {
        this.dataset = featDataset;
    }

    @Override
    public void add(Feature feature) throws Exception {
        this.dataset.add(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        this.dataset.addAll(features);
    }

    @Override
    public void createSpatialIndex() throws Exception {
        this.dataset.createSpatialIndex();
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues) {
        return this.dataset.getByAttribute(atributeNames, atributeValues);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, boolean ascending) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, Filter filter) {
        return this.dataset.getByAttribute(names, values, fieldOrdered, filter);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.dataset.getByAttribute(names, values, fieldOrdered, ascending, filter);
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.dataset.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] key) {
        return this.dataset.getByPrimaryKeys(key);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoredUpdate) {
        return this.dataset.getByPrimaryKeys(keys);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.dataset.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.dataset.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.dataset.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.dataset.getDistintsValues(expr, limit);
    }

    @Override
    public List<Feature> getFeatures() {
        return this.dataset.getFeatures();
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.dataset.iterator();
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.dataset.getFeaturesSamples(n);
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        return this.dataset.getFieldsValues(field, fieldKey, value);
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.dataset.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        return this.dataset.getHistoryOfElement(pkId, filter);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] field, String fieldKey) {
        return this.dataset.getMapFieldsValues(field, fieldKey);
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        return this.dataset.getKeys();
    }

    @Override
    public List<Object> getSortKeys(String colunmn, boolean ascending, Object[] values) {
        return this.dataset.getSortKeys(colunmn, ascending, values);
    }

    @Override
    public Envelope getViewBox() throws Exception {
        return this.dataset.getEnvelope();
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope env = new Envelope();
        FeatureIterator it = null;
        try {
            it = this.dataset.queryIterator(filter, null);
            while (it.hasNext()) {
                Feature feat = it.next();
                env.expandToInclude(feat.getGeometry().getEnvelopeInternal());
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return env;
    }

    @Override
    public List<Feature> query(Envelope rectangle) {
        return this.dataset.query(rectangle);
    }

    @Override
    public List<Feature> query(Filter filter) {
        return this.dataset.query(filter);
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) {
        return this.dataset.query(view, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filtro, rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filter, rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filtro, rectangle, labels);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.dataset.queryIterator(rectangle);
    }

    @Override
    public FeatureIterator queryIterator(Envelope view, Filter filter) {
        return this.dataset.queryIterator(filter, view);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.dataset.queryIterator(filtro, rectangle);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        FeatureIterator itFeats = null;
        List<Object[]> results = new ArrayList<Object[]>();
        try {
            try {
                Filter filter = this.getFilterByPrimaryKey(keys);
                itFeats = this.queryIterator(null, filter);
                results = StatsOperatorsFactory.getInstance().queryStats(operatorsByFieldMap, groupByFields, resultStatPairs, this.dataset.getAvailableStatOperators(), this.size(), itFeats, this);
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

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        this.dataset.removeAll(features);
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.dataset.removeByPKs(pks);
    }

    @Override
    public int size() throws Exception {
        return this.dataset.size();
    }

    @Override
    public void update(Feature feature) throws Exception {
        this.dataset.update(feature);
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        this.dataset.updateAll(features);
    }

    @Override
    public Object clone() {
        DummyDataSourceWrapper newDummyDataSourceWrapper = new DummyDataSourceWrapper(null);
        newDummyDataSourceWrapper.dataset = (FeatureDataset)this.dataset.clone();
        return newDummyDataSourceWrapper;
    }
}

