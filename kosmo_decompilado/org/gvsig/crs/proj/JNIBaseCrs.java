/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.proj;

import org.gvsig.crs.proj.CrsProj;
import org.gvsig.crs.proj.CrsProjException;
import org.gvsig.crs.proj.OperationCrsException;

public class JNIBaseCrs {
    protected long cPtr;
    protected int latLong;
    protected String _strCrs;

    static {
        System.loadLibrary("crsjniproj");
    }

    protected native long loadCrs(String var1);

    protected native void freeCrs(long var1);

    protected native int isLatlong(long var1);

    protected static native int compareDatums(long var0, long var2);

    protected native int getErrno();

    protected static native String strErrno(int var0);

    protected static native int operation(double[] var0, double[] var1, double[] var2, long var3, long var5);

    protected static native int operationSimple(double var0, double var2, double var4, long var6, long var8);

    protected static native int operationArraySimple(double[] var0, long var1, long var3);

    protected void createCrs(String strCrs) throws CrsProjException {
        this.cPtr = this.loadCrs(strCrs);
        int errNo = this.getErrNo();
        if (errNo < 0 && errNo != -10) {
            throw new CrsProjException("Error creating CRS " + strCrs);
        }
        this._strCrs = strCrs;
    }

    protected void deleteCrs() {
        if (this.cPtr > 0L) {
            // empty if block
        }
    }

    public boolean isLatlong() {
        this.latLong = this.isLatlong(this.cPtr);
        return this.latLong != 0;
    }

    protected long getPtr() {
        return this.cPtr;
    }

    public String getStr() {
        return this._strCrs;
    }

    public void changeStrCrs(String code) {
        this._strCrs = String.valueOf(this._strCrs) + code;
    }

    protected int getErrNo() {
        return this.getErrno();
    }

    protected static String strErrNo(int errno) {
        return JNIBaseCrs.strErrno(errno);
    }

    public static int operate(double[] firstCoord, double[] secondCoord, double[] thirdCoord, CrsProj srcCrs, CrsProj destCrs) throws OperationCrsException {
        int error = JNIBaseCrs.operation(firstCoord, secondCoord, thirdCoord, srcCrs.getPtr(), destCrs.getPtr());
        if (error != 0 && error != -38 && error != -45 && error != -14) {
            throw new OperationCrsException(srcCrs, destCrs, "");
        }
        return error;
    }

    public static void operateSimple(double firstCoord, double secondCoord, double thirdCoord, CrsProj srcCrs, CrsProj destCrs) throws OperationCrsException {
        int error = JNIBaseCrs.operationSimple(firstCoord, secondCoord, thirdCoord, srcCrs.getPtr(), destCrs.getPtr());
        if (error != 1) {
            throw new OperationCrsException(srcCrs, destCrs, "");
        }
    }

    public static void operateArraySimple(double[] Coord, CrsProj srcCrs, CrsProj destCrs) throws OperationCrsException {
        int error = JNIBaseCrs.operationArraySimple(Coord, srcCrs.getPtr(), destCrs.getPtr());
        if (error != 1) {
            throw new OperationCrsException(srcCrs, destCrs, "");
        }
    }

    public static int compareDatums(CrsProj crs1, CrsProj crs2) {
        int compare = 0;
        compare = JNIBaseCrs.compareDatums(crs1.getPtr(), crs2.getPtr());
        return compare;
    }
}

