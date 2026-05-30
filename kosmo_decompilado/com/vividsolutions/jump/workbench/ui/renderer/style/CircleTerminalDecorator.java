/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.TerminalDecorator;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import org.saig.jump.lang.I18N;

public abstract class CircleTerminalDecorator
extends TerminalDecorator {
    private static final int DIAMETER = 8;
    private Stroke circleStroke = new BasicStroke(2.0f);

    private CircleTerminalDecorator(String name, boolean start, String iconFile) {
        super(name, IconLoader.icon(iconFile), start);
    }

    @Override
    protected void paint(Point2D terminal, Point2D next, Viewport viewport, Graphics2D graphics) throws NoninvertibleTransformException {
        graphics.setColor(this.lineColorWithAlpha);
        graphics.setStroke(this.circleStroke);
        graphics.draw(this.toShape(terminal));
    }

    private Shape toShape(Point2D viewPoint) {
        return new Ellipse2D.Double(viewPoint.getX() - 4.0, viewPoint.getY() - 4.0, 8.0, 8.0);
    }

    /* synthetic */ CircleTerminalDecorator(String string, boolean bl, String string2, CircleTerminalDecorator circleTerminalDecorator) {
        this(string, bl, string2);
    }

    public static class End
    extends CircleTerminalDecorator {
        public End() {
            super(I18N.getString("workbench.ui.renderer.style.CircleTerminalDecorator.end-circle"), false, "CircleEnd.gif", null);
        }
    }

    public static class Start
    extends CircleTerminalDecorator {
        public Start() {
            super(I18N.getString("workbench.ui.renderer.style.CircleTerminalDecorator.start-circle"), true, "CircleStart.gif", null);
        }
    }
}

