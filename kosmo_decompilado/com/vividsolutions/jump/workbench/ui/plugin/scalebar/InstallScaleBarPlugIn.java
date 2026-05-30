/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.InstallRendererPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;

public class InstallScaleBarPlugIn
extends InstallRendererPlugIn {
    public InstallScaleBarPlugIn() {
        super("SCALE_BAR", true);
    }

    @Override
    protected AbstractRenderer.Factory createFactory(final TaskFrame frame) {
        return new AbstractRenderer.Factory(){

            @Override
            public AbstractRenderer create() {
                return new ScaleBarRenderer(frame.getLayerViewPanel());
            }
        };
    }
}

