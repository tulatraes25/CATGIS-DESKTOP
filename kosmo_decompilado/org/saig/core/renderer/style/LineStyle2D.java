/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.renderer.style;

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.Stroke;
import org.geotools.resources.Utilities;
import org.saig.core.renderer.style.Style2D;

public class LineStyle2D
extends Style2D {
    protected Paint contour;
    protected Stroke stroke;
    protected Composite contourComposite;
    private double offset;
    private Style2D graphicStroke;

    public Stroke getStroke() {
        return this.stroke;
    }

    public Stroke getScaledStroke(double factor) {
        if (this.stroke instanceof BasicStroke) {
            BasicStroke bstroke = (BasicStroke)this.stroke;
            return new BasicStroke((int)((double)bstroke.getLineWidth() * factor), bstroke.getEndCap(), bstroke.getLineJoin(), bstroke.getMiterLimit(), bstroke.getDashArray(), bstroke.getDashPhase());
        }
        return this.stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Paint getContour() {
        return this.contour;
    }

    public void setContour(Paint contour) {
        this.contour = contour;
    }

    public Composite getContourComposite() {
        return this.contourComposite;
    }

    public void setContourComposite(Composite contourComposite) {
        this.contourComposite = contourComposite;
    }

    public Style2D getGraphicStroke() {
        return this.graphicStroke;
    }

    public void setGraphicStroke(Style2D graphicStroke) {
        this.graphicStroke = graphicStroke;
    }

    public String toString() {
        return String.valueOf(Utilities.getShortClassName((Object)this)) + '[' + this.contour + ']';
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getOffset() {
        return this.offset;
    }
}

