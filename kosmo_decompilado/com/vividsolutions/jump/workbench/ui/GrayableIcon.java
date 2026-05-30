/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JButton;

public class GrayableIcon
implements Icon {
    private Icon originalIcon;
    private Icon grayedIcon;
    private Icon currentIcon;

    public GrayableIcon(Icon originalIcon) {
        this.originalIcon = originalIcon;
        this.grayedIcon = new JButton(originalIcon).getDisabledIcon();
        this.currentIcon = originalIcon;
    }

    public void setGrayed(boolean grayed) {
        this.currentIcon = grayed ? this.grayedIcon : this.originalIcon;
    }

    public boolean isGrayed() {
        return this.currentIcon == this.grayedIcon;
    }

    @Override
    public int getIconHeight() {
        return this.currentIcon.getIconHeight();
    }

    @Override
    public int getIconWidth() {
        return this.currentIcon.getIconWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        this.currentIcon.paintIcon(c, g, x, y);
    }
}

