/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Map;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import org.saig.core.renderer3.IG2dRenderer;
import org.saig.core.renderer3.IRenderer;

public class WMSLayerRenderer
implements IRenderer,
IG2dRenderer {
    private static Logger LOGGER = Logger.getLogger(WMSLayerRenderer.class);

    @Override
    public void render(Graphics2D g2d, int width, int height, Envelope envelope, Layerable layer, double angle, double panelScale, boolean strategy, Unit<Length> mapUnits) {
        Image image = null;
        WMSLayer wmslayer = (WMSLayer)layer;
        try {
            image = wmslayer.createMapRequest(envelope, width, height).getImage();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        g2d.drawImage(image, 0, 0, null);
    }

    @Override
    public void render(Image imageToCopy, Envelope envelope, Layerable layerable, double angle, double panelScale, boolean strategy, Unit<Length> mapUnits, Map<Object, Object> renderingHints) {
        Graphics2D g2d = (Graphics2D)imageToCopy.getGraphics();
        if (renderingHints != null) {
            g2d.setRenderingHints(renderingHints);
        }
        this.render(g2d, imageToCopy.getWidth(null), imageToCopy.getHeight(null), envelope, layerable, angle, panelScale, strategy, mapUnits);
    }

    @Override
    public void cancel() {
    }
}

