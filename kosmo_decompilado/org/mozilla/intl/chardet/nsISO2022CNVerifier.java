/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsISO2022CNVerifier
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

    public nsISO2022CNVerifier() {
        cclass = new int[32];
        nsISO2022CNVerifier.cclass[0] = 2;
        nsISO2022CNVerifier.cclass[1] = 0;
        nsISO2022CNVerifier.cclass[2] = 0;
        nsISO2022CNVerifier.cclass[3] = 4096;
        nsISO2022CNVerifier.cclass[4] = 0;
        nsISO2022CNVerifier.cclass[5] = 48;
        nsISO2022CNVerifier.cclass[6] = 0;
        nsISO2022CNVerifier.cclass[7] = 0;
        nsISO2022CNVerifier.cclass[8] = 16384;
        nsISO2022CNVerifier.cclass[9] = 0;
        nsISO2022CNVerifier.cclass[10] = 0;
        nsISO2022CNVerifier.cclass[11] = 0;
        nsISO2022CNVerifier.cclass[12] = 0;
        nsISO2022CNVerifier.cclass[13] = 0;
        nsISO2022CNVerifier.cclass[14] = 0;
        nsISO2022CNVerifier.cclass[15] = 0;
        nsISO2022CNVerifier.cclass[16] = 0x22222222;
        nsISO2022CNVerifier.cclass[17] = 0x22222222;
        nsISO2022CNVerifier.cclass[18] = 0x22222222;
        nsISO2022CNVerifier.cclass[19] = 0x22222222;
        nsISO2022CNVerifier.cclass[20] = 0x22222222;
        nsISO2022CNVerifier.cclass[21] = 0x22222222;
        nsISO2022CNVerifier.cclass[22] = 0x22222222;
        nsISO2022CNVerifier.cclass[23] = 0x22222222;
        nsISO2022CNVerifier.cclass[24] = 0x22222222;
        nsISO2022CNVerifier.cclass[25] = 0x22222222;
        nsISO2022CNVerifier.cclass[26] = 0x22222222;
        nsISO2022CNVerifier.cclass[27] = 0x22222222;
        nsISO2022CNVerifier.cclass[28] = 0x22222222;
        nsISO2022CNVerifier.cclass[29] = 0x22222222;
        nsISO2022CNVerifier.cclass[30] = 0x22222222;
        nsISO2022CNVerifier.cclass[31] = 0x22222222;
        states = new int[8];
        nsISO2022CNVerifier.states[0] = 304;
        nsISO2022CNVerifier.states[1] = 0x11111110;
        nsISO2022CNVerifier.states[2] = 0x22222211;
        nsISO2022CNVerifier.states[3] = 0x14111222;
        nsISO2022CNVerifier.states[4] = 0x11112111;
        nsISO2022CNVerifier.states[5] = 0x11111165;
        nsISO2022CNVerifier.states[6] = 0x11112111;
        nsISO2022CNVerifier.states[7] = 0x1211111;
        charset = "ISO-2022-CN";
        stFactor = 9;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

