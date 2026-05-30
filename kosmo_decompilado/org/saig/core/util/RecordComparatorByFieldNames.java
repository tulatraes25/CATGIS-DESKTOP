/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.model.data.Record;

public class RecordComparatorByFieldNames
implements Comparator<Record>,
Serializable {
    private static final long serialVersionUID = 1L;
    private final List<String> names;
    private final boolean tryComparable;
    private final boolean isCaseSensitive;
    private final int signoDelOrden;
    private final int signoDeNulos;

    public RecordComparatorByFieldNames(List<String> fieldNameList, boolean compareByValue, boolean useCaseSensitiveOnString, boolean ascendingOrder, boolean nullAtEnd) {
        this.names = fieldNameList;
        this.tryComparable = compareByValue;
        this.isCaseSensitive = useCaseSensitiveOnString;
        this.signoDelOrden = ascendingOrder ? 1 : -1;
        this.signoDeNulos = nullAtEnd ? 1 : -1;
    }

    public RecordComparatorByFieldNames(List<String> fieldNameList) {
        this(fieldNameList, true, true, true, true);
    }

    @Override
    public int compare(Record o1, Record o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return this.signoDeNulos;
        }
        if (o2 == null) {
            return -this.signoDeNulos;
        }
        int comp = 0;
        if (CollectionUtils.isNotEmpty(this.names)) {
            Iterator<String> namesIt = this.names.iterator();
            while (namesIt.hasNext() && comp == 0) {
                comp = this.compare(o1, o2, namesIt.next());
            }
        }
        if (comp == 0) {
            return this.getComparation(o1.getPrimaryKey(), o2.getPrimaryKey());
        }
        return comp;
    }

    private int compare(Record o1, Record o2, String fieldName) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return this.signoDeNulos;
        }
        if (o2 == null) {
            return -this.signoDeNulos;
        }
        if (fieldName == null) {
            return 0;
        }
        if (!o1.getSchema().hasAttribute(fieldName) || !o1.getSchema().hasAttribute(fieldName)) {
            return 0;
        }
        return this.getComparation(o1.getAttribute(fieldName), o2.getAttribute(fieldName));
    }

    private int getComparation(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return 0;
        }
        if (obj1 == null) {
            return this.signoDeNulos;
        }
        if (obj2 == null) {
            return -this.signoDeNulos;
        }
        if (this.tryComparable && !(obj1 instanceof String) && !(obj2 instanceof String) && obj1 instanceof Comparable && obj2 instanceof Comparable) {
            return this.signoDelOrden * ((Comparable)obj1).compareTo((Comparable)obj2);
        }
        if (!this.isCaseSensitive) {
            return this.signoDelOrden * obj1.toString().compareToIgnoreCase(obj2.toString());
        }
        return this.signoDelOrden * obj1.toString().compareTo(obj2.toString());
    }
}

