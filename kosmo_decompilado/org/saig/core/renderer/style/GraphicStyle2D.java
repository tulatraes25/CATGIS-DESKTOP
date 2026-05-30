/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.image.BufferedImage;
import org.saig.core.renderer.style.Style2D;

public class GraphicStyle2D
extends Style2D {
    BufferedImage image;
    float rotation;
    float opacity;
    int border = 0;

    public GraphicStyle2D(BufferedImage image, float rotation, float opacity) {
        this.image = image;
        this.rotation = rotation;
        this.opacity = opacity;
    }

    public GraphicStyle2D(BufferedImage image, float rotation, float opacity, int border) {
        this.image = image;
        this.rotation = rotation;
        this.opacity = opacity;
        this.border = border;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public float getOpacity() {
        return this.opacity;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setOpacity(float f) {
        this.opacity = f;
    }

    public void setRotation(float f) {
        this.rotation = f;
    }

    public int getBorder() {
        return this.border;
    }

    public void setBorder(int border) {
        this.border = border;
    }
}

