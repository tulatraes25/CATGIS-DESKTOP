/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.check.self;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class OnlySinglePartCheck
extends AbstractSelfTopologyCheck {
    private static final String REASON = I18N.getString("org.saig.core.check.self.OnlySinglePartCheck.only-single-part-check");
    private static final String CONDITION = "only_single_part";

    public OnlySinglePartCheck(Layer sourceLayer) {
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
                    Geometry geom = feat.getGeometry();
                    if (geom.getNumGeometries() <= 1) continue;
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
        return "only_single_part(" + this.sourceLayer.getName() + ")";
    }
}

