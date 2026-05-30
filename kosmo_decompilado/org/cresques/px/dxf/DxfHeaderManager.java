/*
 * Decompiled with CFR 0.152.
 */
package org.cresques.px.dxf;

import org.cresques.io.DxfFile;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.dxf.DxfHeaderVariables;

public class DxfHeaderManager
implements DxfFile.VarSettings {
    private DxfHeaderVariables dxfHeadVars = new DxfHeaderVariables();

    @Override
    public DxfHeaderVariables getDxfHeaderVars() {
        return this.dxfHeadVars;
    }

    @Override
    public void setAcadVersion(DxfGroupVector v) throws Exception {
        if (v.hasCode(1)) {
            String codedVersion = new String(v.getDataAsString(1));
            this.dxfHeadVars.setAcadVersion(this.dxfHeadVars.decodeAcadVersion(codedVersion));
        }
    }

    @Override
    public String getAcadVersion() {
        return this.dxfHeadVars.getAcadVersion();
    }

    @Override
    public boolean isWritedDxf3D() {
        return this.dxfHeadVars.isWritedDxf3D();
    }

    @Override
    public void loadMinZFromHeader(double d) {
        this.dxfHeadVars.loadMinZFromHeader(d);
    }

    @Override
    public void loadMaxZFromHeader(double d) {
        this.dxfHeadVars.loadMaxZFromHeader(d);
    }
}

