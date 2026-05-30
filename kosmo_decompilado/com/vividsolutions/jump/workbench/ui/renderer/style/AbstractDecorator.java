/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.Icon;

public abstract class AbstractDecorator
implements Style {
    private String name;
    private Icon icon;
    private boolean enabled = true;
    protected Stroke stroke;
    protected Color lineColorWithAlpha;
    protected Color fillColorWithAlpha;

    public Color getFillColorWithAlpha() {
        return this.fillColorWithAlpha;
    }

    public void setFillColorWithAlpha(Color fillColorWithAlpha) {
        this.fillColorWithAlpha = fillColorWithAlpha;
    }

    public Color getLineColorWithAlpha() {
        return this.lineColorWithAlpha;
    }

    public void setLineColorWithAlpha(Color lineColorWithAlpha) {
        this.lineColorWithAlpha = lineColorWithAlpha;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractDecorator(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void initialize(Layer layer) {
        this.stroke = new BasicStroke(layer.getBasicStyle().getLineWidth(), 1, 1);
        this.lineColorWithAlpha = GUIUtil.alphaColor(layer.getBasicStyle().getLineColor(), layer.getBasicStyle().getAlpha());
        this.fillColorWithAlpha = GUIUtil.alphaColor(layer.getBasicStyle().getFillColor(), layer.getBasicStyle().getAlpha());
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        this.paintGeometry(f.getGeometry(), g, viewport);
    }

    protected abstract void paintGeometry(Geometry var1, Graphics2D var2, Viewport var3) throws Exception;
}

