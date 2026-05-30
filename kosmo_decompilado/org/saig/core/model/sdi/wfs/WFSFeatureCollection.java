/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.PrecisionModel
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.deegree.framework.xml.XMLFragment
 */
package org.saig.core.model.sdi.wfs;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.geometry.filters.ZCoordinateCountFilter;
import es.kosmo.core.model.sdi.wfs.transaction.AbstractWFSTransaction;
import es.kosmo.core.model.sdi.wfs.transaction.WFSTransactionFactory;
import es.kosmo.core.utils.FeatureSchemaUtils;
import es.kosmo.core.utils.GeometryUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.sdi.wfs.WFSFeature;
import org.saig.core.model.sdi.wfs.WFSFeatureDataset;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;
import org.w3c.dom.Element;

public class WFSFeatureCollection
implements FeatureCollection {
    private static final Logger LOGGER = Logger.getLogger(WFSFeatureCollection.class);
    protected WFSFeatureDataset dataset;
    protected WFSFeatureTypeInfo info;
    protected AbstractWFSWrapper service;
    public static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 0);

    public WFSFeatureCollection(FeatureSchema schema, WFSFeatureTypeInfo ftInfo, AbstractWFSWrapper serviceWrapper) {
        this.dataset = new WFSFeatureDataset(schema);
        this.info = ftInfo;
        this.service = serviceWrapper;
    }

    @Override
    public void add(Feature feature) throws Exception {
        if (!(feature instanceof WFSFeature)) {
            feature = new WFSFeature(feature, null);
        }
        this.dataset.add(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        ArrayList<Feature> newFeats = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (!(feature instanceof WFSFeature)) {
                feature = new WFSFeature(feature, null);
            }
            newFeats.add(feature);
        }
        this.dataset.addAll(newFeats);
    }

    @Override
    public void clear() throws Exception {
        this.dataset.clear();
    }

    public void commit(boolean performTransaction) throws Exception {
        if (performTransaction) {
            LOGGER.info((Object)I18N.getString(this.getClass(), "generating-transaction"));
            AbstractWFSTransaction transaction = WFSTransactionFactory.createTransaction(this.info, this.service);
            StringBuffer xmlRequest = transaction.createRequest(this.getCorrectGeometries(this.dataset.getNewFeatures()), this.getCorrectGeometries(this.dataset.getUpdatedFeatures()), this.dataset.getDeletedFeatures(), false);
            LOGGER.info((Object)("WFS-T request:\n" + xmlRequest.toString()));
            XMLFragment doc = transaction.doTransaction(xmlRequest.toString());
            if (doc == null) {
                throw new Exception(I18N.getMessage(this.getClass(), "wfs-server-{0}-answered-incorrectly", new Object[]{this.service.getBaseWfsURL()}));
            }
            LOGGER.info((Object)(String.valueOf(I18N.getString(this.getClass(), "complete-answer-from-server")) + ": \n" + doc.getAsPrettyString()));
            Element root = doc.getRootElement();
            String rootName = root.getNodeName();
            if (rootName.equals("ows:ServiceExceptionReport") || rootName.equals("ows:ExceptionReport") || rootName.equals("ServiceExceptionReport") || rootName.equals("ExceptionReport")) {
                Element exceptionNode = XMLTools.getFirstChildElement(root);
                Element exceptionTextNode = XMLTools.getFirstChildElement(exceptionNode);
                String msg = exceptionTextNode == null ? exceptionNode.getTextContent() : exceptionTextNode.getTextContent();
                throw new Exception(String.valueOf(I18N.getMessage(this.getClass(), "wfs-server-{0}-answered-incorrectly", new Object[]{this.service.getBaseWfsURL()})) + ":\n" + msg);
            }
            if (CollectionUtils.isNotEmpty(this.dataset.getNewFeatures())) {
                List<String> featureIds = XMLTools.getNodesAsStringList(doc.getRootElement(), "//ogc:FeatureId/@fid", AbstractWFSTransaction.nsContext);
                for (Feature newFeature : this.dataset.getNewFeatures()) {
                    ((WFSFeature)newFeature).setGMLId(featureIds.get(0));
                    featureIds.remove(0);
                }
            }
            this.dataset.commit();
        } else {
            this.dataset.commit();
        }
    }

    @Override
    public void commit() throws Exception {
        this.commit(true);
    }

    @Override
    public void createSpatialIndex() {
        this.dataset.createSpatialIndex();
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values) {
        return this.dataset.getByAttribute(fields, values);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered) {
        return this.dataset.getByAttribute(fields, values, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending) {
        return this.dataset.getByAttribute(fields, values, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, Filter filter) {
        return this.dataset.getByAttribute(fields, values, fieldOrdered, filter);
    }

    @Override
    public List<Feature> getByAttribute(String[] fields, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.dataset.getByAttribute(fields, values, fieldOrdered, ascending, filter);
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.dataset.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKeys(Object[] keys) {
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
    public Envelope getEnvelope() throws Exception {
        return this.dataset.getEnvelope();
    }

    @Override
    public Envelope getEnvelope(Filter filter) throws Exception {
        return this.dataset.getEnvelope(filter);
    }

    @Override
    public FeatureSchema getFeatureSchema() {
        return this.dataset.getFeatureSchema();
    }

    @Override
    public List<Feature> getFeatures() {
        return this.dataset.getFeatures();
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.dataset.getFeaturesSamples(n);
    }

    @Override
    public Object getFieldsValues(String field, String fieldKey, Object value) {
        return this.dataset.getFieldsValues(field, fieldKey, value);
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.dataset.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        return this.dataset.getHistoryOfElement(pkId, filter);
    }

    @Override
    public List<int[]> getIntervalSelection() {
        return this.dataset.getIntervalSelection();
    }

    @Override
    public List<Object> getKeys() {
        return this.dataset.getKeys();
    }

    @Override
    public Filter getLayerFilter() {
        return this.dataset.getLayerFilter();
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.dataset.getMapFieldsValues(fields, fieldKey);
    }

    @Override
    public String getName() {
        return this.dataset.getName();
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.dataset.getSortKeys(column, ascending, values);
    }

    @Override
    public void invertSelection() {
        this.dataset.invertSelection();
    }

    @Override
    public boolean is3d() {
        return this.dataset.is3d();
    }

    @Override
    public boolean isCad() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return this.dataset.isEditable();
    }

    @Override
    public boolean isEmpty() {
        return this.dataset.isEmpty();
    }

    @Override
    public boolean isSpatialIndex() {
        return this.dataset.isSpatialIndex();
    }

    @Override
    public FeatureIterator iterator() {
        return this.dataset.iterator();
    }

    @Override
    public List<Feature> query(Envelope envelope) {
        return this.dataset.query(envelope);
    }

    @Override
    public List<Feature> query(Envelope envelope, Filter filter) {
        return this.dataset.query(envelope, filter);
    }

    @Override
    public List<Feature> query(Filter filter) {
        return this.dataset.query(filter);
    }

    @Override
    public FeatureIterator queryIterator(Envelope envelope) {
        return this.dataset.queryIterator(envelope);
    }

    @Override
    public FeatureIterator queryIterator(Filter filter, Envelope envelope) {
        return this.dataset.queryIterator(filter, envelope);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Envelope envelope, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(envelope, labels);
    }

    @Override
    public FeatureIterator queryOnlyGeometryIterator(Filter filter, Envelope envelope, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filter, envelope, labels);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.dataset.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        this.dataset.refreshSelection(features);
    }

    @Override
    public void remove(Feature feature) throws Exception {
        if (!(feature instanceof WFSFeature)) {
            feature = new WFSFeature(feature, null);
        }
        this.dataset.remove(feature);
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        return this.dataset.remove(env);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        ArrayList<Feature> removeFeats = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (!(feature instanceof WFSFeature)) {
                feature = new WFSFeature(feature, null);
            }
            removeFeats.add(feature);
        }
        this.dataset.removeAll(removeFeats);
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.dataset.removeByPKs(pks);
    }

    @Override
    public void rollBack() {
        this.dataset.rollBack();
    }

    @Override
    public void set3d(boolean is3d) {
        this.dataset.set3d(is3d);
    }

    @Override
    public void setEditable(boolean editable) {
        this.dataset.setEditable(editable);
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        this.dataset.setEnvelope(envelope);
    }

    @Override
    public void setFeatureSchema(FeatureSchema schema) {
        this.dataset.setFeatureSchema(schema);
    }

    @Override
    public void setLayerFilter(Filter layerFilter) {
        this.dataset.setLayerFilter(layerFilter);
    }

    @Override
    public void setName(String name) {
        this.dataset.setName(name);
    }

    @Override
    public void setTopologyRelations(List<ITopologyRelation> topologyRelations) {
        this.dataset.setTopologyRelations(topologyRelations);
    }

    @Override
    public void setTriggers(Set<ITrigger> triggers) {
        this.dataset.setTriggers(triggers);
    }

    @Override
    public int size() throws Exception {
        return this.dataset.size();
    }

    @Override
    public void update(Feature feature) throws Exception {
        if (!(feature instanceof WFSFeature)) {
            feature = new WFSFeature(feature, null);
        }
        this.dataset.update(feature);
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        ArrayList<Feature> updateFeats = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (!(feature instanceof WFSFeature)) {
                feature = new WFSFeature(feature, null);
            }
            updateFeats.add(feature);
        }
        this.dataset.updateAll(updateFeats);
    }

    public void addDownloadedFeatures(List<Feature> features) {
        this.dataset.removeAllDirect();
        this.dataset.addAllDirect(features);
    }

    public WFSFeatureTypeInfo getInfo() {
        return this.info;
    }

    public AbstractWFSWrapper getService() {
        return this.service;
    }

    @Override
    public Object clone() {
        throw new I18NUnsupportedOperationException();
    }

    protected Collection<Feature> getCorrectGeometries(Collection<Feature> features) throws Exception {
        ArrayList<Feature> correctFeatures = new ArrayList<Feature>();
        for (Feature element : features) {
            Geometry geom = element.getGeometry();
            if (!this.checkGeometryType(geom)) {
                geom = GeometryUtils.convertToGoodGeometry(this.dataset.getFeatureSchema(), geom);
            }
            if (!this.checkZ(geom)) {
                geom = GeometryUtils.applyZFilter(geom, this.is3d());
            }
            element.setGeometry(geom);
            correctFeatures.add(element);
        }
        return correctFeatures;
    }

    protected boolean checkGeometryType(Geometry geom) {
        Class<?> schemaGeometry = FeatureSchemaUtils.getGeometryClass(this.dataset.getFeatureSchema());
        return geom.getClass().equals(schemaGeometry);
    }

    protected boolean checkZ(Geometry geometry) {
        ZCoordinateCountFilter filter = new ZCoordinateCountFilter();
        geometry.apply((CoordinateFilter)filter);
        return !this.is3d() && filter.getCount() == 0;
    }

    @Override
    public void dispose() {
        if (this.dataset != null) {
            this.dataset.clear();
            this.dataset = null;
        }
    }
}

