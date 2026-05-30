/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Graphics2D;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.ImageDataAccesor;
import org.saig.core.renderer.RendererParameterWrapper;

public class GridCoverage
extends Coverage {
    public GridCoverage() {
    }

    public GridCoverage(ImageDataAccesor dataAccesor) {
        this.dataAccesor = dataAccesor;
    }

    @Override
    public Envelope getEnvelope() {
        if (this.dataAccesor != null) {
            return this.dataAccesor.getEnvelope();
        }
        return null;
    }

    @Override
    public void getImage(Graphics2D g2d, RendererParameterWrapper renderPS) {
        this.dataAccesor.getImagen(g2d, renderPS);
    }
}

