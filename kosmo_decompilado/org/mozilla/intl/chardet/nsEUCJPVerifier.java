/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsEUCJPVerifier
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

    public nsEUCJPVerifier() {
        cclass = new int[32];
        nsEUCJPVerifier.cclass[0] = 0x44444444;
        nsEUCJPVerifier.cclass[1] = 0x55444444;
        nsEUCJPVerifier.cclass[2] = 0x44444444;
        nsEUCJPVerifier.cclass[3] = 0x44445444;
        nsEUCJPVerifier.cclass[4] = 0x44444444;
        nsEUCJPVerifier.cclass[5] = 0x44444444;
        nsEUCJPVerifier.cclass[6] = 0x44444444;
        nsEUCJPVerifier.cclass[7] = 0x44444444;
        nsEUCJPVerifier.cclass[8] = 0x44444444;
        nsEUCJPVerifier.cclass[9] = 0x44444444;
        nsEUCJPVerifier.cclass[10] = 0x44444444;
        nsEUCJPVerifier.cclass[11] = 0x44444444;
        nsEUCJPVerifier.cclass[12] = 0x44444444;
        nsEUCJPVerifier.cclass[13] = 0x44444444;
        nsEUCJPVerifier.cclass[14] = 0x44444444;
        nsEUCJPVerifier.cclass[15] = 0x44444444;
        nsEUCJPVerifier.cclass[16] = 0x55555555;
        nsEUCJPVerifier.cclass[17] = 0x31555555;
        nsEUCJPVerifier.cclass[18] = 0x55555555;
        nsEUCJPVerifier.cclass[19] = 0x55555555;
        nsEUCJPVerifier.cclass[20] = 0x22222225;
        nsEUCJPVerifier.cclass[21] = 0x22222222;
        nsEUCJPVerifier.cclass[22] = 0x22222222;
        nsEUCJPVerifier.cclass[23] = 0x22222222;
        nsEUCJPVerifier.cclass[24] = 0x22222222;
        nsEUCJPVerifier.cclass[25] = 0x22222222;
        nsEUCJPVerifier.cclass[26] = 0x22222222;
        nsEUCJPVerifier.cclass[27] = 0x22222222;
        nsEUCJPVerifier.cclass[28] = 0;
        nsEUCJPVerifier.cclass[29] = 0;
        nsEUCJPVerifier.cclass[30] = 0;
        nsEUCJPVerifier.cclass[31] = 0x50000000;
        states = new int[5];
        nsEUCJPVerifier.states[0] = 286282563;
        nsEUCJPVerifier.states[1] = 0x22221111;
        nsEUCJPVerifier.states[2] = 0x11101022;
        nsEUCJPVerifier.states[3] = 0x13111011;
        nsEUCJPVerifier.states[4] = 4371;
        charset = "EUC-JP";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

