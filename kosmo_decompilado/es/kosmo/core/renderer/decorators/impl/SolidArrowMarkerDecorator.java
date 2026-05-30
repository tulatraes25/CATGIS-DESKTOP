/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.GeneralPathX
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import es.kosmo.core.renderer.decorators.impl.ArrowMarkerDecorator;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

public abstract class SolidArrowMarkerDecorator
extends ArrowMarkerDecorator {
    @Override
    protected GeneralPathX getGeneralPathX(double pixelSize, Unit<Length> viewUnit) {
        double radian_half_sharpeness = Math.toRadians(this.getSharpness() * 0.5);
        double sizeInPx = this.getSize() * this.getTransformationFactor(viewUnit);
        if (!"pixel".equals(this.unit)) {
            sizeInPx *= pixelSize;
        }
        double halfHeight = sizeInPx * Math.tan(radian_half_sharpeness);
        GeneralPathX gp = new GeneralPathX();
        gp.moveTo(0.0, 0.0);
        gp.lineTo(sizeInPx, -halfHeight);
        gp.lineTo(sizeInPx, halfHeight);
        gp.closePath();
        return gp;
    }

    @Override
    protected boolean isFill() {
        return true;
    }
}

