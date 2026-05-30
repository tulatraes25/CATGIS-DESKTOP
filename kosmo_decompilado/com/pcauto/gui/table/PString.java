/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import java.text.ParseException;
import org.saig.jump.lang.I18N;

public class PString {
    public static String alignLeft(String value, int width, char fillChar) {
        int len = 0;
        if (value != null) {
            len = value.trim().length();
        }
        if (len > width) {
            return value.substring(0, len - (len - width));
        }
        String newValue = len > 0 ? value.trim() : "";
        int i = 0;
        while (i < width - len) {
            newValue = String.valueOf(newValue) + fillChar;
            ++i;
        }
        return newValue;
    }

    public static String alignRight(String value, int width, char fillChar) {
        int len = 0;
        if (value != null) {
            len = value.trim().length();
        }
        if (len > width) {
            return value.substring(len - width);
        }
        String newValue = "";
        int i = 0;
        while (i < width - len) {
            newValue = String.valueOf(newValue) + fillChar;
            ++i;
        }
        if (len == 0) {
            return newValue;
        }
        return newValue.concat(value.trim());
    }

    public static String center(String value, int width, char fillChar, boolean roundLeft) {
        int len = 0;
        if (value != null) {
            len = value.trim().length();
        }
        if (len > width) {
            return value.substring(len - width);
        }
        int limit = (width - len) / 2;
        String newValue = "";
        int i = 0;
        while (i < limit) {
            newValue = String.valueOf(newValue) + fillChar;
            ++i;
        }
        if ((width - len) % 2 != 0 && !roundLeft) {
            newValue = String.valueOf(newValue) + fillChar;
        }
        if (len != 0) {
            newValue = String.valueOf(newValue) + value;
        }
        i = 0;
        while (i < limit) {
            newValue = String.valueOf(newValue) + fillChar;
            ++i;
        }
        if ((width - len) % 2 == 1 && roundLeft) {
            newValue = String.valueOf(newValue) + fillChar;
        }
        return newValue;
    }

    public static String center(String value, int width, char fillChar) {
        return PString.center(value, width, fillChar, true);
    }

    public static boolean areEqual(String val1, String val2) {
        if (val1 == null) {
            return val2 == null;
        }
        return val1.equals(val2);
    }

    public static String parseEscapes(String input) throws ParseException {
        String output = "";
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) != '\\') {
                output = String.valueOf(output) + input.charAt(i);
            } else {
                char c = input.charAt(++i);
                switch (c) {
                    case 'u': {
                        try {
                            int unicode = Integer.parseInt(input.substring(++i, i + 4), 16);
                            output = String.valueOf(output) + (char)unicode;
                            i += 3;
                            break;
                        }
                        catch (NumberFormatException nfex) {
                            throw new ParseException(I18N.getMessage("com.pcauto.gui.table.PString.parse-failed-{0}", new Object[]{nfex.getMessage()}), i);
                        }
                    }
                    case 'b': {
                        output = String.valueOf(output) + '\b';
                        break;
                    }
                    case 't': {
                        output = String.valueOf(output) + '\t';
                        break;
                    }
                    case 'n': {
                        output = String.valueOf(output) + '\n';
                        break;
                    }
                    case 'f': {
                        output = String.valueOf(output) + '\f';
                        break;
                    }
                    case 'r': {
                        output = String.valueOf(output) + '\r';
                        break;
                    }
                    case '\"': 
                    case '\'': 
                    case '\\': {
                        output = String.valueOf(output) + c;
                        break;
                    }
                    default: {
                        try {
                            int j = 0;
                            while (j < 3) {
                                if (input.charAt(i + j) < '0' || input.charAt(i + j) > '9') break;
                                ++j;
                            }
                            int octal = Integer.parseInt(input.substring(i, i + j), 8);
                            output = String.valueOf(output) + (char)octal;
                            i += j - 1;
                            break;
                        }
                        catch (NumberFormatException nfex) {
                            throw new ParseException(I18N.getMessage("com.pcauto.gui.table.PString.parse-failure-illegal-escape-encountered-{0}", new Object[]{Character.valueOf(c)}), i - 1);
                        }
                    }
                }
            }
            ++i;
        }
        return output;
    }
}

