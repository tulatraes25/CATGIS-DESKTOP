/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.ICoordTrans
 */
package es.kosmo.core.renderer.decorators;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.jump.util.MeasureUtils;

public abstract class AbstractDecorator
implements IDecorator {
    protected String name;
    protected Icon icon;
    protected Color color = Color.BLACK;
    protected Color selectionColor = Color.YELLOW;
    protected boolean fixedRotation;
    protected double rotation;
    protected Point2D offset = new Point2D.Double();
    protected double size;
    protected String unit;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public boolean isFixedRotation() {
        return this.fixedRotation;
    }

    @Override
    public void setFixedRotation(boolean fixed) {
        this.fixedRotation = fixed;
    }

    @Override
    public double getRotation() {
        return this.rotation;
    }

    @Override
    public void setRotation(double r) {
        this.rotation = r;
    }

    @Override
    public Point2D getOffset() {
        if (this.offset == null) {
            this.offset = new Point();
        }
        return this.offset;
    }

    @Override
    public void setOffset(Point2D offset) {
        this.offset = offset;
    }

    @Override
    public double getSize() {
        return this.size;
    }

    @Override
    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    @Override
    public Color getSelectionColor() {
        return this.selectionColor;
    }

    @Override
    public void paint(Feature f, Graphics2D g, IDecoratorPoint2DTransformer viewport, ICoordTrans ct, double pixelSize, Unit<Length> viewUnit) throws Exception {
        if (f != null && f.getGeometry() != null) {
            Geometry featGeom = null;
            if (ct != null) {
                IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(f.getGeometry());
                geom.reProject(ct);
                featGeom = ShapeGeometryConverter.java2d_to_jts(geom.getShp());
            } else {
                featGeom = f.getGeometry();
            }
            this.paintGeometry(featGeom, g, viewport, pixelSize, viewUnit);
        }
    }

    protected abstract void paintGeometry(Geometry var1, Graphics2D var2, IDecoratorPoint2DTransformer var3, double var4, Unit<Length> var6) throws Exception;

    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractDecorator)) {
            return false;
        }
        AbstractDecorator dec = (AbstractDecorator)obj;
        return this.getName().equals(dec.getName());
    }

    public String toString() {
        return this.getName();
    }

    protected Coordinate getNextNonEqual(Geometry geom) {
        Coordinate[] coords = geom.getCoordinates();
        Coordinate initialCoord = coords[0];
        int i = 1;
        while (i < coords.length) {
            Coordinate tmpCoord = coords[i];
            if (!tmpCoord.equals2D(initialCoord)) {
                return tmpCoord;
            }
            ++i;
        }
        return initialCoord;
    }

    protected Coordinate getPreviousNonEqual(Geometry geom) {
        Coordinate[] coords = geom.getCoordinates();
        Coordinate finalCoord = coords[coords.length - 1];
        int i = coords.length - 2;
        while (i >= 0) {
            Coordinate tmpCoord = coords[i];
            if (!tmpCoord.equals2D(finalCoord)) {
                return tmpCoord;
            }
            --i;
        }
        return finalCoord;
    }

    @Override
    public String getUnit() {
        return this.unit;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    protected double getTransformationFactor(Unit<Length> viewUnit) {
        if ("pixel".equals(this.unit)) {
            return 1.0;
        }
        Unit<Length> heightUnits = MeasureUtils.getLenghtUnitByName(this.unit);
        double unitsFactor = MeasureUtils.getLengthTransformFactor(heightUnits, viewUnit);
        return unitsFactor;
    }
}

