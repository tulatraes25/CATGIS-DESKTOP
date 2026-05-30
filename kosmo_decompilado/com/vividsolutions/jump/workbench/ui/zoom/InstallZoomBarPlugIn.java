/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomBar;
import java.awt.Dimension;

public class InstallZoomBarPlugIn
extends AbstractPlugIn {
    @Override
    public void initialize(PlugInContext context) throws Exception {
        ZoomBar zoomBar = new ZoomBar(false, true, context.getWorkbenchFrame());
        zoomBar.setMaximumSize(new Dimension(200, 10000));
        context.getWorkbenchFrame().getToolBar().add(zoomBar);
    }
}

