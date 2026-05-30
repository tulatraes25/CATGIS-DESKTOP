/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.renderer.style.CoordinatesEqualDecorator;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class IncrementalWarpingVectorLayerFinder
extends AbstractVectorLayerFinder {
    public static final String LAYER_NAME = I18N.getString("workbench.ui.warp.IncrementalWarpingVectorLayerFinder.name");
    public static final Color COLOR = Color.green.darker().darker();

    public IncrementalWarpingVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        super(LAYER_NAME, layerManagerProxy, COLOR);
    }

    @Override
    protected void applyStyles(Layer layer) {
        super.applyStyles(layer);
        if (layer.getStyle(CoordinatesEqualDecorator.class) == null) {
            layer.addStyle(new CoordinatesEqualDecorator(COLOR));
        }
    }
}

