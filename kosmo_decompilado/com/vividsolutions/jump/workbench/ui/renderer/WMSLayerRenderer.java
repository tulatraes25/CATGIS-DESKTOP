/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;

public class WMSLayerRenderer
extends AbstractRenderer {
    public WMSLayerRenderer(WMSLayer layer, LayerViewPanel panel, double factor) {
        super(layer, panel, factor);
    }

    @Override
    public ThreadSafeImage getImage() {
        if (!this.getLayer().isVisible()) {
            return new ThreadSafeImage(this.panel);
        }
        return super.getImage();
    }

    @Override
    public Runnable createRunnable() {
        if (!this.getLayer().isVisible()) {
            return null;
        }
        return super.createRunnable();
    }

    private WMSLayer getLayer() {
        return (WMSLayer)this.getContentID();
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        if (!this.getLayer().isVisible() || !this.getLayer().isEnabled()) {
            return;
        }
        Envelope viewportEnvelope = this.panel.getViewport().getEnvelopeInModelCoordinates();
        Envelope layerEnvelope = this.getLayer().getFullEnvelope();
        if (layerEnvelope != null && !layerEnvelope.isNull() && !this.getLayer().getFullEnvelope().intersects(viewportEnvelope)) {
            return;
        }
        final Image sourceImage = this.getLayer().createImage(this.panel);
        if (this.cancelled) {
            return;
        }
        image.draw(new ThreadSafeImage.Drawer(){

            @Override
            public void draw(Graphics2D g) throws Exception {
                g.setComposite(AlphaComposite.getInstance(3, (float)WMSLayerRenderer.this.getLayer().getAlpha() / 255.0f));
                g.drawImage(sourceImage, 0, 0, null);
            }
        });
    }
}

