/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringEndpointStyle;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import org.saig.jump.lang.I18N;

public class ArrowLineStringEndpointStyle
extends LineStringEndpointStyle {
    private static final double SMALL_ANGLE = 10.0;
    private static final double MEDIUM_ANGLE = 30.0;
    private static final double MEDIUM_LENGTH = 10.0;
    private static final double LARGE_LENGTH = 15.0;
    private boolean filled;
    private double finAngle;
    protected double finLength;

    public ArrowLineStringEndpointStyle(String name, boolean start, String iconFile, double finAngle, double finLength, boolean filled) {
        super(name, IconLoader.icon(iconFile), start);
        this.finAngle = finAngle;
        this.finLength = finLength;
        this.filled = filled;
    }

    @Override
    protected void paint(Point2D terminal, Point2D next, Viewport viewport, Graphics2D graphics) throws NoninvertibleTransformException {
        if (terminal.equals(next)) {
            return;
        }
        graphics.setColor(this.lineColorWithAlpha);
        graphics.setStroke(this.stroke);
        GeneralPath arrowhead = this.arrowhead(terminal, next, this.finLength, this.finAngle);
        if (this.filled) {
            arrowhead.closePath();
            graphics.fill(arrowhead);
        }
        graphics.draw(arrowhead);
    }

    private GeneralPath arrowhead(Point2D shaftTip, Point2D shaftTail, double finLength, double finAngle) {
        GeneralPath arrowhead = new GeneralPath();
        Point2D finTip1 = this.fin(shaftTip, shaftTail, finLength, finAngle);
        Point2D finTip2 = this.fin(shaftTip, shaftTail, finLength, -finAngle);
        arrowhead.moveTo((float)finTip1.getX(), (float)finTip1.getY());
        arrowhead.lineTo((float)shaftTip.getX(), (float)shaftTip.getY());
        arrowhead.lineTo((float)finTip2.getX(), (float)finTip2.getY());
        return arrowhead;
    }

    private Point2D fin(Point2D shaftTip, Point2D shaftTail, double length, double angle) {
        double shaftLength = shaftTip.distance(shaftTail);
        Point2D finTail = shaftTip;
        Point2D finTip = GUIUtil.add(GUIUtil.multiply(GUIUtil.subtract(shaftTail, shaftTip), length / shaftLength), finTail);
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(angle * Math.PI / 180.0, finTail.getX(), finTail.getY());
        return affineTransform.transform(finTip, null);
    }

    public static abstract class Feathers
    extends ArrowLineStringEndpointStyle {
        private static final int SPACING = 5;
        private static final int FEATHERS = 2;

        public Feathers(String name, boolean start, String iconFile) {
            super(name, start, iconFile, 30.0, 10.0, false);
        }

        @Override
        protected void paint(Point2D terminal, Point2D next, Viewport viewport, Graphics2D graphics) throws NoninvertibleTransformException {
            int i = 0;
            while (i < 2) {
                Point2D unit = GUIUtil.multiply(GUIUtil.subtract(next, terminal), 1.0 / next.distance(terminal));
                Point2D pseudoTerminal = GUIUtil.add(terminal, GUIUtil.multiply(unit, this.finLength + (double)(i * 5)));
                super.paint(pseudoTerminal, terminal, viewport, graphics);
                ++i;
            }
        }

        @Override
        public void initialize(Layer layer) {
            super.initialize(layer);
            this.stroke = new BasicStroke(1.0f, 1, 1);
        }
    }

    public static class FeathersEnd
    extends Feathers {
        public FeathersEnd() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.end-feathers"), true, "FeathersEnd.gif");
        }
    }

    public static class FeathersStart
    extends Feathers {
        public FeathersStart() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.start-feathers"), true, "FeathersStart.gif");
        }
    }

    public static class NarrowSolidEnd
    extends ArrowLineStringEndpointStyle {
        public NarrowSolidEnd() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.end-arrow-solid-narrow"), false, "ArrowEndSolidNarrow.gif", 10.0, 15.0, true);
        }
    }

    public static class NarrowSolidStart
    extends ArrowLineStringEndpointStyle {
        public NarrowSolidStart() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.start-arrow-solid-narrow"), true, "ArrowStartSolidNarrow.gif", 10.0, 15.0, true);
        }
    }

    public static class OpenEnd
    extends ArrowLineStringEndpointStyle {
        public OpenEnd() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.end-arrow-open"), false, "ArrowEndOpen.gif", 30.0, 10.0, false);
        }
    }

    public static class OpenStart
    extends ArrowLineStringEndpointStyle {
        public OpenStart() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.start-arrow-open"), true, "ArrowStartOpen.gif", 30.0, 10.0, false);
        }
    }

    public static class SolidEnd
    extends ArrowLineStringEndpointStyle {
        public SolidEnd() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.end-arrow-solid"), false, "ArrowEndSolid.gif", 30.0, 10.0, true);
        }
    }

    public static class SolidStart
    extends ArrowLineStringEndpointStyle {
        public SolidStart() {
            super(I18N.getString("workbench.ui.renderer.style.ArrowLineStringEndpointStyle.start-arrow-solid"), true, "ArrowStartSolid.gif", 30.0, 10.0, true);
        }
    }
}

