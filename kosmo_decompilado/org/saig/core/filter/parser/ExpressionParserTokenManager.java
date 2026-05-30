/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import java.io.IOException;
import java.io.PrintStream;
import org.saig.core.filter.parser.ExpressionParserConstants;
import org.saig.core.filter.parser.SimpleCharStream;
import org.saig.core.filter.parser.Token;
import org.saig.core.filter.parser.TokenMgrError;

public class ExpressionParserTokenManager
implements ExpressionParserConstants {
    public PrintStream debugStream = System.out;
    static final long[] jjbitVec0;
    static final int[] jjnextStates;
    public static final String[] jjstrLiteralImages;
    public static final String[] lexStateNames;
    public static final int[] jjnewLexState;
    static final long[] jjtoToken;
    static final long[] jjtoSkip;
    static final long[] jjtoMore;
    private SimpleCharStream input_stream;
    private final int[] jjrounds = new int[53];
    private final int[] jjstateSet = new int[106];
    StringBuffer image;
    int jjimageLen;
    int lengthOfMatch;
    protected char curChar;
    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    static {
        long[] lArray = new long[4];
        lArray[2] = -1L;
        lArray[3] = -1L;
        jjbitVec0 = lArray;
        jjnextStates = new int[]{44, 45, 25, 47, 48, 50, 44, 45, 25, 47, 48, 50, 40, 42, 36, 38, 32, 34, 28, 29, 51, 52};
        String[] stringArray = new String[45];
        stringArray[0] = "";
        stringArray[30] = "(";
        stringArray[31] = ")";
        stringArray[32] = "[";
        stringArray[33] = "]";
        stringArray[40] = "+";
        stringArray[41] = "-";
        stringArray[42] = "*";
        stringArray[43] = "/";
        stringArray[44] = ",";
        jjstrLiteralImages = stringArray;
        lexStateNames = new String[]{"DEFAULT", "IN_SQ", "IN_DQ"};
        int[] nArray = new int[45];
        nArray[0] = -1;
        nArray[1] = -1;
        nArray[2] = -1;
        nArray[3] = -1;
        nArray[4] = -1;
        nArray[5] = -1;
        nArray[6] = 1;
        nArray[7] = 2;
        nArray[10] = -1;
        nArray[11] = -1;
        nArray[12] = -1;
        nArray[13] = -1;
        nArray[14] = -1;
        nArray[15] = -1;
        nArray[16] = -1;
        nArray[17] = -1;
        nArray[18] = -1;
        nArray[19] = -1;
        nArray[20] = -1;
        nArray[21] = -1;
        nArray[22] = -1;
        nArray[23] = -1;
        nArray[24] = -1;
        nArray[25] = -1;
        nArray[26] = -1;
        nArray[27] = -1;
        nArray[28] = -1;
        nArray[29] = -1;
        nArray[30] = -1;
        nArray[31] = -1;
        nArray[32] = -1;
        nArray[33] = -1;
        nArray[34] = -1;
        nArray[35] = -1;
        nArray[36] = -1;
        nArray[37] = -1;
        nArray[38] = -1;
        nArray[39] = -1;
        nArray[40] = -1;
        nArray[41] = -1;
        nArray[42] = -1;
        nArray[43] = -1;
        nArray[44] = -1;
        jjnewLexState = nArray;
        jjtoToken = new long[]{34222299411201L};
        jjtoSkip = new long[]{62L};
        jjtoMore = new long[]{3264L};
    }

    public void setDebugStream(PrintStream ds) {
        this.debugStream = ds;
    }

    private final int jjStopAtPos(int pos, int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        return pos + 1;
    }

    private final int jjMoveStringLiteralDfa0_0() {
        switch (this.curChar) {
            case '\t': {
                this.jjmatchedKind = 2;
                return this.jjMoveNfa_0(2, 0);
            }
            case '\n': {
                this.jjmatchedKind = 3;
                return this.jjMoveNfa_0(2, 0);
            }
            case '\f': {
                this.jjmatchedKind = 5;
                return this.jjMoveNfa_0(2, 0);
            }
            case '\r': {
                this.jjmatchedKind = 4;
                return this.jjMoveNfa_0(2, 0);
            }
            case ' ': {
                this.jjmatchedKind = 1;
                return this.jjMoveNfa_0(2, 0);
            }
            case '\"': {
                this.jjmatchedKind = 7;
                return this.jjMoveNfa_0(2, 0);
            }
            case '\'': {
                this.jjmatchedKind = 6;
                return this.jjMoveNfa_0(2, 0);
            }
            case '(': {
                this.jjmatchedKind = 30;
                return this.jjMoveNfa_0(2, 0);
            }
            case ')': {
                this.jjmatchedKind = 31;
                return this.jjMoveNfa_0(2, 0);
            }
            case '*': {
                this.jjmatchedKind = 42;
                return this.jjMoveNfa_0(2, 0);
            }
            case '+': {
                this.jjmatchedKind = 40;
                return this.jjMoveNfa_0(2, 0);
            }
            case ',': {
                this.jjmatchedKind = 44;
                return this.jjMoveNfa_0(2, 0);
            }
            case '-': {
                this.jjmatchedKind = 41;
                return this.jjMoveNfa_0(2, 0);
            }
            case '/': {
                this.jjmatchedKind = 43;
                return this.jjMoveNfa_0(2, 0);
            }
            case 'F': {
                return this.jjMoveStringLiteralDfa1_0(0x400000L);
            }
            case 'G': {
                return this.jjMoveStringLiteralDfa1_0(0x20000000L);
            }
            case 'L': {
                return this.jjMoveStringLiteralDfa1_0(0x1000000L);
            }
            case 'M': {
                return this.jjMoveStringLiteralDfa1_0(0x1C000000L);
            }
            case 'P': {
                return this.jjMoveStringLiteralDfa1_0(0x2800000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa1_0(0x200000L);
            }
            case '[': {
                this.jjmatchedKind = 32;
                return this.jjMoveNfa_0(2, 0);
            }
            case ']': {
                this.jjmatchedKind = 33;
                return this.jjMoveNfa_0(2, 0);
            }
            case 'f': {
                return this.jjMoveStringLiteralDfa1_0(0x400000L);
            }
            case 'g': {
                return this.jjMoveStringLiteralDfa1_0(0x20000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa1_0(0x1000000L);
            }
            case 'm': {
                return this.jjMoveStringLiteralDfa1_0(0x1C000000L);
            }
            case 'p': {
                return this.jjMoveStringLiteralDfa1_0(0x2800000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa1_0(0x200000L);
            }
        }
        return this.jjMoveNfa_0(2, 0);
    }

    private final int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 0);
        }
        switch (this.curChar) {
            case 'A': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x400000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1000000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x2800000L);
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x200000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1C000000L);
            }
            case 'a': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x400000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x2800000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x200000L);
            }
            case 'u': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1C000000L);
            }
        }
        return this.jjMoveNfa_0(2, 1);
    }

    private final int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 1);
        }
        switch (this.curChar) {
            case 'I': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x800000L);
            }
            case 'L': {
                return this.jjMoveStringLiteralDfa3_0(active0, 507510784L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x20000000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x200000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x800000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa3_0(active0, 507510784L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x20000000L);
            }
            case 'u': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x200000L);
            }
        }
        return this.jjMoveNfa_0(2, 2);
    }

    private final int jjMoveStringLiteralDfa3_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 2);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 2);
        }
        switch (this.curChar) {
            case 'E': {
                if ((active0 & 0x200000L) != 0L) {
                    this.jjmatchedKind = 21;
                    this.jjmatchedPos = 3;
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
            }
            case 'M': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x20000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x800000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1C000000L);
            }
            case 'Y': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x2000000L);
            }
            case 'e': {
                if ((active0 & 0x200000L) != 0L) {
                    this.jjmatchedKind = 21;
                    this.jjmatchedPos = 3;
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
            }
            case 'm': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x20000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x800000L);
            }
            case 's': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1C000000L);
            }
            case 'y': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x2000000L);
            }
        }
        return this.jjMoveNfa_0(2, 3);
    }

    private final int jjMoveStringLiteralDfa4_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 3);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 3);
        }
        switch (this.curChar) {
            case 'E': {
                if ((active0 & 0x400000L) != 0L) {
                    this.jjmatchedKind = 22;
                    this.jjmatchedPos = 4;
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x20000000L);
            }
            case 'G': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1C000000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
            }
            case 'T': {
                if ((active0 & 0x800000L) == 0L) break;
                this.jjmatchedKind = 23;
                this.jjmatchedPos = 4;
                break;
            }
            case 'e': {
                if ((active0 & 0x400000L) != 0L) {
                    this.jjmatchedKind = 22;
                    this.jjmatchedPos = 4;
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x20000000L);
            }
            case 'g': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1C000000L);
            }
            case 's': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
            }
            case 't': {
                if ((active0 & 0x800000L) == 0L) break;
                this.jjmatchedKind = 23;
                this.jjmatchedPos = 4;
                break;
            }
        }
        return this.jjMoveNfa_0(2, 4);
    }

    private final int jjMoveStringLiteralDfa5_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 4);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 4);
        }
        switch (this.curChar) {
            case 'L': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x8000000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x2000000L);
            }
            case 'P': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x14000000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x21000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x8000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x2000000L);
            }
            case 'p': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x14000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x21000000L);
            }
        }
        return this.jjMoveNfa_0(2, 5);
    }

    private final int jjMoveStringLiteralDfa6_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 5);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 5);
        }
        switch (this.curChar) {
            case 'I': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x8000000L);
            }
            case 'N': {
                if ((active0 & 0x2000000L) == 0L) break;
                this.jjmatchedKind = 25;
                this.jjmatchedPos = 6;
                break;
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x14000000L);
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x21000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x8000000L);
            }
            case 'n': {
                if ((active0 & 0x2000000L) == 0L) break;
                this.jjmatchedKind = 25;
                this.jjmatchedPos = 6;
                break;
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x14000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x21000000L);
            }
        }
        return this.jjMoveNfa_0(2, 6);
    }

    private final int jjMoveStringLiteralDfa7_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 6);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 6);
        }
        switch (this.curChar) {
            case 'I': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x5000000L);
            }
            case 'L': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x10000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x8000000L);
            }
            case 'Y': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x20000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x5000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x10000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x8000000L);
            }
            case 'y': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x20000000L);
            }
        }
        return this.jjMoveNfa_0(2, 7);
    }

    private final int jjMoveStringLiteralDfa8_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 7);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 7);
        }
        switch (this.curChar) {
            case 'C': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x20000000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x8000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x5000000L);
            }
            case 'Y': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x10000000L);
            }
            case 'c': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x20000000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x8000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x5000000L);
            }
            case 'y': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x10000000L);
            }
        }
        return this.jjMoveNfa_0(2, 8);
    }

    private final int jjMoveStringLiteralDfa9_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 8);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 8);
        }
        switch (this.curChar) {
            case 'G': {
                if ((active0 & 0x1000000L) != 0L) {
                    this.jjmatchedKind = 24;
                    this.jjmatchedPos = 9;
                }
                return this.jjMoveStringLiteralDfa10_0(active0, 0x10000000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x20000000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x8000000L);
            }
            case 'T': {
                if ((active0 & 0x4000000L) == 0L) break;
                this.jjmatchedKind = 26;
                this.jjmatchedPos = 9;
                break;
            }
            case 'g': {
                if ((active0 & 0x1000000L) != 0L) {
                    this.jjmatchedKind = 24;
                    this.jjmatchedPos = 9;
                }
                return this.jjMoveStringLiteralDfa10_0(active0, 0x10000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x20000000L);
            }
            case 's': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x8000000L);
            }
            case 't': {
                if ((active0 & 0x4000000L) == 0L) break;
                this.jjmatchedKind = 26;
                this.jjmatchedPos = 9;
                break;
            }
        }
        return this.jjMoveNfa_0(2, 9);
    }

    private final int jjMoveStringLiteralDfa10_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 9);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 9);
        }
        switch (this.curChar) {
            case 'L': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x20000000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x10000000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x8000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x20000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x10000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x8000000L);
            }
        }
        return this.jjMoveNfa_0(2, 10);
    }

    private final int jjMoveStringLiteralDfa11_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 10);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 10);
        }
        switch (this.curChar) {
            case 'L': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x20000000L);
            }
            case 'N': {
                if ((active0 & 0x10000000L) == 0L) break;
                this.jjmatchedKind = 28;
                this.jjmatchedPos = 11;
                break;
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x8000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x20000000L);
            }
            case 'n': {
                if ((active0 & 0x10000000L) == 0L) break;
                this.jjmatchedKind = 28;
                this.jjmatchedPos = 11;
                break;
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x8000000L);
            }
        }
        return this.jjMoveNfa_0(2, 11);
    }

    private final int jjMoveStringLiteralDfa12_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 11);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 11);
        }
        switch (this.curChar) {
            case 'E': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x20000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x8000000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x20000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x8000000L);
            }
        }
        return this.jjMoveNfa_0(2, 12);
    }

    private final int jjMoveStringLiteralDfa13_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 12);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 12);
        }
        switch (this.curChar) {
            case 'C': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x20000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x8000000L);
            }
            case 'c': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x20000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x8000000L);
            }
        }
        return this.jjMoveNfa_0(2, 13);
    }

    private final int jjMoveStringLiteralDfa14_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 13);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 13);
        }
        switch (this.curChar) {
            case 'G': {
                if ((active0 & 0x8000000L) == 0L) break;
                this.jjmatchedKind = 27;
                this.jjmatchedPos = 14;
                break;
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa15_0(active0, 0x20000000L);
            }
            case 'g': {
                if ((active0 & 0x8000000L) == 0L) break;
                this.jjmatchedKind = 27;
                this.jjmatchedPos = 14;
                break;
            }
            case 't': {
                return this.jjMoveStringLiteralDfa15_0(active0, 0x20000000L);
            }
        }
        return this.jjMoveNfa_0(2, 14);
    }

    private final int jjMoveStringLiteralDfa15_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 14);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 14);
        }
        switch (this.curChar) {
            case 'I': {
                return this.jjMoveStringLiteralDfa16_0(active0, 0x20000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa16_0(active0, 0x20000000L);
            }
        }
        return this.jjMoveNfa_0(2, 15);
    }

    private final int jjMoveStringLiteralDfa16_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 15);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 15);
        }
        switch (this.curChar) {
            case 'O': {
                return this.jjMoveStringLiteralDfa17_0(active0, 0x20000000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa17_0(active0, 0x20000000L);
            }
        }
        return this.jjMoveNfa_0(2, 16);
    }

    private final int jjMoveStringLiteralDfa17_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjMoveNfa_0(2, 16);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return this.jjMoveNfa_0(2, 16);
        }
        switch (this.curChar) {
            case 'N': {
                if ((active0 & 0x20000000L) == 0L) break;
                this.jjmatchedKind = 29;
                this.jjmatchedPos = 17;
                break;
            }
            case 'n': {
                if ((active0 & 0x20000000L) == 0L) break;
                this.jjmatchedKind = 29;
                this.jjmatchedPos = 17;
                break;
            }
        }
        return this.jjMoveNfa_0(2, 17);
    }

    private final void jjCheckNAdd(int state) {
        if (this.jjrounds[state] != this.jjround) {
            this.jjstateSet[this.jjnewStateCnt++] = state;
            this.jjrounds[state] = this.jjround;
        }
    }

    private final void jjAddStates(int start, int end) {
        do {
            this.jjstateSet[this.jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private final void jjCheckNAddTwoStates(int state1, int state2) {
        this.jjCheckNAdd(state1);
        this.jjCheckNAdd(state2);
    }

    private final void jjCheckNAddStates(int start, int end) {
        do {
            this.jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    private final void jjCheckNAddStates(int start) {
        this.jjCheckNAdd(jjnextStates[start]);
        this.jjCheckNAdd(jjnextStates[start + 1]);
    }

    private final int jjMoveNfa_0(int startState, int curPos) {
        int strKind = this.jjmatchedKind;
        int strPos = this.jjmatchedPos;
        int seenUpto = curPos + 1;
        this.input_stream.backup(seenUpto);
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            throw new Error("Internal Error");
        }
        curPos = 0;
        int startsAt = 0;
        this.jjnewStateCnt = 53;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            if (++this.jjround == Integer.MAX_VALUE) {
                this.ReInitRounds();
            }
            if (this.curChar < '@') {
                long l = 1L << this.curChar;
                block65: do {
                    switch (this.jjstateSet[--i]) {
                        case 2: {
                            if ((0x3FF000000000000L & l) != 0L) {
                                if (kind > 35) {
                                    kind = 35;
                                }
                                this.jjCheckNAddStates(0, 5);
                            } else if (this.curChar == '-') {
                                this.jjCheckNAddStates(6, 8);
                            } else if (this.curChar == '.') {
                                this.jjCheckNAdd(26);
                            } else if (this.curChar == '<') {
                                this.jjstateSet[this.jjnewStateCnt++] = 21;
                            } else if (this.curChar == '>') {
                                this.jjstateSet[this.jjnewStateCnt++] = 19;
                            } else if (this.curChar == '!') {
                                this.jjstateSet[this.jjnewStateCnt++] = 15;
                            } else if (this.curChar == '=') {
                                if (kind > 15) {
                                    kind = 15;
                                }
                            } else if (this.curChar == '&') {
                                this.jjstateSet[this.jjnewStateCnt++] = 3;
                            }
                            if (this.curChar == '<') {
                                if (kind <= 18) break;
                                kind = 18;
                                break;
                            }
                            if (this.curChar == '>') {
                                if (kind <= 17) break;
                                kind = 17;
                                break;
                            }
                            if (this.curChar == '=') {
                                this.jjstateSet[this.jjnewStateCnt++] = 12;
                                break;
                            }
                            if (this.curChar != '!' || kind <= 14) continue block65;
                            kind = 14;
                            break;
                        }
                        case 3: {
                            if (this.curChar != '&' || kind <= 12) continue block65;
                            kind = 12;
                            break;
                        }
                        case 4: {
                            if (this.curChar != '&') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 3;
                            break;
                        }
                        case 9: {
                            if (this.curChar != '!' || kind <= 14) continue block65;
                            kind = 14;
                            break;
                        }
                        case 12: {
                            if (this.curChar != '=' || kind <= 15) continue block65;
                            kind = 15;
                            break;
                        }
                        case 13: {
                            if (this.curChar != '=') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 12;
                            break;
                        }
                        case 14: {
                            if (this.curChar != '=' || kind <= 15) continue block65;
                            kind = 15;
                            break;
                        }
                        case 15: {
                            if (this.curChar != '=' || kind <= 16) continue block65;
                            kind = 16;
                            break;
                        }
                        case 16: {
                            if (this.curChar != '!') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 15;
                            break;
                        }
                        case 17: {
                            if (this.curChar != '>' || kind <= 17) continue block65;
                            kind = 17;
                            break;
                        }
                        case 18: {
                            if (this.curChar != '<' || kind <= 18) continue block65;
                            kind = 18;
                            break;
                        }
                        case 19: {
                            if (this.curChar != '=' || kind <= 19) continue block65;
                            kind = 19;
                            break;
                        }
                        case 20: {
                            if (this.curChar != '>') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 19;
                            break;
                        }
                        case 21: {
                            if (this.curChar != '=' || kind <= 20) continue block65;
                            kind = 20;
                            break;
                        }
                        case 22: {
                            if (this.curChar != '<') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 21;
                            break;
                        }
                        case 24: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 34) {
                                kind = 34;
                            }
                            this.jjstateSet[this.jjnewStateCnt++] = 24;
                            break;
                        }
                        case 25: {
                            if (this.curChar != '.') break;
                            this.jjCheckNAdd(26);
                            break;
                        }
                        case 26: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAddTwoStates(26, 27);
                            break;
                        }
                        case 28: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(29);
                            break;
                        }
                        case 29: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAdd(29);
                            break;
                        }
                        case 43: {
                            if (this.curChar != '-') break;
                            this.jjCheckNAddStates(6, 8);
                            break;
                        }
                        case 44: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 35) {
                                kind = 35;
                            }
                            this.jjCheckNAdd(44);
                            break;
                        }
                        case 45: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(45, 25);
                            break;
                        }
                        case 46: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 35) {
                                kind = 35;
                            }
                            this.jjCheckNAddStates(0, 5);
                            break;
                        }
                        case 47: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAddStates(9, 11);
                            break;
                        }
                        case 48: {
                            if (this.curChar != '.') continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAddTwoStates(49, 50);
                            break;
                        }
                        case 49: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAddTwoStates(49, 50);
                            break;
                        }
                        case 51: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(52);
                            break;
                        }
                        case 52: {
                            if ((0x3FF000000000000L & l) == 0L) continue block65;
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAdd(52);
                            break;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < '\u0080') {
                long l = 1L << (this.curChar & 0x3F);
                block66: do {
                    switch (this.jjstateSet[--i]) {
                        case 2: {
                            if ((0x7FFFFFE87FFFFFEL & l) != 0L) {
                                if (kind > 34) {
                                    kind = 34;
                                }
                                this.jjCheckNAdd(24);
                            } else if (this.curChar == '|') {
                                this.jjstateSet[this.jjnewStateCnt++] = 7;
                            }
                            if ((0x100000001000L & l) != 0L) {
                                this.jjAddStates(12, 13);
                                break;
                            }
                            if ((0x8000000080L & l) != 0L) {
                                this.jjAddStates(14, 15);
                                break;
                            }
                            if ((0x400000004000L & l) != 0L) {
                                this.jjAddStates(16, 17);
                                break;
                            }
                            if ((0x2000000020L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 10;
                                break;
                            }
                            if ((0x800000008000L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 5;
                                break;
                            }
                            if ((0x200000002L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 1;
                            break;
                        }
                        case 0: {
                            if ((0x1000000010L & l) == 0L || kind <= 12) continue block66;
                            kind = 12;
                            break;
                        }
                        case 1: {
                            if ((0x400000004000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 0;
                            break;
                        }
                        case 5: {
                            if ((0x4000000040000L & l) == 0L || kind <= 13) continue block66;
                            kind = 13;
                            break;
                        }
                        case 6: {
                            if ((0x800000008000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 5;
                            break;
                        }
                        case 7: {
                            if (this.curChar != '|' || kind <= 13) continue block66;
                            kind = 13;
                            break;
                        }
                        case 8: {
                            if (this.curChar != '|') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 7;
                            break;
                        }
                        case 10: {
                            if ((0x2000000020000L & l) == 0L || kind <= 15) continue block66;
                            kind = 15;
                            break;
                        }
                        case 11: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 10;
                            break;
                        }
                        case 23: 
                        case 24: {
                            if ((0x7FFFFFE87FFFFFEL & l) == 0L) continue block66;
                            if (kind > 34) {
                                kind = 34;
                            }
                            this.jjCheckNAdd(24);
                            break;
                        }
                        case 27: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(18, 19);
                            break;
                        }
                        case 30: {
                            if ((0x400000004000L & l) == 0L) break;
                            this.jjAddStates(16, 17);
                            break;
                        }
                        case 31: {
                            if ((0x10000000100000L & l) == 0L || kind <= 14) continue block66;
                            kind = 14;
                            break;
                        }
                        case 32: {
                            if ((0x800000008000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 31;
                            break;
                        }
                        case 33: {
                            if ((0x2000000020000L & l) == 0L || kind <= 16) continue block66;
                            kind = 16;
                            break;
                        }
                        case 34: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 33;
                            break;
                        }
                        case 35: {
                            if ((0x8000000080L & l) == 0L) break;
                            this.jjAddStates(14, 15);
                            break;
                        }
                        case 36: {
                            if ((0x10000000100000L & l) == 0L || kind <= 17) continue block66;
                            kind = 17;
                            break;
                        }
                        case 37: {
                            if ((0x2000000020L & l) == 0L || kind <= 19) continue block66;
                            kind = 19;
                            break;
                        }
                        case 38: {
                            if ((0x10000000100000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 37;
                            break;
                        }
                        case 39: {
                            if ((0x100000001000L & l) == 0L) break;
                            this.jjAddStates(12, 13);
                            break;
                        }
                        case 40: {
                            if ((0x10000000100000L & l) == 0L || kind <= 18) continue block66;
                            kind = 18;
                            break;
                        }
                        case 41: {
                            if ((0x2000000020L & l) == 0L || kind <= 20) continue block66;
                            kind = 20;
                            break;
                        }
                        case 42: {
                            if ((0x10000000100000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 41;
                            break;
                        }
                        case 50: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(20, 21);
                            break;
                        }
                    }
                } while (i != startsAt);
            } else {
                int i2 = (this.curChar & 0xFF) >> 6;
                long l2 = 1L << (this.curChar & 0x3F);
                do {
                    int cfr_ignored_0 = this.jjstateSet[--i];
                } while (i != startsAt);
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            if (i == (startsAt = 53 - this.jjnewStateCnt)) break;
            try {
                this.curChar = this.input_stream.readChar();
            }
            catch (IOException i2) {
                // empty catch block
                break;
            }
        }
        if (this.jjmatchedPos > strPos) {
            return curPos;
        }
        int toRet = Math.max(curPos, seenUpto);
        if (curPos < toRet) {
            i = toRet - Math.min(curPos, seenUpto);
            while (i-- > 0) {
                try {
                    this.curChar = this.input_stream.readChar();
                }
                catch (IOException e) {
                    throw new Error("Internal Error : Please send a bug report.");
                }
            }
        }
        if (this.jjmatchedPos < strPos) {
            this.jjmatchedKind = strKind;
            this.jjmatchedPos = strPos;
        } else if (this.jjmatchedPos == strPos && this.jjmatchedKind > strKind) {
            this.jjmatchedKind = strKind;
        }
        return toRet;
    }

    private final int jjStopStringLiteralDfa_1(int pos, long active0) {
        return -1;
    }

    private final int jjStartNfa_1(int pos, long active0) {
        return this.jjMoveNfa_1(this.jjStopStringLiteralDfa_1(pos, active0), pos + 1);
    }

    private final int jjStartNfaWithStates_1(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return pos + 1;
        }
        return this.jjMoveNfa_1(state, pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_1() {
        switch (this.curChar) {
            case '\'': {
                return this.jjStopAtPos(0, 8);
            }
        }
        return this.jjMoveNfa_1(0, 0);
    }

    private final int jjMoveNfa_1(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 3;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            if (++this.jjround == Integer.MAX_VALUE) {
                this.ReInitRounds();
            }
            if (this.curChar < '@') {
                long l = 1L << this.curChar;
                block15: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if (kind <= 10) break;
                            kind = 10;
                            break;
                        }
                        case 1: {
                            if (this.curChar != '\'' || kind <= 10) continue block15;
                            kind = 10;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < '\u0080') {
                long l = 1L << (this.curChar & 0x3F);
                block16: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if (kind > 10) {
                                kind = 10;
                            }
                            if (this.curChar != '\\') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 1;
                            break;
                        }
                        case 1: {
                            if (this.curChar != '\\' || kind <= 10) continue block16;
                            kind = 10;
                            break;
                        }
                        case 2: {
                            if (kind <= 10) break;
                            kind = 10;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else {
                int i2 = (this.curChar & 0xFF) >> 6;
                long l2 = 1L << (this.curChar & 0x3F);
                block17: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if ((jjbitVec0[i2] & l2) == 0L || kind <= 10) continue block17;
                            kind = 10;
                            break;
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            if (i == (startsAt = 3 - this.jjnewStateCnt)) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            }
            catch (IOException e) {
                return curPos;
            }
        }
    }

    private final int jjStopStringLiteralDfa_2(int pos, long active0) {
        return -1;
    }

    private final int jjStartNfa_2(int pos, long active0) {
        return this.jjMoveNfa_2(this.jjStopStringLiteralDfa_2(pos, active0), pos + 1);
    }

    private final int jjStartNfaWithStates_2(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return pos + 1;
        }
        return this.jjMoveNfa_2(state, pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_2() {
        switch (this.curChar) {
            case '\"': {
                return this.jjStopAtPos(0, 9);
            }
        }
        return this.jjMoveNfa_2(0, 0);
    }

    private final int jjMoveNfa_2(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 3;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            if (++this.jjround == Integer.MAX_VALUE) {
                this.ReInitRounds();
            }
            if (this.curChar < '@') {
                long l = 1L << this.curChar;
                block15: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if (kind <= 11) break;
                            kind = 11;
                            break;
                        }
                        case 1: {
                            if (this.curChar != '\"' || kind <= 11) continue block15;
                            kind = 11;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < '\u0080') {
                long l = 1L << (this.curChar & 0x3F);
                block16: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if (kind > 11) {
                                kind = 11;
                            }
                            if (this.curChar != '\\') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 1;
                            break;
                        }
                        case 1: {
                            if (this.curChar != '\\' || kind <= 11) continue block16;
                            kind = 11;
                            break;
                        }
                        case 2: {
                            if (kind <= 11) break;
                            kind = 11;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else {
                int i2 = (this.curChar & 0xFF) >> 6;
                long l2 = 1L << (this.curChar & 0x3F);
                block17: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if ((jjbitVec0[i2] & l2) == 0L || kind <= 11) continue block17;
                            kind = 11;
                            break;
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            if (i == (startsAt = 3 - this.jjnewStateCnt)) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            }
            catch (IOException e) {
                return curPos;
            }
        }
    }

    public ExpressionParserTokenManager(SimpleCharStream stream) {
        this.input_stream = stream;
    }

    public ExpressionParserTokenManager(SimpleCharStream stream, int lexState) {
        this(stream);
        this.SwitchTo(lexState);
    }

    public void ReInit(SimpleCharStream stream) {
        this.jjnewStateCnt = 0;
        this.jjmatchedPos = 0;
        this.curLexState = this.defaultLexState;
        this.input_stream = stream;
        this.ReInitRounds();
    }

    private final void ReInitRounds() {
        this.jjround = -2147483647;
        int i = 53;
        while (i-- > 0) {
            this.jjrounds[i] = Integer.MIN_VALUE;
        }
    }

    public void ReInit(SimpleCharStream stream, int lexState) {
        this.ReInit(stream);
        this.SwitchTo(lexState);
    }

    public void SwitchTo(int lexState) {
        if (lexState >= 3 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
        }
        this.curLexState = lexState;
    }

    private final Token jjFillToken() {
        Token t = Token.newToken(this.jjmatchedKind);
        t.kind = this.jjmatchedKind;
        String im = jjstrLiteralImages[this.jjmatchedKind];
        t.image = im == null ? this.input_stream.GetImage() : im;
        t.beginLine = this.input_stream.getBeginLine();
        t.beginColumn = this.input_stream.getBeginColumn();
        t.endLine = this.input_stream.getEndLine();
        t.endColumn = this.input_stream.getEndColumn();
        return t;
    }

    public final Token getNextToken() {
        Object specialToken = null;
        int curPos = 0;
        block11: while (true) {
            try {
                this.curChar = this.input_stream.BeginToken();
            }
            catch (IOException e) {
                this.jjmatchedKind = 0;
                Token matchedToken = this.jjFillToken();
                return matchedToken;
            }
            this.image = null;
            this.jjimageLen = 0;
            while (true) {
                switch (this.curLexState) {
                    case 0: {
                        this.jjmatchedKind = Integer.MAX_VALUE;
                        this.jjmatchedPos = 0;
                        curPos = this.jjMoveStringLiteralDfa0_0();
                        break;
                    }
                    case 1: {
                        this.jjmatchedKind = Integer.MAX_VALUE;
                        this.jjmatchedPos = 0;
                        curPos = this.jjMoveStringLiteralDfa0_1();
                        break;
                    }
                    case 2: {
                        this.jjmatchedKind = Integer.MAX_VALUE;
                        this.jjmatchedPos = 0;
                        curPos = this.jjMoveStringLiteralDfa0_2();
                    }
                }
                if (this.jjmatchedKind == Integer.MAX_VALUE) break block11;
                if (this.jjmatchedPos + 1 < curPos) {
                    this.input_stream.backup(curPos - this.jjmatchedPos - 1);
                }
                if ((jjtoToken[this.jjmatchedKind >> 6] & 1L << (this.jjmatchedKind & 0x3F)) != 0L) {
                    Token matchedToken = this.jjFillToken();
                    this.TokenLexicalActions(matchedToken);
                    if (jjnewLexState[this.jjmatchedKind] != -1) {
                        this.curLexState = jjnewLexState[this.jjmatchedKind];
                    }
                    return matchedToken;
                }
                if ((jjtoSkip[this.jjmatchedKind >> 6] & 1L << (this.jjmatchedKind & 0x3F)) != 0L) {
                    if (jjnewLexState[this.jjmatchedKind] == -1) continue block11;
                    this.curLexState = jjnewLexState[this.jjmatchedKind];
                    continue block11;
                }
                this.jjimageLen += this.jjmatchedPos + 1;
                if (jjnewLexState[this.jjmatchedKind] != -1) {
                    this.curLexState = jjnewLexState[this.jjmatchedKind];
                }
                curPos = 0;
                this.jjmatchedKind = Integer.MAX_VALUE;
                try {
                    this.curChar = this.input_stream.readChar();
                }
                catch (IOException e) {
                    // empty catch block
                    break block11;
                }
            }
            break;
        }
        int error_line = this.input_stream.getEndLine();
        int error_column = this.input_stream.getEndColumn();
        String error_after = null;
        boolean EOFSeen = false;
        try {
            this.input_stream.readChar();
            this.input_stream.backup(1);
        }
        catch (IOException e1) {
            EOFSeen = true;
            String string = error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
            if (this.curChar == '\n' || this.curChar == '\r') {
                ++error_line;
                error_column = 0;
            }
            ++error_column;
        }
        if (!EOFSeen) {
            this.input_stream.backup(1);
            error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
        }
        throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
    }

    final void TokenLexicalActions(Token matchedToken) {
        switch (this.jjmatchedKind) {
            case 8: {
                if (this.image == null) {
                    this.lengthOfMatch = this.jjmatchedPos + 1;
                    this.image = new StringBuffer(new String(this.input_stream.GetSuffix(this.jjimageLen + this.lengthOfMatch)));
                } else {
                    this.lengthOfMatch = this.jjmatchedPos + 1;
                    this.image.append(new String(this.input_stream.GetSuffix(this.jjimageLen + this.lengthOfMatch)));
                }
                matchedToken.image = this.image.substring(1, this.image.length() - 1);
                break;
            }
            case 9: {
                if (this.image == null) {
                    this.lengthOfMatch = this.jjmatchedPos + 1;
                    this.image = new StringBuffer(new String(this.input_stream.GetSuffix(this.jjimageLen + this.lengthOfMatch)));
                } else {
                    this.lengthOfMatch = this.jjmatchedPos + 1;
                    this.image.append(new String(this.input_stream.GetSuffix(this.jjimageLen + this.lengthOfMatch)));
                }
                matchedToken.image = this.image.substring(1, this.image.length() - 1);
                break;
            }
        }
    }
}

