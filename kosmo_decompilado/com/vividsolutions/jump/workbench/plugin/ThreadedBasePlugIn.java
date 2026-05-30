/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;

public abstract class ThreadedBasePlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static void main(String[] args) {
        new TaskMonitorManager().execute(new ThreadedBasePlugIn(){

            @Override
            public boolean execute(PlugInContext context) throws Exception {
                return true;
            }

            @Override
            public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            }
        }, null);
    }
}

