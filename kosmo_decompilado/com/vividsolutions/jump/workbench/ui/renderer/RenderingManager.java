/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.OrderedMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.LayerRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.LineStringSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.PartSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.SegmentSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.SelectionBackgroundRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.TemporalGeometriesRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.TextBalloonLayerRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;
import com.vividsolutions.jump.workbench.ui.renderer.WMSLayerRenderer;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import org.apache.log4j.Logger;
import org.saig.core.renderer.RenderingHintsManager;

public class RenderingManager {
    public static final String USE_MULTI_RENDERING_THREAD_QUEUE_KEY = String.valueOf(RenderingManager.class.getName()) + " - USE MULTI RENDERING THREAD QUEUE";
    protected static final Logger LOGGER = Logger.getLogger(RenderingManager.class);
    private double factor = 1.0;
    private LayerViewPanel panel;
    private Map<Object, AbstractRenderer> contentIDToRendererMap = new OrderedMap<Object, AbstractRenderer>();
    private OrderedMap<Object, AbstractRenderer.Factory> contentIDToLowRendererFactoryMap = new OrderedMap();
    private OrderedMap<Object, AbstractRenderer.Factory> contentIDToHighRendererFactoryMap = new OrderedMap();
    private ThreadQueue defaultRendererThreadQueue = new ThreadQueue(1);
    private ThreadQueue wmsRendererThreadQueue = new ThreadQueue(20);
    private boolean paintingEnabled = true;
    private Timer repaintTimer;
    private boolean includeAllRenders;

    public RenderingManager(LayerViewPanel panel) {
        this(panel, true, 500);
    }

    public RenderingManager(final LayerViewPanel panel, boolean includeAllRenderers, int repaintTime) {
        this.panel = panel;
        this.includeAllRenders = includeAllRenderers;
        this.repaintTimer = new Timer(repaintTime, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                for (AbstractRenderer renderer : RenderingManager.this.contentIDToRendererMap.values()) {
                    if (!renderer.isRendering()) continue;
                    RenderingManager.this.repaintPanel();
                    return;
                }
                RenderingManager.this.repaintTimer.stop();
                RenderingManager.this.repaintPanel();
                RenderingManager.this.fireRenderingFinished();
            }
        });
        this.repaintTimer.setCoalesce(true);
        if (includeAllRenderers) {
            this.putAboveLayerables("SELECTION_BACKGROUND", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new SelectionBackgroundRenderer(panel, RenderingManager.this.factor);
                }
            });
            this.putAboveLayerables("SELECTED_FEATURES", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new FeatureSelectionRenderer(panel, RenderingManager.this.factor);
                }
            });
            this.putAboveLayerables("SELECTED_LINESTRINGS", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new LineStringSelectionRenderer(panel, RenderingManager.this.factor);
                }
            });
            this.putAboveLayerables("SELECTED_PARTS", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new PartSelectionRenderer(panel, RenderingManager.this.factor);
                }
            });
            this.putAboveLayerables("SELECTED_SEGMENTS", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new SegmentSelectionRenderer(panel, RenderingManager.this.factor);
                }
            });
            this.putAboveLayerables("TEMPORAL_GEOMETRY", new AbstractRenderer.Factory(){

                @Override
                public AbstractRenderer create() {
                    return new TemporalGeometriesRenderer(panel, RenderingManager.this.factor);
                }
            });
        }
    }

    public void rebootSelectionRenderer() {
        this.setRenderer("SELECTED_FEATURES", new FeatureSelectionRenderer(this.panel, this.factor));
    }

    public void rebootSegmentsSelectionRenderer() {
        this.setRenderer("SELECTED_SEGMENTS", new SegmentSelectionRenderer(this.panel, this.factor));
    }

    public void rebootTemporalGeometryRenderer() {
        this.setRenderer("TEMPORAL_GEOMETRY", new TemporalGeometriesRenderer(this.panel, this.factor));
    }

    protected void fireRenderingFinished() {
        this.panel.fireRenderingFinished();
    }

    public void putBelowLayerables(Object contentID, AbstractRenderer.Factory factory) {
        this.contentIDToLowRendererFactoryMap.put(contentID, factory);
    }

    public void putAboveLayerables(Object contentID, AbstractRenderer.Factory factory) {
        this.contentIDToHighRendererFactoryMap.put(contentID, factory);
    }

    public void renderAll() {
        if (!this.paintingEnabled) {
            return;
        }
        this.defaultRendererThreadQueue.clear();
        this.wmsRendererThreadQueue.clear();
        for (Object contentID : this.contentIDs()) {
            Layerable layer;
            if (contentID instanceof Layerable && (!(layer = (Layerable)contentID).isEnabled() || !layer.isVisible())) continue;
            this.render(contentID);
        }
    }

    protected List<Object> contentIDs() {
        ArrayList<Object> contentIDs = new ArrayList<Object>();
        contentIDs.addAll(this.contentIDToLowRendererFactoryMap.keyList());
        Iterator<Layerable> i = this.panel.getLayerManager().reverseIterator(Layerable.class);
        while (i.hasNext()) {
            Layerable layerable = i.next();
            contentIDs.add(layerable);
        }
        Iterator<Layer> itHideLayers = this.panel.getLayerManager().getHideLayers().iterator();
        while (itHideLayers.hasNext()) {
            contentIDs.add(itHideLayers.next());
        }
        if (this.includeAllRenders) {
            contentIDs.addAll(this.contentIDToHighRendererFactoryMap.keyList());
        }
        return contentIDs;
    }

    public AbstractRenderer getRenderer(Object contentID) {
        return this.contentIDToRendererMap.get(contentID);
    }

    private synchronized void setRenderer(Object contentID, AbstractRenderer renderer) {
        this.contentIDToRendererMap.put(contentID, renderer);
    }

    public void removeRenderer(Object contentID) {
        if (this.contentIDToRendererMap.containsKey(contentID)) {
            this.contentIDToRendererMap.remove(contentID);
        }
    }

    public void render(Object contentID) {
        if (!this.paintingEnabled) {
            return;
        }
        this.render(contentID, true);
    }

    public void render(Object contentID, boolean clearImageCache) {
        if (!this.paintingEnabled) {
            return;
        }
        if (this.getRenderer(contentID) == null) {
            this.setRenderer(contentID, this.createRenderer(contentID));
        } else if (this.getRenderer(contentID).isRendering()) {
            if (this.getRenderer(contentID) != null) {
                this.getRenderer(contentID).cancel();
            }
            this.setRenderer(contentID, this.createRenderer(contentID));
        }
        if (clearImageCache) {
            this.getRenderer(contentID).clearImageCache();
        }
        this.getRenderer(contentID).setFactor(this.factor);
        Runnable runnable = this.getRenderer(contentID).createRunnable();
        if (runnable != null) {
            if (contentID instanceof WMSLayer) {
                this.wmsRendererThreadQueue.add(runnable);
            } else {
                this.defaultRendererThreadQueue.add(runnable);
            }
        }
        if (!this.repaintTimer.isRunning()) {
            this.repaintPanel();
            this.repaintTimer.start();
        }
    }

    public void repaintPanel() {
        if (!this.paintingEnabled) {
            return;
        }
        this.panel.superRepaint();
    }

    protected AbstractRenderer createRenderer(Object contentID) {
        if (contentID instanceof Layer) {
            return new LayerRenderer((Layer)contentID, this.panel, this.factor);
        }
        if (contentID instanceof WMSLayer) {
            return new WMSLayerRenderer((WMSLayer)contentID, this.panel, this.factor);
        }
        if (contentID instanceof TextBalloonLayer) {
            return new TextBalloonLayerRenderer((TextBalloonLayer)contentID, this.panel, this.factor);
        }
        if (this.contentIDToLowRendererFactoryMap.containsKey(contentID)) {
            return this.contentIDToLowRendererFactoryMap.get(contentID).create();
        }
        if (this.contentIDToHighRendererFactoryMap.containsKey(contentID)) {
            return this.contentIDToHighRendererFactoryMap.get(contentID).create();
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public void setPaintingEnabled(boolean paintingEnabled) {
        this.paintingEnabled = paintingEnabled;
    }

    public void copyTo(Graphics2D destination) {
        destination.setRenderingHints(RenderingHintsManager.getRenderingHints());
        for (Object contentID : this.contentIDs()) {
            AbstractRenderer renderer = this.getRenderer(contentID);
            if (renderer == null || renderer.getImage() == null) continue;
            renderer.getImage().copyTo(destination, null);
        }
    }

    public ThreadQueue getDefaultRendererThreadQueue() {
        return this.defaultRendererThreadQueue;
    }

    public void dispose() {
        this.repaintTimer.stop();
        this.defaultRendererThreadQueue.dispose();
        this.wmsRendererThreadQueue.dispose();
        this.contentIDToLowRendererFactoryMap.clear();
        this.contentIDToHighRendererFactoryMap.clear();
        this.contentIDToRendererMap.clear();
        this.panel = null;
        this.contentIDToLowRendererFactoryMap = null;
        this.contentIDToHighRendererFactoryMap = null;
        this.contentIDToRendererMap = null;
    }

    public void cancelAll() {
        this.repaintTimer.stop();
        for (AbstractRenderer element : this.contentIDToRendererMap.values()) {
            element.cancel();
        }
        this.contentIDToRendererMap.clear();
    }

    public boolean hasRenderers() {
        return this.contentIDToRendererMap.size() > 0;
    }

    public boolean isRendering() {
        return this.repaintTimer.isRunning();
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return this.factor;
    }
}

