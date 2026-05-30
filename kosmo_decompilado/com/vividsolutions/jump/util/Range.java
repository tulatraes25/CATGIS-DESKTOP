/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

import java.util.Comparator;
import java.util.TreeMap;

public class Range {
    private boolean includingMin;
    private boolean includingMax;
    private Object max;
    private Object min;
    private static final Comparator INFINITY_COMPARATOR = new Comparator(){

        public int compare(Object o1, Object o2) {
            if (o1 instanceof PositiveInfinity || o2 instanceof NegativeInfinity) {
                return 1;
            }
            if (o1 instanceof NegativeInfinity || o2 instanceof PositiveInfinity) {
                return -1;
            }
            return ((Comparable)o1).compareTo(o2);
        }
    };
    public static final Comparator<Range> RANGE_COMPARATOR = new Comparator<Range>(){

        @Override
        public int compare(Range o1, Range o2) {
            Range range1 = o1 instanceof Range ? o1 : new Range(o1, true, o1, true);
            Range range2 = o2 instanceof Range ? o2 : new Range(o2, true, o2, true);
            int max1ComparedToMin2 = INFINITY_COMPARATOR.compare(range1.getMax(), range2.getMin());
            if (max1ComparedToMin2 < 0 || max1ComparedToMin2 == 0 && (!range1.isIncludingMax() || !range2.isIncludingMin())) {
                return -1;
            }
            int min1ComparedToMax2 = INFINITY_COMPARATOR.compare(range1.getMin(), range2.getMax());
            if (min1ComparedToMax2 > 0 || min1ComparedToMax2 == 0 && (!range1.isIncludingMin() || !range2.isIncludingMax())) {
                return 1;
            }
            return 0;
        }
    };

    public Range() {
        this(new NegativeInfinity(), false, new PositiveInfinity(), false);
    }

    public Range(Object min, boolean includingMin, Object max, boolean includingMax) {
        this.min = min;
        this.max = max;
        this.includingMin = includingMin;
        this.includingMax = includingMax;
    }

    public boolean equals(Object obj) {
        return RANGE_COMPARATOR.compare(this, (Range)obj) == 0;
    }

    public boolean isIncludingMax() {
        return this.includingMax;
    }

    public boolean isIncludingMin() {
        return this.includingMin;
    }

    public Object getMax() {
        return this.max;
    }

    public Object getMin() {
        return this.min;
    }

    public void setIncludingMax(boolean b) {
        this.includingMax = b;
    }

    public void setIncludingMin(boolean b) {
        this.includingMin = b;
    }

    public void setMax(Object object) {
        this.max = object;
    }

    public void setMin(Object object) {
        this.min = object;
    }

    public static final class NegativeInfinity {
    }

    public static final class PositiveInfinity {
    }

    public static class RangeTreeMap
    extends TreeMap {
        public RangeTreeMap() {
            super(RANGE_COMPARATOR);
        }
    }
}

