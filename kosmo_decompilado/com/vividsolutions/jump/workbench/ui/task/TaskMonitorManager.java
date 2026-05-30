/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.task;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Date;
import javax.swing.Timer;

public class TaskMonitorManager {
    public void execute(ThreadedPlugIn plugIn, PlugInContext context) {
        TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)context.getWorkbenchFrame(), context.getErrorHandler());
        progressDialog.setTitle(plugIn.getName());
        final TaskWrapper taskWrapper = new TaskWrapper(plugIn, context, progressDialog);
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(taskWrapper).start();
            }
        });
        GUIUtil.centreOnWindow(progressDialog);
        Timer timer = this.timer(new Date(), plugIn, context.getWorkbenchFrame());
        timer.start();
        try {
            progressDialog.setVisible(true);
        }
        finally {
            timer.stop();
        }
    }

    private Timer timer(final Date start, final ThreadedPlugIn plugIn, final WorkbenchFrame workbenchFrame) {
        return new Timer(1000, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "";
                message = String.valueOf(message) + StringUtil.toTimeString(new Date().getTime() - start.getTime());
                message = String.valueOf(message) + " (" + plugIn.getName() + ")";
                workbenchFrame.setTimeMessage(message);
            }
        });
    }

    private class TaskWrapper
    implements Runnable {
        private ThreadedPlugIn plugIn;
        private PlugInContext context;
        private TaskMonitorDialog dialog;

        public TaskWrapper(ThreadedPlugIn plugIn, PlugInContext context, TaskMonitorDialog dialog) {
            this.plugIn = plugIn;
            this.context = context;
            this.dialog = dialog;
        }

        @Override
        public void run() {
            Exception exception = null;
            try {
                try {
                    this.plugIn.run(this.dialog, this.context);
                }
                catch (Exception ex) {
                    exception = ex;
                    this.dialog.setVisible(false);
                    if (exception != null) {
                        this.context.getErrorHandler().handleThrowable(exception);
                    }
                    this.context = null;
                }
            }
            finally {
                this.dialog.setVisible(false);
                if (exception != null) {
                    this.context.getErrorHandler().handleThrowable(exception);
                }
                this.context = null;
            }
        }
    }
}

