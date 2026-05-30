/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.SystemLayerFinder;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleTerminalDecorator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVectorLayerFinder
extends SystemLayerFinder {
    protected Color color;

    public AbstractVectorLayerFinder(String layerName, LayerManagerProxy layerManagerProxy, Color color) {
        super(layerName, layerManagerProxy);
        this.color = color;
    }

    public List<Geometry> getVectors() {
        if (this.getLayer() == null) {
            return new ArrayList<Geometry>();
        }
        return FeatureUtil.toGeometries(this.getLayer().getFeatureCollectionWrapper().getFeatures());
    }

    @Override
    protected void applyStyles(Layer layer) {
        if (layer.getStyle(ArrowTerminalDecorator.class) == null) {
            layer.addStyle(new ArrowTerminalDecorator.SolidEnd());
        }
        if (layer.getStyle(CircleTerminalDecorator.Start.class) == null) {
            layer.addStyle(new CircleTerminalDecorator.Start());
        }
        layer.getBasicStyle().setLineColor(this.color);
        layer.getBasicStyle().setFillColor(this.color);
        layer.getBasicStyle().setRenderingFill(false);
        layer.setDrawingLast(true);
    }
}

