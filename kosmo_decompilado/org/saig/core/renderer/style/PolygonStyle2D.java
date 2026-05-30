/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.renderer.style;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import org.geotools.resources.Utilities;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.Style2D;

public class PolygonStyle2D
extends LineStyle2D {
    protected Paint fill;
    protected Composite fillComposite;
    protected String wktFillName;
    protected Color wktFillColor;
    protected Style2D graphicFill;

    public Paint getFill() {
        return this.fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public Composite getFillComposite() {
        return this.fillComposite;
    }

    public void setFillComposite(Composite fillComposite) {
        this.fillComposite = fillComposite;
    }

    @Override
    public String toString() {
        return String.valueOf(Utilities.getShortClassName((Object)this)) + '[' + this.fill + ']';
    }

    public String getWktFillName() {
        return this.wktFillName;
    }

    public void setWktFillName(String wktFillName) {
        this.wktFillName = wktFillName;
    }

    public Color getWktFillColor() {
        return this.wktFillColor;
    }

    public void setWktFillColor(Color wktFillColor) {
        this.wktFillColor = wktFillColor;
    }

    public Style2D getGraphicFill() {
        return this.graphicFill;
    }

    public void setGraphicFill(Style2D graphicFill) {
        this.graphicFill = graphicFill;
    }
}

