/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;

public class MacroPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    protected PlugIn[] plugIns;

    public MacroPlugIn(PlugIn[] plugIns) {
        this.plugIns = (PlugIn[])plugIns.clone();
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        int i = 0;
        while (i < this.plugIns.length) {
            this.plugIns[i].initialize(context);
            ++i;
        }
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        int i = 0;
        while (i < this.plugIns.length) {
            if (!this.plugIns[i].execute(context)) {
                return false;
            }
            ++i;
        }
        return true;
    }

    @Override
    public String getName() {
        String name = "";
        int i = 0;
        while (i < this.plugIns.length) {
            if (i > 0) {
                name = String.valueOf(name) + " + ";
            }
            name = String.valueOf(name) + this.plugIns[i].getName();
            ++i;
        }
        return name;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        int i = 0;
        while (i < this.plugIns.length) {
            PlugIn plugIn = this.plugIns[i];
            if (plugIn instanceof ThreadedPlugIn) {
                ((ThreadedPlugIn)plugIn).run(monitor, context);
            }
            ++i;
        }
    }
}

