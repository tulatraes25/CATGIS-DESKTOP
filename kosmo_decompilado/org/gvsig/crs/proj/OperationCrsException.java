/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.proj;

import org.gvsig.crs.proj.CrsProj;

public class OperationCrsException
extends Exception {
    private static final long serialVersionUID = 1L;
    private String _strSrcCrs;
    private String _strDestCrs;

    public OperationCrsException(CrsProj srcCrs, CrsProj destCrs, String message) {
        super("Coordinate operation error: " + srcCrs.getStr() + " to " + destCrs.getStr() + ": " + message);
        this._strSrcCrs = srcCrs.getStr();
        this._strDestCrs = destCrs.getStr();
    }

    public String getStrError() {
        return "Error en operacion del CRS " + this._strSrcCrs + " al CRS " + this._strDestCrs;
    }
}

