/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf.utils;

import java.util.Date;
import java.util.GregorianCalendar;

public class JulianDate {
    public static double toJulian(Date dt) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(dt);
        int month = calendar.get(2) + 1;
        int year = calendar.get(1);
        int day = calendar.get(5);
        double a = Math.floor((14 - month) / 12);
        double y = (double)(year + 4800) - a;
        double m = (double)month + 12.0 * a - 3.0;
        double jdn = (double)day + Math.floor((153.0 * m + 2.0) / 5.0) + 365.0 * y + Math.floor(y / 4.0) - Math.floor(y / 100.0) + Math.floor(y / 400.0) - 32045.0;
        double hour = calendar.get(11);
        double minutes = calendar.get(12);
        double seconds = calendar.get(13);
        double jd = jdn + (hour - 12.0) / 24.0 + minutes / 1440.0 + seconds / 86400.0;
        return jd;
    }
}

