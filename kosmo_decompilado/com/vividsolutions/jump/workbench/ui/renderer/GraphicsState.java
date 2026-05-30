/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class GraphicsState {
    private Stroke stroke;
    private Color color;

    public GraphicsState() {
    }

    public GraphicsState(Graphics2D g) {
        this.stroke = g.getStroke();
        this.color = g.getColor();
    }

    public void restore(Graphics2D g) {
        g.setStroke(this.stroke);
        g.setColor(this.color);
    }
}

