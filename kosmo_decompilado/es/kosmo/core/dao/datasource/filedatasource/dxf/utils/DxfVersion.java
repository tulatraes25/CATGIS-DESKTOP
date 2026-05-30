/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf.utils;

public class DxfVersion {
    public static String decodeAcadVersion(String codedVersion) {
        if (codedVersion.equals("AC1006")) {
            return new String("DXF Release 10");
        }
        if (codedVersion.equals("AC1009")) {
            return new String("DXF Release 11 & 12");
        }
        if (codedVersion.equals("AC1012")) {
            return new String("DXF Release 13");
        }
        if (codedVersion.equals("AC1014")) {
            return new String("DXF Release 14");
        }
        if (codedVersion.equals("AC1015")) {
            return new String("DXF 2000/2000i/2002");
        }
        if (codedVersion.equals("AC1018")) {
            return new String("DXF 2004/2005/2006");
        }
        if (codedVersion.equals("AC1021")) {
            return new String("DXF 2007/2008/2009");
        }
        if (codedVersion.equals("AC1024")) {
            return new String("DXF 2010/2011/2012");
        }
        if (codedVersion.equals("AC1027")) {
            return new String("DXF 2013");
        }
        return new String("Unknown DXF version " + codedVersion);
    }
}

