/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsUTF8Verifier
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

    public nsUTF8Verifier() {
        cclass = new int[32];
        nsUTF8Verifier.cclass[0] = 0x11111111;
        nsUTF8Verifier.cclass[1] = 0x111111;
        nsUTF8Verifier.cclass[2] = 0x11111111;
        nsUTF8Verifier.cclass[3] = 0x11110111;
        nsUTF8Verifier.cclass[4] = 0x11111111;
        nsUTF8Verifier.cclass[5] = 0x11111111;
        nsUTF8Verifier.cclass[6] = 0x11111111;
        nsUTF8Verifier.cclass[7] = 0x11111111;
        nsUTF8Verifier.cclass[8] = 0x11111111;
        nsUTF8Verifier.cclass[9] = 0x11111111;
        nsUTF8Verifier.cclass[10] = 0x11111111;
        nsUTF8Verifier.cclass[11] = 0x11111111;
        nsUTF8Verifier.cclass[12] = 0x11111111;
        nsUTF8Verifier.cclass[13] = 0x11111111;
        nsUTF8Verifier.cclass[14] = 0x11111111;
        nsUTF8Verifier.cclass[15] = 0x11111111;
        nsUTF8Verifier.cclass[16] = 0x33332222;
        nsUTF8Verifier.cclass[17] = 0x44444444;
        nsUTF8Verifier.cclass[18] = 0x44444444;
        nsUTF8Verifier.cclass[19] = 0x44444444;
        nsUTF8Verifier.cclass[20] = 0x55555555;
        nsUTF8Verifier.cclass[21] = 0x55555555;
        nsUTF8Verifier.cclass[22] = 0x55555555;
        nsUTF8Verifier.cclass[23] = 0x55555555;
        nsUTF8Verifier.cclass[24] = 0x66666600;
        nsUTF8Verifier.cclass[25] = 0x66666666;
        nsUTF8Verifier.cclass[26] = 0x66666666;
        nsUTF8Verifier.cclass[27] = 0x66666666;
        nsUTF8Verifier.cclass[28] = -2004318073;
        nsUTF8Verifier.cclass[29] = -2003269496;
        nsUTF8Verifier.cclass[30] = -1145324614;
        nsUTF8Verifier.cclass[31] = 16702940;
        states = new int[26];
        nsUTF8Verifier.states[0] = -1408167679;
        nsUTF8Verifier.states[1] = 878082233;
        nsUTF8Verifier.states[2] = 0x11111111;
        nsUTF8Verifier.states[3] = 0x11111111;
        nsUTF8Verifier.states[4] = 0x22222222;
        nsUTF8Verifier.states[5] = 0x22222222;
        nsUTF8Verifier.states[6] = 0x11555511;
        nsUTF8Verifier.states[7] = 0x11111111;
        nsUTF8Verifier.states[8] = 0x11555111;
        nsUTF8Verifier.states[9] = 0x11111111;
        nsUTF8Verifier.states[10] = 0x11777711;
        nsUTF8Verifier.states[11] = 0x11111111;
        nsUTF8Verifier.states[12] = 0x11771111;
        nsUTF8Verifier.states[13] = 0x11111111;
        nsUTF8Verifier.states[14] = 0x11999911;
        nsUTF8Verifier.states[15] = 0x11111111;
        nsUTF8Verifier.states[16] = 0x11911111;
        nsUTF8Verifier.states[17] = 0x11111111;
        nsUTF8Verifier.states[18] = 0x11CCCC11;
        nsUTF8Verifier.states[19] = 0x11111111;
        nsUTF8Verifier.states[20] = 0x11C11111;
        nsUTF8Verifier.states[21] = 0x11111111;
        nsUTF8Verifier.states[22] = 0x111CCC11;
        nsUTF8Verifier.states[23] = 0x11111111;
        nsUTF8Verifier.states[24] = 0x11000011;
        nsUTF8Verifier.states[25] = 0x11111111;
        charset = "UTF-8";
        stFactor = 16;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

