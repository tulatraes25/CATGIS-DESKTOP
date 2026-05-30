/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.awt.Color;
import org.saig.core.styling.TextSymbolizer;

public class LineToLabel {
    private double width;
    private Color color;
    private float[] dash;
    protected TextSymbolizer.LineToLabelEndingAnchorOptions lineToLabelEndingAnchorOption;

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float[] getDash() {
        return this.dash;
    }

    public void setDash(float[] dash) {
        this.dash = dash;
    }

    public TextSymbolizer.LineToLabelEndingAnchorOptions getLineToLabelEndingAnchorOption() {
        return this.lineToLabelEndingAnchorOption;
    }

    public void setLineToLabelEndingAnchorOption(TextSymbolizer.LineToLabelEndingAnchorOptions lineToLabelEndingAnchorOption) {
        this.lineToLabelEndingAnchorOption = lineToLabelEndingAnchorOption;
    }
}

