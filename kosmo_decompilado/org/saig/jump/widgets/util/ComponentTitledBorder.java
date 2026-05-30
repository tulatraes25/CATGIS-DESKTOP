/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class ComponentTitledBorder
implements Border,
MouseListener,
SwingConstants {
    int offset = 5;
    Component comp;
    JComponent container;
    Rectangle rect;
    Border border;

    public ComponentTitledBorder(Component comp, JComponent container, Border border) {
        this.comp = comp;
        this.container = container;
        this.border = border;
        container.addMouseListener(this);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Insets borderInsets = this.border.getBorderInsets(c);
        Insets insets = this.getBorderInsets(c);
        int temp = (insets.top - borderInsets.top) / 2;
        this.border.paintBorder(c, g, x, y + temp, width, height - temp);
        Dimension size = this.comp.getPreferredSize();
        this.rect = new Rectangle(this.offset, 0, size.width, size.height);
        SwingUtilities.paintComponent(g, this.comp, (Container)c, this.rect);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        Dimension size = this.comp.getPreferredSize();
        Insets insets = this.border.getBorderInsets(c);
        insets.top = Math.max(insets.top, size.height);
        return insets;
    }

    private void dispatchEvent(MouseEvent me) {
        if (this.rect != null && this.rect.contains(me.getX(), me.getY())) {
            Point pt = me.getPoint();
            pt.translate(-this.offset, 0);
            this.comp.setBounds(this.rect);
            this.comp.dispatchEvent(new MouseEvent(this.comp, me.getID(), me.getWhen(), me.getModifiers(), pt.x, pt.y, me.getClickCount(), me.isPopupTrigger(), me.getButton()));
            if (!this.comp.isValid()) {
                this.container.repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        this.dispatchEvent(me);
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        this.dispatchEvent(me);
    }

    @Override
    public void mouseExited(MouseEvent me) {
        this.dispatchEvent(me);
    }

    @Override
    public void mousePressed(MouseEvent me) {
        this.dispatchEvent(me);
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        this.dispatchEvent(me);
    }
}

