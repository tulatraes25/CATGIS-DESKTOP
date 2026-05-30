/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsHZVerifier
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

    public nsHZVerifier() {
        cclass = new int[32];
        nsHZVerifier.cclass[0] = 1;
        nsHZVerifier.cclass[1] = 0;
        nsHZVerifier.cclass[2] = 0;
        nsHZVerifier.cclass[3] = 4096;
        nsHZVerifier.cclass[4] = 0;
        nsHZVerifier.cclass[5] = 0;
        nsHZVerifier.cclass[6] = 0;
        nsHZVerifier.cclass[7] = 0;
        nsHZVerifier.cclass[8] = 0;
        nsHZVerifier.cclass[9] = 0;
        nsHZVerifier.cclass[10] = 0;
        nsHZVerifier.cclass[11] = 0;
        nsHZVerifier.cclass[12] = 0;
        nsHZVerifier.cclass[13] = 0;
        nsHZVerifier.cclass[14] = 0;
        nsHZVerifier.cclass[15] = 38813696;
        nsHZVerifier.cclass[16] = 0x11111111;
        nsHZVerifier.cclass[17] = 0x11111111;
        nsHZVerifier.cclass[18] = 0x11111111;
        nsHZVerifier.cclass[19] = 0x11111111;
        nsHZVerifier.cclass[20] = 0x11111111;
        nsHZVerifier.cclass[21] = 0x11111111;
        nsHZVerifier.cclass[22] = 0x11111111;
        nsHZVerifier.cclass[23] = 0x11111111;
        nsHZVerifier.cclass[24] = 0x11111111;
        nsHZVerifier.cclass[25] = 0x11111111;
        nsHZVerifier.cclass[26] = 0x11111111;
        nsHZVerifier.cclass[27] = 0x11111111;
        nsHZVerifier.cclass[28] = 0x11111111;
        nsHZVerifier.cclass[29] = 0x11111111;
        nsHZVerifier.cclass[30] = 0x11111111;
        nsHZVerifier.cclass[31] = 0x11111111;
        states = new int[6];
        nsHZVerifier.states[0] = 0x11000310;
        nsHZVerifier.states[1] = 0x22221111;
        nsHZVerifier.states[2] = 335548706;
        nsHZVerifier.states[3] = 341120533;
        nsHZVerifier.states[4] = 0x14144414;
        nsHZVerifier.states[5] = 36;
        charset = "HZ-GB-2312";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

