/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractSelectionRenderer;
import java.awt.Color;
import java.util.HashSet;

public class SelectionBackgroundRenderer
extends AbstractSelectionRenderer {
    public static final String CONTENT_ID = "SELECTION_BACKGROUND";

    public SelectionBackgroundRenderer(LayerViewPanel panel, double factor) {
        super(CONTENT_ID, panel, Color.yellow, false, true, factor);
    }

    @Override
    protected CollectionMap featureToSelectedItemsMap(Layer layer) {
        HashSet<Feature> featuresNeedingBackground = new HashSet<Feature>();
        featuresNeedingBackground.addAll(this.panel.getSelectionManager().getPartSelection().getFeaturesWithSelectedItems(layer));
        featuresNeedingBackground.addAll(this.panel.getSelectionManager().getLineStringSelection().getFeaturesWithSelectedItems(layer));
        CollectionMap map = new CollectionMap();
        for (Feature feature : featuresNeedingBackground) {
            map.addItem(feature, feature.getGeometry());
        }
        return map;
    }
}

