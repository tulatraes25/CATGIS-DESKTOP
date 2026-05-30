/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsISO2022JPVerifier
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

    public nsISO2022JPVerifier() {
        cclass = new int[32];
        nsISO2022JPVerifier.cclass[0] = 2;
        nsISO2022JPVerifier.cclass[1] = 0x22000000;
        nsISO2022JPVerifier.cclass[2] = 0;
        nsISO2022JPVerifier.cclass[3] = 4096;
        nsISO2022JPVerifier.cclass[4] = 458752;
        nsISO2022JPVerifier.cclass[5] = 3;
        nsISO2022JPVerifier.cclass[6] = 0;
        nsISO2022JPVerifier.cclass[7] = 0;
        nsISO2022JPVerifier.cclass[8] = 1030;
        nsISO2022JPVerifier.cclass[9] = 1280;
        nsISO2022JPVerifier.cclass[10] = 0;
        nsISO2022JPVerifier.cclass[11] = 0;
        nsISO2022JPVerifier.cclass[12] = 0;
        nsISO2022JPVerifier.cclass[13] = 0;
        nsISO2022JPVerifier.cclass[14] = 0;
        nsISO2022JPVerifier.cclass[15] = 0;
        nsISO2022JPVerifier.cclass[16] = 0x22222222;
        nsISO2022JPVerifier.cclass[17] = 0x22222222;
        nsISO2022JPVerifier.cclass[18] = 0x22222222;
        nsISO2022JPVerifier.cclass[19] = 0x22222222;
        nsISO2022JPVerifier.cclass[20] = 0x22222222;
        nsISO2022JPVerifier.cclass[21] = 0x22222222;
        nsISO2022JPVerifier.cclass[22] = 0x22222222;
        nsISO2022JPVerifier.cclass[23] = 0x22222222;
        nsISO2022JPVerifier.cclass[24] = 0x22222222;
        nsISO2022JPVerifier.cclass[25] = 0x22222222;
        nsISO2022JPVerifier.cclass[26] = 0x22222222;
        nsISO2022JPVerifier.cclass[27] = 0x22222222;
        nsISO2022JPVerifier.cclass[28] = 0x22222222;
        nsISO2022JPVerifier.cclass[29] = 0x22222222;
        nsISO2022JPVerifier.cclass[30] = 0x22222222;
        nsISO2022JPVerifier.cclass[31] = 0x22222222;
        states = new int[6];
        nsISO2022JPVerifier.states[0] = 304;
        nsISO2022JPVerifier.states[1] = 0x11111111;
        nsISO2022JPVerifier.states[2] = 0x22222222;
        nsISO2022JPVerifier.states[3] = 0x41115111;
        nsISO2022JPVerifier.states[4] = 0x12121111;
        nsISO2022JPVerifier.states[5] = 0x11221111;
        charset = "ISO-2022-JP";
        stFactor = 8;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

