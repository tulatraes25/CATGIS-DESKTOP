/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

public class Fmt {
    public static final int ZF = 1;
    public static final int LJ = 2;
    public static final int HX = 4;
    public static final int OC = 8;
    private static final int WN = 16;

    public static String fmt(byte b) {
        return Fmt.fmt(b, 0, 0);
    }

    public static String fmt(byte b, int minWidth) {
        return Fmt.fmt(b, minWidth, 0);
    }

    public static String fmt(byte b, int minWidth, int flags) {
        boolean octal;
        boolean hexadecimal = (flags & 4) != 0;
        boolean bl = octal = (flags & 8) != 0;
        if (hexadecimal) {
            return Fmt.fmt(Integer.toString(b & 0xFF, 16), minWidth, flags | 0x10);
        }
        if (octal) {
            return Fmt.fmt(Integer.toString(b & 0xFF, 8), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Integer.toString(b & 0xFF), minWidth, flags | 0x10);
    }

    public static String fmt(short s) {
        return Fmt.fmt(s, 0, 0);
    }

    public static String fmt(short s, int minWidth) {
        return Fmt.fmt(s, minWidth, 0);
    }

    public static String fmt(short s, int minWidth, int flags) {
        boolean octal;
        boolean hexadecimal = (flags & 4) != 0;
        boolean bl = octal = (flags & 8) != 0;
        if (hexadecimal) {
            return Fmt.fmt(Integer.toString(s & 0xFFFF, 16), minWidth, flags | 0x10);
        }
        if (octal) {
            return Fmt.fmt(Integer.toString(s & 0xFFFF, 8), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Integer.toString(s), minWidth, flags | 0x10);
    }

    public static String fmt(int i) {
        return Fmt.fmt(i, 0, 0);
    }

    public static String fmt(int i, int minWidth) {
        return Fmt.fmt(i, minWidth, 0);
    }

    public static String fmt(int i, int minWidth, int flags) {
        boolean octal;
        boolean hexadecimal = (flags & 4) != 0;
        boolean bl = octal = (flags & 8) != 0;
        if (hexadecimal) {
            return Fmt.fmt(Long.toString((long)i & 0xFFFFFFFFL, 16), minWidth, flags | 0x10);
        }
        if (octal) {
            return Fmt.fmt(Long.toString((long)i & 0xFFFFFFFFL, 8), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Integer.toString(i), minWidth, flags | 0x10);
    }

    public static String fmt(long l) {
        return Fmt.fmt(l, 0, 0);
    }

    public static String fmt(long l, int minWidth) {
        return Fmt.fmt(l, minWidth, 0);
    }

    public static String fmt(long l, int minWidth, int flags) {
        boolean octal;
        boolean hexadecimal = (flags & 4) != 0;
        boolean bl = octal = (flags & 8) != 0;
        if (hexadecimal) {
            if ((l & 0xF000000000000000L) != 0L) {
                return Fmt.fmt(String.valueOf(Long.toString(l >>> 60, 16)) + Fmt.fmt(l & 0xFFFFFFFFFFFFFFFL, 15, 5), minWidth, flags | 0x10);
            }
            return Fmt.fmt(Long.toString(l, 16), minWidth, flags | 0x10);
        }
        if (octal) {
            if ((l & Long.MIN_VALUE) != 0L) {
                return Fmt.fmt(String.valueOf(Long.toString(l >>> 63, 8)) + Fmt.fmt(l & Long.MAX_VALUE, 21, 9), minWidth, flags | 0x10);
            }
            return Fmt.fmt(Long.toString(l, 8), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Long.toString(l), minWidth, flags | 0x10);
    }

    public static String fmt(float f) {
        return Fmt.fmt(f, 0, 0, 0);
    }

    public static String fmt(float f, int minWidth) {
        return Fmt.fmt(f, minWidth, 0, 0);
    }

    public static String fmt(float f, int minWidth, int sigFigs) {
        return Fmt.fmt(f, minWidth, sigFigs, 0);
    }

    public static String fmt(float f, int minWidth, int sigFigs, int flags) {
        if (sigFigs != 0) {
            return Fmt.fmt(Fmt.sigFigFix(Float.toString(f), sigFigs), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Float.toString(f), minWidth, flags | 0x10);
    }

    public static String fmt(double d) {
        return Fmt.fmt(d, 0, 0, 0);
    }

    public static String fmt(double d, int minWidth) {
        return Fmt.fmt(d, minWidth, 0, 0);
    }

    public static String fmt(double d, int minWidth, int sigFigs) {
        return Fmt.fmt(d, minWidth, sigFigs, 0);
    }

    public static String fmt(double d, int minWidth, int sigFigs, int flags) {
        if (sigFigs != 0) {
            return Fmt.fmt(Fmt.sigFigFix(Fmt.doubleToString(d), sigFigs), minWidth, flags | 0x10);
        }
        return Fmt.fmt(Fmt.doubleToString(d), minWidth, flags | 0x10);
    }

    public static String fmt(char c) {
        return Fmt.fmt(c, 0, 0);
    }

    public static String fmt(char c, int minWidth) {
        return Fmt.fmt(c, minWidth, 0);
    }

    public static String fmt(char c, int minWidth, int flags) {
        return Fmt.fmt(new Character(c).toString(), minWidth, flags);
    }

    public static String fmt(Object o) {
        return Fmt.fmt(o, 0, 0);
    }

    public static String fmt(Object o, int minWidth) {
        return Fmt.fmt(o, minWidth, 0);
    }

    public static String fmt(Object o, int minWidth, int flags) {
        return Fmt.fmt(o.toString(), minWidth, flags);
    }

    public static String fmt(String s) {
        return Fmt.fmt(s, 0, 0);
    }

    public static String fmt(String s, int minWidth) {
        return Fmt.fmt(s, minWidth, 0);
    }

    public static String fmt(String s, int minWidth, int flags) {
        boolean wasNumber;
        int len = s.length();
        boolean zeroFill = (flags & 1) != 0;
        boolean leftJustify = (flags & 2) != 0;
        boolean hexadecimal = (flags & 4) != 0;
        boolean octal = (flags & 8) != 0;
        boolean bl = wasNumber = (flags & 0x10) != 0;
        if ((hexadecimal || octal || zeroFill) && !wasNumber) {
            throw new InternalError("Acme.Fmt: number flag on a non-number");
        }
        if (zeroFill && leftJustify) {
            throw new InternalError("Acme.Fmt: zero-fill left-justify is silly");
        }
        if (hexadecimal && octal) {
            throw new InternalError("Acme.Fmt: can't do both hex and octal");
        }
        if (len >= minWidth) {
            return s;
        }
        int fillWidth = minWidth - len;
        StringBuffer fill = new StringBuffer(fillWidth);
        int i = 0;
        while (i < fillWidth) {
            if (zeroFill) {
                fill.append('0');
            } else {
                fill.append(' ');
            }
            ++i;
        }
        if (leftJustify) {
            return String.valueOf(s) + fill;
        }
        if (zeroFill && s.startsWith("-")) {
            return "-" + fill + s.substring(1);
        }
        return fill + s;
    }

    private static String sigFigFix(String s, int sigFigs) {
        int mantFigs;
        StringBuffer fraction;
        StringBuffer number;
        String exponent;
        String mantissa;
        String unsigned;
        String sign;
        if (s.startsWith("-") || s.startsWith("+")) {
            sign = s.substring(0, 1);
            unsigned = s.substring(1);
        } else {
            sign = "";
            unsigned = s;
        }
        int eInd = unsigned.indexOf(101);
        if (eInd == -1) {
            eInd = unsigned.indexOf(69);
        }
        if (eInd == -1) {
            mantissa = unsigned;
            exponent = "";
        } else {
            mantissa = unsigned.substring(0, eInd);
            exponent = unsigned.substring(eInd);
        }
        int dotInd = mantissa.indexOf(46);
        if (dotInd == -1) {
            number = new StringBuffer(mantissa);
            fraction = new StringBuffer("");
        } else {
            number = new StringBuffer(mantissa.substring(0, dotInd));
            fraction = new StringBuffer(mantissa.substring(dotInd + 1));
        }
        int numFigs = number.length();
        int fracFigs = fraction.length();
        if ((numFigs == 0 || number.toString().equals("0")) && fracFigs > 0) {
            numFigs = 0;
            int i = 0;
            while (i < fraction.length()) {
                if (fraction.charAt(i) != '0') break;
                --fracFigs;
                ++i;
            }
        }
        if (sigFigs > (mantFigs = numFigs + fracFigs)) {
            int i = mantFigs;
            while (i < sigFigs) {
                fraction.append('0');
                ++i;
            }
        } else if (sigFigs < mantFigs && sigFigs >= numFigs) {
            fraction.setLength(fraction.length() - (fracFigs - (sigFigs - numFigs)));
        } else if (sigFigs < numFigs) {
            fraction.setLength(0);
            int i = sigFigs;
            while (i < numFigs) {
                number.setCharAt(i, '0');
                ++i;
            }
        }
        if (fraction.length() == 0) {
            return String.valueOf(sign) + number + exponent;
        }
        return String.valueOf(sign) + number + "." + fraction + exponent;
    }

    public static String doubleToString(double d) {
        int exp;
        String expStr;
        String mantStr;
        String unsStr;
        int eInd;
        if (Double.isNaN(d)) {
            return "NaN";
        }
        if (d == Double.NEGATIVE_INFINITY) {
            return "-Inf";
        }
        if (d == Double.POSITIVE_INFINITY) {
            return "Inf";
        }
        boolean negative = false;
        if (d < 0.0) {
            negative = true;
            d = -d;
        }
        if ((eInd = (unsStr = Double.toString(d)).indexOf(101)) == -1) {
            eInd = unsStr.indexOf(69);
        }
        if (eInd == -1) {
            mantStr = unsStr;
            expStr = "";
            exp = 0;
        } else {
            mantStr = unsStr.substring(0, eInd);
            expStr = unsStr.substring(eInd + 1);
            exp = expStr.startsWith("+") ? Integer.parseInt(expStr.substring(1)) : Integer.parseInt(expStr);
        }
        int dotInd = mantStr.indexOf(46);
        String numStr = dotInd == -1 ? mantStr : mantStr.substring(0, dotInd);
        long num = numStr.length() == 0 ? 0L : (long)Integer.parseInt(numStr);
        StringBuffer newMantBuf = new StringBuffer(String.valueOf(numStr) + ".");
        double p = Math.pow(10.0, exp);
        double frac = d - (double)num * p;
        String digits = "0123456789";
        int nDigits = 16 - numStr.length();
        int i = 0;
        while (i < nDigits) {
            int dig = (int)(frac / (p /= 10.0));
            if (dig < 0) {
                dig = 0;
            }
            if (dig > 9) {
                dig = 9;
            }
            newMantBuf.append(digits.charAt(dig));
            frac -= (double)dig * p;
            ++i;
        }
        if ((int)(frac / p + 0.5) == 1) {
            boolean roundMore = true;
            int i2 = newMantBuf.length() - 1;
            while (i2 >= 0) {
                int dig = digits.indexOf(newMantBuf.charAt(i2));
                if (dig != -1) {
                    if (++dig == 10) {
                        newMantBuf.setCharAt(i2, '0');
                    } else {
                        newMantBuf.setCharAt(i2, digits.charAt(dig));
                        roundMore = false;
                        break;
                    }
                }
                --i2;
            }
            if (roundMore) {
                newMantBuf.append("ROUNDMORE");
            }
        }
        int len = newMantBuf.length();
        while (newMantBuf.charAt(len - 1) == '0') {
            newMantBuf.setLength(--len);
        }
        if (newMantBuf.charAt(len - 1) == '.') {
            newMantBuf.setLength(--len);
        }
        return String.valueOf(negative ? "-" : "") + newMantBuf + (expStr.length() != 0 ? "e" + expStr : "");
    }
}

