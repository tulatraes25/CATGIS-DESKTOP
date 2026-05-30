/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.model.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import es.kosmo.core.model.relations.topology.OperationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.IPostAddTrigger;
import org.saig.core.model.data.trigger.IPostDeleteTrigger;
import org.saig.core.model.data.trigger.IPostUpdateTrigger;
import org.saig.core.model.data.trigger.IPreAddTrigger;
import org.saig.core.model.data.trigger.IPreDeleteTrigger;
import org.saig.core.model.data.trigger.IPreUpdateTrigger;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.data.trigger.TriggerFactory;
import org.saig.core.model.feature.DummyFeatureIterator;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class FeatureCollectionOnDemand
implements FeatureCollection {
    protected String name;
    protected AbstractDataSource dataAccesor;
    protected long identificador;
    protected List<ITopologyRelation> topologyRelations = new ArrayList<ITopologyRelation>();
    protected Set<ITrigger> triggers = new LinkedHashSet<ITrigger>();

    public void setId(long id) {
        this.identificador = id;
    }

    public long getIdentificador() {
        return this.identificador;
    }

    public void setSchema(FeatureSchema schema) {
        this.dataAccesor.setSchema(schema);
    }

    public void setDataAccesor(AbstractDataSource dataAccesor) {
        this.dataAccesor = dataAccesor;
    }

    public AbstractDataSource getDataAccesor() {
        return this.dataAccesor;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public FeatureSchema getFeatureSchema() {
        return this.dataAccesor.getSchema();
    }

    @Override
    public void setFeatureSchema(FeatureSchema schema) {
        this.dataAccesor.setSchema(schema);
    }

    @Override
    public Envelope getEnvelope() throws Exception {
        return this.dataAccesor.getViewBox();
    }

    @Override
    public Envelope getEnvelope(Filter filter) throws Exception {
        return this.dataAccesor.getViewBox(filter);
    }

    @Override
    public int size() throws Exception {
        return this.dataAccesor.size();
    }

    @Override
    public boolean isEmpty() throws Exception {
        return this.dataAccesor.size() == 0;
    }

    @Override
    public List<Feature> getFeatures() {
        return this.dataAccesor.getFeatures();
    }

    @Override
    public FeatureIterator iterator() {
        FeatureIterator iterator = this.dataAccesor.getFeaturesIterator();
        if (iterator == null) {
            return new DummyFeatureIterator();
        }
        return iterator;
    }

    @Override
    public List<Feature> query(Envelope envelope) throws Exception {
        return this.dataAccesor.query(envelope);
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) throws Exception {
        return this.dataAccesor.query(envelope, filter);
    }

    @Override
    public FeatureIterator queryIterator(Envelope env) {
        FeatureIterator iterator = this.dataAccesor.queryIterator(env);
        if (iterator == null) {
            return new DummyFeatureIterator();
        }
        return iterator;
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues) {
        return this.dataAccesor.getByAttribute(atributeNames, atributeValues);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered) {
        return this.dataAccesor.getByAttribute(atributeNames, atributeValues, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, Filter filter) {
        return this.dataAccesor.getByAttribute(atributeNames, atributeValues, fieldOrdered, filter);
    }

    @Override
    public void add(Feature feature) throws Exception {
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        featuresToAdd.add(feature);
        this.checkTopologicRelations(featuresToAdd);
        if (this.launchPreAddTriggers(featuresToAdd)) {
            this.dataAccesor.add(feature);
            this.launchPostAddTriggers(featuresToAdd);
        }
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        this.checkTopologicRelations(features);
        if (this.launchPreAddTriggers(features)) {
            this.dataAccesor.addAll(features);
            this.launchPostAddTriggers(features);
        }
    }

    @Override
    public void remove(Feature featureToRemove) throws Exception {
        ArrayList<Feature> featuresToRemove = new ArrayList<Feature>();
        featuresToRemove.add(featureToRemove);
        this.checkTopologicRelations(featuresToRemove);
        if (this.launchPreRemoveTriggers(featuresToRemove)) {
            this.dataAccesor.removeAll(featuresToRemove);
            this.launchPostRemoveTriggers(featuresToRemove);
        }
    }

    @Override
    public void removeAll(Collection<Feature> featuresToRemove) throws Exception {
        this.checkTopologicRelations(featuresToRemove, OperationType.REMOVE);
        if (this.launchPreRemoveTriggers(featuresToRemove)) {
            this.dataAccesor.removeAll(featuresToRemove);
            this.launchPostRemoveTriggers(featuresToRemove);
        }
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.dataAccesor.removeByPKs(pks);
    }

    @Override
    public void clear() {
        throw new I18NUnsupportedOperationException(I18N.getString("org.saig.core.model.feature.FeatureCollectionOnDemand.clear-method-is-unimplemented-yet"));
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        List<Feature> featuresToRemove = this.dataAccesor.query(env);
        this.removeAll(featuresToRemove);
        return featuresToRemove;
    }

    public Feature createFeature() {
        BasicFeature feature = new BasicFeature(this.dataAccesor.getSchema());
        return feature;
    }

    public List<Feature> getByExpresion(Filter filter, Envelope vista) throws Exception {
        return this.dataAccesor.query(vista, filter);
    }

    @Override
    public FeatureIterator queryIterator(Filter filter, Envelope envelope) {
        return this.dataAccesor.queryIterator(envelope, filter);
    }

    @Override
    public void commit() throws Exception {
        this.dataAccesor.commit();
    }

    @Override
    public void rollBack() {
        this.dataAccesor.rollback();
    }

    @Override
    public void update(Feature feature) throws Exception {
        ArrayList<Feature> featuresToUpdate = new ArrayList<Feature>();
        featuresToUpdate.add(feature);
        this.checkTopologicRelations(featuresToUpdate);
        if (this.launchPreUpdateTriggers(featuresToUpdate)) {
            this.dataAccesor.update(feature);
            this.launchPostUpdateTriggers(featuresToUpdate);
        }
    }

    @Override
    public void updateAll(Collection<Feature> featuresToUpdate) throws Exception {
        this.checkTopologicRelations(featuresToUpdate);
        if (this.launchPreUpdateTriggers(featuresToUpdate)) {
            this.dataAccesor.updateAll(featuresToUpdate);
            this.launchPostUpdateTriggers(featuresToUpdate);
        }
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
        return this.dataAccesor.query(filter);
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.dataAccesor.getFeaturesSamples(n);
    }

    public Object getPrimaryKey(Feature feature) {
        return feature.getAttribute(this.dataAccesor.getSchema().getPrimaryKeyName());
    }

    @Override
    public List<Object> getKeys() {
        return this.dataAccesor.getOrderedPrimaryKeyList();
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.dataAccesor.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] key) {
        return this.dataAccesor.getByPrimaryKey(key);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.dataAccesor.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.dataAccesor.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.dataAccesor.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.dataAccesor.getDistintsValues(expr, limit);
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        return this.dataAccesor.getFieldValue(field, fieldKey, value);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.dataAccesor.getMapFieldsValues(fields, fieldKey);
    }

    public List<Feature> getByExpresion(Filter filtro) throws Exception {
        return this.dataAccesor.query(filtro);
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.dataAccesor.getSortKeys(column, ascending, values);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Envelope envelope, List<String> labels) {
        return this.dataAccesor.queryGeometryIterator(envelope, labels);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Filter filter, Envelope envelope, List<String> labels) {
        return this.dataAccesor.queryGeometryIterator(envelope, filter, labels);
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.dataAccesor.setViewBox(envelope);
    }

    @Override
    public boolean isCad() {
        return this.getDataAccesor() instanceof AbstractCadDataSource;
    }

    @Override
    public void createSpatialIndex() {
        if (this.dataAccesor instanceof ShapeFileDataSource) {
            ((ShapeFileDataSource)this.dataAccesor).createSpatialIndex();
        }
    }

    @Override
    public boolean isSpatialIndex() {
        if (this.dataAccesor instanceof ShapeFileDataSource) {
            return ((ShapeFileDataSource)this.dataAccesor).getSpatialIndex() != null;
        }
        return false;
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
        this.dataAccesor.setEditable(editable);
    }

    @Override
    public boolean isEditable() {
        return this.dataAccesor != null && this.dataAccesor.isEditable();
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        this.dataAccesor.refreshSelection(features);
    }

    @Override
    public List<int[]> getIntervalSelection() {
        return this.dataAccesor.getIntervalSelection();
    }

    @Override
    public void invertSelection() {
        this.dataAccesor.invertSelection();
    }

    @Override
    public Filter getLayerFilter() {
        return this.dataAccesor.getLayerFilter();
    }

    @Override
    public void setLayerFilter(Filter layerFilter) {
        this.dataAccesor.setLayerFilter(layerFilter);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        return this.dataAccesor.getHistoryOfElement(pkId, filter);
    }

    @Override
    public void setTriggers(Set<ITrigger> triggers) {
        this.triggers = triggers;
    }

    @Override
    public boolean is3d() {
        return this.dataAccesor.is3d();
    }

    @Override
    public void set3d(boolean is3d) {
        if (this.dataAccesor != null) {
            this.dataAccesor.set3d(is3d);
        }
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.dataAccesor.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending) {
        return this.dataAccesor.getByAttribute(fields, values, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.dataAccesor.getByAttribute(fields, values, fieldOrdered, ascending, filter);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.dataAccesor.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public Object clone() {
        FeatureCollectionOnDemand newFCD = new FeatureCollectionOnDemand();
        newFCD.dataAccesor = (AbstractDataSource)this.dataAccesor.clone();
        newFCD.identificador = this.identificador;
        newFCD.name = this.name;
        return newFCD;
    }

    @Override
    public void dispose() {
        if (this.dataAccesor != null) {
            this.dataAccesor.dispose();
            this.dataAccesor = null;
        }
    }
}

