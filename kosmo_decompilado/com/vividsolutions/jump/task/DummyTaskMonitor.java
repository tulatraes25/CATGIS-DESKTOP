/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.task;

import com.vividsolutions.jump.task.TaskMonitor;

public class DummyTaskMonitor
implements TaskMonitor {
    @Override
    public void report(String description) {
    }

    @Override
    public void report(int subtasksDone, int totalSubtasks, String subtaskDescription) {
    }

    @Override
    public void allowCancellationRequests() {
    }

    @Override
    public boolean isCancelRequested() {
        return false;
    }

    @Override
    public void report(Exception exception) {
    }
}

