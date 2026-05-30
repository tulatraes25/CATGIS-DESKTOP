/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check.self;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class NoSelfIntersectionCheck
extends AbstractSelfTopologyCheck {
    private static final String REASON = I18N.getString("org.saig.core.check.self.NoSelfIntersectionCheck.No-self-intersection-check");
    private static final String CONDITION = "no_self_intersection_check";

    public NoSelfIntersectionCheck(Layer sourceLayer) {
        this.sourceLayer = sourceLayer;
    }

    @Override
    public List<Feature> execute(List<SummaryMessage> messageList) {
        ArrayList<Feature> incorrectFeatures = new ArrayList<Feature>();
        this.incidentSchema = this.generateIncidentSchema(this.sourceLayer.getGeometryType());
        FeatureIterator it = null;
        try {
            try {
                FeatureCollection fc = this.sourceLayer.getUltimateFeatureCollectionWrapper();
                it = fc.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    if (feat.getGeometry().isSimple()) continue;
                    incorrectFeatures.add(this.generateIncidentFeature(feat, false));
                }
            }
            catch (Exception e) {
                messageList.add(this.buildCheckErrorMessage(null, e));
                ArrayList<Feature> arrayList = incorrectFeatures;
                if (it != null) {
                    it.close();
                }
                return arrayList;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return incorrectFeatures;
    }

    @Override
    protected String generateReason() {
        return REASON;
    }

    @Override
    protected String generateConditionFromFilter() {
        return "no_self_intersection_check(" + this.sourceLayer.getName() + ")";
    }
}

