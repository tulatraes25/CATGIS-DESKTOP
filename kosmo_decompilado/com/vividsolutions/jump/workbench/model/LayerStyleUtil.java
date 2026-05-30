/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Color;

public class LayerStyleUtil {
    public static void setLinearStyle(Layer lyr, Color lineColor, int lineWidth, int vertexSize) {
        lyr.getBasicStyle().setLineColor(lineColor);
        lyr.getBasicStyle().setRenderingFill(false);
        lyr.getBasicStyle().setAlpha(255);
        lyr.getBasicStyle().setLineWidth(lineWidth);
        lyr.setSynchronizingLineColor(false);
        lyr.getVertexStyle().setSize(vertexSize);
        lyr.getVertexStyle().setEnabled(vertexSize > 0);
    }
}

