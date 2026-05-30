/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.LayerRendererWorker;
import org.saig.core.renderer3.RenderParams;
import org.saig.core.renderer3.RendererStrategy;
import org.saig.core.util.ScaleManager;

public class MultiThreadedMapDrawer {
    private static final Logger LOGGER = Logger.getLogger(MultiThreadedMapDrawer.class);

    public void drawMap(BufferedImage image, Envelope envelope, List<Layerable> layerables, Unit<Length> units, Map<Object, Object> renderingHints) {
        ArrayList<Layerable> visibles = new ArrayList<Layerable>();
        for (Layerable layerable : layerables) {
            if (!layerable.isVisible()) continue;
            visibles.add(layerable);
        }
        if (visibles.isEmpty()) {
            return;
        }
        try {
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch endSignal = new CountDownLatch(visibles.size());
            ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
            for (Layerable layerable : visibles) {
                if (!layerable.isVisible()) continue;
                IRenderer renderer = RendererStrategy.getRenderer(layerable, 1.0);
                BufferedImage layerImage = new BufferedImage(image.getWidth(), image.getHeight(), 2);
                RenderParams renderParams = new RenderParams(layerImage, envelope, layerable, 0.0, this.getScale(envelope, image.getWidth(), layerable.getProjection(), units), false, units, renderingHints);
                new Thread(new LayerRendererWorker(startSignal, endSignal, renderer, renderParams)).start();
                images.add(layerImage);
            }
            startSignal.countDown();
            endSignal.await();
            for (BufferedImage layerImage : images) {
                Graphics2D graphics = (Graphics2D)image.getGraphics();
                graphics.setRenderingHints(renderingHints);
                graphics.drawImage((Image)layerImage, 0, 0, null);
            }
        }
        catch (InterruptedException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public double getScale(Envelope bounds, int width, IProjection proj, Unit<Length> mapLengthUnit) {
        double newScale = ScaleManager.getInstance().generateScaleValue(bounds.getMaxX(), bounds.getMinX(), width, proj, mapLengthUnit);
        return newScale;
    }
}

