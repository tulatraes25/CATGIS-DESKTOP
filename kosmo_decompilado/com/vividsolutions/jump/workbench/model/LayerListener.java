/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.LayerEvent;

public interface LayerListener {
    public void featuresChanged(FeatureEvent var1);

    public void layerChanged(LayerEvent var1);

    public void categoryChanged(CategoryEvent var1);
}

