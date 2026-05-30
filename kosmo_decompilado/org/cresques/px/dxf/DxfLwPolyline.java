/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 *  org.cresques.px.dxf.DxfLayer
 */
package org.cresques.px.dxf;

import java.awt.geom.Point2D;
import java.util.Vector;
import org.cresques.cts.IProjection;
import org.cresques.io.DxfGroup;
import org.cresques.px.dxf.DxfLayer;
import org.cresques.px.dxf.DxfPolyline;

public class DxfLwPolyline
extends DxfPolyline {
    public DxfLwPolyline(IProjection proj, DxfLayer layer) {
        super(proj, layer);
    }

    @Override
    public String toDxfString() {
        StringBuffer sb = null;
        sb = new StringBuffer(DxfGroup.toString(0, "LWPOLYLINE"));
        sb.append(DxfGroup.toString(5, this.getHandle()));
        sb.append(DxfGroup.toString(100, "AcDbEntity"));
        sb.append(DxfGroup.toString(8, this.layer.getName()));
        sb.append(DxfGroup.toString(62, this.dxfColor));
        sb.append(DxfGroup.toString(100, "AcDbPolyline"));
        sb.append(DxfGroup.toString(90, this.pts.size()));
        sb.append(DxfGroup.toString(70, this.flags));
        sb.append(DxfGroup.toString(38, new Double(this.getElevation())));
        Point2D pt = null;
        Vector bulges = this.getBulges();
        int i = 0;
        while (i < this.pts.size()) {
            pt = (Point2D)this.pts.get(i);
            double bulge = (Double)bulges.get(i);
            sb.append(DxfGroup.toString(10, pt.getX(), 12));
            sb.append(DxfGroup.toString(20, pt.getY(), 12));
            sb.append(DxfGroup.toString(42, bulge, 12));
            ++i;
        }
        return sb.toString();
    }
}

