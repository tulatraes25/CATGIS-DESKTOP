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
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringVertexStyle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import org.saig.jump.lang.I18N;

public class VertexXYLineSegmentStyle
extends LineStringVertexStyle {
    public static final int FONT_BASE_SIZE = 10;
    private Font font = new Font("Dialog", 0, 10);

    public VertexXYLineSegmentStyle(String name, String iconFile) {
        super(name, IconLoader.icon(iconFile));
    }

    @Override
    protected void paint(Point2D p, LineString line, int index, Viewport viewport, Graphics2D g) throws Exception {
        Coordinate pt = line.getCoordinateN(index);
        String text = String.valueOf(pt.x) + ", " + pt.y;
        g.setColor(Color.BLACK);
        g.setStroke(this.stroke);
        TextLayout layout = new TextLayout(text, this.font, g.getFontRenderContext());
        layout.draw(g, (float)p.getX(), (float)p.getY());
    }

    public static class VertexXY
    extends VertexXYLineSegmentStyle {
        public VertexXY() {
            super(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.VertexXYLineSegmentStyle.vertexes-xy-coordinates"), "VertexXYDecorator.gif");
        }
    }
}

