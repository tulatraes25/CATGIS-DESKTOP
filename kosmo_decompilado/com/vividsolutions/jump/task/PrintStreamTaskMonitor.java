/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.task;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.StringUtil;
import java.io.PrintStream;

public class PrintStreamTaskMonitor
implements TaskMonitor {
    private PrintStream stream;
    boolean isLoggingSubtasks = false;

    public PrintStreamTaskMonitor(PrintStream stream) {
        this.stream = stream;
    }

    public PrintStreamTaskMonitor() {
        this.stream = System.out;
    }

    public void setLoggingSubtasks(boolean isLoggingSubtasks) {
        this.isLoggingSubtasks = isLoggingSubtasks;
    }

    @Override
    public void report(String description) {
        this.stream.println(description);
    }

    @Override
    public void report(Exception exception) {
        this.stream.println(StringUtil.stackTrace(exception));
    }

    @Override
    public void report(int subtasksDone, int totalSubtasks, String subtaskDescription) {
        if (this.isLoggingSubtasks) {
            this.stream.println(String.valueOf(subtasksDone) + " / " + totalSubtasks + " " + subtaskDescription);
        }
    }

    @Override
    public void allowCancellationRequests() {
    }

    @Override
    public boolean isCancelRequested() {
        return false;
    }
}

