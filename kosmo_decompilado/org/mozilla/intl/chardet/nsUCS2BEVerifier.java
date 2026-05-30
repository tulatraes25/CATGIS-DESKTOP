/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsUCS2BEVerifier
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

    public nsUCS2BEVerifier() {
        cclass = new int[32];
        nsUCS2BEVerifier.cclass[0] = 0;
        nsUCS2BEVerifier.cclass[1] = 0x200100;
        nsUCS2BEVerifier.cclass[2] = 0;
        nsUCS2BEVerifier.cclass[3] = 12288;
        nsUCS2BEVerifier.cclass[4] = 0;
        nsUCS2BEVerifier.cclass[5] = 0x333330;
        nsUCS2BEVerifier.cclass[6] = 0;
        nsUCS2BEVerifier.cclass[7] = 0;
        nsUCS2BEVerifier.cclass[8] = 0;
        nsUCS2BEVerifier.cclass[9] = 0;
        nsUCS2BEVerifier.cclass[10] = 0;
        nsUCS2BEVerifier.cclass[11] = 0;
        nsUCS2BEVerifier.cclass[12] = 0;
        nsUCS2BEVerifier.cclass[13] = 0;
        nsUCS2BEVerifier.cclass[14] = 0;
        nsUCS2BEVerifier.cclass[15] = 0;
        nsUCS2BEVerifier.cclass[16] = 0;
        nsUCS2BEVerifier.cclass[17] = 0;
        nsUCS2BEVerifier.cclass[18] = 0;
        nsUCS2BEVerifier.cclass[19] = 0;
        nsUCS2BEVerifier.cclass[20] = 0;
        nsUCS2BEVerifier.cclass[21] = 0;
        nsUCS2BEVerifier.cclass[22] = 0;
        nsUCS2BEVerifier.cclass[23] = 0;
        nsUCS2BEVerifier.cclass[24] = 0;
        nsUCS2BEVerifier.cclass[25] = 0;
        nsUCS2BEVerifier.cclass[26] = 0;
        nsUCS2BEVerifier.cclass[27] = 0;
        nsUCS2BEVerifier.cclass[28] = 0;
        nsUCS2BEVerifier.cclass[29] = 0;
        nsUCS2BEVerifier.cclass[30] = 0;
        nsUCS2BEVerifier.cclass[31] = 0x54000000;
        states = new int[7];
        nsUCS2BEVerifier.states[0] = 288626549;
        nsUCS2BEVerifier.states[1] = 0x22221111;
        nsUCS2BEVerifier.states[2] = 0x11666622;
        nsUCS2BEVerifier.states[3] = 0x66266666;
        nsUCS2BEVerifier.states[4] = 393569894;
        nsUCS2BEVerifier.states[5] = 1717659269;
        nsUCS2BEVerifier.states[6] = 0x116666;
        charset = "UTF-16BE";
        stFactor = 6;
    }

    @Override
    public boolean isUCS2() {
        return true;
    }
}

