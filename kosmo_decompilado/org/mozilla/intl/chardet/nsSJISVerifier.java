/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsSJISVerifier
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

    public nsSJISVerifier() {
        cclass = new int[32];
        nsSJISVerifier.cclass[0] = 0x11111110;
        nsSJISVerifier.cclass[1] = 0x111111;
        nsSJISVerifier.cclass[2] = 0x11111111;
        nsSJISVerifier.cclass[3] = 0x11110111;
        nsSJISVerifier.cclass[4] = 0x11111111;
        nsSJISVerifier.cclass[5] = 0x11111111;
        nsSJISVerifier.cclass[6] = 0x11111111;
        nsSJISVerifier.cclass[7] = 0x11111111;
        nsSJISVerifier.cclass[8] = 0x22222222;
        nsSJISVerifier.cclass[9] = 0x22222222;
        nsSJISVerifier.cclass[10] = 0x22222222;
        nsSJISVerifier.cclass[11] = 0x22222222;
        nsSJISVerifier.cclass[12] = 0x22222222;
        nsSJISVerifier.cclass[13] = 0x22222222;
        nsSJISVerifier.cclass[14] = 0x22222222;
        nsSJISVerifier.cclass[15] = 0x12222222;
        nsSJISVerifier.cclass[16] = 0x33333333;
        nsSJISVerifier.cclass[17] = 0x33333333;
        nsSJISVerifier.cclass[18] = 0x33333333;
        nsSJISVerifier.cclass[19] = 0x33333333;
        nsSJISVerifier.cclass[20] = 0x22222224;
        nsSJISVerifier.cclass[21] = 0x22222222;
        nsSJISVerifier.cclass[22] = 0x22222222;
        nsSJISVerifier.cclass[23] = 0x22222222;
        nsSJISVerifier.cclass[24] = 0x22222222;
        nsSJISVerifier.cclass[25] = 0x22222222;
        nsSJISVerifier.cclass[26] = 0x22222222;
        nsSJISVerifier.cclass[27] = 0x22222222;
        nsSJISVerifier.cclass[28] = 0x33333333;
        nsSJISVerifier.cclass[29] = 0x44455333;
        nsSJISVerifier.cclass[30] = 0x44444444;
        nsSJISVerifier.cclass[31] = 279620;
        states = new int[3];
        nsSJISVerifier.states[0] = 0x11113001;
        nsSJISVerifier.states[1] = 0x22221111;
        nsSJISVerifier.states[2] = 4386;
        charset = "Shift_JIS";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

