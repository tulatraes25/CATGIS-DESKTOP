/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.RendererStrategy;

public abstract class FeatureCollectionRenderer
extends AbstractRenderer {
    private Viewport viewPort;
    private IRenderer renderer;

    public FeatureCollectionRenderer(Object contentID, LayerViewPanel panel, double factor) {
        super(contentID, panel, factor);
        this.viewPort = panel.getViewport();
    }

    protected void renderFeatures(ThreadSafeImage image, Envelope viewportEnvelope, Layer layer) throws Exception {
        if (!layer.isEnabled() || !layer.isVisible()) {
            return;
        }
        this.renderer = RendererStrategy.getRenderer(layer, this.factor);
        this.renderer.render(image.getImage(), viewportEnvelope, layer, this.viewPort.getAngle(), this.viewPort.getPanel().getScale(), layer.isOneQueryByRule(), this.viewPort.getPanel().getUserLengthUnit(), RenderingHintsManager.getRenderingHints());
        this.renderer = null;
    }

    @Override
    public void cancel() {
        if (this.renderer != null) {
            this.renderer.cancel();
        }
        super.cancel();
    }
}

