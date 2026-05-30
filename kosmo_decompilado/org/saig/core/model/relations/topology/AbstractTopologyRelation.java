/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.relations.topology;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import es.kosmo.core.model.relations.topology.OperationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public abstract class AbstractTopologyRelation
implements ITopologyRelation {
    protected static final Logger LOGGER = Logger.getLogger(AbstractTopologyRelation.class);
    public static FeatureSchema errorSchema;
    public static final String FIELD_ID_ERROR = "GID";
    public static final String FIELD_FEATURE_ID = "ID_FEATURE";
    public static final String FIELD_REASON_ERROR = "REASON";
    public static final String FIELD_TOPOLOGY_OPERATION = "OPERATION";
    public static final String FIELD_SOURCE_LAYER = "SOURCE";
    public static final String FIELD_TARGET_LAYER = "TARGET";
    public static final String FILTER_REASON_ERROR;
    public static final String TOPOLOGY_REASON_ERROR;
    protected int checkStrategy = 1;
    public Set<ITrigger> successTriggers;
    public Set<ITrigger> failureTriggers;
    protected List<Feature> errors;
    protected String sourceLayerName;
    protected Filter entrySourceFilter;
    protected Filter alphanumericFilter;
    protected boolean enabled = true;
    protected static GeometryFactory geomFact;
    protected OperationType operationType = OperationType.UNSPECIFIED;

    static {
        FILTER_REASON_ERROR = I18N.getString(AbstractTopologyRelation.class, "does-not-accomplish-filter");
        TOPOLOGY_REASON_ERROR = I18N.getString(AbstractTopologyRelation.class, "does-not-accomplish-topology");
        geomFact = new GeometryFactory();
        errorSchema = new FeatureSchema();
        errorSchema.addAttribute(FIELD_ID_ERROR, AttributeType.LONG, true);
        errorSchema.addAttribute(FIELD_FEATURE_ID, AttributeType.LONG);
        errorSchema.addAttribute(FIELD_SOURCE_LAYER, AttributeType.STRING);
        errorSchema.addAttribute(FIELD_TOPOLOGY_OPERATION, AttributeType.STRING);
        errorSchema.addAttribute(FIELD_TARGET_LAYER, AttributeType.STRING);
        errorSchema.addAttribute(FIELD_REASON_ERROR, AttributeType.STRING);
        errorSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    }

    @Override
    public boolean check(Feature feature, Collection<Feature> features) throws Exception {
        if (this.entrySourceFilter != null && !this.entrySourceFilter.contains(feature)) {
            return true;
        }
        if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feature)) {
            return false;
        }
        return this.checkFeature(feature, features, null);
    }

    @Override
    public boolean checkAll() {
        boolean checkAll = true;
        this.errors = new ArrayList<Feature>();
        Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        FeatureIterator it = null;
        try {
            try {
                FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
                it = fc.queryIterator(this.entrySourceFilter, null);
                while (it.hasNext()) {
                    Feature feat = it.next();
                    if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feat)) {
                        checkAll = false;
                        this.addErrorFeature(feat, FILTER_REASON_ERROR);
                        continue;
                    }
                    boolean check = this.checkFeature(feat, new ArrayList<Feature>(), null);
                    if (!check) {
                        this.addErrorFeature(feat, TOPOLOGY_REASON_ERROR);
                    }
                    boolean bl = checkAll = checkAll && check;
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
        return checkAll;
    }

    public String toString() {
        String toString = this.getName();
        if (this.getSourceLayerName() != null) {
            toString = String.valueOf(toString) + " ( " + this.getSourceLayerName() + " )";
        }
        return toString;
    }

    @Override
    public List<Feature> obtainErrors() {
        return this.errors;
    }

    @Override
    public String getSourceLayerName() {
        return this.sourceLayerName;
    }

    @Override
    public void setSourceLayerName(String sourceLayer) {
        this.sourceLayerName = sourceLayer;
    }

    @Override
    public Filter getEntrySourceFilter() {
        return this.entrySourceFilter;
    }

    @Override
    public void setEntrySourceFilter(Filter sourceFilter) {
        this.entrySourceFilter = sourceFilter;
    }

    @Override
    public Filter getAlphanumericFilter() {
        return this.alphanumericFilter;
    }

    @Override
    public void setAlphanumericFilter(Filter alphanumericFilter) {
        this.alphanumericFilter = alphanumericFilter;
    }

    protected void addErrorFeature(Feature feat, String reasonError) {
        if (this.errors == null) {
            return;
        }
        Feature errorFeat = FeatureUtil.toFeature(feat.getGeometry(), errorSchema);
        errorFeat.setAttribute(FIELD_SOURCE_LAYER, (Object)this.sourceLayerName);
        errorFeat.setAttribute(FIELD_TARGET_LAYER, null);
        errorFeat.setAttribute(FIELD_REASON_ERROR, (Object)reasonError);
        errorFeat.setAttribute(FIELD_TOPOLOGY_OPERATION, (Object)this.toString());
        if (feat.getPrimaryKey() != null) {
            errorFeat.setAttribute(FIELD_FEATURE_ID, (Object)((Number)feat.getPrimaryKey()).longValue());
        }
        this.errors.add(errorFeat);
    }

    protected abstract boolean checkFeature(Feature var1, Collection<Feature> var2, FeatureCollection var3) throws Exception;

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void success(Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.successTriggers)) {
            return;
        }
        for (ITrigger currentTrigger : this.successTriggers) {
            currentTrigger.execute(features);
        }
    }

    @Override
    public void failure(Feature currentFeat, Collection<Feature> features) throws Exception {
        if (CollectionUtils.isEmpty(this.failureTriggers)) {
            String message = I18N.getMessage("org.saig.core.model.feature.FeatureCollectionOnDemand.error-topology-relation-{0}-feature-{1}", new Object[]{this.toString(), "PK = " + currentFeat.getPrimaryKey()});
            throw new TopologyRelationException(message);
        }
        for (ITrigger currentTrigger : this.failureTriggers) {
            currentTrigger.execute(features);
        }
    }

    @Override
    public Set<ITrigger> getSuccessTriggers() {
        return this.successTriggers;
    }

    @Override
    public void setSuccessTriggers(Set<ITrigger> triggers) {
        this.successTriggers = triggers;
    }

    @Override
    public Set<ITrigger> getFailureTriggers() {
        return this.failureTriggers;
    }

    @Override
    public void setFailureTriggers(Set<ITrigger> triggers) {
        this.failureTriggers = triggers;
    }

    @Override
    public int getCheckStrategy() {
        return this.checkStrategy;
    }

    @Override
    public void setCheckStrategy(int checkStrategy) {
        this.checkStrategy = checkStrategy;
    }

    @Override
    public boolean checkValidGeometryType(int geomType) {
        return true;
    }

    @Override
    public OperationType getOperationType() {
        return this.operationType;
    }

    @Override
    public void setOperationType(OperationType type) {
        this.operationType = type;
    }
}

