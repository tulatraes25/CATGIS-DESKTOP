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

public class DxfPoint
extends DxfEntity {
    private Point2D pt;

    public DxfPoint(IProjection proj, DxfLayer layer) {
        super(proj, layer);
        this.extent = new Extent();
        this.pt = new Point2D.Double();
    }

    public void setPt(Point2D pt) {
        this.pt = pt;
        this.extent.add(pt);
    }

    @Override
    public void reProject(ICoordTrans rp) {
        Point2D savePt = this.pt;
        this.pt = new Point2D.Double();
        this.extent = new Extent();
        Point2D ptDest = rp.getPDest().createPoint(0.0, 0.0);
        if (savePt == null) {
            ptDest = null;
        } else {
            ptDest = rp.convert(savePt, ptDest);
            this.extent.add(ptDest);
        }
        this.pt = ptDest;
        this.setProjection(rp.getPDest());
    }

    public void draw(Graphics2D g, ViewPortData vp) {
        if (this.dxfColor == 256) {
            g.setColor(this.layer.getColor());
        } else {
            g.setColor(AcadColor.getColor((int)this.dxfColor));
        }
        Point2D.Double ptT = new Point2D.Double(0.0, 0.0);
        vp.mat.transform(ptT, ptT);
        ((Point2D)ptT).setLocation(this.pt.getX(), this.pt.getY());
        vp.mat.transform(ptT, ptT);
        g.drawLine((int)((Point2D)ptT).getX(), (int)((Point2D)ptT).getY(), (int)((Point2D)ptT).getX(), (int)((Point2D)ptT).getY());
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "POINT"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(100, "AcDbPoint"));
        sb.append(DxfGroup.toString(10, this.pt.getX(), 12));
        sb.append(DxfGroup.toString(20, this.pt.getY(), 12));
        sb.append(DxfGroup.toString(30, this.z, 12));
        return sb.toString();
    }

    public Point2D getPt() {
        return this.pt;
    }
}

