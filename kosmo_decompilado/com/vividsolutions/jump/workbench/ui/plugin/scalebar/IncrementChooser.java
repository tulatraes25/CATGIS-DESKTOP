/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.RoundQuantity;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.jump.lang.I18N;

public class IncrementChooser {
    public RoundQuantity chooseGoodIncrement(Collection<Unit<Length>> units, double idealIncrement, Unit<Length> mapLengthUnit) {
        return this.goodIncrement(this.goodUnit(units, idealIncrement, mapLengthUnit), idealIncrement, mapLengthUnit);
    }

    private Unit<Length> goodUnit(Collection<Unit<Length>> units, double idealIncrement, Unit<Length> mapLengthUnit) {
        Unit<Length> goodUnit = Collections.min(units, new Comparator<Unit<Length>>(){

            @Override
            public int compare(Unit<Length> o1, Unit<Length> o2) {
                return new Double(o1.getConverterTo(o2).convert(1.0)).compareTo(1.0);
            }
        });
        for (Unit<Length> candidateUnit : units) {
            double candidateValue = candidateUnit.getConverterTo(mapLengthUnit).convert(1.0);
            if (candidateValue > idealIncrement) continue;
            double goodUnitDistance = goodUnit.getConverterTo(mapLengthUnit).convert(1.0);
            if (!(this.distance(candidateValue, idealIncrement) < this.distance(goodUnitDistance, idealIncrement))) continue;
            goodUnit = candidateUnit;
        }
        return goodUnit;
    }

    private double distance(double a, double b) {
        return Math.abs(MathUtil.orderOfMagnitude(a) - MathUtil.orderOfMagnitude(b));
    }

    private RoundQuantity goodIncrement(Unit<Length> unit, double idealIncrement, Unit<Length> mapLengthUnit) {
        double goodUnitDistance = unit.getConverterTo(mapLengthUnit).convert(1.0);
        RoundQuantity mantissa1Candidate = new RoundQuantity(1, (int)Math.floor(MathUtil.orderOfMagnitude(idealIncrement) - MathUtil.orderOfMagnitude(goodUnitDistance)), unit, mapLengthUnit);
        Assert.isTrue((mantissa1Candidate.getModelValue() <= idealIncrement ? 1 : 0) != 0, (String)(String.valueOf(I18N.getString("workbench.ui.plugin.scalebar.IncrementChooser.unit")) + "=" + goodUnitDistance + ", " + I18N.getString("workbench.ui.plugin.scalebar.IncrementChooser.ideal-increment") + "=" + idealIncrement));
        RoundQuantity mantissa2Candidate = new RoundQuantity(2, mantissa1Candidate.getExponent(), unit, mapLengthUnit);
        RoundQuantity mantissa5Candidate = new RoundQuantity(5, mantissa1Candidate.getExponent(), unit, mapLengthUnit);
        if (mantissa5Candidate.getModelValue() <= idealIncrement) {
            return mantissa5Candidate;
        }
        if (mantissa2Candidate.getModelValue() <= idealIncrement) {
            return mantissa2Candidate;
        }
        return mantissa1Candidate;
    }
}

