/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.trigger;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;

public interface IPostDeleteTrigger {
    public void onDelete(Collection<Feature> var1) throws Exception;
}

