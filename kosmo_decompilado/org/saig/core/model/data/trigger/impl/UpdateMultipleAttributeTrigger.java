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
import org.saig.core.model.data.trigger.TriggerException;
import org.saig.core.model.data.trigger.impl.UpdateAttributeTrigger;
import org.saig.jump.lang.I18N;

public abstract class UpdateMultipleAttributeTrigger
extends AbstractTrigger
implements IPreUpdateTrigger,
IPreAddTrigger {
    public static final String NAME = I18N.getString("org.saig.core.model.data.trigger.impl.UpdateMultipleAttributeTrigger.Update-attribute-values");
    public static final String DESCRIPTION = I18N.getString("org.saig.core.model.data.trigger.impl.UpdateMultipleAttributeTrigger.Allow-to-update-multiple-attribute-values");
    public static final String ID = UpdateMultipleAttributeTrigger.class.getName();
    protected String[] attrNames;
    protected String sourceLayerName;

    public UpdateMultipleAttributeTrigger() {
        this.name = NAME;
        this.description = DESCRIPTION;
    }

    public UpdateMultipleAttributeTrigger(String sourceLayerName, String[] attributeNames) {
        this();
        this.sourceLayerName = sourceLayerName;
        this.attrNames = attributeNames;
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
            String message = I18N.getMessage(UpdateAttributeTrigger.class, "source-layer-{0}-could-not-been-recovered", new Object[]{this.sourceLayerName});
            LOGGER.warn((Object)message);
            throw new TriggerException(message);
        }
        int i = 0;
        while (i < this.attrNames.length) {
            String attrName = this.attrNames[i];
            if (!sourceLayer.getFeatureSchema().hasAttribute(attrName)) {
                String message = I18N.getMessage(UpdateAttributeTrigger.class, "source-layer-{0}-has-no-such-attribute-{1}", new Object[]{this.sourceLayerName, attrName});
                LOGGER.warn((Object)message);
                throw new TriggerException(message);
            }
            ++i;
        }
        for (Feature currentFeat : features) {
            Object[] values = this.getFeatureValues(currentFeat);
            int i2 = 0;
            while (i2 < values.length) {
                currentFeat.setAttribute(this.attrNames[i2], values[i2]);
                ++i2;
            }
        }
        return true;
    }

    public abstract Object[] getFeatureValues(Feature var1) throws Exception;

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

