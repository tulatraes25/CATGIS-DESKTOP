/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsCP1252Verifier
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

    public nsCP1252Verifier() {
        cclass = new int[32];
        nsCP1252Verifier.cclass[0] = 0x22222221;
        nsCP1252Verifier.cclass[1] = 0x222222;
        nsCP1252Verifier.cclass[2] = 0x22222222;
        nsCP1252Verifier.cclass[3] = 0x22220222;
        nsCP1252Verifier.cclass[4] = 0x22222222;
        nsCP1252Verifier.cclass[5] = 0x22222222;
        nsCP1252Verifier.cclass[6] = 0x22222222;
        nsCP1252Verifier.cclass[7] = 0x22222222;
        nsCP1252Verifier.cclass[8] = 0x22222222;
        nsCP1252Verifier.cclass[9] = 0x22222222;
        nsCP1252Verifier.cclass[10] = 0x22222222;
        nsCP1252Verifier.cclass[11] = 0x22222222;
        nsCP1252Verifier.cclass[12] = 0x22222222;
        nsCP1252Verifier.cclass[13] = 0x22222222;
        nsCP1252Verifier.cclass[14] = 0x22222222;
        nsCP1252Verifier.cclass[15] = 0x22222222;
        nsCP1252Verifier.cclass[16] = 0x22222202;
        nsCP1252Verifier.cclass[17] = 0x1012122;
        nsCP1252Verifier.cclass[18] = 0x22222220;
        nsCP1252Verifier.cclass[19] = 0x11012122;
        nsCP1252Verifier.cclass[20] = 0x22222222;
        nsCP1252Verifier.cclass[21] = 0x22222222;
        nsCP1252Verifier.cclass[22] = 0x22222222;
        nsCP1252Verifier.cclass[23] = 0x22222222;
        nsCP1252Verifier.cclass[24] = 0x11111111;
        nsCP1252Verifier.cclass[25] = 0x11111111;
        nsCP1252Verifier.cclass[26] = 0x21111111;
        nsCP1252Verifier.cclass[27] = 0x11111111;
        nsCP1252Verifier.cclass[28] = 0x11111111;
        nsCP1252Verifier.cclass[29] = 0x11111111;
        nsCP1252Verifier.cclass[30] = 0x21111111;
        nsCP1252Verifier.cclass[31] = 0x11111111;
        states = new int[3];
        nsCP1252Verifier.states[0] = 571543601;
        nsCP1252Verifier.states[1] = 340853778;
        nsCP1252Verifier.states[2] = 65;
        charset = "windows-1252";
        stFactor = 3;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

