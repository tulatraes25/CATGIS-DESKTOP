/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.GeneralPathX
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import es.kosmo.core.renderer.decorators.AbstractDecorator;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public abstract class ArrowMarkerDecorator
extends AbstractDecorator {
    protected double sharpeness;

    protected abstract GeneralPathX getGeneralPathX(double var1, Unit<Length> var3);

    protected abstract boolean isFill();

    public void draw(Graphics2D g, AffineTransform affineTransform, ShapePoint2D shp, double pixelSize, Unit<Length> viewUnit) {
        ShapePoint2D p = shp;
        double theta = this.getRotation();
        g.setColor(this.getColor());
        g.setStroke(new BasicStroke());
        if (p == null) {
            return;
        }
        GeneralPathX gp = this.getGeneralPathX(pixelSize, viewUnit);
        try {
            g.translate(p.getX(), p.getY());
        }
        catch (NullPointerException npEx) {
            return;
        }
        g.rotate(theta);
        if (this.isFill()) {
            g.fill((Shape)gp);
        } else {
            g.draw((Shape)gp);
        }
        g.rotate(-theta);
        g.translate(-p.getX(), -p.getY());
    }

    public double getSharpness() {
        return this.sharpeness;
    }

    public void setSharpness(double sharpeness) {
        this.sharpeness = sharpeness;
    }

    @Override
    public boolean isCompatible(int geomType) {
        return geomType != 1 && geomType != 8;
    }
}

