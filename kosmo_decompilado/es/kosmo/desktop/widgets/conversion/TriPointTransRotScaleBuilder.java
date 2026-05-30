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

class TriPointTransRotScaleBuilder
extends TransRotScaleBuilder {
    TriPointTransRotScaleBuilder(Coordinate[] srcPt, Coordinate[] destPt) {
        super(srcPt, destPt);
    }

    @Override
    protected void compute(Coordinate[] srcPt, Coordinate[] destPt) {
        boolean isZeroLength;
        this.originX = srcPt[1].x;
        this.originY = srcPt[1].y;
        double srcLenBase = srcPt[1].distance(srcPt[2]);
        double destLenBase = destPt[1].distance(destPt[2]);
        double srcLenSide = srcPt[0].distance(srcPt[1]);
        double destLenSide = destPt[0].distance(destPt[1]);
        boolean bl = isZeroLength = srcLenBase == 0.0 || destLenBase == 0.0 || srcLenSide == 0.0 || destLenSide == 0.0;
        if (!isZeroLength) {
            this.scaleX = destLenBase / srcLenBase;
            this.scaleY = destLenSide / srcLenSide;
            double angleSrc = Angle.angle(srcPt[1], srcPt[2]);
            double angleDest = Angle.angle(destPt[1], destPt[2]);
            double angleRad = angleDest - angleSrc;
            this.angle = Math.toDegrees(angleRad);
        }
        this.dx = destPt[1].x - srcPt[1].x;
        this.dy = destPt[1].y - srcPt[1].y;
    }
}

