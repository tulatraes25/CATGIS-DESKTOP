/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

public abstract class Style
implements Cloneable {
    protected boolean enabled = true;
    protected double maxScale = Double.POSITIVE_INFINITY;
    protected double minScale = 0.0;

    public double getMaxScale() {
        return this.maxScale;
    }

    public double getMinScale() {
        return this.minScale;
    }

    public void setMinMaxScale(double minScale, double maxScale) {
        if (minScale > maxScale) {
            throw new IllegalArgumentException("Max scale must be bigger than min scale");
        }
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    public boolean isScaleInRange(double scale) {
        return scale >= this.minScale && scale <= this.maxScale;
    }

    public Style clone() {
        try {
            return (Style)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

