/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 */
package es.kosmo.core.styling;

import es.kosmo.core.styling.Gradient;
import es.kosmo.core.styling.GradientImpl;
import java.awt.Color;
import org.apache.commons.lang.ArrayUtils;

public class RadialGradientImpl
extends GradientImpl {
    private float radius;
    private float centerX;
    private float centerY;

    @Override
    public Object clone() {
        RadialGradientImpl clone = new RadialGradientImpl();
        clone.centerX = this.centerX;
        clone.centerY = this.centerY;
        clone.radius = this.radius;
        clone.cycleMethod = this.cycleMethod;
        clone.setColors((Color[])ArrayUtils.clone((Object[])this.getColors()));
        clone.setFractions(ArrayUtils.clone((float[])this.getFractions()));
        return clone;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof RadialGradientImpl) {
            RadialGradientImpl other = (RadialGradientImpl)oth;
            return this.centerX == other.centerX && this.centerY == other.centerY && ArrayUtils.isEquals((Object)this.fractions, (Object)other.fractions) && ArrayUtils.isEquals((Object)this.colors, (Object)other.colors);
        }
        return false;
    }

    @Override
    public Gradient.GradientType getType() {
        return Gradient.GradientType.RADIAL;
    }

    public float getRadius() {
        return this.radius;
    }

    public float getCenterX() {
        return this.centerX;
    }

    public float getCenterY() {
        return this.centerY;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }
}

