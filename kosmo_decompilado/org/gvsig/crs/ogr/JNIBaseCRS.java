/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

public class JNIBaseCRS {
    static {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("windows")) {
            System.loadLibrary("jgdal092");
        } else {
            System.loadLibrary("jgdal");
        }
    }

    protected static native String exportToProj4Nat(long var0);

    protected static native String exportToWktNat(long var0);

    protected static native int importFromWktNat(long var0, String var2);

    protected static native int setUTMNat(long var0, int var2, int var3);

    protected static native int setWellKnownGeogCSNat(long var0, String var2);

    protected static native int importFromEPSGNat(long var0, int var2);

    protected static native int importFromProj4Nat(long var0, String var2);

    protected static native int importFromPCINat(long var0, String var2, String var3, double[] var4);

    protected static native int importFromUSGSNat(long var0, long var2, long var4, double[] var6, long var7);

    protected static native int importFromESRINat(long var0, String var2);
}

