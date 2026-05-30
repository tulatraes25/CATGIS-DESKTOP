/*
 * Decompiled with CFR 0.152.
 */
package org.cresques.px.dxf;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DxfGroup {
    private static final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat[] decimalFormats = new DecimalFormat[]{new DecimalFormat("#0", dfs), new DecimalFormat("#0.0", dfs), new DecimalFormat("#0.00", dfs), new DecimalFormat("#0.000", dfs), new DecimalFormat("#0.0000", dfs), new DecimalFormat("#0.00000", dfs), new DecimalFormat("#0.000000", dfs), new DecimalFormat("#0.0000000", dfs), new DecimalFormat("#0.00000000", dfs), new DecimalFormat("#0.000000000", dfs), new DecimalFormat("#0.0000000000", dfs), new DecimalFormat("#0.00000000000", dfs), new DecimalFormat("#0.000000000000", dfs)};
    int code;
    Object data;

    public DxfGroup() {
        this.code = -1;
        this.data = null;
    }

    public DxfGroup(int code, String data) {
        this.code = code;
        this.data = data;
    }

    public static DxfGroup read(BufferedReader fi) throws NumberFormatException, IOException {
        DxfGroup grp = null;
        String txt = fi.readLine();
        if (txt != null && !txt.equals("")) {
            grp = new DxfGroup();
            grp.code = Integer.parseInt(txt.trim());
            grp.readData(fi);
        }
        return grp;
    }

    public int getCode() {
        return this.code;
    }

    public Object getData() {
        return this.data;
    }

    private void readData(BufferedReader fi) throws IOException {
        String txt = fi.readLine().trim();
        if (this.code >= 0 && this.code <= 9) {
            this.data = txt;
        } else if (10 <= this.code && this.code <= 59) {
            this.data = new Double(Double.parseDouble(txt));
        } else if (60 <= this.code && this.code <= 79) {
            try {
                this.data = new Integer(Integer.parseInt(txt));
            }
            catch (NumberFormatException e) {
                this.data = new Integer((int)Double.parseDouble(txt));
            }
        } else if (90 <= this.code && this.code <= 99) {
            this.data = new Integer(Integer.parseInt(txt));
        } else if (this.code == 100) {
            this.data = txt;
        } else if (this.code != 102) {
            if (this.code == 105) {
                this.data = txt;
            } else if (110 <= this.code && this.code <= 139) {
                this.data = new Double(Double.parseDouble(txt));
            } else if (140 <= this.code && this.code <= 149) {
                this.data = new Double(Double.parseDouble(txt));
            } else if (170 <= this.code && this.code <= 179) {
                this.data = new Integer(Integer.parseInt(txt));
            } else if (210 <= this.code && this.code <= 239) {
                this.data = new Double(Double.parseDouble(txt));
            } else if (270 <= this.code && this.code <= 279) {
                this.data = new Integer(Integer.parseInt(txt));
            } else if (280 <= this.code && this.code <= 289) {
                this.data = new Integer(Integer.parseInt(txt));
            } else if (290 <= this.code && this.code <= 299) {
                this.data = new Boolean(Boolean.getBoolean(txt));
            } else if (300 <= this.code && this.code <= 309) {
                this.data = txt;
            } else if (!(310 <= this.code && this.code <= 319 || 320 <= this.code && this.code <= 329 || 330 <= this.code && this.code <= 369)) {
                if (370 <= this.code && this.code <= 379) {
                    this.data = new Integer(Integer.parseInt(txt));
                } else if (380 <= this.code && this.code <= 389) {
                    this.data = new Integer(Integer.parseInt(txt));
                } else if (390 <= this.code && this.code <= 399) {
                    this.data = txt;
                } else if (400 <= this.code && this.code <= 409) {
                    this.data = new Integer(Integer.parseInt(txt));
                } else if (410 <= this.code && this.code <= 419) {
                    this.data = txt;
                } else if (this.code == 999) {
                    this.data = txt;
                } else if (1000 <= this.code && this.code <= 1009) {
                    this.data = txt;
                } else if (1010 <= this.code && this.code <= 1059) {
                    this.data = new Double(Double.parseDouble(txt));
                } else if (1060 <= this.code && this.code <= 1070) {
                    this.data = new Integer(Integer.parseInt(txt));
                } else if (this.code == 1071) {
                    this.data = new Integer(Integer.parseInt(txt));
                } else {
                    throw new IOException("DxfReader: c\u00f3digo " + this.code + " desconocido.");
                }
            }
        }
    }

    public boolean equals(int c, String s) {
        return c == this.code && s.compareTo((String)this.data) == 0;
    }

    public static String int34car(int code) {
        if (code < 10) {
            return "  " + Integer.toString(code);
        }
        if (code < 100) {
            return " " + Integer.toString(code);
        }
        return Integer.toString(code);
    }

    public static String int6car(int value) {
        String s = "     " + Integer.toString(value);
        return s.substring(s.length() - 6, s.length());
    }

    public static String toString(int code, String value) {
        return String.valueOf(DxfGroup.int34car(code)) + "\r\n" + value + "\r\n";
    }

    public static String toString(int code, int value) {
        return String.valueOf(DxfGroup.int34car(code)) + "\r\n" + DxfGroup.int6car(value) + "\r\n";
    }

    public static String toString(int code, float value, int decimalPartLength) {
        return String.valueOf(DxfGroup.int34car(code)) + "\r\n" + decimalFormats[decimalPartLength].format(value) + "\r\n";
    }

    public static String toString(int code, double value, int decimalPartLength) {
        return String.valueOf(DxfGroup.int34car(code)) + "\r\n" + decimalFormats[decimalPartLength].format(value) + "\r\n";
    }

    public static String toString(int code, Object value) {
        if (value instanceof String) {
            return DxfGroup.toString(code, (String)value);
        }
        if (value instanceof Integer) {
            return DxfGroup.toString(code, (Integer)value);
        }
        if (value instanceof Double) {
            return DxfGroup.toString(code, ((Double)value).floatValue(), 3);
        }
        if (value instanceof Double) {
            return DxfGroup.toString(code, (Double)value, 6);
        }
        return DxfGroup.toString(code, value.toString());
    }

    public String toString() {
        return DxfGroup.toString(this.code, this.data);
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

