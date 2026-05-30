/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.function.ClassificationFunction;
import org.saig.core.model.feature.FeatureIterator;

public class EqualIntervalFunction
extends ClassificationFunction {
    private static final Logger LOGGER = Logger.getLogger(EqualIntervalFunction.class);
    FeatureCollection fc = null;
    double min = 0.0;
    double max = 0.0;

    @Override
    public String getName() {
        return "EqualInterval";
    }

    private void calculateMinAndMax() {
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
        FeatureIterator it = null;
        try {
            try {
                it = this.fc.iterator();
                while (it.hasNext()) {
                    Feature f = it.next();
                    double value = ((Number)this.expr.getValue(f)).doubleValue();
                    if (value > this.max) {
                        this.max = value;
                    }
                    if (!(value < this.min)) continue;
                    this.min = value;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public void setNumberOfClasses(int i) {
        this.classNum = i;
    }

    protected int calculateSlot(double val) {
        if (val >= this.max) {
            return this.classNum - 1;
        }
        double slotWidth = (this.max - this.min) / (double)this.classNum;
        return (int)Math.floor((val - this.min) / slotWidth);
    }

    @Override
    public Object getValue(Feature feature) {
        FeatureCollection coll = feature.getParent();
        if (!coll.equals(this.fc)) {
            this.fc = coll;
            this.calculateMinAndMax();
        }
        int slot = this.calculateSlot(((Number)this.expr.getValue(feature)).doubleValue());
        return new Integer(slot);
    }

    @Override
    public void setExpression(Expression e) {
        super.setExpression(e);
        if (this.fc != null) {
            this.calculateMinAndMax();
        }
    }
}

