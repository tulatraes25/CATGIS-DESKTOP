/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.trigger.impl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import java.util.HashSet;
import org.saig.core.model.data.trigger.AbstractTrigger;
import org.saig.core.model.data.trigger.IPostDeleteTrigger;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.jump.lang.I18N;

public class RemoveDescendantsFromLayerTrigger
extends AbstractTrigger
implements IPostDeleteTrigger {
    public static final String NAME = I18N.getString(RemoveDescendantsFromLayerTrigger.class, "remove-elements-from-descendant-layers");
    public static final String DESCRIPTION = I18N.getString(RemoveDescendantsFromLayerTrigger.class, "allows-to-remove-related-elements-from-other-layers");
    public static final String ID = RemoveDescendantsFromLayerTrigger.class.getName();
    protected String sourceLayerName;
    protected String[] descendantsLayerNames;
    protected boolean autocommit = false;

    public RemoveDescendantsFromLayerTrigger() {
        this.name = NAME;
        this.description = DESCRIPTION;
    }

    public RemoveDescendantsFromLayerTrigger(String sourceLayerName, String[] descendantsLayerNames, boolean autocommit) {
        this();
        this.sourceLayerName = sourceLayerName;
        this.descendantsLayerNames = descendantsLayerNames;
        this.autocommit = autocommit;
    }

    @Override
    public boolean execute(Collection<Feature> features) throws Exception {
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        if (sourceLayer == null) {
            sourceLayer = JUMPWorkbench.getHiddenLayer(this.sourceLayerName);
        }
        if (sourceLayer == null) {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "source-layer-{0}-could-not-be-recovered", new Object[]{this.sourceLayerName}));
            return true;
        }
        int i = 0;
        while (i < this.descendantsLayerNames.length) {
            String descendantLayerName = this.descendantsLayerNames[i];
            Layer descendantLayer = JUMPWorkbench.getLayer(descendantLayerName);
            if (sourceLayer == null) {
                descendantLayer = JUMPWorkbench.getHiddenLayer(descendantLayerName);
            }
            if (descendantLayer != null) {
                Collection<Feature> descendantFeatures = this.findDescendantFeatures(sourceLayer, descendantLayer, features);
                if (!descendantFeatures.isEmpty()) {
                    descendantLayer.getFeatureCollectionWrapper().removeAll(descendantFeatures);
                    if (this.autocommit) {
                        descendantLayer.getFeatureCollectionWrapper().commit();
                        descendantLayer.setFeatureCollectionModified(false);
                    } else {
                        descendantLayer.fireAppearanceChanged();
                    }
                }
            } else {
                LOGGER.warn((Object)I18N.getMessage(this.getClass(), "layer-{0}-not-found", new Object[]{descendantLayerName}));
            }
            ++i;
        }
        return true;
    }

    protected Collection<Feature> findDescendantFeatures(Layer sourceLayer, Layer descendantLayer, Collection<Feature> featuresToRemove) throws Exception {
        HashSet<Feature> descendantFeatures = new HashSet<Feature>();
        Collection<Relation<?>> relations = sourceLayer.getAllRelations();
        for (Relation<?> currentRelation : relations) {
            LayerRelation relation;
            if (!(currentRelation instanceof LayerRelation) || !(relation = (LayerRelation)currentRelation).getTargetLayer().equals(descendantLayer)) continue;
            for (Feature currentFeature : featuresToRemove) {
                Object relationValue = currentFeature.getAttribute(relation.getSourceAttribute());
                if (relationValue == null) continue;
                descendantFeatures.addAll(relation.getRelationRecords(relationValue));
            }
        }
        return descendantFeatures;
    }

    @Override
    public void onDelete(Collection<Feature> featuresToRemove) throws Exception {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "executing-remove-in-cascade-rule-for-layer-{0}", new Object[]{this.sourceLayerName}));
        this.execute(featuresToRemove);
    }

    @Override
    public String getID() {
        return ID;
    }

    public void setSourceLayerName(String sourceLayerName) {
        this.sourceLayerName = sourceLayerName;
    }

    public void setDescendantsLayerNames(String[] descendantsLayerNames) {
        this.descendantsLayerNames = descendantsLayerNames;
    }

    public void setAutocommit(boolean auto) {
        this.autocommit = auto;
    }
}

