/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.ViewPortData
 *  org.cresques.px.Extent
 *  org.cresques.px.dxf.AcadColor
 *  org.cresques.px.dxf.DxfLayer
 */
package org.cresques.px.dxf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.cresques.io.DxfGroup;
import org.cresques.px.Extent;
import org.cresques.px.dxf.AcadColor;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfLayer;

public class DxfArc
extends DxfEntity {
    static final Color baseColor = new Color(69, 106, 121);
    Point2D[] pts;
    GeneralPath gp = null;
    boolean closed = false;
    private Point2D centralPoint;
    private Point2D init;
    private Point2D end;
    private Point2D center;
    private double radius;
    private double initAngle;
    private double endAngle;
    private Color color = baseColor;

    public DxfArc(IProjection proj, DxfLayer layer, Point2D[] pts) {
        super(proj, layer);
        this.pts = pts;
        this.extent = new Extent();
        int i = 0;
        while (i < pts.length) {
            this.extent.add(pts[i]);
            ++i;
        }
    }

    public Color c() {
        return this.color;
    }

    public Color c(Color color) {
        this.color = color;
        return color;
    }

    @Override
    public void reProject(ICoordTrans rp) {
        Point2D[] savePts = this.pts;
        this.pts = new Point2D[savePts.length];
        this.extent = new Extent();
        Point2D ptDest = null;
        int i = 0;
        while (i < savePts.length) {
            ptDest = rp.getPDest().createPoint(0.0, 0.0);
            this.pts[i] = ptDest = rp.convert(savePts[i], ptDest);
            this.extent.add(ptDest);
            ++i;
        }
        this.setProjection(rp.getPDest());
    }

    public void draw(Graphics2D g, ViewPortData vp) {
        Color color = null;
        color = this.dxfColor == 256 ? this.layer.getColor() : AcadColor.getColor((int)this.dxfColor);
        this.newGP(vp);
        if (this.closed) {
            g.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), 128));
            g.fill(this.gp);
        }
        g.setColor(color);
        g.draw(this.gp);
    }

    private void newGP(ViewPortData vp) {
        this.gp = new GeneralPath();
        Point2D.Double pt0 = null;
        Object pt = null;
        Point2D pt1 = null;
        Point2D.Double ptTmp = new Point2D.Double(0.0, 0.0);
        int i = 0;
        while (i < this.pts.length) {
            pt1 = this.pts[i];
            vp.mat.transform(pt1, ptTmp);
            if (pt0 == null) {
                pt0 = ptTmp;
                this.gp.moveTo((float)ptTmp.getX(), (float)ptTmp.getY());
            } else {
                this.gp.lineTo((float)ptTmp.getX(), (float)ptTmp.getY());
            }
            ++i;
        }
        if (this.closed) {
            this.gp.closePath();
        }
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "ARC"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(100, "AcDbCircle"));
        sb.append(DxfGroup.toString(10, this.getCenter().getX(), 12));
        sb.append(DxfGroup.toString(20, this.getCenter().getY(), 12));
        if (!Double.isNaN(this.z)) {
            sb.append(DxfGroup.toString(30, this.z, 12));
        } else {
            sb.append(DxfGroup.toString(30, 0.0f, 12));
        }
        sb.append(DxfGroup.toString(40, this.getRadius(), 12));
        sb.append(DxfGroup.toString(100, "AcDbArc"));
        sb.append(DxfGroup.toString(50, this.getInitAngle(), 12));
        sb.append(DxfGroup.toString(51, this.getEndAngle(), 12));
        return sb.toString();
    }

    public Point2D[] getPts() {
        return this.pts;
    }

    public Point2D getCenter() {
        return this.center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public Point2D getEnd() {
        return this.end;
    }

    public void setEnd(Point2D end) {
        this.end = end;
    }

    public Point2D getInit() {
        return this.init;
    }

    public void setInit(Point2D init) {
        this.init = init;
    }

    public Point2D getCentralPoint() {
        return this.centralPoint;
    }

    public void setCentralPoint(Point2D centralPoint) {
        this.centralPoint = centralPoint;
    }

    public double getEndAngle() {
        return this.endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    public double getInitAngle() {
        return this.initAngle;
    }

    public void setInitAngle(double initAngle) {
        this.initAngle = initAngle;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}

