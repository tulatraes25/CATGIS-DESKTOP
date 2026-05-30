/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.text;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class FullLabel
extends JLabel {
    public FullLabel(String text) {
        super(text);
        this.calculateMinimumSize();
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        this.calculateMinimumSize();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.calculateMinimumSize();
    }

    private void calculateMinimumSize() {
        Graphics g = this.getGraphics();
        if (g != null) {
            Border b = this.getBorder();
            FontMetrics fm = g.getFontMetrics(this.getFont());
            Rectangle2D bounds = fm.getStringBounds(this.getText(), g);
            if (b instanceof LineBorder) {
                LineBorder lb = (LineBorder)b;
                this.setMinimumSize(new Dimension((int)bounds.getWidth() + 2 * lb.getThickness(), (int)bounds.getHeight() + 2 * lb.getThickness()));
            } else {
                this.setMinimumSize(new Dimension((int)bounds.getWidth() + 1, (int)bounds.getHeight()));
            }
            this.setBounds(this.getBounds());
        }
    }

    @Override
    public void setSize(Dimension d) {
        if (this.getMinimumSize() != null && d.getWidth() < this.getMinimumSize().getWidth()) {
            super.setSize(new Dimension((int)this.getMinimumSize().getWidth(), (int)d.getHeight()));
        } else {
            super.setSize(d);
        }
    }

    @Override
    public void setSize(int width, int height) {
        if (this.getMinimumSize() != null && (double)width < this.getMinimumSize().getWidth()) {
            super.setSize((int)this.getMinimumSize().getWidth(), height);
        } else {
            super.setSize(width, height);
        }
    }

    @Override
    public void resize(Dimension d) {
        if (this.getMinimumSize() != null && d.getWidth() < this.getMinimumSize().getWidth()) {
            super.resize(new Dimension((int)this.getMinimumSize().getWidth(), (int)d.getHeight()));
        } else {
            super.resize(d);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (this.getMinimumSize() != null && (double)width < this.getMinimumSize().getWidth()) {
            super.resize((int)this.getMinimumSize().getWidth(), height);
        } else {
            super.resize(width, height);
        }
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        if (this.getMinimumSize() != null && (double)w < this.getMinimumSize().getWidth()) {
            super.setBounds(x, y, (int)this.getMinimumSize().getWidth(), h);
        } else {
            super.setBounds(x, y, w, h);
        }
    }

    @Override
    public void setBounds(Rectangle r) {
        if (this.getMinimumSize() != null && r.getWidth() < this.getMinimumSize().getWidth()) {
            Rectangle newRect = new Rectangle(r);
            newRect.width = (int)this.getMinimumSize().getWidth();
            super.setBounds(newRect);
        } else {
            super.setBounds(r);
        }
    }
}

