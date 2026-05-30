/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.trigger;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;

public interface ITrigger {
    public String getID();

    public String getName();

    public void setName(String var1);

    public String getDescription();

    public void setDescription(String var1);

    public boolean execute(Collection<Feature> var1) throws Exception;
}

