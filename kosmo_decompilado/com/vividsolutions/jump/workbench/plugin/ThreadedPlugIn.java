/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public interface ThreadedPlugIn
extends PlugIn {
    public void run(TaskMonitor var1, PlugInContext var2) throws Exception;
}

