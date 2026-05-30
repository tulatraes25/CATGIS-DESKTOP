/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import org.saig.jump.lang.I18N;

public class JColorButton
extends JButton
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Color DISABLED_COLOR = new Color(0.9f, 0.9f, 0.9f, 0.6f);
    private Color selectedColor;
    private int alpha;
    private boolean enabled = true;

    public JColorButton() {
        this.addActionListener(this);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
        g.setColor(Color.BLACK);
        if (this.enabled) {
            g.setColor(this.getBackground());
        } else {
            g.setColor(DISABLED_COLOR);
        }
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Color newColor = JColorChooser.showDialog(this, I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.Choose-color"), this.getBackground());
        if (newColor != null) {
            this.selectedColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), this.alpha);
        }
        this.setBackground(this.selectedColor);
        this.repaint();
    }

    public Color getColor() {
        return this.selectedColor;
    }

    public void setColor(Color c) {
        this.selectedColor = c;
        if (c != null) {
            this.alpha = c.getAlpha();
        }
        this.setBackground(this.selectedColor);
    }

    public void setAlpha(int newAlpha) {
        this.alpha = newAlpha;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        super.setEnabled(enabled);
    }
}

