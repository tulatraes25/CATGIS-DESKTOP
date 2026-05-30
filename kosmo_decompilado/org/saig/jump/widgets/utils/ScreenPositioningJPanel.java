/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

public class ScreenPositioningJPanel
extends JPanel
implements MouseListener {
    private static final long serialVersionUID = 1L;
    private double x = 0.9;
    private double y = 0.1;

    public ScreenPositioningJPanel() {
        this.addMouseListener(this);
    }

    public double getXInScreen() {
        return this.x;
    }

    public double getYInScreen() {
        return this.y;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.x = this.calculateXFromMouseX(e.getPoint().x);
        this.y = this.calculateYFromMouseY(e.getPoint().y);
    }

    private double calculateYFromMouseY(int y2) {
        return this.getHeight() / y2;
    }

    private double calculateXFromMouseX(int x2) {
        return this.getWidth() / x2;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

