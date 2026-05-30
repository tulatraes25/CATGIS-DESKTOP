/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import java.awt.event.MouseEvent;

public abstract class NClickTool
extends MultiClickTool {
    private int n;

    public NClickTool(int n) {
        super(true, false);
        this.n = n;
    }

    public NClickTool(int n, boolean check) {
        super(check, false);
        this.n = n;
    }

    protected Coordinate getModelSource() {
        return this.getCoordinates().get(0);
    }

    protected Coordinate getModelDestination() {
        return this.getCoordinates().get(this.n - 1);
    }

    @Override
    protected boolean isFinishingRelease(MouseEvent e) {
        return e.getClickCount() == 1 && this.shouldGestureFinish();
    }

    protected boolean shouldGestureFinish() {
        return this.getCoordinates().size() >= this.n;
    }
}

