/*
 * Decompiled with CFR 0.152.
 */
package org.cresques.px.dxf;

public class DxfHeaderVariables {
    private String acadVersion;
    private String acadMaintVer;
    private boolean writedDxf3D;
    private double minZFromHeader;
    private double maxZFromHeader;

    public String getAcadVersion() {
        return this.acadVersion;
    }

    public void setAcadVersion(String acadVersion) {
        this.acadVersion = acadVersion;
    }

    public String decodeAcadVersion(String codedVersion) {
        if (codedVersion.equals("AC1006")) {
            return new String("R10");
        }
        if (codedVersion.equals("AC1009")) {
            return new String("R11&R12");
        }
        if (codedVersion.equals("AC1012")) {
            return new String("R13");
        }
        if (codedVersion.equals("AC1014")) {
            return new String("R14");
        }
        if (codedVersion.equals("AC1015")) {
            return new String("ACAD2000/2000i/2002");
        }
        if (codedVersion.equals("AC1018")) {
            return new String("ACAD2004/2005/2006");
        }
        if (codedVersion.equals("AC1021")) {
            return new String("ACAD2007/2008/2009");
        }
        if (codedVersion.equals("AC1024")) {
            return new String("ACAD2010/2011/2012");
        }
        if (codedVersion.equals("AC1027")) {
            return new String("ACAD2013");
        }
        return new String("Unknown codedVersion");
    }

    public boolean isWritedDxf3D() {
        this.writedDxf3D = false;
        double z1 = this.minZFromHeader;
        double z2 = this.maxZFromHeader;
        if (z1 == 9.99999999E8 && z2 == -9.99999999E8) {
            this.writedDxf3D = true;
        }
        return this.writedDxf3D;
    }

    public void loadMinZFromHeader(double d) {
        this.minZFromHeader = d;
    }

    public void loadMaxZFromHeader(double d) {
        this.maxZFromHeader = d;
    }
}

