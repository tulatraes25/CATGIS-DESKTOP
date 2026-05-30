/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check.self;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.Date;
import java.util.List;
import org.saig.core.check.Check;
import org.saig.jump.widgets.summary.SummaryMessage;

public abstract class AbstractSelfTopologyCheck
extends Check {
    protected abstract String generateReason();

    @Override
    protected abstract String generateConditionFromFilter();

    public abstract List<Feature> execute(List<SummaryMessage> var1);

    @Override
    protected Feature generateIncidentFeature(Feature currentFeature, boolean okAnyGeometry) {
        Feature newFeature = FeatureUtil.toFeature(currentFeature.getGeometry(), this.incidentSchema);
        newFeature.setAttribute(CONDITION_ATTR, (Object)this.generateConditionFromFilter());
        newFeature.setAttribute(REASON_ATTR, (Object)this.generateReasonFromFilter(okAnyGeometry));
        newFeature.setAttribute(DATE_ATTR, (Object)new Date());
        return newFeature;
    }
}

