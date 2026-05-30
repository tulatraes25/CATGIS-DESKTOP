/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.proj;

public class JNIBaseOperation {
    static {
        System.loadLibrary("crsjniproj");
    }

    protected static native int operation(double[] var0, double[] var1, double[] var2, long var3, long var5);

    protected static native int operationSimple(double var0, double var2, double var4, long var6, long var8);

    protected static native int operationArraySimple(double[] var0, long var1, long var3);

    protected static native int compareDatums(long var0, long var2);
}

