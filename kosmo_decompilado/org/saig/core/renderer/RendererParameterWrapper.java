/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 */
package org.saig.core.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.Composite;
import java.awt.RenderingHints;

public class RendererParameterWrapper {
    private Envelope originalEnvelope;
    private Envelope envelopeConRecortes;
    private Geometry viewGeom;
    private Envelope viewEnvelope;
    private double scaleFactor;
    private int width;
    private int height;
    private int x;
    private int y;
    private RenderingHints renderHints;
    private Composite compNormal;
    protected int alpha;

    public RendererParameterWrapper(Envelope view, int x, int y, int width, int height) {
        this.originalEnvelope = view;
        this.height = height;
        this.width = width;
        this.x = x;
        this.y = y;
        if (view.getWidth() > view.getHeight()) {
            this.scaleFactor = (double)width / view.getWidth();
            double fx = (double)height / this.scaleFactor;
            double maxy = (view.getMaxY() + view.getMinY() + fx) / 2.0;
            double miny = (view.getMaxY() + view.getMinY() - fx) / 2.0;
            this.viewEnvelope = new Envelope(view.getMaxX(), view.getMinX(), maxy, miny);
        } else {
            this.scaleFactor = (double)height / view.getHeight();
            double fy = (double)width / this.scaleFactor;
            double maxx = (view.getMaxX() + view.getMinX() + fy) / 2.0;
            double minx = (view.getMaxX() + view.getMinX() - fy) / 2.0;
            this.viewEnvelope = new Envelope(maxx, minx, view.getMaxY(), view.getMinY());
        }
        GeometryFactory factory = new GeometryFactory();
        try {
            LinearRing lring = factory.createLinearRing(new Coordinate[]{new Coordinate(this.viewEnvelope.getMinX(), this.viewEnvelope.getMaxY()), new Coordinate(this.viewEnvelope.getMaxX(), this.viewEnvelope.getMaxY()), new Coordinate(this.viewEnvelope.getMaxX(), this.viewEnvelope.getMinY()), new Coordinate(this.viewEnvelope.getMinX(), this.viewEnvelope.getMinY()), new Coordinate(this.viewEnvelope.getMinX(), this.viewEnvelope.getMaxY())});
            this.viewGeom = factory.createPolygon(lring, null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        this.renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        this.renderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        this.renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        this.renderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        this.renderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }

    public Geometry getViewGeom() {
        return this.viewGeom;
    }

    public Envelope getViewEnvelope() {
        return this.viewEnvelope;
    }

    public double getScaleFactor() {
        return this.scaleFactor;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alphaValue) {
        this.alpha = alphaValue;
    }

    public RenderingHints getRenderHints() {
        return this.renderHints;
    }

    public Composite getCompNormal() {
        return this.compNormal;
    }

    public void setCompNormal(Composite compNormal) {
        this.compNormal = compNormal;
    }

    public int[] applyTransform(double x_, double y_) {
        double x = x_ - this.getViewEnvelope().getMinX();
        double y = y_ - this.getViewEnvelope().getMinY();
        return new int[]{(int)(x *= this.getScaleFactor()), (int)(y *= this.getScaleFactor())};
    }

    public Envelope getOriginalEnvelope() {
        return this.originalEnvelope;
    }

    public Envelope getEnvelopeConRecortes() {
        return this.envelopeConRecortes;
    }

    public void setEnvelopeConRecortes(Envelope envelopeConRecortes) {
        this.envelopeConRecortes = envelopeConRecortes;
    }

    public int hashCode() {
        return this.getEnvelopeConRecortes().hashCode() * this.getWidth() * this.getHeight();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RendererParameterWrapper)) {
            return false;
        }
        RendererParameterWrapper rp2 = (RendererParameterWrapper)obj;
        return this.getEnvelopeConRecortes().equals((Object)rp2.getEnvelopeConRecortes()) && this.getWidth() == rp2.getWidth() && this.getHeight() == rp2.getHeight();
    }
}

