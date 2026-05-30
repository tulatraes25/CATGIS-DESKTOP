/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

public abstract class nsVerifier {
    static final byte eStart = 0;
    static final byte eError = 1;
    static final byte eItsMe = 2;
    static final int eidxSft4bits = 3;
    static final int eSftMsk4bits = 7;
    static final int eBitSft4bits = 2;
    static final int eUnitMsk4bits = 15;

    nsVerifier() {
    }

    public abstract String charset();

    public abstract int stFactor();

    public abstract int[] cclass();

    public abstract int[] states();

    public abstract boolean isUCS2();

    public static byte getNextState(nsVerifier v, byte b, byte s) {
        return (byte)(0xFF & (v.states()[(s * v.stFactor() + (v.cclass()[(b & 0xFF) >> 3] >> ((b & 7) << 2) & 0xF) & 0xFF) >> 3] >> ((s * v.stFactor() + (v.cclass()[(b & 0xFF) >> 3] >> ((b & 7) << 2) & 0xF) & 0xFF & 7) << 2) & 0xF));
    }
}

