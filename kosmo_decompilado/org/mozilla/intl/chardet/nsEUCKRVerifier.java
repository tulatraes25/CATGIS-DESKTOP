/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsEUCKRVerifier
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

    public nsEUCKRVerifier() {
        cclass = new int[32];
        nsEUCKRVerifier.cclass[0] = 0x11111111;
        nsEUCKRVerifier.cclass[1] = 0x111111;
        nsEUCKRVerifier.cclass[2] = 0x11111111;
        nsEUCKRVerifier.cclass[3] = 0x11110111;
        nsEUCKRVerifier.cclass[4] = 0x11111111;
        nsEUCKRVerifier.cclass[5] = 0x11111111;
        nsEUCKRVerifier.cclass[6] = 0x11111111;
        nsEUCKRVerifier.cclass[7] = 0x11111111;
        nsEUCKRVerifier.cclass[8] = 0x11111111;
        nsEUCKRVerifier.cclass[9] = 0x11111111;
        nsEUCKRVerifier.cclass[10] = 0x11111111;
        nsEUCKRVerifier.cclass[11] = 0x11111111;
        nsEUCKRVerifier.cclass[12] = 0x11111111;
        nsEUCKRVerifier.cclass[13] = 0x11111111;
        nsEUCKRVerifier.cclass[14] = 0x11111111;
        nsEUCKRVerifier.cclass[15] = 0x11111111;
        nsEUCKRVerifier.cclass[16] = 0;
        nsEUCKRVerifier.cclass[17] = 0;
        nsEUCKRVerifier.cclass[18] = 0;
        nsEUCKRVerifier.cclass[19] = 0;
        nsEUCKRVerifier.cclass[20] = 0x22222220;
        nsEUCKRVerifier.cclass[21] = 0x33322222;
        nsEUCKRVerifier.cclass[22] = 0x22222222;
        nsEUCKRVerifier.cclass[23] = 0x22222222;
        nsEUCKRVerifier.cclass[24] = 0x22222222;
        nsEUCKRVerifier.cclass[25] = 0x22222232;
        nsEUCKRVerifier.cclass[26] = 0x22222222;
        nsEUCKRVerifier.cclass[27] = 0x22222222;
        nsEUCKRVerifier.cclass[28] = 0x22222222;
        nsEUCKRVerifier.cclass[29] = 0x22222222;
        nsEUCKRVerifier.cclass[30] = 0x22222222;
        nsEUCKRVerifier.cclass[31] = 0x2222222;
        states = new int[2];
        nsEUCKRVerifier.states[0] = 0x11111301;
        nsEUCKRVerifier.states[1] = 0x112222;
        charset = "EUC-KR";
        stFactor = 4;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

