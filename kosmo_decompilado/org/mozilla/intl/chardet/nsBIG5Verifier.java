/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsBIG5Verifier
extends nsVerifier {
    static int[] cclass;
    static int[] states;
    static int stFactor;
    static String charset;

    @Override
    public int[] cclass() {
        return cclass;
    }

    @Override
    public int[] states() {
        return states;
    }

    @Override
    public int stFactor() {
        return stFactor;
    }

    @Override
    public String charset() {
        return charset;
    }

    public nsBIG5Verifier() {
        cclass = new int[32];
        nsBIG5Verifier.cclass[0] = 0x11111111;
        nsBIG5Verifier.cclass[1] = 0x111111;
        nsBIG5Verifier.cclass[2] = 0x11111111;
        nsBIG5Verifier.cclass[3] = 0x11110111;
        nsBIG5Verifier.cclass[4] = 0x11111111;
        nsBIG5Verifier.cclass[5] = 0x11111111;
        nsBIG5Verifier.cclass[6] = 0x11111111;
        nsBIG5Verifier.cclass[7] = 0x11111111;
        nsBIG5Verifier.cclass[8] = 0x22222222;
        nsBIG5Verifier.cclass[9] = 0x22222222;
        nsBIG5Verifier.cclass[10] = 0x22222222;
        nsBIG5Verifier.cclass[11] = 0x22222222;
        nsBIG5Verifier.cclass[12] = 0x22222222;
        nsBIG5Verifier.cclass[13] = 0x22222222;
        nsBIG5Verifier.cclass[14] = 0x22222222;
        nsBIG5Verifier.cclass[15] = 0x12222222;
        nsBIG5Verifier.cclass[16] = 0x44444444;
        nsBIG5Verifier.cclass[17] = 0x44444444;
        nsBIG5Verifier.cclass[18] = 0x44444444;
        nsBIG5Verifier.cclass[19] = 0x44444444;
        nsBIG5Verifier.cclass[20] = 0x33333334;
        nsBIG5Verifier.cclass[21] = 0x33333333;
        nsBIG5Verifier.cclass[22] = 0x33333333;
        nsBIG5Verifier.cclass[23] = 0x33333333;
        nsBIG5Verifier.cclass[24] = 0x33333333;
        nsBIG5Verifier.cclass[25] = 0x33333333;
        nsBIG5Verifier.cclass[26] = 0x33333333;
        nsBIG5Verifier.cclass[27] = 0x33333333;
        nsBIG5Verifier.cclass[28] = 0x33333333;
        nsBIG5Verifier.cclass[29] = 0x33333333;
        nsBIG5Verifier.cclass[30] = 0x33333333;
        nsBIG5Verifier.cclass[31] = 0x3333333;
        states = new int[3];
        nsBIG5Verifier.states[0] = 0x11113001;
        nsBIG5Verifier.states[1] = 0x12222211;
        nsBIG5Verifier.states[2] = 1;
        charset = "Big5";
        stFactor = 5;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

