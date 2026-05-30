/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.renderer.style;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.geotools.resources.Utilities;
import org.saig.core.renderer.lite.TransformedShape;
import org.saig.core.renderer.style.PolygonStyle2D;

public class MarkStyle2D
extends PolygonStyle2D {
    static boolean maxMarkSizeEnabled = false;
    Shape shape;
    int size;
    float rotation;

    public float getRotation() {
        return this.rotation;
    }

    public Shape getShape() {
        return this.shape;
    }

    public Shape getTransformedShape(float x, float y) {
        return this.getTransformedShape(x, y, this.rotation);
    }

    public Shape getTransformedShape(float x, float y, float rotation) {
        if (this.shape != null) {
            Rectangle2D bounds = this.shape.getBounds2D();
            double shapeSize = maxMarkSizeEnabled ? Math.max(bounds.getWidth(), bounds.getHeight()) : bounds.getHeight();
            double scale = (double)this.size / shapeSize;
            TransformedShape ts = new TransformedShape();
            ts.shape = this.shape;
            ts.translate(x, y);
            ts.rotate(rotation);
            ts.scale(scale, -scale);
            return ts;
        }
        return null;
    }

    public Shape getScaledTransformedShape(float x, float y, double factor) {
        if (this.shape != null) {
            Rectangle2D bounds = this.shape.getBounds2D();
            double shapeSize = Math.max(bounds.getWidth(), bounds.getHeight());
            double scale = (double)this.size / shapeSize;
            TransformedShape ts = new TransformedShape();
            ts.shape = this.shape;
            ts.translate(x, y);
            ts.rotate(this.rotation);
            ts.scale(scale * factor, -scale * factor);
            return ts;
        }
        return null;
    }

    public int getSize() {
        return this.size;
    }

    public void setRotation(float f) {
        this.rotation = f;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setSize(int i) {
        this.size = i;
    }

    @Override
    public String toString() {
        return String.valueOf(Utilities.getShortClassName((Object)this)) + '[' + this.shape + ']';
    }

    public static boolean isMaxMarkSizeEnabled() {
        return maxMarkSizeEnabled;
    }

    public static void setMaxMarkSizeEnabled(boolean useMaxMarkSize) {
        maxMarkSizeEnabled = useMaxMarkSize;
    }
}

