/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.renderer.label;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

class TransformedIcon
implements Icon {
    AffineTransform at;
    Icon icon;
    int width;
    int height;

    public TransformedIcon(Icon icon, AffineTransform at) {
        this.icon = icon;
        this.at = at;
        Rectangle2D bounds = new Rectangle2D.Double(0.0, 0.0, icon.getIconWidth(), icon.getIconHeight());
        bounds = at.createTransformedShape(bounds).getBounds2D();
        this.width = (int)Math.round(bounds.getWidth());
        this.height = (int)Math.round(bounds.getHeight());
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    @Override
    public int getIconWidth() {
        return this.width;
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
            at.concatenate(this.at);
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

