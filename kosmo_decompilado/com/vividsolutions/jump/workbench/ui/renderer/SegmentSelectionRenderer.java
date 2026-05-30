/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Envelope;
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
import org.saig.core.renderer2.SelectionRenderer;
import org.saig.core.styling.PointSymbolizer;

public class SegmentSelectionRenderer
extends AbstractSelectionRenderer {
    public static final String CONTENT_ID = "SELECTED_SEGMENTS";
    public static final String SELECTION_FILL_COLOR = String.valueOf(SegmentSelectionRenderer.class.getName()) + " - FILL SELECTION COLOR";
    public static final String SELECTION_LINE_COLOR = String.valueOf(SegmentSelectionRenderer.class.getName()) + " - LINE SELECTION COLOR";
    public static final String SELECTION_FILL_TRANSPARENCY = String.valueOf(SegmentSelectionRenderer.class.getName()) + " - FILL SELECTION TRANSPARENCY";
    public static final Color SELECTION_LINE_DEFAULT_COLOR = Color.BLUE;
    public static final Color SELECTION_FILL_DEFAULT_COLOR = null;
    public static final double FILL_TRANSPARENCY_DEFAULT_VALUE = 1.0;
    public static final String SELECTION_VERTEX_DEFAULT_SYMBOL = String.valueOf(SegmentSelectionRenderer.class.getName()) + " - VERTEX DEFAULT SYMBOL";

    public SegmentSelectionRenderer(LayerViewPanel panel, double factor) {
        super(CONTENT_ID, panel, Color.BLUE, true, false, factor);
        Blackboard blackboard = JUMPWorkbench.getFrameInstance().getContext().getBlackboard();
        Color fill = (Color)PersistentBlackboardPlugIn.get(blackboard).get(SELECTION_FILL_COLOR, SELECTION_FILL_DEFAULT_COLOR);
        Color line = (Color)PersistentBlackboardPlugIn.get(blackboard).get(SELECTION_LINE_COLOR, SELECTION_LINE_DEFAULT_COLOR);
        double transparency = PersistentBlackboardPlugIn.get(blackboard).get(SELECTION_FILL_TRANSPARENCY, 1.0);
        if (transparency > 1.0) {
            PersistentBlackboardPlugIn.get(blackboard).put(SELECTION_FILL_TRANSPARENCY, transparency /= 255.0);
        }
        this.setLineColor(line);
        if (fill != null) {
            this.setFillColor(GUIUtil.alphaColor(fill, (int)(transparency * 255.0)));
        } else {
            this.setFillColor(null);
        }
        this.setHandleLineColor(Color.RED);
        PointSymbolizer pointSymbolizer = (PointSymbolizer)PersistentBlackboardPlugIn.get(blackboard).get(SELECTION_VERTEX_DEFAULT_SYMBOL, this.generateDefaultVertexSymbolizer());
        this.setVertexSymbolizer(pointSymbolizer);
    }

    @Override
    protected CollectionMap featureToSelectedItemsMap(Layer layer) {
        return this.panel.getSelectionManager().getSegmentSelection().getFeatureToSelectedItemCollectionMap(layer);
    }

    @Override
    protected Collection<Feature> featureToSelectedItemsMap(Layer layer, Envelope envelope) {
        return this.panel.getSelectionManager().getSegmentSelection().getFeatureToSelectedItemCollectionMap(layer, envelope);
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
        renderer.renderSelection(image, layer, this.panel.getViewport(), featureIterator, this.getSelectionRule(layer), layer.isEditable() || layer.getUltimateFeatureCollectionWrapper().isEditable(), this.vertexSymbolizer, this);
    }
}

