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
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;

public interface IShapeGeometry {
    public static final int BEST = 0;
    public static final int N = 1;
    public static final int NE = 2;
    public static final int E = 3;
    public static final int SE = 4;
    public static final int S = 5;
    public static final int SW = 6;
    public static final int W = 7;
    public static final int NW = 8;

    public Geometry toJTSGeometry();

    public int getGeometryType();

    public IShapeGeometry cloneGeometry();

    public boolean intersects(Rectangle2D var1);

    public boolean fastIntersects(double var1, double var3, double var5, double var7);

    public Rectangle2D getBounds2D();

    public SAIGGeneralPathIterator getGeneralPathXIterator();

    public IShape getPathShapeInt(AffineTransform var1);

    public void reProject(ICoordTrans var1);

    public IShape getShp();

    public void transform(AffineTransform var1);
}

