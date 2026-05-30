/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.BalloonDrawer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.Graphics2D;

public class TextBalloonLayerRenderer
extends AbstractRenderer {
    public TextBalloonLayerRenderer(TextBalloonLayer layer, LayerViewPanel panel, double factor) {
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

    private TextBalloonLayer getLayer() {
        return (TextBalloonLayer)this.getContentID();
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        if (!this.getLayer().isVisible() || !this.getLayer().isEnabled()) {
            return;
        }
        if (this.cancelled) {
            return;
        }
        image.draw(new ThreadSafeImage.Drawer(){

            @Override
            public void draw(Graphics2D g) throws Exception {
                BalloonDrawer bd = BalloonDrawer.getInstance();
                bd.draw(g, TextBalloonLayerRenderer.this.getLayer(), TextBalloonLayerRenderer.this.panel.getViewport().getPanel().getSize(), TextBalloonLayerRenderer.this.panel.getViewport().getEnvelopeInModelCoordinates());
            }
        });
    }
}

