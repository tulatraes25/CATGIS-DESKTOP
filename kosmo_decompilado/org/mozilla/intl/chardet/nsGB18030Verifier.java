/*
 * Decompiled with CFR 0.152.
 */
package org.mozilla.intl.chardet;

import org.mozilla.intl.chardet.nsVerifier;

public class nsGB18030Verifier
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

    public nsGB18030Verifier() {
        cclass = new int[32];
        nsGB18030Verifier.cclass[0] = 0x11111111;
        nsGB18030Verifier.cclass[1] = 0x111111;
        nsGB18030Verifier.cclass[2] = 0x11111111;
        nsGB18030Verifier.cclass[3] = 0x11110111;
        nsGB18030Verifier.cclass[4] = 0x11111111;
        nsGB18030Verifier.cclass[5] = 0x11111111;
        nsGB18030Verifier.cclass[6] = 0x33333333;
        nsGB18030Verifier.cclass[7] = 0x11111133;
        nsGB18030Verifier.cclass[8] = 0x22222222;
        nsGB18030Verifier.cclass[9] = 0x22222222;
        nsGB18030Verifier.cclass[10] = 0x22222222;
        nsGB18030Verifier.cclass[11] = 0x22222222;
        nsGB18030Verifier.cclass[12] = 0x22222222;
        nsGB18030Verifier.cclass[13] = 0x22222222;
        nsGB18030Verifier.cclass[14] = 0x22222222;
        nsGB18030Verifier.cclass[15] = 0x42222222;
        nsGB18030Verifier.cclass[16] = 0x66666665;
        nsGB18030Verifier.cclass[17] = 0x66666666;
        nsGB18030Verifier.cclass[18] = 0x66666666;
        nsGB18030Verifier.cclass[19] = 0x66666666;
        nsGB18030Verifier.cclass[20] = 0x66666666;
        nsGB18030Verifier.cclass[21] = 0x66666666;
        nsGB18030Verifier.cclass[22] = 0x66666666;
        nsGB18030Verifier.cclass[23] = 0x66666666;
        nsGB18030Verifier.cclass[24] = 0x66666666;
        nsGB18030Verifier.cclass[25] = 0x66666666;
        nsGB18030Verifier.cclass[26] = 0x66666666;
        nsGB18030Verifier.cclass[27] = 0x66666666;
        nsGB18030Verifier.cclass[28] = 0x66666666;
        nsGB18030Verifier.cclass[29] = 0x66666666;
        nsGB18030Verifier.cclass[30] = 0x66666666;
        nsGB18030Verifier.cclass[31] = 0x6666666;
        states = new int[6];
        nsGB18030Verifier.states[0] = 0x13000001;
        nsGB18030Verifier.states[1] = 0x22111111;
        nsGB18030Verifier.states[2] = 0x1122222;
        nsGB18030Verifier.states[3] = 0x11110014;
        nsGB18030Verifier.states[4] = 0x12111511;
        nsGB18030Verifier.states[5] = 17;
        charset = "GB18030";
        stFactor = 7;
    }

    @Override
    public boolean isUCS2() {
        return false;
    }
}

