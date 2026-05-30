/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.ui.cursortool.DrawRectangleFenceTool;
import com.vividsolutions.jump.workbench.ui.cursortool.PolygonTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Cursor;
import javax.swing.Icon;

public class DrawPolygonFenceTool
extends PolygonTool {
    public static final Icon ICON = IconLoader.icon("Box.gif");
    public static final Cursor CURSOR = DrawPolygonFenceTool.createCursor(IconLoader.icon("FenceCursor.gif").getImage());

    public DrawPolygonFenceTool() {
        super(false);
        this.setColor(DrawRectangleFenceTool.COLOR);
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    private boolean doubleClicked() {
        return this.getCoordinates().size() == 1;
    }

    @Override
    protected void gestureFinished() throws Exception {
        Polygon fence;
        this.reportNothingToUndoYet();
        if (this.doubleClicked()) {
            fence = null;
        } else {
            if (!this.checkPolygon()) {
                return;
            }
            this.getPanel().setViewportInitialized(true);
            fence = this.getPolygon();
        }
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(this.getPanel());
        fenceLayerFinder.setFence((Geometry)fence);
        if (!fenceLayerFinder.getLayer().isVisible()) {
            fenceLayerFinder.getLayer().setVisible(true);
        }
    }
}

