/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package es.kosmo.desktop.widgets.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.geom.Angle;
import es.kosmo.desktop.widgets.conversion.TransRotScaleBuilder;

class TwoPointTransRotScaleBuilder
extends TransRotScaleBuilder {
    TwoPointTransRotScaleBuilder(Coordinate[] srcVector, Coordinate[] destVector) {
        super(srcVector, destVector);
    }

    @Override
    protected void compute(Coordinate[] srcVector, Coordinate[] destVector) {
        boolean isZeroLength;
        this.originX = srcVector[0].x;
        this.originY = srcVector[0].y;
        double srcLen = srcVector[0].distance(srcVector[1]);
        double destLen = destVector[0].distance(destVector[1]);
        boolean bl = isZeroLength = srcLen == 0.0 || destLen == 0.0;
        if (!isZeroLength) {
            this.scaleY = this.scaleX = destLen / srcLen;
            double angleSrc = Angle.angle(srcVector[0], srcVector[1]);
            double angleDest = Angle.angle(destVector[0], destVector[1]);
            double angleRad = angleDest - angleSrc;
            this.angle = Math.toDegrees(angleRad);
        }
        this.dx = destVector[0].x - srcVector[0].x;
        this.dy = destVector[0].y - srcVector[0].y;
    }
}

