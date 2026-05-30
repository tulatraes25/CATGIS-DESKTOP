/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.persistence;

import org.gvsig.crs.persistence.TrData;

public class CompTrData
extends TrData {
    TrData firstTr = null;
    TrData secondTr = null;

    public CompTrData(String authority, int code, String name, String crsSource, String crsTarget, String details, TrData firstTr, TrData secondTr) {
        super(authority, code, name, crsSource, crsTarget, details);
        this.firstTr = firstTr;
        this.secondTr = secondTr;
    }

    public CompTrData(TrData firstTr, TrData secondTr) {
        super("COMP", 0, "----", firstTr.getCrsSource(), secondTr.getCrsTarget(), String.valueOf(firstTr.getDetails()) + " @ " + secondTr.getDetails());
        this.firstTr = firstTr;
        this.secondTr = secondTr;
    }

    public TrData getFirstTr() {
        return this.firstTr;
    }

    public void setFirstTr(TrData firstTr) {
        this.firstTr = firstTr;
    }

    public TrData getSecondTr() {
        return this.secondTr;
    }

    public void setSecondTr(TrData secondTr) {
        this.secondTr = secondTr;
    }
}

