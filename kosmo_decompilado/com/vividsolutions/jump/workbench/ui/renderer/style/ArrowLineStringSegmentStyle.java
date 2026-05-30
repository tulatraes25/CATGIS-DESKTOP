/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringSegmentStyle;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import org.saig.jump.lang.I18N;

public class ArrowLineStringSegmentStyle
extends LineStringSegmentStyle {
    private static final double SMALL_ANGLE = 10.0;
    private static final double MEDIUM_ANGLE = 30.0;
    private static final double MEDIUM_LENGTH = 10.0;
    private static final double LARGE_LENGTH = 15.0;
    private boolean filled;
    private double finAngle;
    protected double finLength;

    public ArrowLineStringSegmentStyle(String name, String iconFile, double finAngle, double finLength, boolean filled) {
        super(name, IconLoader.icon(iconFile));
        this.finAngle = finAngle;
        this.finLength = finLength;
        this.filled = filled;
    }

    @Override
    protected void paint(Point2D p0, Point2D p1, Viewport viewport, Graphics2D graphics) throws NoninvertibleTransformException {
        if (p0.equals(p1)) {
            return;
        }
        graphics.setColor(this.lineColorWithAlpha);
        graphics.setStroke(this.stroke);
        GeneralPath arrowhead = this.arrowhead(p0, p1, this.finLength, this.finAngle);
        if (this.filled) {
            arrowhead.closePath();
            graphics.fill(arrowhead);
        }
        graphics.draw(arrowhead);
    }

    private GeneralPath arrowhead(Point2D p0, Point2D p1, double finLength, double finAngle) {
        Point2D.Float mid = new Point2D.Float((float)((p0.getX() + p1.getX()) / 2.0), (float)((p0.getY() + p1.getY()) / 2.0));
        GeneralPath arrowhead = new GeneralPath();
        Point2D finTip1 = this.fin(mid, p0, finLength, finAngle);
        Point2D finTip2 = this.fin(mid, p0, finLength, -finAngle);
        arrowhead.moveTo((float)finTip1.getX(), (float)finTip1.getY());
        arrowhead.lineTo((float)((Point2D)mid).getX(), (float)((Point2D)mid).getY());
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

    public static class NarrowSolid
    extends ArrowLineStringSegmentStyle {
        public NarrowSolid() {
            super(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringSegmentStyle.mid-segment-arrow-solid-narrow"), "ArrowMidSolidNarrow.gif", 10.0, 15.0, true);
        }
    }

    public static class Open
    extends ArrowLineStringSegmentStyle {
        public Open() {
            super(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringSegmentStyle.mid-segment-arrow-open"), "ArrowMidOpen.gif", 30.0, 10.0, false);
        }
    }

    public static class Solid
    extends ArrowLineStringSegmentStyle {
        public Solid() {
            super(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringSegmentStyle.mid-segment-arrow-solid"), "ArrowMidSolid.gif", 30.0, 10.0, true);
        }
    }
}

