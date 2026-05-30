/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package es.kosmo.desktop.widgets.conversion;

import com.vividsolutions.jts.geom.Coordinate;

abstract class TransRotScaleBuilder {
    protected double originX = 0.0;
    protected double originY = 0.0;
    protected double scaleX = 0.0;
    protected double scaleY = 0.0;
    protected double dx = 0.0;
    protected double dy = 0.0;
    protected double angle = 0.0;

    public TransRotScaleBuilder(Coordinate[] srcPts, Coordinate[] destPts) {
        this.compute(srcPts, destPts);
    }

    protected abstract void compute(Coordinate[] var1, Coordinate[] var2);

    public double getOriginX() {
        return this.originX;
    }

    public double getOriginY() {
        return this.originY;
    }

    public boolean isScale() {
        return this.scaleX > 0.0;
    }

    public double getScaleX() {
        return this.scaleX;
    }

    public double getScaleY() {
        return this.scaleY;
    }

    public boolean isTranslate() {
        return this.dx != 0.0 | this.dy != 0.0;
    }

    public double getTranslateX() {
        return this.dx;
    }

    public double getTranslateY() {
        return this.dy;
    }

    public double getRotationAngle() {
        return this.angle;
    }
}

