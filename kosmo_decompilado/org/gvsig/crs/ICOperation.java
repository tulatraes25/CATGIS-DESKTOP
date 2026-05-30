/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 */
package org.gvsig.crs;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.cresques.cts.ICoordTrans;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;

public interface ICOperation
extends ICoordTrans {
    public ICrs getSource();

    public ICrs getTarget();

    public Point2D operate(Point2D var1) throws CrsException;

    public Point2D convert(Point2D var1, Point2D var2);

    public Rectangle2D convert(Rectangle2D var1);
}

