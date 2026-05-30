/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;

public abstract class AbstractTopologyBinaryRelation
extends AbstractTopologyRelation
implements ITopologyBinaryRelation {
    protected String targetLayerName;
    protected Filter entryTargetFilter;

    @Override
    public boolean check(Feature feature, Collection<Feature> features) throws Exception {
        if (this.entrySourceFilter != null && !this.entrySourceFilter.contains(feature)) {
            return true;
        }
        if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feature)) {
            return false;
        }
        Layer targetLayer = JUMPWorkbench.getLayer(this.getTargetLayerName());
        FeatureCollection fcTarget = targetLayer.getUltimateFeatureCollectionWrapper();
        return this.checkFeature(feature, features, fcTarget);
    }

    @Override
    public boolean checkAll() {
        this.errors = new ArrayList();
        boolean checkAll = true;
        FeatureIterator itSource = null;
        try {
            try {
                Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
                FeatureCollection fcSource = sourceLayer.getUltimateFeatureCollectionWrapper();
                Layer targetLayer = JUMPWorkbench.getLayer(this.getTargetLayerName());
                FeatureCollection fcTarget = targetLayer.getUltimateFeatureCollectionWrapper();
                itSource = fcSource.queryIterator(this.entrySourceFilter, null);
                while (itSource.hasNext()) {
                    Feature feat = itSource.next();
                    if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feat)) {
                        checkAll = false;
                        this.addErrorFeature(feat, AbstractTopologyRelation.FILTER_REASON_ERROR);
                        continue;
                    }
                    boolean check = this.checkFeature(feat, new ArrayList<Feature>(), fcTarget);
                    if (!check) {
                        this.addErrorFeature(feat, AbstractTopologyRelation.TOPOLOGY_REASON_ERROR);
                    }
                    boolean bl = checkAll = checkAll && check;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itSource != null) {
                    itSource.close();
                }
            }
        }
        finally {
            if (itSource != null) {
                itSource.close();
            }
        }
        return checkAll;
    }

    @Override
    public Filter getEntryTargetFilter() {
        return this.entryTargetFilter;
    }

    @Override
    public void setEntryTargetFilter(Filter targetFilter) {
        this.entryTargetFilter = targetFilter;
    }

    @Override
    public String getTargetLayerName() {
        return this.targetLayerName;
    }

    @Override
    public void setTargetLayerName(String targetLayer) {
        this.targetLayerName = targetLayer;
    }

    @Override
    public String toString() {
        String cadenaStr = this.getName();
        if (this.getSourceLayerName() != null && this.getTargetLayerName() != null) {
            cadenaStr = String.valueOf(cadenaStr) + " ( " + this.getSourceLayerName() + " -> " + this.getTargetLayerName() + " )";
        }
        return cadenaStr;
    }

    @Override
    protected void addErrorFeature(Feature feat, String reasonError) {
        if (this.errors == null) {
            return;
        }
        Feature errorFeat = FeatureUtil.toFeature(feat.getGeometry(), errorSchema);
        errorFeat.setAttribute("SOURCE", (Object)this.sourceLayerName);
        errorFeat.setAttribute("TARGET", (Object)this.targetLayerName);
        errorFeat.setAttribute("REASON", (Object)reasonError);
        errorFeat.setAttribute("OPERATION", (Object)this.toString());
        if (feat.getPrimaryKey() != null) {
            errorFeat.setAttribute("ID_FEATURE", (Object)((Number)feat.getPrimaryKey()).longValue());
        }
        this.errors.add(errorFeat);
    }
}

