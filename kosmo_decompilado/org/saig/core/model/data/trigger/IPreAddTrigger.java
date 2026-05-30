/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.trigger;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;

public interface IPreAddTrigger {
    public boolean onAdd(Collection<Feature> var1) throws Exception;
}

