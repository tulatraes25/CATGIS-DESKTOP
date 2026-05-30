/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.trigger.impl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import org.saig.core.model.data.trigger.AbstractTrigger;
import org.saig.core.model.data.trigger.IPreAddTrigger;
import org.saig.core.model.data.trigger.IPreUpdateTrigger;
import org.saig.jump.lang.I18N;

public abstract class UpdateAttributeTrigger
extends AbstractTrigger
implements IPreUpdateTrigger,
IPreAddTrigger {
    public static final String NAME = I18N.getString(UpdateAttributeTrigger.class, "update-attribute-value");
    public static final String DESCRIPTION = I18N.getString(UpdateAttributeTrigger.class, "allows-to-update-an-attribute-s-value");
    public static final String ID = UpdateAttributeTrigger.class.getName();
    protected String attrName;
    protected String sourceLayerName;

    public UpdateAttributeTrigger() {
        this.name = NAME;
        this.description = DESCRIPTION;
    }

    public UpdateAttributeTrigger(String sourceLayerName, String attributeName) {
        this();
        this.sourceLayerName = sourceLayerName;
        this.attrName = attributeName;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public boolean execute(Collection<Feature> features) throws Exception {
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        if (sourceLayer == null) {
            sourceLayer = JUMPWorkbench.getHiddenLayer(this.sourceLayerName);
        }
        if (sourceLayer == null) {
            LOGGER.warn((Object)I18N.getMessage(UpdateAttributeTrigger.class, "source-layer-{0}-could-not-been-recovered", new Object[]{this.sourceLayerName}));
            return true;
        }
        if (!sourceLayer.getFeatureSchema().hasAttribute(this.attrName)) {
            LOGGER.warn((Object)I18N.getMessage(UpdateAttributeTrigger.class, "source-layer-{0}-has-no-such-attribute-{1}", new Object[]{this.sourceLayerName, this.attrName}));
            return true;
        }
        for (Feature currentFeat : features) {
            currentFeat.setAttribute(this.attrName, this.getFeatureValue(currentFeat));
        }
        return true;
    }

    public abstract Object getFeatureValue(Feature var1);

    @Override
    public boolean onAdd(Collection<Feature> featuresToAdd) throws Exception {
        LOGGER.info((Object)(String.valueOf(I18N.getString(UpdateAttributeTrigger.class, "executing-rule-for-elements-update-in-the-layer")) + this.sourceLayerName));
        this.execute(featuresToAdd);
        return true;
    }

    @Override
    public boolean onUpdate(Collection<Feature> featuresToUpdate) throws Exception {
        LOGGER.info((Object)(String.valueOf(I18N.getString(UpdateAttributeTrigger.class, "executing-rule-for-elements-update-in-the-layer")) + this.sourceLayerName));
        return this.execute(featuresToUpdate);
    }
}

