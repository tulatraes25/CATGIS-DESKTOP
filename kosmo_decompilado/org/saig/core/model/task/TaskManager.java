/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.task;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import es.kosmo.desktop.widgets.task.TaskManagerPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.saig.core.model.task.events.TaskEvent;
import org.saig.core.model.task.events.TaskListener;

public class TaskManager {
    private List<TaskFrame> taskFrameList = new ArrayList<TaskFrame>();
    private TaskManagerPanel frame;
    private List<TaskListener> taskListeners = new ArrayList<TaskListener>();

    public TaskManagerPanel getTaskManagerPanel() {
        if (this.frame == null) {
            this.frame = new TaskManagerPanel();
        }
        return this.frame;
    }

    public void addTask(TaskFrame task) {
        this.taskFrameList.add(task);
        Collections.sort(this.taskFrameList);
        this.getTaskManagerPanel().refresh();
        this.fireTaskAdded(task);
    }

    public void remove(TaskFrame task) {
        this.taskFrameList.remove(task);
        JUMPWorkbench.getFrameInstance().removeInternalFrame(task);
        this.getTaskManagerPanel().refresh();
    }

    public int indexOf(TaskFrame taskFrame) {
        return this.taskFrameList.indexOf(taskFrame);
    }

    public Iterator<TaskFrame> iterator() {
        return this.getTasks().iterator();
    }

    public TaskFrame getTask(String name) {
        Iterator<TaskFrame> i = this.iterator();
        while (i.hasNext()) {
            TaskFrame taskFrame = i.next();
            if (!taskFrame.getName().equals(name)) continue;
            return taskFrame;
        }
        return null;
    }

    public TaskFrame getTask(int index) {
        return this.getTasks().get(index);
    }

    public int size() {
        return this.getTasks().size();
    }

    public List<TaskFrame> getTasks() {
        return this.taskFrameList;
    }

    public void clear() {
        TaskFrame[] task = new TaskFrame[this.taskFrameList.size()];
        this.taskFrameList.toArray(task);
        int i = 0;
        while (i < task.length) {
            task[i].dispose();
            ++i;
        }
        this.taskFrameList.clear();
        this.getTaskManagerPanel().refresh();
        this.taskListeners.clear();
    }

    public void addTaskListener(TaskListener listener) {
        this.taskListeners.add(listener);
    }

    public void removeListener(TaskListener listener) {
        this.taskListeners.remove(listener);
    }

    public void removeAllListeners() {
        this.taskListeners.clear();
    }

    private void fireTaskAdded(TaskFrame task) {
        TaskEvent event = new TaskEvent(task);
        for (TaskListener listener : this.taskListeners) {
            if (listener == null) continue;
            listener.taskCreated(event);
        }
    }
}

