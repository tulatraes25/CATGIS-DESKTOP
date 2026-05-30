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

public abstract class LineStringVertexStyle
extends LineStringStyle
implements ChoosableStyle {
    protected String name;
    protected Icon icon;

    public LineStringVertexStyle(String name, Icon icon) {
        super(name, icon);
        this.name = name;
        this.icon = icon;
    }

    @Override
    protected void paintLineString(LineString lineString, Viewport viewport, Graphics2D graphics) throws Exception {
        int numPtsToRender = lineString.getNumPoints();
        if (lineString.isClosed()) {
            --numPtsToRender;
        }
        int i = 0;
        while (i < numPtsToRender) {
            Coordinate p = lineString.getCoordinateN(i);
            this.paint(viewport.toViewPoint(new Point2D.Double(p.x, p.y)), lineString, i, viewport, graphics);
            ++i;
        }
    }

    protected abstract void paint(Point2D var1, LineString var2, int var3, Viewport var4, Graphics2D var5) throws Exception;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }
}

