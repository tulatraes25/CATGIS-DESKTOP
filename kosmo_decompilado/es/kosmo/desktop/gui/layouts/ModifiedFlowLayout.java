/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class ModifiedFlowLayout
extends FlowLayout {
    private static final long serialVersionUID = 1L;

    public ModifiedFlowLayout() {
    }

    public ModifiedFlowLayout(int align) {
        super(align);
    }

    public ModifiedFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return this.computeMinSize(target);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return this.computeSize(target);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Dimension computeSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Insets insets;
            int hgap = this.getHgap();
            int vgap = this.getVgap();
            int w = target.getWidth();
            if (w == 0) {
                w = Integer.MAX_VALUE;
            }
            if ((insets = target.getInsets()) == null) {
                insets = new Insets(0, 0, 0, 0);
            }
            int reqdWidth = 0;
            int maxwidth = w - (insets.left + insets.right + hgap * 2);
            int n = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;
            int i = 0;
            while (i < n) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    if (x == 0 || x + d.width <= maxwidth) {
                        if (x > 0) {
                            x += hgap;
                        }
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        x = d.width;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
                ++i;
            }
            y += rowHeight;
            return new Dimension(reqdWidth + insets.left + insets.right, y += insets.bottom);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Dimension computeMinSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MIN_VALUE;
            boolean found_one = false;
            int n = target.getComponentCount();
            int i = 0;
            while (i < n) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    found_one = true;
                    Dimension d = c.getPreferredSize();
                    minx = Math.min(minx, d.width);
                    miny = Math.min(miny, d.height);
                }
                ++i;
            }
            if (found_one) {
                return new Dimension(minx, miny);
            }
            return new Dimension(0, 0);
        }
    }
}

