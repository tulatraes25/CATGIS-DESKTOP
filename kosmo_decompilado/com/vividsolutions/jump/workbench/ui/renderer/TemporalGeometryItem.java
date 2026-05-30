/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import org.saig.jump.widgets.config.ConfigSelectionPanel;

public class TemporalGeometryItem {
    private final Geometry geom;
    private final Color color;
    private final Stroke stroke;

    public Geometry getGeometry() {
        return this.geom;
    }

    public Color getColor() {
        return this.color;
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    private TemporalGeometryItem(Builder builder) {
        this.geom = builder.geom;
        this.color = builder.color;
        this.stroke = builder.stroke;
    }

    /* synthetic */ TemporalGeometryItem(Builder builder, TemporalGeometryItem temporalGeometryItem) {
        this(builder);
    }

    public static class Builder {
        private static Color DEFAULT_GRAPHICS_2D_COLOR = (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).get(ConfigSelectionPanel.SELECTION_LINE_COLOR, ConfigSelectionPanel.SELECTION_LINE_DEFAULT_COLOR);
        private static Stroke DEFAULT_GRAPHICS_2D_STROKE = new BasicStroke(3.0f, 1, 1);
        private final Geometry geom;
        private Color color = DEFAULT_GRAPHICS_2D_COLOR;
        private Stroke stroke = DEFAULT_GRAPHICS_2D_STROKE;

        public Builder(Geometry geometry) {
            this.geom = geometry;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder stroke(Stroke stroke) {
            this.stroke = stroke;
            return this;
        }

        public TemporalGeometryItem build() {
            return new TemporalGeometryItem(this, null);
        }
    }
}

