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

public class LinearGradientImpl
extends GradientImpl {
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    @Override
    public Object clone() {
        LinearGradientImpl clone = new LinearGradientImpl();
        clone.startX = this.startX;
        clone.startY = this.startY;
        clone.endX = this.endX;
        clone.endY = this.endY;
        clone.cycleMethod = this.cycleMethod;
        clone.setColors((Color[])ArrayUtils.clone((Object[])this.getColors()));
        clone.setFractions(ArrayUtils.clone((float[])this.getFractions()));
        return clone;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof LinearGradientImpl) {
            LinearGradientImpl other = (LinearGradientImpl)oth;
            return this.startX == other.startX && this.startY == other.startY && this.endX == this.endY && ArrayUtils.isEquals((Object)this.fractions, (Object)other.fractions) && ArrayUtils.isEquals((Object)this.colors, (Object)other.colors);
        }
        return false;
    }

    @Override
    public Gradient.GradientType getType() {
        return Gradient.GradientType.LINEAR;
    }

    public float getStartX() {
        return this.startX;
    }

    public float getStartY() {
        return this.startY;
    }

    public float getEndX() {
        return this.endX;
    }

    public float getEndY() {
        return this.endY;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }
}

