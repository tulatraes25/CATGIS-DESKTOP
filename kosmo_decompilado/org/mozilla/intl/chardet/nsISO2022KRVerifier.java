/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsISO2022KRVerifier
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

    public nsISO2022KRVerifier() {
        cclass = new int[32];
        nsISO2022KRVerifier.cclass[0] = 2;
        nsISO2022KRVerifier.cclass[1] = 0;
        nsISO2022KRVerifier.cclass[2] = 0;
        nsISO2022KRVerifier.cclass[3] = 4096;
        nsISO2022KRVerifier.cclass[4] = 196608;
        nsISO2022KRVerifier.cclass[5] = 64;
        nsISO2022KRVerifier.cclass[6] = 0;
        nsISO2022KRVerifier.cclass[7] = 0;
        nsISO2022KRVerifier.cclass[8] = 20480;
        nsISO2022KRVerifier.cclass[9] = 0;
        nsISO2022KRVerifier.cclass[10] = 0;
        nsISO2022KRVerifier.cclass[11] = 0;
        nsISO2022KRVerifier.cclass[12] = 0;
        nsISO2022KRVerifier.cclass[13] = 0;
        nsISO2022KRVerifier.cclass[14] = 0;
        nsISO2022KRVerifier.cclass[15] = 0;
        nsISO2022KRVerifier.cclass[16] = 0x22222222;
        nsISO2022KRVerifier.cclass[17] = 0x22222222;
        nsISO2022KRVerifier.cclass[18] = 0x22222222;
        nsISO2022KRVerifier.cclass[19] = 0x22222222;
        nsISO2022KRVerifier.cclass[20] = 0x22222222;
        nsISO2022KRVerifier.cclass[21] = 0x22222222;
        nsISO2022KRVerifier.cclass[22] = 0x22222222;
        nsISO2022KRVerifier.cclass[23] = 0x22222222;
        nsISO2022KRVerifier.cclass[24] = 0x22222222;
        nsISO2022KRVerifier.cclass[25] = 0x22222222;
        nsISO2022KRVerifier.cclass[26] = 0x22222222;
        nsISO2022KRVerifier.cclass[27] = 0x22222222;
        nsISO2022KRVerifier.cclass[28] = 0x22222222;
        nsISO2022KRVerifier.cclass[29] = 0x22222222;
        nsISO2022KRVerifier.cclass[30] = 0x22222222;
        nsISO2022KRVerifier.cclass[31] = 0x22222222;
        states = new int[5];
        nsISO2022KRVerifier.states[0] = 0x11000130;
        nsISO2022KRVerifier.states[1] = 0x22221111;
        nsISO2022KRVerifier.states[2] = 0x11411122;
        nsISO2022KRVerifier.states[3] = 0x11151111;
        nsISO2022KRVerifier.states[4] = 8465;
        charset = "ISO-2022-KR";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

