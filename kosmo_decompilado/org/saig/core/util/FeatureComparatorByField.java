/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import com.vividsolutions.jump.feature.Feature;
import java.io.Serializable;
import java.util.Comparator;

public class FeatureComparatorByField
implements Comparator<Feature>,
Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private final boolean valuesToString;

    public FeatureComparatorByField(String fieldName) {
        this(fieldName, true);
    }

    public FeatureComparatorByField(String fieldName, boolean valuesToString) {
        this.name = fieldName;
        this.valuesToString = valuesToString;
    }

    @Override
    public int compare(Feature o1, Feature o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        Object value1 = o1.getAttribute(this.name);
        Object value2 = o2.getAttribute(this.name);
        if (this.valuesToString) {
            String str1 = null;
            String str2 = null;
            if (value1 != null) {
                str1 = value1.toString();
            }
            if (value2 != null) {
                str2 = value2.toString();
            }
            if (str1 == null && str2 == null) {
                return ((Comparable)o1.getPrimaryKey()).compareTo((Comparable)o2.getPrimaryKey());
            }
            if (str1 == null) {
                return 1;
            }
            if (str2 == null) {
                return -1;
            }
            int comp = str1.compareTo(str2);
            if (comp == 0) {
                return ((Comparable)o1.getPrimaryKey()).compareTo((Comparable)o2.getPrimaryKey());
            }
            return comp;
        }
        if (value1 == null && value2 == null) {
            return ((Comparable)o1.getPrimaryKey()).compareTo((Comparable)o2.getPrimaryKey());
        }
        if (value1 == null) {
            return 1;
        }
        if (value2 == null) {
            return -1;
        }
        int comp = ((Comparable)value1).compareTo((Comparable)value2);
        if (comp == 0) {
            return ((Comparable)o1.getPrimaryKey()).compareTo((Comparable)o2.getPrimaryKey());
        }
        return comp;
    }
}

