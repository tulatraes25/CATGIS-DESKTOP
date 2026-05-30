/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Graphics2D;
import org.saig.core.dao.coverage.ImageDataAccesor;
import org.saig.core.renderer.RendererParameterWrapper;

public abstract class Coverage {
    protected ImageDataAccesor dataAccesor;

    public abstract Envelope getEnvelope();

    public abstract void getImage(Graphics2D var1, RendererParameterWrapper var2);

    public ImageDataAccesor getDataAccesor() {
        return this.dataAccesor;
    }

    public void close() {
        this.dataAccesor.close();
        this.dataAccesor = null;
    }
}

