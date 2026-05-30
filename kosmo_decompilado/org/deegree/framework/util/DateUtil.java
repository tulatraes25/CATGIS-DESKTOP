/*
 * Decompiled with CFR 0.152.
 */
package org.deegree.framework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {
    public static final String ISO_8601_2004_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String ISO_8601_2004_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String JDBC_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static Calendar parseISO8601Date(String dateString) throws ParseException {
        String dayOfMonth;
        String regex = "^(\\d{4})-?(([wW]([012345]\\d)-?([1234567])?)|(([01]\\d)(-([0123]\\d))?)|([01]\\d)([0123]\\d)|([0123]\\d\\d))?(T([012]\\d):?([012345]\\d)?(:?([012345]\\d)(.(\\d{1,3}))?)?)?((Z)|(([+-])(\\d{2})):?(\\d{2})?)?$";
        Pattern pattern = Pattern.compile("^(\\d{4})-?(([wW]([012345]\\d)-?([1234567])?)|(([01]\\d)(-([0123]\\d))?)|([01]\\d)([0123]\\d)|([0123]\\d\\d))?(T([012]\\d):?([012345]\\d)?(:?([012345]\\d)(.(\\d{1,3}))?)?)?((Z)|(([+-])(\\d{2})):?(\\d{2})?)?$");
        Matcher matcher = pattern.matcher(dateString);
        if (!matcher.matches()) {
            throw new ParseException("error while parsing iso8601 date: " + dateString, 0);
        }
        String year = matcher.group(1);
        String week = matcher.group(4);
        String dayOfWeek = matcher.group(5);
        String month = matcher.group(7);
        if (month == null) {
            month = matcher.group(10);
        }
        if ((dayOfMonth = matcher.group(9)) == null) {
            dayOfMonth = matcher.group(11);
        }
        String dayOfYear = matcher.group(12);
        String hourOfDay = matcher.group(14);
        String minutesOfHour = matcher.group(15);
        String seconds = matcher.group(17);
        String milliseconds = matcher.group(19);
        String timeZoneSign = matcher.group(23);
        String timeZoneHour = matcher.group(24);
        String timeZoneMinutes = matcher.group(25);
        boolean localeTime = true;
        if (timeZoneHour != null) {
            localeTime = false;
        }
        if (matcher.group(21) != null) {
            localeTime = false;
            timeZoneHour = "00";
            timeZoneMinutes = "00";
        }
        Calendar calendar = localeTime ? Calendar.getInstance() : Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.clear();
        if (year != null) {
            calendar.set(1, Integer.parseInt(year));
        }
        if (month != null) {
            calendar.set(2, Integer.parseInt(month) - 1);
            if (dayOfMonth != null) {
                calendar.set(5, Integer.parseInt(dayOfMonth));
            }
        } else if (week != null) {
            calendar.set(3, Integer.parseInt(week));
            if (dayOfWeek != null) {
                calendar.set(7, Integer.parseInt(dayOfWeek));
            }
        } else if (dayOfYear != null) {
            calendar.set(6, Integer.parseInt(dayOfYear));
        }
        if (hourOfDay != null) {
            calendar.set(11, Integer.parseInt(hourOfDay));
        }
        if (minutesOfHour != null) {
            calendar.set(12, Integer.parseInt(minutesOfHour));
        }
        if (seconds != null) {
            calendar.set(13, Integer.parseInt(seconds));
        }
        if (milliseconds != null) {
            calendar.set(14, Integer.parseInt(milliseconds));
        }
        if (timeZoneHour != null) {
            int zoneOffsetInMillis = Integer.parseInt(timeZoneHour) * 60 * 60 * 1000;
            if ("-".equals(timeZoneSign)) {
                zoneOffsetInMillis *= -1;
            }
            if (timeZoneMinutes != null) {
                int minuteOffsetInMillis = Integer.parseInt(timeZoneMinutes) * 60 * 1000;
                zoneOffsetInMillis = zoneOffsetInMillis < 0 ? (zoneOffsetInMillis -= minuteOffsetInMillis) : (zoneOffsetInMillis += minuteOffsetInMillis);
            }
            calendar.set(15, zoneOffsetInMillis);
        }
        Calendar result = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        result.setTime(calendar.getTime());
        return result;
    }

    public static String formatISO8601Date(Calendar date) {
        if (date.getTimeZone().getRawOffset() == 0) {
            return new SimpleDateFormat(ISO_8601_2004_FORMAT_UTC).format(date.getTime());
        }
        return DateUtil.formatISO8601Date(date.getTime());
    }

    public static String formatISO8601Date(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(ISO_8601_2004_FORMAT).format(date);
    }

    public static String formatJDBCTimeStamp(Date date) {
        return new SimpleDateFormat(JDBC_TIMESTAMP_FORMAT).format(date);
    }

    public static String formatLocaleDate(Date date) {
        return DateUtil.formatLocaleDate(date, Locale.getDefault());
    }

    public static String formatLocaleDate(Date date, Locale locale) {
        return DateFormat.getDateTimeInstance(2, 1, locale).format(date);
    }

    private DateUtil() {
    }
}

