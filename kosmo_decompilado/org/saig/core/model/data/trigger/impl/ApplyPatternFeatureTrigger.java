/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.precision.EnhancedPrecisionOp
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.trigger.impl;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.saig.core.model.data.trigger.AbstractTrigger;
import org.saig.core.model.data.trigger.IPostAddTrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class ApplyPatternFeatureTrigger
extends AbstractTrigger
implements IPostAddTrigger {
    protected static final Logger LOGGER = Logger.getLogger(ApplyPatternFeatureTrigger.class);
    public static final String NAME = I18N.getString(ApplyPatternFeatureTrigger.class, "apply-pattern-feature");
    public static final String DESCRIPTION = I18N.getString(ApplyPatternFeatureTrigger.class, "allows-to-create-new-features-using-another-feature-in-related-layers-as-pattern");
    public static final String ID = ApplyPatternFeatureTrigger.class.getName();
    protected String sourceLayerName;
    protected String[] relationedLayerNames;
    protected boolean autocommit = false;

    public ApplyPatternFeatureTrigger() {
        this.name = NAME;
        this.description = DESCRIPTION;
    }

    public ApplyPatternFeatureTrigger(String sourceLayerName, String[] relationedLayerNames, boolean autocommit) {
        this();
        this.sourceLayerName = sourceLayerName;
        this.relationedLayerNames = relationedLayerNames;
        this.autocommit = autocommit;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void onAdd(Collection<Feature> featuresToAdd) throws Exception {
        LOGGER.info((Object)I18N.getMessage(ApplyPatternFeatureTrigger.class, "executing-update-rule-by-pattern-for-layer-{0}", new Object[]{this.sourceLayerName}));
        this.execute(featuresToAdd);
    }

    @Override
    public boolean execute(Collection<Feature> features) throws Exception {
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        if (sourceLayer == null) {
            sourceLayer = JUMPWorkbench.getHiddenLayer(this.sourceLayerName);
        }
        if (sourceLayer == null) {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "source-layer-{0}-was-no-recovered", new Object[]{this.sourceLayerName}));
            return true;
        }
        int i = 0;
        while (i < this.relationedLayerNames.length) {
            String relationedLayerName = this.relationedLayerNames[i];
            Layer relationedLayer = JUMPWorkbench.getLayer(relationedLayerName);
            if (relationedLayer == null) {
                relationedLayer = JUMPWorkbench.getHiddenLayer(relationedLayerName);
            }
            if (relationedLayer != null) {
                Collection<Feature> affectedFeatures = this.findAffectedFeatures(sourceLayer, relationedLayer, features);
                if (!affectedFeatures.isEmpty()) {
                    relationedLayer.getFeatureCollectionWrapper().updateAll(affectedFeatures);
                    if (this.autocommit) {
                        relationedLayer.getFeatureCollectionWrapper().commit();
                        relationedLayer.setFeatureCollectionModified(false);
                    }
                    relationedLayer.fireAppearanceChanged();
                }
            } else {
                LOGGER.warn((Object)I18N.getMessage(ApplyPatternFeatureTrigger.class, "layer-{0}-was-not-found", new Object[]{relationedLayer}));
            }
            ++i;
        }
        return true;
    }

    protected Collection<Feature> findAffectedFeatures(Layer sourceLayer, Layer relationedLayer, Collection<Feature> featuresToAdd) {
        HashSet<Feature> affectedFeatures = new HashSet<Feature>();
        for (Feature feat : featuresToAdd) {
            Geometry geomSource = feat.getGeometry();
            if (geomSource == null) continue;
            Envelope envelope = geomSource.getEnvelopeInternal();
            FeatureIterator itFeats = null;
            try {
                try {
                    itFeats = relationedLayer.getFeatureCollectionWrapper().queryIterator(envelope);
                    while (itFeats.hasNext()) {
                        Feature currentFeat = itFeats.next();
                        Geometry geomOverlay = currentFeat.getGeometry();
                        if (!this.fillConditions(feat, currentFeat) || !geomOverlay.intersects(geomSource)) continue;
                        Geometry geomRes = EnhancedPrecisionOp.intersection((Geometry)geomOverlay, (Geometry)geomSource);
                        Feature clonedFeat = currentFeat.clone(true);
                        Geometry newGeom = EnhancedPrecisionOp.difference((Geometry)geomOverlay, (Geometry)geomRes);
                        clonedFeat.setGeometry(newGeom);
                        affectedFeatures.add(clonedFeat);
                    }
                }
                catch (Exception ex) {
                    LOGGER.error((Object)ex);
                    if (itFeats == null) continue;
                    itFeats.close();
                    continue;
                }
            }
            catch (Throwable throwable) {
                if (itFeats != null) {
                    itFeats.close();
                }
                throw throwable;
            }
            if (itFeats == null) continue;
            itFeats.close();
        }
        return affectedFeatures;
    }

    protected boolean fillConditions(Feature sourceFeat, Feature targetFeat) {
        return true;
    }
}

