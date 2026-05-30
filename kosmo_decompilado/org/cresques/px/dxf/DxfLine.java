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

public class DxfLine
extends DxfEntity {
    static final Color baseColor = new Color(255, 106, 121);
    Point2D[] pts;
    GeneralPath gp = null;
    private Color color = baseColor;

    public DxfLine(IProjection proj, DxfLayer layer, Point2D p1, Point2D p2) {
        super(proj, layer);
        this.extent = new Extent(p1, p2);
        this.pts = new Point2D[2];
        this.pts[0] = p1;
        this.pts[1] = p2;
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
        this.pts = new Point2D[2];
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
        if (this.dxfColor == 256) {
            g.setColor(this.layer.getColor());
        } else {
            g.setColor(AcadColor.getColor((int)this.dxfColor));
        }
        this.newGP(vp);
        g.draw(this.gp);
    }

    private void newGP(ViewPortData vp) {
        Point2D.Double pt0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double pt1 = new Point2D.Double(0.0, 0.0);
        vp.mat.transform(this.pts[0], pt0);
        vp.mat.transform(this.pts[1], pt1);
        this.gp = new GeneralPath();
        this.gp.moveTo((float)pt0.getX(), (float)pt0.getY());
        this.gp.lineTo((float)pt1.getX(), (float)pt1.getY());
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "LINE"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(100, "AcDbLine"));
        sb.append(DxfGroup.toString(10, this.pts[0].getX(), 12));
        sb.append(DxfGroup.toString(20, this.pts[0].getY(), 12));
        sb.append(DxfGroup.toString(11, this.pts[1].getX(), 12));
        sb.append(DxfGroup.toString(21, this.pts[1].getY(), 12));
        return sb.toString();
    }

    public Point2D[] getPts() {
        return this.pts;
    }
}

