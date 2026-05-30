/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IDatum
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.ViewPortData
 */
package org.saig.core.projections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IDatum;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;

public class DummyProjection
implements IProjection {
    public Point2D createPoint(double arg0, double arg1) {
        return null;
    }

    public void drawGrid(Graphics2D arg0, ViewPortData arg1) {
    }

    public Point2D fromGeo(Point2D arg0, Point2D arg1) {
        return null;
    }

    public String getAbrev() {
        return null;
    }

    public ICoordTrans getCT(IProjection arg0) {
        return null;
    }

    public IDatum getDatum() {
        return null;
    }

    public Rectangle2D getExtent(Rectangle2D arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {
        return null;
    }

    public String getFullCode() {
        return null;
    }

    public Color getGridColor() {
        return null;
    }

    public double getScale(double arg0, double arg1, double arg2, double arg3) {
        return 0.0;
    }

    public boolean isProjected() {
        return false;
    }

    public void setGridColor(Color arg0) {
    }

    public Point2D toGeo(Point2D arg0) {
        return null;
    }
}

