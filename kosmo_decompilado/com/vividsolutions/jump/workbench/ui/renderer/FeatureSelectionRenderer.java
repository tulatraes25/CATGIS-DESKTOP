/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.saig.core.renderer3.SelectionRenderer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.jump.widgets.config.ConfigSelectionPanel;

public class FeatureSelectionRenderer
extends AbstractSelectionRenderer {
    public static final String CONTENT_ID = "SELECTED_FEATURES";
    public static final String SHOW_SELECTION_HANDLES_FOR_INTERNAL_LAYERS_KEY = "SHOW_SELECTION_HANDLES_FOR_INTERNAL_LAYERS";
    private boolean showSelectionHandlesForInternalLayers = true;

    public FeatureSelectionRenderer(LayerViewPanel panel, double factor) {
        super(CONTENT_ID, panel, Color.yellow, false, false, factor);
        Blackboard blackboard = JUMPWorkbench.getFrameInstance().getContext().getBlackboard();
        Color fill = (Color)PersistentBlackboardPlugIn.get(blackboard).get(ConfigSelectionPanel.SELECTION_FILL_COLOR, ConfigSelectionPanel.SELECTION_FILL_DEFAULT_COLOR);
        Color line = (Color)PersistentBlackboardPlugIn.get(blackboard).get(ConfigSelectionPanel.SELECTION_LINE_COLOR, ConfigSelectionPanel.SELECTION_LINE_DEFAULT_COLOR);
        double transparency = PersistentBlackboardPlugIn.get(blackboard).get(ConfigSelectionPanel.SELECTION_FILL_TRANSPARENCY, 1.0);
        this.showSelectionHandlesForInternalLayers = PersistentBlackboardPlugIn.get(blackboard).get(SHOW_SELECTION_HANDLES_FOR_INTERNAL_LAYERS_KEY, true);
        if (transparency > 1.0) {
            PersistentBlackboardPlugIn.get(blackboard).put(ConfigSelectionPanel.SELECTION_FILL_TRANSPARENCY, transparency /= 255.0);
        }
        this.setLineColor(line);
        if (fill != null) {
            this.setFillColor(GUIUtil.alphaColor(fill, (int)(transparency * 255.0)));
        } else {
            this.setFillColor(null);
        }
        this.setHandleLineColor(Color.RED);
        PointSymbolizer pointSymbolizer = (PointSymbolizer)PersistentBlackboardPlugIn.get(blackboard).get(ConfigSelectionPanel.SELECTION_VERTEX_DEFAULT_SYMBOL, this.generateDefaultVertexSymbolizer());
        this.setVertexSymbolizer(pointSymbolizer);
    }

    @Override
    protected CollectionMap featureToSelectedItemsMap(Layer layer) {
        return this.panel.getSelectionManager().getFeatureSelection().getFeatureToSelectedItemCollectionMap(layer);
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        List<Layer> layers = this.panel.getLayerManager().getLayers();
        layers.addAll(this.panel.getLayerManager().getHideLayers());
        for (Layer layer : layers) {
            Collection<Feature> featureToSelectedItemsMap = this.featureToSelectedItemsMap(layer, this.panel.getViewport().getEnvelopeInModelCoordinates());
            if (featureToSelectedItemsMap.isEmpty()) continue;
            this.featureIterator = featureToSelectedItemsMap.iterator();
            this.renderFeatures(image, this.featureIterator, this, layer);
        }
    }

    @Override
    protected void renderFeatures(ThreadSafeImage image, Iterator<Feature> featureIterator, Style style, Layer layer) throws Exception {
        if (!layer.isVisible() || !style.isEnabled()) {
            return;
        }
        SelectionRenderer renderer = new SelectionRenderer(1.0);
        renderer.renderSelection(image, image.getImage().getWidth(null), image.getImage().getHeight(null), this.panel.getViewport().getEnvelopeInModelCoordinates(), layer, this.panel.getViewport().getAngle(), this.panel.getViewport().getScale(), layer.isOneQueryByRule(), this.panel.getUserLengthUnit(), featureIterator, this.getSelectionRule(layer), layer.isEditable() || this.showSelectionHandlesForInternalLayers && layer.isInternal(), this.vertexSymbolizer, this);
    }
}

