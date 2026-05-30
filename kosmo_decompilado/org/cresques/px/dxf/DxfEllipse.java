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

public class DxfEllipse
extends DxfEntity {
    static final Color baseColor = new Color(69, 106, 121);
    GeneralPath gp = null;
    boolean closed = true;
    Point2D[] pts;
    private double minorAxisLength;
    private Point2D center;
    private double minorToMajorAxisRatio;
    private Color color = baseColor;

    public DxfEllipse(IProjection proj, DxfLayer layer, Point2D pt1, Point2D pt2, double minorAxisLength) {
        super(proj, layer);
        this.pts = new Point2D[2];
        this.pts[0] = pt1;
        this.pts[1] = pt2;
        this.minorAxisLength = minorAxisLength;
        this.extent = new Extent();
        int i = 0;
        while (i < this.pts.length) {
            this.extent.add(this.pts[i]);
            ++i;
        }
        this.center = new Point2D.Double((this.pts[0].getX() + this.pts[1].getX()) / 2.0, (this.pts[0].getY() + this.pts[1].getY()) / 2.0);
        double majorAxisLength = pt1.distance(pt2);
        this.minorToMajorAxisRatio = minorAxisLength / majorAxisLength;
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
        sb = new StringBuffer(DxfGroup.toString(0, "ELLIPSE"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(100, "AcDbEllipse"));
        sb.append(DxfGroup.toString(10, this.getCenter().getX(), 12));
        sb.append(DxfGroup.toString(20, this.getCenter().getY(), 12));
        sb.append(DxfGroup.toString(30, 0.0, 12));
        sb.append(DxfGroup.toString(11, this.pts[1].getX() - this.getCenter().getX(), 12));
        sb.append(DxfGroup.toString(21, this.pts[1].getY() - this.getCenter().getY(), 12));
        sb.append(DxfGroup.toString(31, 0.0, 12));
        sb.append(DxfGroup.toString(40, this.getMinorToMajorAxisRatio(), 12));
        sb.append(DxfGroup.toString(41, 0.0, 12));
        sb.append(DxfGroup.toString(42, Math.PI * 2, 12));
        return sb.toString();
    }

    public Point2D[] getPts() {
        return this.pts;
    }

    public double getMinorAxisLength() {
        return this.minorAxisLength;
    }

    public void setMinorAxisLength(double minorAxisLength) {
        this.minorAxisLength = minorAxisLength;
    }

    public void setPts(Point2D[] pts) {
        this.pts = pts;
    }

    public Point2D getCenter() {
        return this.center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public double getMinorToMajorAxisRatio() {
        return this.minorToMajorAxisRatio;
    }

    public void setMinorToMajorAxisRatio(double majorToMinorAxisRatio) {
        this.minorToMajorAxisRatio = majorToMinorAxisRatio;
    }
}

