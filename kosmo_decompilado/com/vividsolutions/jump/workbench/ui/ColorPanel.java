/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class ColorPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private Color fillColor = Color.red;
    private Color lineColor = Color.green;
    private int margin = 0;
    private BasicStroke fillStroke = new BasicStroke(1.0f);
    private int lineWidth = 1;
    private BasicStroke lineStroke = new BasicStroke(this.lineWidth);

    public ColorPanel() {
        this.setBackground(Color.white);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color originalColor = g.getColor();
        g.setColor(this.getBackground());
        ((Graphics2D)g).setStroke(this.fillStroke);
        g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        g.setColor(this.fillColor);
        g.fillRect(this.margin, this.margin, this.getWidth() - 1 - this.margin - this.margin, this.getHeight() - 1 - this.margin - this.margin);
        if (this.lineColor != null) {
            g.setColor(this.lineColor);
            ((Graphics2D)g).setStroke(this.lineStroke);
            g.drawRect(this.margin, this.margin, this.getWidth() - 1 - this.margin - this.margin, this.getHeight() - 1 - this.margin - this.margin);
        }
        g.setColor(originalColor);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        this.validate();
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public Color getFillColor() {
        return this.fillColor;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineWidth(int lineWidth) {
        this.lineStroke = new BasicStroke(lineWidth);
        this.lineWidth = lineWidth;
    }

    public int getLineWidth() {
        return this.lineWidth;
    }
}

