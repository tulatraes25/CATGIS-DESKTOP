/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import es.kosmo.core.renderer.decorators.AbstractDecorator;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public abstract class CircleMarkerDecorator
extends AbstractDecorator {
    protected boolean filled;
    protected double borderSize;

    public void draw(Graphics2D g, AffineTransform affineTransform, ShapePoint2D shp, double pixelSize, Unit<Length> viewUnit) {
        ShapePoint2D p = shp;
        double sizeInPixel = this.getSize() * this.getTransformationFactor(viewUnit);
        if (!"pixel".equals(this.unit)) {
            sizeInPixel *= pixelSize;
        }
        g.setColor(this.getColor());
        g.setStroke(new BasicStroke((float)this.borderSize));
        Ellipse2D.Double circle = new Ellipse2D.Double(p.getX() - sizeInPixel / 2.0, p.getY() - sizeInPixel / 2.0, sizeInPixel, sizeInPixel);
        g.draw(circle);
        if (this.filled) {
            g.fill(circle);
        }
    }

    @Override
    public boolean isCompatible(int geomType) {
        return geomType != 1 && geomType != 8;
    }

    public boolean isFilled() {
        return this.filled;
    }

    public void setFilled(boolean fill) {
        this.filled = fill;
    }

    public double getBorderSize() {
        return this.borderSize;
    }

    public void setBorderSize(double borderSize) {
        this.borderSize = borderSize;
    }
}

