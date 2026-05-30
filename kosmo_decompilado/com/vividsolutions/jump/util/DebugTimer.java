/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Stopwatch
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Stopwatch;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class DebugTimer {
    private static final Logger LOGGER = Logger.getLogger(DebugTimer.class);
    private static final int TIME_LEN = 10;
    private static DebugTimer timer = new DebugTimer();
    private Stopwatch sw = new Stopwatch();
    private String blankStr;

    public DebugTimer() {
        this.sw.start();
        this.blankStr = this.fillString(10, ' ');
    }

    public static void startStatic(String msg) {
        timer.start(msg);
    }

    public static void logEventStatic(String msg) {
        timer.logEvent(msg);
    }

    public void start(String msg) {
        LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.util.DebugTimer.started")) + msg));
        this.sw.start();
    }

    public void logEvent(String msg) {
        String elapsedStr = this.formatTime(this.sw.getTimeString());
        LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.util.DebugTimer.elapsed")) + elapsedStr + "    " + msg));
        this.sw.start();
    }

    public String formatTime(String timeStr) {
        if (timeStr.length() < 10) {
            String filled = String.valueOf(this.blankStr) + timeStr;
            int start = filled.length() - 10;
            return filled.substring(start);
        }
        return timeStr;
    }

    public String fillString(int len, char ch) {
        StringBuffer buf = new StringBuffer(len);
        int i = 0;
        while (i < len) {
            buf.append(ch);
            ++i;
        }
        return buf.toString();
    }
}

