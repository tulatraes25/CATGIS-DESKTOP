/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatManager {
    private static DateFormat localeDateFormat = DateFormat.getDateInstance();
    private static DateFormat localeTimeDateFormat = DateFormat.getDateTimeInstance();
    private static DateFormat jdbcTimestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    private static DateFormat defaultDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static DateFormat defaultDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private DateFormatManager() {
    }

    public static DateFormat getDateFormat() {
        return localeDateFormat;
    }

    public static DateFormat getDateFormat(Locale loc) {
        return DateFormat.getDateInstance(3, loc);
    }

    public static DateFormat getDateTimeFormat() {
        return localeTimeDateFormat;
    }

    public static DateFormat getDateTimeFormat(Locale loc) {
        return DateFormat.getDateTimeInstance(3, 2, loc);
    }

    public static DateFormat getJDBCTimestampFormatter() {
        return jdbcTimestampFormatter;
    }

    public static DateFormat getDefaultDateFormat() {
        return defaultDateFormat;
    }

    public static DateFormat getDefaultDateTimeFormat() {
        return defaultDateTimeFormat;
    }
}

