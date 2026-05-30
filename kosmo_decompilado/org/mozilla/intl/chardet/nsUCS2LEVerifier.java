/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsUCS2LEVerifier
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

    public nsUCS2LEVerifier() {
        cclass = new int[32];
        nsUCS2LEVerifier.cclass[0] = 0;
        nsUCS2LEVerifier.cclass[1] = 0x200100;
        nsUCS2LEVerifier.cclass[2] = 0;
        nsUCS2LEVerifier.cclass[3] = 12288;
        nsUCS2LEVerifier.cclass[4] = 0;
        nsUCS2LEVerifier.cclass[5] = 0x333330;
        nsUCS2LEVerifier.cclass[6] = 0;
        nsUCS2LEVerifier.cclass[7] = 0;
        nsUCS2LEVerifier.cclass[8] = 0;
        nsUCS2LEVerifier.cclass[9] = 0;
        nsUCS2LEVerifier.cclass[10] = 0;
        nsUCS2LEVerifier.cclass[11] = 0;
        nsUCS2LEVerifier.cclass[12] = 0;
        nsUCS2LEVerifier.cclass[13] = 0;
        nsUCS2LEVerifier.cclass[14] = 0;
        nsUCS2LEVerifier.cclass[15] = 0;
        nsUCS2LEVerifier.cclass[16] = 0;
        nsUCS2LEVerifier.cclass[17] = 0;
        nsUCS2LEVerifier.cclass[18] = 0;
        nsUCS2LEVerifier.cclass[19] = 0;
        nsUCS2LEVerifier.cclass[20] = 0;
        nsUCS2LEVerifier.cclass[21] = 0;
        nsUCS2LEVerifier.cclass[22] = 0;
        nsUCS2LEVerifier.cclass[23] = 0;
        nsUCS2LEVerifier.cclass[24] = 0;
        nsUCS2LEVerifier.cclass[25] = 0;
        nsUCS2LEVerifier.cclass[26] = 0;
        nsUCS2LEVerifier.cclass[27] = 0;
        nsUCS2LEVerifier.cclass[28] = 0;
        nsUCS2LEVerifier.cclass[29] = 0;
        nsUCS2LEVerifier.cclass[30] = 0;
        nsUCS2LEVerifier.cclass[31] = 0x54000000;
        states = new int[7];
        nsUCS2LEVerifier.states[0] = 288647014;
        nsUCS2LEVerifier.states[1] = 0x22221111;
        nsUCS2LEVerifier.states[2] = 0x12155522;
        nsUCS2LEVerifier.states[3] = 0x66151555;
        nsUCS2LEVerifier.states[4] = 357927015;
        nsUCS2LEVerifier.states[5] = 0x55111555;
        nsUCS2LEVerifier.states[6] = 0x151555;
        charset = "UTF-16LE";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return true;
    }
}

