/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check.self;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.check.Check;
import org.saig.core.check.CheckingException;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.check.self.SelfTopologyCheckFactory;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class SelfCheck
extends Check {
    @Override
    protected void getLayersForCheck() throws CheckingException {
        String sourceLayerName = this.sourceLayer.getName();
        this.sourceLayer = JUMPWorkbench.getLayer(sourceLayerName);
        if (this.sourceLayer == null) {
            throw new CheckingException(I18N.getMessage("org.saig.core.check.self.SelfCheck.The-layer-{0}-could-not-be-loaded", new Object[]{sourceLayerName}));
        }
    }

    @Override
    public FeatureCollection check(List<SummaryMessage> messageList) {
        List<Object> incorrectFeatures = new ArrayList();
        FeatureCollection fcIncident = null;
        AbstractSelfTopologyCheck selfTopologyCheck = null;
        try {
            this.getLayersForCheck();
            selfTopologyCheck = SelfTopologyCheckFactory.buildCheck(this.selfCheck, this.sourceLayer);
        }
        catch (CheckingException ce) {
            messageList.add(this.buildCheckErrorMessage(null, ce));
            return fcIncident;
        }
        this.incidentSchema = this.generateIncidentSchema(this.sourceLayer.getGeometryType());
        incorrectFeatures = selfTopologyCheck.execute(messageList);
        if (incorrectFeatures.size() != 0) {
            fcIncident = this.generateCollectionFromWrongFeatures(incorrectFeatures);
        }
        return fcIncident;
    }

    @Override
    public String getTopologyOperation() {
        return this.selfCheck;
    }

    @Override
    public void setTopologyOperation(String geometricOperation) {
        this.selfCheck = geometricOperation;
    }
}

