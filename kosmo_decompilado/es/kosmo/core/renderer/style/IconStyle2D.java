/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.renderer.style;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import org.saig.core.renderer.style.Style2D;

public class IconStyle2D
extends Style2D {
    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
    private Icon icon;
    private float rotation;
    private Composite composite;
    private float displacementX;
    private float displacementY;

    public IconStyle2D(Icon icon, Object feature, float displacementX, float displacementY, float rotation, Composite composite) {
        this.icon = icon;
        this.rotation = rotation;
        this.composite = composite;
        this.displacementX = displacementX;
        this.displacementY = displacementY;
    }

    public float getRotation() {
        return this.rotation;
    }

    public Composite getComposite() {
        return this.composite;
    }

    public float getDisplacementX() {
        return this.displacementX;
    }

    public float getDisplacementY() {
        return this.displacementY;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void setComposite(Composite composite) {
        this.composite = composite;
    }

    public void setDisplacementX(float displacementX) {
        this.displacementX = displacementX;
    }

    public void setDisplacementY(float displacementY) {
        this.displacementY = displacementY;
    }
}

