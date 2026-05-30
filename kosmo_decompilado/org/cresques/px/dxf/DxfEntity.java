/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.Projected
 *  org.cresques.px.PxObj
 *  org.cresques.px.dxf.DxfLayer
 */
package org.cresques.px.dxf;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Projected;
import org.cresques.px.PxObj;
import org.cresques.px.dxf.DxfLayer;

public abstract class DxfEntity
extends PxObj
implements Projected {
    IProjection proj = null;
    DxfLayer layer = null;
    int dxfColor = 0;
    int entitiesFollow = 0;
    boolean space = false;
    private int handle;
    protected double z = Double.NaN;
    protected boolean is3D = false;

    public DxfEntity(IProjection proj, DxfLayer layer) {
        this.setProjection(proj);
        this.layer = layer;
    }

    public void setProjection(IProjection p) {
        this.proj = p;
    }

    public IProjection getProjection() {
        return this.proj;
    }

    public abstract void reProject(ICoordTrans var1);

    public String getLayerName() {
        return this.layer.getName();
    }

    public String getColor() {
        return "" + this.dxfColor;
    }

    public abstract String toDxfString();

    public int getHandle() {
        return this.handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public DxfLayer getLayer() {
        return this.layer;
    }

    public void setZ(double z) {
        this.z = z;
        if (!Double.isNaN(z)) {
            this.is3D = true;
        }
    }

    public boolean is3D() {
        return this.is3D;
    }
}

