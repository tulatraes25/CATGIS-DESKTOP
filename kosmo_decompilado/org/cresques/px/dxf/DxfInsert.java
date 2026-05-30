/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.ViewPortData
 *  org.cresques.px.Extent
 *  org.cresques.px.dxf.DxfBlock
 *  org.cresques.px.dxf.DxfLayer
 */
package org.cresques.px.dxf;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Vector;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.cresques.io.DxfGroup;
import org.cresques.px.Extent;
import org.cresques.px.dxf.DxfBlock;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfLayer;

public class DxfInsert
extends DxfEntity {
    Point2D pt;
    String blockName;
    Point2D scaleFactor;
    double rotAngle;
    DxfBlock block;
    Vector blkList;
    boolean blockFound;

    public DxfInsert(IProjection proj, DxfLayer layer) {
        super(proj, layer);
        this.block = new DxfBlock(this.proj);
        this.blockFound = false;
        this.extent = new Extent();
    }

    public Point2D getPt() {
        return this.pt;
    }

    public double getRotAngle() {
        return this.rotAngle;
    }

    public Point2D getScaleFactor() {
        return this.scaleFactor;
    }

    public boolean getBlockFound() {
        return this.blockFound;
    }

    public void setBlockFound(boolean found) {
        this.blockFound = found;
    }

    public boolean encuentraBloque(String blockName) {
        int i = 0;
        while (i < this.blkList.size() && !this.blockFound) {
            if (((DxfBlock)this.blkList.get(i)).getBlkName().equals(blockName)) {
                this.block = (DxfBlock)this.blkList.get(i);
                this.blockFound = true;
            } else {
                this.blockFound = false;
            }
            ++i;
        }
        return this.blockFound;
    }

    public DxfLayer getDxfLayer() {
        return this.layer;
    }

    public void setBlkList(Vector blkList) {
        this.blkList = blkList;
    }

    public DxfBlock getDxfBlock() {
        return this.block;
    }

    public void setPt(Point2D pt) {
        this.pt = pt;
        this.extent.add(pt);
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public void setScaleFactor(Point2D scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setRotAngle(double rotAngle) {
        this.rotAngle = rotAngle;
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "INSERT"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(100, "AcDbBlockReference"));
        sb.append(DxfGroup.toString(2, this.getBlockName()));
        sb.append(DxfGroup.toString(10, this.pt.getX(), 12));
        sb.append(DxfGroup.toString(20, this.pt.getY(), 12));
        sb.append(DxfGroup.toString(30, this.z, 12));
        sb.append(DxfGroup.toString(50, this.getRotAngle()));
        sb.append(DxfGroup.toString(41, this.getScaleFactor().getX()));
        sb.append(DxfGroup.toString(42, this.getScaleFactor().getY()));
        return sb.toString();
    }

    @Override
    public void reProject(ICoordTrans rp) {
    }

    public void draw(Graphics2D g, ViewPortData vp) {
    }
}

