/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import javax.swing.JInternalFrame;

public abstract class InstallRendererPlugIn
extends AbstractPlugIn {
    private Object contentID;
    private boolean aboveLayerables;

    public InstallRendererPlugIn(Object contentID, boolean aboveLayerables) {
        this.contentID = contentID;
        this.aboveLayerables = aboveLayerables;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
        int i = 0;
        while (i < frames.length) {
            if (frames[i] instanceof TaskFrame) {
                this.ensureHasRenderer((TaskFrame)frames[i]);
            }
            ++i;
        }
        context.getWorkbenchFrame().getDesktopPane().addContainerListener(new ContainerAdapter(){

            @Override
            public void componentAdded(ContainerEvent e) {
                if (!(e.getChild() instanceof TaskFrame)) {
                    return;
                }
                TaskFrame taskFrame = (TaskFrame)e.getChild();
                InstallRendererPlugIn.this.ensureHasRenderer(taskFrame);
            }
        });
    }

    private void ensureHasRenderer(TaskFrame taskFrame) {
        if (this.aboveLayerables) {
            taskFrame.getLayerViewPanel().getRenderingManager().putAboveLayerables(this.contentID, this.createFactory(taskFrame));
        } else {
            taskFrame.getLayerViewPanel().getRenderingManager().putBelowLayerables(this.contentID, this.createFactory(taskFrame));
        }
    }

    protected abstract AbstractRenderer.Factory createFactory(TaskFrame var1);
}

