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

public abstract class LineStringSegmentStyle
extends LineStringStyle
implements ChoosableStyle {
    protected String name;
    protected Icon icon;

    public LineStringSegmentStyle(String name, Icon icon) {
        super(name, icon);
        this.name = name;
        this.icon = icon;
    }

    @Override
    protected void paintLineString(LineString lineString, Viewport viewport, Graphics2D graphics) throws Exception {
        int i = 0;
        while (i < lineString.getNumPoints() - 1) {
            this.paint(lineString.getCoordinateN(i), lineString.getCoordinateN(i + 1), viewport, graphics);
            ++i;
        }
    }

    protected void paint(Coordinate p0, Coordinate p1, Viewport viewport, Graphics2D graphics) throws Exception {
        this.paint(viewport.toViewPoint(new Point2D.Double(p0.x, p0.y)), viewport.toViewPoint(new Point2D.Double(p1.x, p1.y)), viewport, graphics);
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

