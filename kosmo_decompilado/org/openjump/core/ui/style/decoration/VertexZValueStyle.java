/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.LineString
 */
package org.openjump.core.ui.style.decoration;

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

public class VertexZValueStyle
extends LineStringVertexStyle {
    public static final int FONT_BASE_SIZE = 10;
    private Font font = new Font("Dialog", 0, 10);

    public VertexZValueStyle(String name, String iconFile) {
        super(name, IconLoader.icon(iconFile));
    }

    @Override
    protected void paint(Point2D p, LineString line, int index, Viewport viewport, Graphics2D g) throws Exception {
        String text = Double.toString(line.getCoordinates()[index].z);
        g.setColor(Color.BLACK);
        g.setStroke(this.stroke);
        TextLayout layout = new TextLayout(text, this.font, g.getFontRenderContext());
        layout.draw(g, (float)p.getX(), (float)p.getY());
    }

    public static class VertexZValue
    extends VertexZValueStyle {
        public VertexZValue() {
            super(I18N.getString("org.openjump.core.ui.style.decoration.VertexZValueStyle.vertexes-z-coordinate"), "ZValueDecorator.gif");
        }
    }
}

