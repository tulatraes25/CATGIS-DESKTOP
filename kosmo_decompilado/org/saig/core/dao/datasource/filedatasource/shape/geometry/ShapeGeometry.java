/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;

public class ShapeGeometry
implements IShapeGeometry {
    protected IShape shp;

    ShapeGeometry(IShape shp) {
        this.shp = shp;
    }

    @Override
    public void transform(AffineTransform at) {
        if (this.shp instanceof ShapePolyline2D) {
            ((ShapePolyline2D)this.shp).transform(at);
        }
        if (this.shp instanceof ShapePoint2D) {
            ((ShapePoint2D)this.shp).transform(at);
        }
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.shp.intersects(r);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.shp.getBounds2D();
    }

    @Override
    public Geometry toJTSGeometry() {
        return ShapeGeometryConverter.java2d_to_jts(this.shp);
    }

    @Override
    public int getGeometryType() {
        return this.shp.getShapeType();
    }

    @Override
    public IShapeGeometry cloneGeometry() {
        return new ShapeGeometry(this.shp.cloneShape());
    }

    @Override
    public SAIGGeneralPathIterator getGeneralPathXIterator() {
        return (SAIGGeneralPathIterator)this.shp.getPathIterator(null);
    }

    @Override
    public boolean fastIntersects(double x, double y, double w, double h) {
        return this.shp.intersects(x, y, w, h);
    }

    @Override
    public IShape getPathShapeInt(AffineTransform aft) {
        this.shp = ShapeGeometryConverter.transformToInts(this, aft);
        return this.shp;
    }

    @Override
    public IShape getShp() {
        return this.shp;
    }

    public String toString() {
        return this.shp.toString();
    }

    @Override
    public void reProject(ICoordTrans ct) {
        this.shp.reProject(ct);
    }
}

