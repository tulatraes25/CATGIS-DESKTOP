/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.dbf;

public class FormatedString {
    static boolean leadingzeros = false;

    public static final String format(double d) {
        return FormatedString.format("" + d, 2);
    }

    public static final String format(int i) {
        return FormatedString.format("" + i, 0);
    }

    public static final String format(int i, int len) {
        return FormatedString.format("" + i, 0, len);
    }

    public static final String format(String in) {
        return FormatedString.format(in, 2);
    }

    public static final String format(String in, int dp, int len) {
        StringBuffer sb = new StringBuffer();
        String s = FormatedString.format(in, dp).trim();
        int diff = len - s.length();
        int i = 0;
        while (i < diff) {
            sb.append(" ");
            ++i;
        }
        sb.append(s);
        return sb.toString();
    }

    public static final String format(String in, int dp) {
        int i;
        int e2;
        int e1 = in.indexOf(101);
        int e = Math.max(e1, e2 = in.indexOf(69));
        if (e > -1) {
            in = FormatedString.expand(in, e);
        }
        if ((i = in.lastIndexOf(46)) != -1) {
            String dec = "";
            String num = in.substring(0, i);
            dec = dp > 0 ? (i + dp + 1 < in.length() ? in.substring(i, i + dp + 1) : in.substring(i)) : "";
            while (dec.length() < dp + 1) {
                dec = String.valueOf(dec) + "0";
            }
            if (dp == 0) {
                dec = "";
            }
            if (!leadingzeros) {
                char[] tmp = num.toCharArray();
                i = 0;
                while (i < tmp.length - 1) {
                    if (tmp[i] != '0' && tmp[i] != ' ') break;
                    if (tmp[i] == '0') {
                        tmp[i] = 32;
                    }
                    if (tmp[i + 1] == '.' && tmp[i] == ' ') {
                        tmp[i] = 48;
                    }
                    ++i;
                }
                num = new String(tmp);
            }
            return String.valueOf(num) + dec;
        }
        String dec = ".";
        while (dec.length() < dp + 1) {
            dec = String.valueOf(dec) + "0";
        }
        if (dp == 0) {
            dec = "";
        }
        if (!leadingzeros) {
            char[] tmp = in.toCharArray();
            i = 0;
            while (i < tmp.length - 1) {
                if (tmp[i] != '0' && tmp[i] != ' ') break;
                if (tmp[i] == '0') {
                    tmp[i] = 32;
                }
                if (tmp[i + 1] == '.' && tmp[i] == ' ') {
                    tmp[i] = 48;
                }
                ++i;
            }
            in = new String(tmp);
        }
        return String.valueOf(in) + dec;
    }

    private static final String expand(String s, int e) {
        String last = s.substring(e + 1);
        String start = s.substring(0, e);
        int pow = Integer.parseInt(last);
        int i = start.indexOf(46);
        if (i > 0) {
            int d = start.length() - i - 1;
            String a = start.substring(0, i);
            start = String.valueOf(a) + start.substring(i + 1);
            pow -= d;
        }
        i = 0;
        while (i < pow) {
            start = String.valueOf(start) + "0";
            ++i;
        }
        i = pow;
        while (i < 0) {
            start = "0" + start;
            ++i;
        }
        if (pow < 0) {
            int lp = start.length() + pow;
            start = String.valueOf(start.substring(0, lp)) + "." + start.substring(lp);
        }
        return start.trim();
    }

    public static void main(String[] args) {
        System.out.println(String.valueOf(args[0]) + " " + FormatedString.format(args[0], 6));
    }

    public static final String format(long l) {
        return FormatedString.format("" + l, 0);
    }

    public static final String format(long l, int len) {
        return FormatedString.format("" + l, 0, len);
    }
}

