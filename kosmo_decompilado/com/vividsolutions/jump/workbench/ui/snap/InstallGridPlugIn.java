/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.InstallRendererPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.snap.GridRenderer;
import com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;

public class InstallGridPlugIn
extends InstallRendererPlugIn {
    public InstallGridPlugIn() {
        super("GRID", false);
    }

    @Override
    protected AbstractRenderer.Factory createFactory(final TaskFrame frame) {
        return new AbstractRenderer.Factory(){

            @Override
            public AbstractRenderer create() {
                return new GridRenderer(JUMPWorkbench.getBlackboard(), frame.getLayerViewPanel());
            }
        };
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        super.initialize(context);
        OptionsDialog.instance(context.getWorkbenchContext().getWorkbench()).addTab(new SnapOptionsPanel());
        OptionsDialog.instance(context.getWorkbenchContext().getWorkbench()).addTab(new SnapLayersOptionsPanel());
    }
}

