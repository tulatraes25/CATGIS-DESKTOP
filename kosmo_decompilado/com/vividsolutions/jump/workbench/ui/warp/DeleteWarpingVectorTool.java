/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.warp.AbstractDeleteVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import java.awt.Cursor;
import javax.swing.Icon;

public class DeleteWarpingVectorTool
extends AbstractDeleteVectorTool {
    @Override
    protected AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        return new WarpingVectorLayerFinder(layerManagerProxy);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("BlueDeleteVectors.gif");
    }

    @Override
    public Cursor getCursor() {
        return DeleteWarpingVectorTool.createCursor(IconLoader.icon("BlueDeleteVectorCursor.gif").getImage());
    }
}

