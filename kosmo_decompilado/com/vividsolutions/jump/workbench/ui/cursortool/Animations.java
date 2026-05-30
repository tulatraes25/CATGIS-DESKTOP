/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;

public class Animations {
    private static void drawRings(Collection<Point2D> centers, int radius, int delay, Color color, LayerViewPanel panel, float[] dash) {
        GeneralPath path = new GeneralPath();
        for (Point2D center : centers) {
            path.append(new Ellipse2D.Double(center.getX() - (double)radius, center.getY() - (double)radius, radius * 2, radius * 2), false);
        }
        panel.flash(path, color, new BasicStroke(5.0f, 0, 1, 10.0f, dash, 0.0f), delay);
    }

    public static void drawExpandingRing(Point2D center, boolean expanding, Color color, LayerViewPanel panel, float[] dash) {
        Animations.drawExpandingRings(Arrays.asList(center), expanding, color, panel, dash);
    }

    public static void drawExpandingRings(Collection<Point2D> centers, boolean expanding, Color color, LayerViewPanel panel, float[] dash) {
        int start = expanding ? 0 : 5;
        int end = 5 - start;
        int increment = expanding ? 1 : -1;
        int i = start;
        while (i != end) {
            Animations.drawRings(centers, i * 10, 30, color, panel, dash);
            i += increment;
        }
    }
}

