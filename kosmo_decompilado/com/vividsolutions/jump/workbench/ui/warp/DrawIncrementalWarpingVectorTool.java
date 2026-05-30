/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.cursortool.VectorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.warp.IncrementalWarpingVectorLayerFinder;
import com.vividsolutions.jump.workbench.ui.warp.WarpingPanel;
import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.Icon;

public class DrawIncrementalWarpingVectorTool
extends VectorTool {
    private WarpingPanel warpingPanel;

    public DrawIncrementalWarpingVectorTool(WarpingPanel warpingPanel) {
        this.setColor(IncrementalWarpingVectorLayerFinder.COLOR);
        this.warpingPanel = warpingPanel;
    }

    @Override
    protected AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        return new IncrementalWarpingVectorLayerFinder(layerManagerProxy);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("GreenVectorToolBar.gif");
    }

    @Override
    public Cursor getCursor() {
        return DrawIncrementalWarpingVectorTool.createCursor(IconLoader.icon("GreenVectorCursor.gif").getImage());
    }

    @Override
    protected UndoableCommand createCommand() throws NoninvertibleTransformException {
        return this.warpingPanel.addWarping(this.warpingPanel.addWarpingVectorGeneration(super.createCommand()));
    }
}

