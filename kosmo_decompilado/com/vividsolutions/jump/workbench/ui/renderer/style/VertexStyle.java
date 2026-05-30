/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import javax.swing.Icon;
import org.saig.core.util.I18NUnsupportedOperationException;

public abstract class VertexStyle
implements Style {
    protected RectangularShape shape;
    protected int size = 4;
    private Color fillColor;
    private boolean enabled = false;

    protected VertexStyle(RectangularShape shape) {
        this.shape = shape;
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public void initialize(Layer layer) {
        this.fillColor = GUIUtil.alphaColor(layer.getBasicStyle().getLineColor(), layer.getBasicStyle().getAlpha());
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        Coordinate[] coordinates = f.getGeometry().getCoordinates();
        g.setColor(this.fillColor);
        int i = 0;
        while (i < coordinates.length) {
            this.paint(g, viewport.toViewPoint(new Point2D.Double(coordinates[i].x, coordinates[i].y)));
            ++i;
        }
    }

    public void paint(Graphics2D g, Point2D p) {
        this.setFrame(p);
        this.render(g);
    }

    private void setFrame(Point2D p) {
        this.shape.setFrame(p.getX() - (double)this.getSize() / 2.0, p.getY() - (double)this.getSize() / 2.0, this.getSize(), this.getSize());
    }

    protected void render(Graphics2D g) {
        g.fill(this.shape);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }
}

