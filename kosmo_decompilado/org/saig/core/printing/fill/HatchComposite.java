/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.printing.fill;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class HatchComposite
implements Composite {
    ColorModel colorModel;
    public static final int SIMPLE = 1;
    public static final int DOUBLE = 2;
    public static final int TRIPLE = 3;
    private int style;
    private Color color;
    private int distance;
    private int angle;

    public HatchComposite(int style, Color color, int distance, int angle) {
        this.style = style;
        this.color = color;
        this.distance = distance * 10;
        this.angle = angle;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        this.colorModel = srcColorModel;
        return new CompositeContext(){

            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            }

            @Override
            public void dispose() {
            }
        };
    }

    public int getStyle() {
        return this.style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getDistance() {
        return this.distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getAngle() {
        return this.angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}

