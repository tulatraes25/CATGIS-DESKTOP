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
import es.kosmo.core.renderer.decorators.impl.ArrowMarkerDecorator;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public abstract class FeathersMarkerDecorator
extends ArrowMarkerDecorator {
    public static final int FEATHERS_NUMBER = 3;
    public static final int FEATHERS_SPACING = 7;
    protected boolean reverse = false;

    @Override
    protected boolean isFill() {
        return false;
    }

    @Override
    protected GeneralPathX getGeneralPathX(double pixelSize, Unit<Length> viewUnit) {
        return null;
    }

    @Override
    public void draw(Graphics2D g, AffineTransform affineTransform, ShapePoint2D shp, double pixelSize, Unit<Length> viewUnit) {
        ShapePoint2D p = shp;
        double radian_half_sharpeness = Math.toRadians(this.getSharpness() * 0.5);
        double sizeInPixel = this.getSize() * this.getTransformationFactor(viewUnit);
        if (!"pixel".equals(this.unit)) {
            sizeInPixel *= pixelSize;
        }
        double halfHeight = sizeInPixel * Math.tan(radian_half_sharpeness);
        double theta = this.getRotation();
        if (this.reverse) {
            theta += Math.PI;
        }
        g.setColor(this.getColor());
        g.setStroke(new BasicStroke());
        if (p == null) {
            return;
        }
        int i = 0;
        while (i < 3) {
            double incX = (7.0 + sizeInPixel / 4.0) * (double)i;
            GeneralPathX gp = new GeneralPathX();
            gp.moveTo(sizeInPixel + incX, halfHeight);
            gp.lineTo(0.0 + incX, 0.0);
            gp.lineTo(sizeInPixel + incX, -halfHeight);
            try {
                g.translate(p.getX(), p.getY());
            }
            catch (NullPointerException npEx) {
                return;
            }
            g.rotate(theta);
            g.draw((Shape)gp);
            g.rotate(-theta);
            g.translate(-p.getX(), -p.getY());
            ++i;
        }
    }
}

