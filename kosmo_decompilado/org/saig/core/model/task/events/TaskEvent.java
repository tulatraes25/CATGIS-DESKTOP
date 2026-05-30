/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.task.events;

import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class TaskEvent {
    TaskFrame task;

    public TaskEvent(TaskFrame task) {
        this.task = task;
    }

    public TaskFrame getTask() {
        return this.task;
    }

    public void setTaskFrame(TaskFrame task) {
        this.task = task;
    }
}

