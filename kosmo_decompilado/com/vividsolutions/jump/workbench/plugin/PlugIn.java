/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import javax.swing.Icon;

public interface PlugIn {
    public void initialize(PlugInContext var1) throws Exception;

    public boolean execute(PlugInContext var1) throws Exception;

    public void finish(PlugInContext var1);

    public String getName();

    public Icon getIcon();

    public Icon getDisabledIcon();

    public EnableCheck getCheck();
}

