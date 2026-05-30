/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Graphics2D;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.renderer3.G2dRendererStrategy;
import org.saig.core.renderer3.IG2dRenderer;
import org.saig.core.renderer3.SingleThreadedMapDrawer;

public class G2dSingleThreadedMapDrawer
extends SingleThreadedMapDrawer {
    public void drawMap(Graphics2D g, int width, int height, Envelope envelope, List<Layerable> layerables, Unit<Length> units) {
        for (Layerable layerable : layerables) {
            if (!layerable.isVisible()) continue;
            IG2dRenderer renderer = G2dRendererStrategy.getRenderer(layerable, 1.0);
            double scale = this.getScale(envelope, width, layerable.getProjection(), units);
            renderer.render(g, width, height, envelope, layerable, 0.0, scale, false, units);
        }
    }
}

