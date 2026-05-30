/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class LayerRenderer
extends FeatureCollectionRenderer {
    private static final Logger LOGGER = Logger.getLogger((String)"com.vividsolutions.jump.workbench.ui.renderer.LayerRenderer");
    private Layer layer;

    public LayerRenderer(Layer layer, LayerViewPanel panel, double factor) {
        super(layer, panel, factor);
        this.layer = layer;
    }

    @Override
    public ThreadSafeImage getImage() {
        if (!this.layer.isVisible()) {
            return new ThreadSafeImage(this.panel);
        }
        return super.getImage();
    }

    @Override
    public Runnable createRunnable() {
        int size = 0;
        try {
            size = this.layer.getUltimateFeatureCollectionWrapper().size();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (this.layer == null || !this.layer.isVisible() || !this.layer.isEnabled() || this.layer.getUltimateFeatureCollectionWrapper() == null || size == 0) {
            return null;
        }
        return super.createRunnable();
    }

    @Override
    public void renderHook(ThreadSafeImage image) throws Exception {
        Envelope viewportEnvelope = this.panel.getViewport().getEnvelopeInModelCoordinates();
        long t1 = System.currentTimeMillis();
        this.renderFeatures(image, viewportEnvelope, this.layer);
        LOGGER.debug((Object)(String.valueOf(I18N.getString("workbench.ui.renderer.LayerRenderer.time-in-rendering-layer")) + " " + this.layer.getName() + " " + (System.currentTimeMillis() - t1)));
    }

    public void renderHook(ThreadSafeImage image, Envelope viewportEnvelope) throws Exception {
        long t1 = System.currentTimeMillis();
        this.renderFeatures(image, viewportEnvelope, this.layer);
        LOGGER.debug((Object)(String.valueOf(I18N.getString("workbench.ui.renderer.LayerRenderer.time-in-rendering-layer")) + " " + this.layer.getName() + " " + (System.currentTimeMillis() - t1)));
    }
}

