/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.ui.cursortool.RectangleTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.awt.Cursor;
import javax.swing.Icon;

public class DrawRectangleFenceTool
extends RectangleTool {
    public static final Icon ICON = IconLoader.icon("Box.gif");
    public static final Cursor CURSOR = DrawRectangleFenceTool.createCursor(IconLoader.icon("FenceCursor.gif").getImage());
    public static final Color COLOR = Color.black;

    public DrawRectangleFenceTool() {
        this.setColor(COLOR);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        this.getPanel().setViewportInitialized(true);
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(this.getPanel());
        fenceLayerFinder.setFence((Geometry)this.getRectangle());
        if (!fenceLayerFinder.getLayer().isVisible()) {
            fenceLayerFinder.getLayer().setVisible(true);
        } else {
            fenceLayerFinder.getLayer().fireAppearanceChanged();
        }
    }
}

