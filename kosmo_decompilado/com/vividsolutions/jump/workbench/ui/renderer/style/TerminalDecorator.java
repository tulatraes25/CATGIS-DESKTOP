/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringDecorator;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.Icon;

public abstract class TerminalDecorator
extends LineStringDecorator {
    private boolean start;

    public boolean isStart() {
        return this.start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public TerminalDecorator(String name, Icon icon, boolean start) {
        super(name, icon);
        this.start = start;
    }

    @Override
    protected void paintLineString(LineString lineString, Viewport viewport, Graphics2D graphics) throws Exception {
        if (lineString.isEmpty()) {
            return;
        }
        this.paint(this.start ? lineString.getCoordinateN(0) : lineString.getCoordinateN(lineString.getNumPoints() - 1), this.start ? this.getNextNonEqual(lineString) : this.getPreviousNonEqual(lineString), viewport, graphics);
    }

    private void paint(Coordinate terminal, Coordinate next, Viewport viewport, Graphics2D graphics) throws Exception {
        this.paint(viewport.toViewPoint(new Point2D.Double(terminal.x, terminal.y)), viewport.toViewPoint(new Point2D.Double(next.x, next.y)), viewport, graphics);
    }

    protected abstract void paint(Point2D var1, Point2D var2, Viewport var3, Graphics2D var4) throws Exception;

    private Coordinate getNextNonEqual(LineString lineString) {
        Coordinate initialCoord = lineString.getCoordinateN(0);
        int i = 1;
        while (i < lineString.getNumPoints()) {
            Coordinate tmpCoord = lineString.getCoordinateN(i);
            if (!tmpCoord.equals2D(initialCoord)) {
                return tmpCoord;
            }
            ++i;
        }
        return initialCoord;
    }

    private Coordinate getPreviousNonEqual(LineString lineString) {
        Coordinate finalCoord = lineString.getCoordinateN(lineString.getNumPoints() - 1);
        int i = lineString.getNumPoints() - 2;
        while (i >= 0) {
            Coordinate tmpCoord = lineString.getCoordinateN(i);
            if (!tmpCoord.equals2D(finalCoord)) {
                return tmpCoord;
            }
            --i;
        }
        return finalCoord;
    }
}

