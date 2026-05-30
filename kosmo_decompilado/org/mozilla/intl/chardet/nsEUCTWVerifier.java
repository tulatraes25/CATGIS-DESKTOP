/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsEUCTWVerifier
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

    public nsEUCTWVerifier() {
        cclass = new int[32];
        nsEUCTWVerifier.cclass[0] = 0x22222222;
        nsEUCTWVerifier.cclass[1] = 0x222222;
        nsEUCTWVerifier.cclass[2] = 0x22222222;
        nsEUCTWVerifier.cclass[3] = 0x22220222;
        nsEUCTWVerifier.cclass[4] = 0x22222222;
        nsEUCTWVerifier.cclass[5] = 0x22222222;
        nsEUCTWVerifier.cclass[6] = 0x22222222;
        nsEUCTWVerifier.cclass[7] = 0x22222222;
        nsEUCTWVerifier.cclass[8] = 0x22222222;
        nsEUCTWVerifier.cclass[9] = 0x22222222;
        nsEUCTWVerifier.cclass[10] = 0x22222222;
        nsEUCTWVerifier.cclass[11] = 0x22222222;
        nsEUCTWVerifier.cclass[12] = 0x22222222;
        nsEUCTWVerifier.cclass[13] = 0x22222222;
        nsEUCTWVerifier.cclass[14] = 0x22222222;
        nsEUCTWVerifier.cclass[15] = 0x22222222;
        nsEUCTWVerifier.cclass[16] = 0;
        nsEUCTWVerifier.cclass[17] = 0x6000000;
        nsEUCTWVerifier.cclass[18] = 0;
        nsEUCTWVerifier.cclass[19] = 0;
        nsEUCTWVerifier.cclass[20] = 0x44444430;
        nsEUCTWVerifier.cclass[21] = 0x11111155;
        nsEUCTWVerifier.cclass[22] = 0x11111111;
        nsEUCTWVerifier.cclass[23] = 0x11111111;
        nsEUCTWVerifier.cclass[24] = 0x33331311;
        nsEUCTWVerifier.cclass[25] = 0x33333333;
        nsEUCTWVerifier.cclass[26] = 0x33333333;
        nsEUCTWVerifier.cclass[27] = 0x33333333;
        nsEUCTWVerifier.cclass[28] = 0x33333333;
        nsEUCTWVerifier.cclass[29] = 0x33333333;
        nsEUCTWVerifier.cclass[30] = 0x33333333;
        nsEUCTWVerifier.cclass[31] = 0x3333333;
        states = new int[6];
        nsEUCTWVerifier.states[0] = 338898961;
        nsEUCTWVerifier.states[1] = 0x22111111;
        nsEUCTWVerifier.states[2] = 0x10122222;
        nsEUCTWVerifier.states[3] = 0x11111000;
        nsEUCTWVerifier.states[4] = 0x101115;
        nsEUCTWVerifier.states[5] = 16;
        charset = "x-euc-tw";
        stFactor = 7;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

