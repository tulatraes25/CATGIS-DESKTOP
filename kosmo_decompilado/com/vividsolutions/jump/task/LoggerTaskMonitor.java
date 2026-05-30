/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.task;

import com.vividsolutions.jump.task.TaskMonitor;
import org.apache.log4j.Logger;

public class LoggerTaskMonitor
implements TaskMonitor {
    private static Logger LOGGER = Logger.getLogger(LoggerTaskMonitor.class);

    @Override
    public void allowCancellationRequests() {
    }

    @Override
    public boolean isCancelRequested() {
        return false;
    }

    @Override
    public void report(String description) {
        LOGGER.info((Object)description);
    }

    @Override
    public void report(int itemsDone, int totalItems, String itemDescription) {
        LOGGER.info((Object)(String.valueOf(itemsDone) + " / " + totalItems + " " + itemDescription));
    }

    @Override
    public void report(Exception exception) {
        LOGGER.error((Object)"", (Throwable)exception);
    }
}

