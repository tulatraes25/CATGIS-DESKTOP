/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin.test;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import org.saig.jump.lang.I18N;

public class ProgressReportingPlugIn
extends ThreadedBasePlugIn {
    private static final int MS_PER_SUBTASK = 3000;
    private static final int SUBTASK_COUNT = 5;
    private static final int SUBSUBTASK_COUNT = 1000;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem(this, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_OTHERS}, this.getName(), false, null, null);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) {
        monitor.allowCancellationRequests();
        context.getOutputFrame().createNewDocument();
        context.getOutputFrame().addHeader(1, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.header-1"));
        context.getOutputFrame().addHeader(2, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.header-2"));
        context.getOutputFrame().addHeader(3, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.header-3"));
        context.getOutputFrame().addHeader(4, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.header-4"));
        context.getOutputFrame().addHeader(5, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.header-5"));
        int i = 1;
        while (i <= 5) {
            if (monitor.isCancelRequested()) break;
            monitor.report(String.valueOf(I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.doing-subtask")) + " " + i);
            context.getOutputFrame().addField(String.valueOf(I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.progress")) + ":", String.valueOf(i), I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.tasks"));
            int j = 1;
            while (j <= 1000) {
                monitor.report(j, 1000, I18N.getString("workbench.ui.plugin.test.ProgressReportingPlugIn.subtasks"));
                try {
                    Thread.sleep(3L);
                }
                catch (InterruptedException e) {
                    Assert.shouldNeverReachHere();
                }
                ++j;
            }
            ++i;
        }
    }
}

