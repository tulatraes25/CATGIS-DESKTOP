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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.cresques.io.DxfGroup;
import org.cresques.px.Extent;
import org.cresques.px.dxf.AcadColor;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfLayer;

public class DxfText
extends DxfEntity {
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_ALIGNED = 3;
    public static final int ALIGN_MIDDLE = 4;
    public static final int ALIGN_FIT = 5;
    private String text = null;
    Point2D[] pts;
    private Point2D pt;
    private double rot = 0.0;
    private double h = 1.0;
    int align = 0;
    private boolean twoPointsFlag;

    public DxfText(IProjection proj, DxfLayer layer, String txt) {
        super(proj, layer);
        this.extent = new Extent();
        this.text = txt;
        this.pts = new Point2D[2];
        this.pt = new Point2D.Double();
        this.twoPointsFlag = false;
    }

    public void setPt(Point2D pt) {
        this.pt = pt;
    }

    public Point2D getPt() {
        return this.pt;
    }

    public void setTwoPointsFlag(boolean f) {
        this.twoPointsFlag = f;
    }

    public boolean getTwoPointsFlag() {
        return this.twoPointsFlag;
    }

    public void setPt1(Point2D pt) {
        this.pts[0] = pt;
        this.extent.add(pt);
    }

    public Point2D getPt1() {
        return this.pts[0];
    }

    public void setPt2(Point2D pt) {
        this.pts[1] = pt;
        this.extent.add(pt);
    }

    public Point2D getPt2() {
        return this.pts[1];
    }

    public void setHeight(double h) {
        this.h = h;
    }

    public double getHeight() {
        return this.h;
    }

    public void setRotation(double r) {
        this.rot = r;
    }

    public double getRotation() {
        return this.rot;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void reProject(ICoordTrans rp) {
        Point2D[] savePts = this.pts;
        this.pts = new Point2D[2];
        this.extent = new Extent();
        Point2D ptDest = null;
        int i = 0;
        while (i < savePts.length) {
            ptDest = rp.getPDest().createPoint(0.0, 0.0);
            if (savePts[i] == null) {
                ptDest = null;
            } else {
                ptDest = rp.convert(savePts[i], ptDest);
                this.extent.add(ptDest);
            }
            this.pts[i] = ptDest;
            ++i;
        }
        Point2D ptOrig = rp.getPOrig().createPoint(savePts[0].getX(), savePts[0].getY() + this.h);
        ptDest = rp.getPDest().createPoint(0.0, 0.0);
        ptDest = rp.convert(ptOrig, ptDest);
        this.h = ptDest.getY() - this.pts[0].getY();
        this.setProjection(rp.getPDest());
    }

    public void draw(Graphics2D g, ViewPortData vp) {
        if (this.dxfColor == 256) {
            g.setColor(this.layer.getColor());
        } else {
            g.setColor(AcadColor.getColor((int)this.dxfColor));
        }
        Font fntSave = g.getFont();
        Point2D.Double ptT0 = new Point2D.Double(this.pts[0].getX(), this.pts[0].getY());
        Point2D.Double ptT1 = new Point2D.Double(this.pts[0].getX() + this.h, this.pts[0].getY() + this.h);
        vp.mat.transform(ptT0, ptT0);
        vp.mat.transform(ptT1, ptT1);
        Font fnt = new Font(fntSave.getName(), fntSave.getStyle(), (int)(((Point2D)ptT1).getX() - ((Point2D)ptT0).getX()));
        g.setFont(fnt);
        ((Point2D)ptT0).setLocation(this.pts[0].getX(), this.pts[0].getY());
        vp.mat.transform(ptT0, ptT0);
        g.drawString(this.text, (int)((Point2D)ptT0).getX(), (int)((Point2D)ptT0).getY());
        g.setFont(fntSave);
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "TEXT"));
        sb.append(DxfGroup.toString(1, this.getText()));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(10, this.pt.getX(), 12));
        sb.append(DxfGroup.toString(20, this.pt.getY(), 12));
        if (!Double.isNaN(this.z)) {
            sb.append(DxfGroup.toString(30, this.z, 12));
        } else {
            sb.append(DxfGroup.toString(30, 0.0f, 12));
        }
        sb.append(DxfGroup.toString(40, this.getHeight(), 12));
        sb.append(DxfGroup.toString(50, this.getRotation(), 12));
        return sb.toString();
    }
}

