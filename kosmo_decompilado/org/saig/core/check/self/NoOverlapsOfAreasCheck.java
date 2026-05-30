/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check.self;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class NoOverlapsOfAreasCheck
extends AbstractSelfTopologyCheck {
    private static final String REASON = I18N.getString("org.saig.core.check.self.NoOverlapsOfAreasCheck.No-overlaps-of-areas-check");
    private static final String CONDITION = "no_overlaps_of_areas";

    public NoOverlapsOfAreasCheck(Layer sourceLayer) {
        this.sourceLayer = sourceLayer;
    }

    @Override
    public List<Feature> execute(List<SummaryMessage> messageList) {
        HashSet<Object> procesados = new HashSet<Object>();
        ArrayList<Feature> incorrectFeatures = new ArrayList<Feature>();
        this.incidentSchema = this.generateIncidentSchema(this.sourceLayer.getGeometryType());
        FeatureIterator it = null;
        try {
            try {
                FeatureCollection fc = this.sourceLayer.getUltimateFeatureCollectionWrapper();
                it = fc.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    if (procesados.contains(feat.getPrimaryKey())) continue;
                    FeatureIterator itCandidatos = null;
                    try {
                        try {
                            boolean check = false;
                            itCandidatos = fc.queryIterator(feat.getGeometry().getEnvelopeInternal());
                            while (itCandidatos.hasNext()) {
                                Feature candidato = itCandidatos.next();
                                if (candidato.equals(feat) || !candidato.getGeometry().overlaps(feat.getGeometry())) continue;
                                check = true;
                            }
                            if (check) {
                                incorrectFeatures.add(this.generateIncidentFeature(feat, false));
                            }
                            procesados.add(feat.getPrimaryKey());
                        }
                        catch (Exception e) {
                            messageList.add(this.buildCheckErrorMessage(feat, e));
                            if (itCandidatos == null) continue;
                            itCandidatos.close();
                            continue;
                        }
                    }
                    catch (Throwable throwable) {
                        if (itCandidatos != null) {
                            itCandidatos.close();
                        }
                        throw throwable;
                    }
                    if (itCandidatos == null) continue;
                    itCandidatos.close();
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
        return "no_overlaps_of_areas(" + this.sourceLayer.getName() + ")";
    }
}

