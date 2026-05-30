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
import java.awt.Image;
import java.util.Map;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

public class RenderParams {
    private Image image;
    private Envelope envelope;
    private Layerable layer;
    private double angle;
    private double panelScale;
    private boolean strategy;
    private Unit<Length> units;
    private Map<Object, Object> renderingHints;

    public RenderParams() {
    }

    public RenderParams(Image image, Envelope envelope, Layerable layer, double angle, double panelScale, boolean strategy, Unit<Length> units, Map<Object, Object> rHints) {
        this.image = image;
        this.envelope = envelope;
        this.layer = layer;
        this.angle = angle;
        this.panelScale = panelScale;
        this.strategy = strategy;
        this.units = units;
        this.renderingHints = rHints;
    }

    public Image getImage() {
        return this.image;
    }

    public Envelope getEnvelope() {
        return this.envelope;
    }

    public Layerable getLayer() {
        return this.layer;
    }

    public double getAngle() {
        return this.angle;
    }

    public double getPanelScale() {
        return this.panelScale;
    }

    public boolean isStrategy() {
        return this.strategy;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public void setLayer(Layerable layer) {
        this.layer = layer;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setPanelScale(double panelScale) {
        this.panelScale = panelScale;
    }

    public void setStrategy(boolean strategy) {
        this.strategy = strategy;
    }

    public Unit<Length> getUnits() {
        return this.units;
    }

    public void setUnits(Unit<Length> units) {
        this.units = units;
    }

    public Map<Object, Object> getRenderingHints() {
        return this.renderingHints;
    }

    public void setRenderingHints(Map<Object, Object> renderingHints) {
        this.renderingHints = renderingHints;
    }
}

