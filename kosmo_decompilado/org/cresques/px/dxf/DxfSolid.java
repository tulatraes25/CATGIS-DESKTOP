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
import org.cresques.px.Extent;
import org.cresques.px.dxf.AcadColor;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfLayer;

public class DxfSolid
extends DxfEntity {
    static final Color baseColor = new Color(69, 106, 121);
    Point2D[] pts;
    GeneralPath gp = null;
    boolean closed = true;
    private Color color = baseColor;

    public DxfSolid(IProjection proj, DxfLayer layer, Point2D[] pts) {
        super(proj, layer);
        Point2D aux = pts[2];
        pts[2] = pts[3];
        pts[3] = aux;
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
            g.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), 32));
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
        return "";
    }

    public Point2D[] getPts() {
        return this.pts;
    }
}

