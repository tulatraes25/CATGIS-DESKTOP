/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.sdi;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;

public interface ISDIService {
    public String getName();

    public String getDescription();

    public UndoableCommand loadResults(PlugInContext var1, WizardDialog var2, TaskMonitor var3);
}

