/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class WrapLayout
extends FlowLayout {
    private static final long serialVersionUID = 1L;
    private Dimension preferredLayoutSize;

    public WrapLayout() {
        super(0);
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return this.layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return this.layoutSize(target, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        Object object = target.getTreeLock();
        synchronized (object) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            int hgap = this.getHgap();
            int vgap = this.getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;
            int nmembers = target.getComponentCount();
            int i = 0;
            while (i < nmembers) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d;
                    Dimension dimension = d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        this.addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth > 0) {
                        rowWidth += hgap;
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                ++i;
            }
            this.addRow(dim, rowWidth, rowHeight);
            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;
            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += this.getVgap();
        }
        dim.height += rowHeight;
    }

    @Override
    public void layoutContainer(Container target) {
        Dimension size = this.preferredLayoutSize(target);
        if (size.equals(this.preferredLayoutSize)) {
            super.layoutContainer(target);
        } else {
            this.preferredLayoutSize = size;
            target.invalidate();
            Container top = target;
            if (top.getParent() != null) {
                top = top.getParent();
            }
            top.validate();
        }
    }
}

