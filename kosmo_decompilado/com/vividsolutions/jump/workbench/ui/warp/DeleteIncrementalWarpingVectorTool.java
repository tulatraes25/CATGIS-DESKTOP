/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.warp.AbstractDeleteVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.IncrementalWarpingVectorLayerFinder;
import com.vividsolutions.jump.workbench.ui.warp.WarpingPanel;
import java.awt.Cursor;
import javax.swing.Icon;

public class DeleteIncrementalWarpingVectorTool
extends AbstractDeleteVectorTool {
    private WarpingPanel warpingPanel;

    public DeleteIncrementalWarpingVectorTool(WarpingPanel warpingPanel) {
        this.warpingPanel = warpingPanel;
    }

    @Override
    protected AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        return new IncrementalWarpingVectorLayerFinder(layerManagerProxy);
    }

    @Override
    protected UndoableCommand createCommand() throws Exception {
        return this.warpingPanel.addWarping(this.warpingPanel.addWarpingVectorGeneration(super.createCommand()));
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("DeleteVectors.gif");
    }

    @Override
    public Cursor getCursor() {
        return DeleteIncrementalWarpingVectorTool.createCursor(IconLoader.icon("DeleteVectorCursor.gif").getImage());
    }
}

