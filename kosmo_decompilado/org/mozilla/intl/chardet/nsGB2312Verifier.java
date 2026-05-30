/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsGB2312Verifier
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

    public nsGB2312Verifier() {
        cclass = new int[32];
        nsGB2312Verifier.cclass[0] = 0x11111111;
        nsGB2312Verifier.cclass[1] = 0x111111;
        nsGB2312Verifier.cclass[2] = 0x11111111;
        nsGB2312Verifier.cclass[3] = 0x11110111;
        nsGB2312Verifier.cclass[4] = 0x11111111;
        nsGB2312Verifier.cclass[5] = 0x11111111;
        nsGB2312Verifier.cclass[6] = 0x11111111;
        nsGB2312Verifier.cclass[7] = 0x11111111;
        nsGB2312Verifier.cclass[8] = 0x11111111;
        nsGB2312Verifier.cclass[9] = 0x11111111;
        nsGB2312Verifier.cclass[10] = 0x11111111;
        nsGB2312Verifier.cclass[11] = 0x11111111;
        nsGB2312Verifier.cclass[12] = 0x11111111;
        nsGB2312Verifier.cclass[13] = 0x11111111;
        nsGB2312Verifier.cclass[14] = 0x11111111;
        nsGB2312Verifier.cclass[15] = 0x11111111;
        nsGB2312Verifier.cclass[16] = 0;
        nsGB2312Verifier.cclass[17] = 0;
        nsGB2312Verifier.cclass[18] = 0;
        nsGB2312Verifier.cclass[19] = 0;
        nsGB2312Verifier.cclass[20] = 0x22222220;
        nsGB2312Verifier.cclass[21] = 0x33333322;
        nsGB2312Verifier.cclass[22] = 0x22222222;
        nsGB2312Verifier.cclass[23] = 0x22222222;
        nsGB2312Verifier.cclass[24] = 0x22222222;
        nsGB2312Verifier.cclass[25] = 0x22222222;
        nsGB2312Verifier.cclass[26] = 0x22222222;
        nsGB2312Verifier.cclass[27] = 0x22222222;
        nsGB2312Verifier.cclass[28] = 0x22222222;
        nsGB2312Verifier.cclass[29] = 0x22222222;
        nsGB2312Verifier.cclass[30] = 0x22222222;
        nsGB2312Verifier.cclass[31] = 0x2222222;
        states = new int[2];
        nsGB2312Verifier.states[0] = 0x11111301;
        nsGB2312Verifier.states[1] = 0x112222;
        charset = "GB2312";
        stFactor = 4;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

