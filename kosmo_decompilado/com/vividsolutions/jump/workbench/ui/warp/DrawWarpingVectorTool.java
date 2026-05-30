/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.VectorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import java.awt.Cursor;
import javax.swing.Icon;

public class DrawWarpingVectorTool
extends VectorTool {
    public DrawWarpingVectorTool() {
        this.setColor(WarpingVectorLayerFinder.COLOR);
    }

    @Override
    protected AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        return new WarpingVectorLayerFinder(layerManagerProxy);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("BlueVectorToolBar.gif");
    }

    @Override
    public Cursor getCursor() {
        return DrawWarpingVectorTool.createCursor(IconLoader.icon("BlueVectorCursor.gif").getImage());
    }
}

