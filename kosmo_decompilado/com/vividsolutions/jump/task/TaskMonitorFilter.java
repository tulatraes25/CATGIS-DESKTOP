/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.task;

import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;

public class TaskMonitorFilter
extends DummyTaskMonitor {
    private TaskMonitor taskMonitor;

    public TaskMonitorFilter(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
    }

    public static TaskMonitor get(TaskMonitor taskMonitor) {
        if (taskMonitor instanceof TaskMonitorFilter) {
            return ((TaskMonitorFilter)taskMonitor).taskMonitor;
        }
        return taskMonitor;
    }
}

