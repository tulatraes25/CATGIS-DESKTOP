/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class RingVertexStyle
extends VertexStyle {
    private BasicStroke stroke = new BasicStroke(5.0f);

    public RingVertexStyle() {
        super(new Ellipse2D.Double());
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public int getSize() {
        return 50;
    }

    @Override
    protected void render(Graphics2D g) {
        g.setStroke(this.stroke);
        g.draw(this.shape);
    }
}

