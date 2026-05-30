/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.proj.CrsProj;
import org.gvsig.crs.proj.JNIBaseCrs;
import org.gvsig.crs.proj.OperationCrsException;

public class COperation
implements ICoordTrans {
    private static final Logger LOGGER = Logger.getLogger(COperation.class);
    private ICrs sourceCrs;
    private ICrs targetCrs;
    private CrsProj paramsCrsProj = null;
    private boolean paramsInTarget;
    private String sourceParams = null;
    private String targetParams = null;
    private CrsProj source = null;
    private CrsProj target = null;

    public COperation(ICrs from, ICrs to) throws CrsException {
        this.sourceCrs = from;
        this.targetCrs = to;
        this.source = this.sourceCrs.getCrsProj();
        this.target = this.targetCrs.getCrsProj();
    }

    public COperation(ICrs sourceCrs, ICrs targetCrs, String sourceParams, String targetParams) throws CrsException {
        this.sourceCrs = sourceCrs;
        this.targetCrs = targetCrs;
        this.sourceParams = sourceParams;
        this.targetParams = targetParams;
        this.source = sourceParams != null ? new CrsProj(String.valueOf(sourceCrs.getProj4String()) + sourceParams) : sourceCrs.getCrsProj();
        this.target = targetParams != null ? new CrsProj(String.valueOf(targetCrs.getProj4String()) + targetParams) : targetCrs.getCrsProj();
    }

    private Point2D operate(Point2D pt) throws CrsException, OperationCrsException {
        double[] x = new double[]{pt.getX()};
        double[] y = new double[]{pt.getY()};
        double[] z = new double[]{0.0};
        int errno = 0;
        errno = JNIBaseCrs.operate(x, y, z, this.source, this.target);
        if (errno != -38) {
            return new Point2D.Double(x[0], y[0]);
        }
        x[0] = pt.getX();
        y[0] = pt.getY();
        z[0] = 0.0;
        JNIBaseCrs.operate(x, y, z, this.sourceCrs.getCrsProj(), this.targetCrs.getCrsProj());
        return new Point2D.Double(x[0], y[0]);
    }

    public IProjection getPOrig() {
        return this.sourceCrs;
    }

    public IProjection getPDest() {
        return this.targetCrs;
    }

    private double[] operate(double[] ptOrig) throws OperationCrsException, CrsException {
        CrsProj target;
        double[] x = new double[]{ptOrig[0]};
        double[] y = new double[]{ptOrig[1]};
        double[] z = new double[]{ptOrig[2]};
        int errno = 0;
        CrsProj source = this.sourceParams != null ? new CrsProj(String.valueOf(this.sourceCrs.getProj4String()) + this.sourceParams) : this.sourceCrs.getCrsProj();
        errno = JNIBaseCrs.operate(x, y, z, source, target = this.targetParams != null ? new CrsProj(String.valueOf(this.targetCrs.getProj4String()) + this.targetParams) : this.targetCrs.getCrsProj());
        if (errno != -38) {
            double[] ptDest = new double[]{x[0], y[0], z[0]};
            return ptDest;
        }
        x[0] = ptOrig[0];
        y[0] = ptOrig[1];
        z[0] = ptOrig[2];
        JNIBaseCrs.operate(x, y, z, this.sourceCrs.getCrsProj(), this.targetCrs.getCrsProj());
        double[] ptDest = new double[]{x[0], y[0], z[0]};
        return ptDest;
    }

    public Point2D convert(Point2D ptOrig, Point2D ptDest) {
        try {
            ptDest = this.operate(ptOrig);
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new IllegalStateException(e.getMessage());
        }
        catch (OperationCrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new IllegalStateException(e.getMessage());
        }
        return ptDest;
    }

    public Rectangle2D convert(Rectangle2D rect) {
        Point2D pt1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
        Point2D pt2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
        try {
            pt1 = this.operate(pt1);
            pt2 = this.operate(pt2);
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new IllegalStateException(e.getMessage());
        }
        catch (OperationCrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new IllegalStateException(e.getMessage());
        }
        rect = new Rectangle2D.Double();
        rect.setFrameFromDiagonal(pt1, pt2);
        return rect;
    }

    public ICoordTrans getInverted() {
        try {
            COperation operation = new COperation(this.targetCrs, this.sourceCrs, this.targetParams, this.sourceParams);
            return operation;
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }
}

