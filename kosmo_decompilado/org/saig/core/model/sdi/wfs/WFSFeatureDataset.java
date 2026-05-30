/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 */
package org.saig.core.model.sdi.wfs;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.deegree.datatypes.QualifiedName;
import org.saig.core.model.sdi.wfs.WFSFeature;

public class WFSFeatureDataset
extends FeatureDataset {
    protected QualifiedName featureTypeName;

    public WFSFeatureDataset(Collection<Feature> newFeatures, FeatureSchema featureSchema) {
        super(newFeatures, featureSchema);
    }

    public WFSFeatureDataset(FeatureSchema featureSchema) {
        super(featureSchema);
    }

    public void removeAllDirect() {
        this.features.clear();
        this.createSpatialIndex();
    }

    public void addAllDirect(List<Feature> feats) {
        ArrayList<Feature> toRemove = new ArrayList<Feature>();
        for (Feature feat : feats) {
            if (this.isUpdatedFeature((WFSFeature)feat)) {
                toRemove.add(feat);
            }
            if (!this.isDeletedFeature((WFSFeature)feat)) continue;
            toRemove.add(feat);
        }
        feats.removeAll(toRemove);
        this.features.addAll(feats);
        this.createSpatialIndex();
    }

    private boolean isUpdatedFeature(WFSFeature feat) {
        for (Feature feature : this.updateFeatures.values()) {
            WFSFeature wf = (WFSFeature)feature;
            if (!feat.getGMLId().equals(wf.getGMLId())) continue;
            return true;
        }
        return false;
    }

    private boolean isDeletedFeature(WFSFeature feat) {
        for (Feature feature : this.deletedFeatures.values()) {
            WFSFeature wf = (WFSFeature)feature;
            if (!feat.getGMLId().equals(wf.getGMLId())) continue;
            return true;
        }
        return false;
    }

    public QualifiedName getFeatureTypeName() {
        return this.featureTypeName;
    }

    public void setFeatureTypeName(QualifiedName featureTypeName) {
        this.featureTypeName = featureTypeName;
    }
}

