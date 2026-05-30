/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class WarpingVectorLayerFinder
extends AbstractVectorLayerFinder {
    public static final String LAYER_NAME = I18N.getString("workbench.ui.warp.WarpingVectorLayerFinder.name");
    public static final Color COLOR = Color.blue;

    public WarpingVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        super(LAYER_NAME, layerManagerProxy, COLOR);
    }
}

