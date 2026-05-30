/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.Point3D
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
import java.util.Iterator;
import java.util.Vector;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Point3D;
import org.cresques.geo.ViewPortData;
import org.cresques.io.DxfGroup;
import org.cresques.px.Extent;
import org.cresques.px.dxf.AcadColor;
import org.cresques.px.dxf.DxfCalArcs;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfLayer;

public class DxfPolyline
extends DxfEntity {
    static final Color baseColor = new Color(69, 106, 121);
    Vector pts = null;
    Vector faces = null;
    GeneralPath gp = null;
    int flags = 0;
    boolean closed = false;
    boolean hasFaces = false;
    private Vector bulges;
    private Color color = baseColor;
    private double elevation;
    private String subclassMarker;

    public DxfPolyline(IProjection proj, DxfLayer layer) {
        super(proj, layer);
        this.extent = new Extent();
        this.pts = new Vector();
        this.bulges = new Vector();
    }

    public void add(Point2D pt) {
        this.pts.add(pt);
        this.extent.add(pt);
    }

    public void addBulge(Double bulge) {
        this.bulges.add(bulge);
    }

    public void addFace(int[] face) {
        this.hasFaces = true;
        if (this.faces == null) {
            this.faces = new Vector();
        }
        this.faces.add(face);
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
        Vector savePts = this.pts;
        this.pts = new Vector();
        this.extent = new Extent();
        Point2D ptDest = null;
        int i = 0;
        while (i < savePts.size()) {
            ptDest = rp.getPDest().createPoint(0.0, 0.0);
            ptDest = rp.convert((Point2D)savePts.get(i), ptDest);
            this.pts.add(ptDest);
            this.extent.add(ptDest);
            ++i;
        }
        this.setProjection(rp.getPDest());
    }

    public void draw(Graphics2D g, ViewPortData vp) {
        Color color = null;
        color = this.dxfColor == 256 ? this.layer.getColor() : AcadColor.getColor((int)this.dxfColor);
        System.out.println("PLINE color=" + color);
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
        Point2D pt0 = null;
        Object pt = null;
        Point2D pt12 = null;
        Point2D.Double ptTmp = new Point2D.Double(0.0, 0.0);
        if (!this.hasFaces) {
            for (Point2D pt12 : this.pts) {
                vp.mat.transform(pt12, ptTmp);
                if (pt0 == null) {
                    pt0 = ptTmp;
                    this.gp.moveTo((float)ptTmp.getX(), (float)ptTmp.getY());
                    continue;
                }
                this.gp.lineTo((float)ptTmp.getX(), (float)ptTmp.getY());
            }
            if (this.closed) {
                this.gp.closePath();
            }
        } else {
            System.out.println("POLYLINE: caras=" + this.faces.size() + ", puntos=" + this.pts.size());
            for (int[] face : this.faces) {
                int i0 = face[3];
                int i = 0;
                while (i < 4) {
                    int i1 = face[i];
                    if (i0 > 0) {
                        pt0 = (Point2D)this.pts.get(i0 - 1);
                        vp.mat.transform(pt0, ptTmp);
                        this.gp.moveTo((float)ptTmp.getX(), (float)ptTmp.getY());
                        pt12 = (Point2D)this.pts.get(Math.abs(i1) - 1);
                        vp.mat.transform(pt12, ptTmp);
                        this.gp.lineTo((float)ptTmp.getX(), (float)ptTmp.getY());
                    }
                    i0 = i1;
                    ++i;
                }
            }
        }
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "POLYLINE"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(100, this.getSubclassMarker()));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(70, this.flags));
        sb.append(DxfGroup.toString(66, 1));
        Point3D pt = null;
        Iterator iter = this.pts.iterator();
        System.out.println("pts.size() = " + this.pts.size());
        int i = 1;
        while (iter.hasNext()) {
            pt = (Point3D)iter.next();
            sb.append(DxfGroup.toString(0, "VERTEX"));
            sb.append(DxfGroup.toString(5, this.getHandle() + i));
            sb.append(DxfGroup.toString(100, "AcDbEntity"));
            sb.append(DxfGroup.toString(8, this.layer.getName()));
            sb.append(DxfGroup.toString(100, "AcDbVertex"));
            sb.append(DxfGroup.toString(100, "AcDb3dPolylineVertex"));
            sb.append(DxfGroup.toString(70, 32));
            sb.append(DxfGroup.toString(10, pt.getX(), 12));
            sb.append(DxfGroup.toString(20, pt.getY(), 12));
            sb.append(DxfGroup.toString(30, pt.getZ(), 12));
            ++i;
        }
        sb.append(DxfGroup.toString(0, "SEQEND"));
        sb.append(DxfGroup.toString(5, this.getHandle() + i));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        return sb.toString();
    }

    public GeneralPath getGeneralPath(ViewPortData vp) {
        this.newGP(vp);
        return (GeneralPath)this.gp.clone();
    }

    public int getFlags() {
        return this.flags;
    }

    public static Vector createArc(Point2D coord1, Point2D coord2, double bulge) {
        return new DxfCalArcs(coord1, coord2, bulge).getPoints(1.0);
    }

    public Vector getPts() {
        return this.pts;
    }

    public void setPts(Vector pts) {
        this.pts = pts;
    }

    public Vector getBulges() {
        return this.bulges;
    }

    public void setBulges(Vector bulges) {
        this.bulges = bulges;
    }

    public double getElevation() {
        return this.elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public String getSubclassMarker() {
        return this.subclassMarker;
    }

    public void setSubclassMarker(String subclassMarker) {
        this.subclassMarker = subclassMarker;
    }
}

