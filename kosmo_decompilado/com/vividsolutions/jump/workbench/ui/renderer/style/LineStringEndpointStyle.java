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
import com.vividsolutions.jump.workbench.ui.renderer.style.ChoosableStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringStyle;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.Icon;

public abstract class LineStringEndpointStyle
extends LineStringStyle
implements ChoosableStyle {
    private boolean start;
    protected String name;
    protected Icon icon;

    public LineStringEndpointStyle(String name, Icon icon, boolean start) {
        super(name, icon);
        this.name = name;
        this.icon = icon;
        this.start = start;
    }

    @Override
    protected void paintLineString(LineString lineString, Viewport viewport, Graphics2D graphics) throws Exception {
        if (lineString.isEmpty()) {
            return;
        }
        this.paint(this.start ? lineString.getCoordinateN(0) : lineString.getCoordinateN(lineString.getNumPoints() - 1), this.start ? lineString.getCoordinateN(1) : lineString.getCoordinateN(lineString.getNumPoints() - 2), viewport, graphics);
    }

    private void paint(Coordinate terminal, Coordinate next, Viewport viewport, Graphics2D graphics) throws Exception {
        this.paint(viewport.toViewPoint(new Point2D.Double(terminal.x, terminal.y)), viewport.toViewPoint(new Point2D.Double(next.x, next.y)), viewport, graphics);
    }

    protected abstract void paint(Point2D var1, Point2D var2, Viewport var3, Graphics2D var4) throws Exception;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }
}

