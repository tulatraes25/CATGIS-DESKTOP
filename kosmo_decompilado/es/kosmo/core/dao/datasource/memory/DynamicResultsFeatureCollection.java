/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package es.kosmo.core.dao.datasource.memory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class DynamicResultsFeatureCollection
implements FeatureCollection {
    private static final Logger LOGGER = Logger.getLogger(DynamicResultsFeatureCollection.class);
    protected FeatureSchema featureSchema;
    protected Envelope envelope = null;
    protected String name;
    protected boolean is3d;
    protected AbstractResultsFeatureIterator iterator;

    public DynamicResultsFeatureCollection(FeatureSchema fs, AbstractResultsFeatureIterator it) {
        this.featureSchema = fs;
        this.iterator = it;
    }

    @Override
    public FeatureSchema getFeatureSchema() {
        return this.featureSchema;
    }

    @Override
    public void setFeatureSchema(FeatureSchema schema) {
        this.featureSchema = schema;
    }

    @Override
    public Envelope getEnvelope() throws Exception {
        return this.envelope;
    }

    @Override
    public Envelope getEnvelope(Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public int size() throws Exception {
        return this.iterator.size();
    }

    @Override
    public boolean isEmpty() throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getFeatures() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator iterator() {
        return this.iterator;
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> query(Envelope envelope) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
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
    public List<Object> getKeys() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] key) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void add(Feature feature) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void update(Feature feature) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void remove(Feature feature) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void clear() throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void commit() throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void rollBack() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        throw new I18NUnsupportedOperationException();
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
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, Filter filter) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        throw new I18NUnsupportedOperationException();
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
        ArrayList<Feature> results = new ArrayList<Feature>(n);
        int cont = 0;
        try {
            try {
                while (this.iterator.hasNext()) {
                    if (cont++ >= n) {
                        return results;
                    }
                    results.add(this.iterator.next());
                }
                return results;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.iterator.reset();
                return results;
            }
        }
        finally {
            this.iterator.reset();
        }
    }

    @Override
    public List<Object> getSortKeys(String colunmn, boolean ascending, Object[] values) {
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
    public Set<Object> getDistintsValues(Expression expr) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] field, String fieldKey) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public boolean isCad() {
        return false;
    }

    @Override
    public void createSpatialIndex() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public boolean isSpatialIndex() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setTopologyRelations(List<ITopologyRelation> topologyRelations) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setEditable(boolean editable) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public boolean isEditable() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void invertSelection() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<int[]> getIntervalSelection() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Filter getLayerFilter() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setLayerFilter(Filter layerFilter) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setTriggers(Set<ITrigger> triggers) {
        throw new I18NUnsupportedOperationException();
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
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Object clone() {
        return new I18NUnsupportedOperationException();
    }

    public FeatureCollection toFeatureDataset() {
        FeatureDataset fd = new FeatureDataset(this.getFeatureSchema());
        fd.set3d(this.is3d());
        fd.setName(this.getName());
        FeatureIterator itFeats = null;
        try {
            try {
                itFeats = this.iterator();
                while (itFeats.hasNext()) {
                    Feature feat = itFeats.next();
                    fd.addWithNewKey(feat);
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
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
        return fd;
    }

    @Override
    public void dispose() {
        if (this.iterator != null) {
            this.iterator.close();
        }
        this.iterator = null;
        this.envelope = null;
        this.featureSchema = null;
    }
}

