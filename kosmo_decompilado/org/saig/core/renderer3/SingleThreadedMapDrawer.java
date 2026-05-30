/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.IProjection
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.cresques.cts.IProjection;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.RendererStrategy;
import org.saig.core.util.ScaleManager;

public class SingleThreadedMapDrawer {
    public void drawMap(BufferedImage image, Envelope envelope, List<Layerable> layerables, Unit<Length> units, Map<Object, Object> renderingHints) {
        for (Layerable layerable : layerables) {
            if (!layerable.isVisible()) continue;
            IRenderer renderer = RendererStrategy.getRenderer(layerable, 1.0);
            renderer.render(image, envelope, layerable, 0.0, this.getScale(envelope, image.getWidth(), layerable.getProjection(), units), false, units, renderingHints);
        }
    }

    public double getScale(Envelope bounds, int width, IProjection proj, Unit<Length> mapLengthUnit) {
        double newScale = ScaleManager.getInstance().generateScaleValue(bounds.getMaxX(), bounds.getMinX(), width, proj, mapLengthUnit);
        return newScale;
    }
}

