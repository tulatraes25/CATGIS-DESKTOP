/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractSelectionRenderer;
import java.awt.Color;
import java.util.Collection;

public class PartSelectionRenderer
extends AbstractSelectionRenderer {
    public static final String CONTENT_ID = "SELECTED_PARTS";

    public PartSelectionRenderer(LayerViewPanel panel, double factor) {
        super(CONTENT_ID, panel, Color.red, true, false, factor);
    }

    @Override
    protected CollectionMap featureToSelectedItemsMap(Layer layer) {
        return this.panel.getSelectionManager().getPartSelection().getFeatureToSelectedItemCollectionMap(layer);
    }

    @Override
    protected Collection<Feature> featureToSelectedItemsMap(Layer layer, Envelope envelope) {
        return this.panel.getSelectionManager().getPartSelection().getFeatureToSelectedItemCollectionMap(layer, envelope);
    }
}

