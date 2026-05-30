/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;

class RescaledIcon
implements Icon {
    double scale;
    Icon icon;

    public RescaledIcon(Icon icon, double scale) {
        this.icon = icon;
        this.scale = scale;
    }

    @Override
    public int getIconHeight() {
        return (int)Math.round((double)this.icon.getIconHeight() * this.scale);
    }

    @Override
    public int getIconWidth() {
        return (int)Math.round((double)this.icon.getIconWidth() * this.scale);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform tmp = g2d.getTransform();
        Object oldInterpolation = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        if (oldInterpolation == null) {
            oldInterpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
        try {
            AffineTransform at = new AffineTransform(tmp);
            at.translate(x, y);
            at.scale(this.scale, this.scale);
            g2d.setTransform(at);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            this.icon.paintIcon(c, g2d, 0, 0);
        }
        finally {
            g2d.setTransform(tmp);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }
}

