/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Fmt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;

public class StringUtil {
    public static int DEFAULT_TOOLTIP_FORMAT_NUM_LINES = 25;
    public static int DEFAULT_TOOLTIP_FORMAT_NUM_CHARS_PER_LINE = 80;
    private static final String DEFAULT_ELEMENT_SEPARATOR = ", ";

    public static String newLine() {
        return System.getProperty("line.separator");
    }

    public static String s(int n) {
        return n != 1 ? "s" : "";
    }

    public static String ies(int n) {
        return n != 1 ? "ies" : "y";
    }

    public static String classNameWithoutQualifiers(String className) {
        return className.substring(Math.max(className.lastIndexOf("."), className.lastIndexOf("$")) + 1);
    }

    public static String classNameWithoutPackageQualifiers(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static String repeat(char c, int n) {
        StringBuffer b = new StringBuffer();
        int i = 0;
        while (i < n) {
            b.append(c);
            ++i;
        }
        return b.toString();
    }

    public static String split(String s, int n) {
        StringBuffer b = new StringBuffer();
        boolean wrapPending = false;
        int i = 0;
        while (i < s.length()) {
            if (i % n == 0 && i > 0) {
                wrapPending = true;
            }
            char c = s.charAt(i);
            if (wrapPending && c == ' ') {
                b.append("\n");
                wrapPending = false;
            } else {
                b.append(c);
            }
            ++i;
        }
        return b.toString();
    }

    public static String capitalize(String word) {
        if (word.length() == 0) {
            return word;
        }
        return String.valueOf(String.valueOf(word.charAt(0)).toUpperCase()) + word.substring(1);
    }

    public static String uncapitalize(String word) {
        if (word.length() == 0) {
            return word;
        }
        return String.valueOf(String.valueOf(word.charAt(0)).toLowerCase()) + word.substring(1);
    }

    public static List<String> fromCommaDelimitedString(String s) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s, ",");
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken().trim());
        }
        return result;
    }

    public static List<String> fromPercentDelimitedString(String s) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s, "%");
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken().trim());
        }
        return result;
    }

    public static List<String> blankStringList(int size) {
        ArrayList<String> list = new ArrayList<String>();
        int i = 0;
        while (i < size) {
            list.add("");
            ++i;
        }
        return list;
    }

    public static String toFriendlyName(String className) {
        return StringUtil.toFriendlyName(className, null);
    }

    public static String friendlyName(Class<?> c) {
        return StringUtil.toFriendlyName(c.getName());
    }

    public static String toFriendlyName(String className, String substringToRemove) {
        String name = className;
        if (substringToRemove != null) {
            name = StringUtil.replaceAll(name, substringToRemove, "");
        }
        name = StringUtil.classNameWithoutQualifiers(name);
        name = StringUtil.insertSpaces(name);
        return name;
    }

    public static String insertSpaces(String s) {
        if (s.length() < 2) {
            return s;
        }
        String result = "";
        int i = 0;
        while (i < s.length() - 2) {
            result = String.valueOf(result) + s.charAt(i);
            if (Character.isLowerCase(s.charAt(i)) && Character.isUpperCase(s.charAt(i + 1)) || Character.isUpperCase(s.charAt(i + 1)) && Character.isLowerCase(s.charAt(i + 2))) {
                result = String.valueOf(result) + " ";
            }
            ++i;
        }
        result = String.valueOf(result) + s.charAt(s.length() - 2);
        result = String.valueOf(result) + s.charAt(s.length() - 1);
        return result.trim();
    }

    public static String toCommaDelimitedString(Collection<?> c) {
        return StringUtil.toDelimitedString(c, DEFAULT_ELEMENT_SEPARATOR);
    }

    public static String toPercentDelimitedString(Collection<Object> c) {
        return StringUtil.toDelimitedString(c, "% ");
    }

    public static String replaceAll(String original, String oldSubstring, String newSubstring) {
        return StringUtil.replace(original, oldSubstring, newSubstring, true);
    }

    public static String replace(String original, String oldSubstring, String newSubstring, boolean all) {
        StringBuffer b = new StringBuffer(original);
        StringUtil.replace(b, oldSubstring, newSubstring, all);
        return b.toString();
    }

    public static void replace(StringBuffer orig, String o, String n, boolean all) {
        if (orig == null || o == null || o.length() == 0 || n == null) {
            throw new IllegalArgumentException(I18N.getString("com.vividsolutions.jump.util.StringUtil.null-or-zero-lenght-string"));
        }
        int i = 0;
        while (i + o.length() <= orig.length()) {
            if (orig.substring(i, i + o.length()).equals(o)) {
                orig.replace(i, i + o.length(), n);
                if (!all) break;
                i += n.length();
                continue;
            }
            ++i;
        }
    }

    public static String stackTrace(Throwable t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        t.printStackTrace(ps);
        return os.toString();
    }

    public static String limitLength(String s, int maxLength) {
        Assert.isTrue((maxLength >= 3 ? 1 : 0) != 0);
        if (s == null) {
            return null;
        }
        if (s.length() > maxLength) {
            return String.valueOf(s.substring(0, maxLength - 3)) + "...";
        }
        return s;
    }

    public static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDate(String token) {
        try {
            DateFormat.getInstance().parse(token);
            return true;
        }
        catch (ParseException e) {
            return false;
        }
    }

    public static String toDelimitedString(Collection<?> c, String delimiter) {
        if (c.isEmpty()) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        for (Object o : c) {
            result.append(String.valueOf(delimiter) + (o == null ? "" : o.toString()));
        }
        return result.substring(delimiter.length());
    }

    public static String toTimeString(long milliseconds) {
        long remainder = milliseconds;
        long days = remainder / 86400000L;
        long hours = (remainder %= 86400000L) / 3600000L;
        long minutes = (remainder %= 3600000L) / 60000L;
        long seconds = (remainder %= 60000L) / 1000L;
        String s = "";
        if (days > 0L) {
            s = String.valueOf(s) + days + " days ";
        }
        s = String.valueOf(s) + Fmt.fmt(hours, 2, 1) + ":" + Fmt.fmt(minutes, 2, 1) + ":" + Fmt.fmt(seconds, 2, 1);
        return s;
    }

    public static String reducePath(String path, int max_path_lenght) {
        File file = new File(path);
        String reducedPath = new String(path);
        if (path.length() > max_path_lenght) {
            String separator = File.separator;
            String p = "...";
            String limited_Path = file.getParent();
            String limited_File = file.getName();
            boolean ok = limited_Path.length() + separator.length() + limited_File.length() < max_path_lenght;
            while (limited_Path.length() > 2 && !ok) {
                int last_sep = limited_Path.lastIndexOf(separator);
                limited_Path = last_sep > 1 ? limited_Path.substring(0, last_sep) : "";
                boolean bl = ok = limited_Path.length() + separator.length() + limited_File.length() < max_path_lenght;
            }
            limited_Path = !ok ? ((limited_Path = file.getParent()).indexOf(separator) == 0 ? limited_Path.substring(0, 5) : limited_Path.substring(0, 3)) : String.valueOf(limited_Path) + separator;
            String final_text = String.valueOf(limited_Path) + p + separator + limited_File;
            int sobrante = final_text.length() - max_path_lenght;
            if (sobrante > 3) {
                final_text = final_text.subSequence(0, final_text.length() - sobrante) + p;
            }
            reducedPath = final_text;
        }
        return reducedPath;
    }

    public static int countMatches(String str, String sub) {
        if (StringUtil.isEmpty(str) || StringUtil.isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            ++count;
            idx += sub.length();
        }
        return count;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String formatTooltip(String toolTip) {
        return StringUtil.formatTooltip(toolTip, DEFAULT_TOOLTIP_FORMAT_NUM_CHARS_PER_LINE, DEFAULT_TOOLTIP_FORMAT_NUM_LINES);
    }

    public static String formatTooltip(String toolTip, int numChar, int numLines) {
        if (StringUtils.isEmpty((String)toolTip)) {
            return "";
        }
        StringBuilder htmlToolTipSB = new StringBuilder("");
        toolTip = toolTip.replaceAll("(?i)<br>", "\n");
        toolTip = toolTip.replaceAll("(?i)<pre>", "<PRE>");
        toolTip = toolTip.replaceAll("(?i)</pre>", "</PRE>");
        int firstPRE = StringUtils.indexOf((String)toolTip, (String)"<PRE>");
        int lastPRE = StringUtils.lastIndexOf((String)toolTip, (String)"</PRE>");
        if (!StringUtils.startsWithIgnoreCase((String)toolTip, (String)"<HTML>")) {
            htmlToolTipSB.append("<HTML>");
        }
        boolean addNewLine = false;
        int offset = 0;
        int lineCont = 0;
        int i = 0;
        i = 0;
        while (i < toolTip.length() && lineCont < numLines) {
            Character currentChar = Character.valueOf(toolTip.charAt(i));
            if (addNewLine && Character.isWhitespace(currentChar.charValue())) {
                offset = i % numChar;
                htmlToolTipSB.append("<BR>");
                ++lineCont;
                addNewLine = false;
            } else if (currentChar.charValue() == '\n') {
                offset = i % numChar;
                if (i <= firstPRE || i >= lastPRE) {
                    htmlToolTipSB.append("<BR>");
                } else {
                    htmlToolTipSB.append(currentChar);
                }
                ++lineCont;
                addNewLine = false;
            } else {
                htmlToolTipSB.append(currentChar);
            }
            if ((i + 1 - offset) % numChar == 0) {
                addNewLine = true;
            }
            ++i;
        }
        if (i > firstPRE && i < lastPRE) {
            htmlToolTipSB.append("</PRE>");
        }
        if (lineCont == numLines && i < toolTip.length()) {
            htmlToolTipSB.append("<B></I>...</I></B>");
        }
        if (!StringUtils.endsWithIgnoreCase((String)htmlToolTipSB.toString(), (String)"</HTML>")) {
            htmlToolTipSB.append("</HTML>");
        }
        return htmlToolTipSB.toString();
    }

    public static String formatDouble(double d, int nDecimales) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(nDecimales);
        return format.format(d);
    }

    public static float[] floatArrayFromString(String floatString) {
        StringTokenizer tok = new StringTokenizer(floatString, DEFAULT_ELEMENT_SEPARATOR);
        ArrayList<Float> fList = new ArrayList<Float>();
        while (tok.hasMoreTokens()) {
            String sf = tok.nextToken();
            fList.add(Float.valueOf(Float.parseFloat(sf)));
        }
        float[] floats = new float[fList.size()];
        int i = 0;
        for (Float f : fList) {
            floats[i++] = f.floatValue();
        }
        return floats;
    }

    public static String join(float[] floats) {
        return StringUtil.join(floats, DEFAULT_ELEMENT_SEPARATOR);
    }

    public static String join(float[] floats, String separator) {
        int len = floats.length;
        if (len == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        float[] fArray = floats;
        int n = floats.length;
        int n2 = 0;
        while (n2 < n) {
            float f = fArray[n2];
            result.append("" + f);
            result.append(separator);
            ++n2;
        }
        String strResult = result.toString();
        return strResult.substring(0, strResult.length() - 1);
    }

    public static String trimPreviousChars(String cadena, char charToTrim) {
        if (cadena == null) {
            return null;
        }
        int i = 0;
        while (i < cadena.length()) {
            if (cadena.charAt(i) != charToTrim) break;
            ++i;
        }
        return cadena.substring(i);
    }
}

