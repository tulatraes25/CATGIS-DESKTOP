/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource;

import java.text.Collator;
import org.saig.jump.lang.I18N;

public class SortedAttribute
implements Comparable<SortedAttribute> {
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private Object value;
    private Object recordNumber;
    private boolean ascending;
    private boolean isString;

    public SortedAttribute(Object value, Object recordNumber, boolean ascending, boolean isString) {
        this.value = value;
        this.recordNumber = recordNumber;
        this.ascending = ascending;
        this.isString = isString;
    }

    public SortedAttribute(Object value, boolean ascending, boolean isString) {
        this.value = value;
        this.ascending = ascending;
        this.isString = isString;
    }

    public Object getRecordNumber() {
        return this.recordNumber;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public int compareTo(SortedAttribute other) {
        if (this.ascending) {
            if (this.value == null) {
                return -1;
            }
            if (other == null || other.value == null) {
                return 1;
            }
            if (this.isString) {
                int compare = this.collator.compare(this.value, other.value);
                if (compare != 0) {
                    return compare;
                }
                return 1;
            }
            int compare = ((Comparable)this.value).compareTo(other.value);
            if (compare != 0) {
                return compare;
            }
            return 1;
        }
        if (this.value == null) {
            return 1;
        }
        if (other == null || other.value == null) {
            return -1;
        }
        if (this.isString) {
            int compare = this.collator.compare(other.value, this.value);
            if (compare != 0) {
                return compare;
            }
            return -1;
        }
        int compare = ((Comparable)other.value).compareTo(this.value);
        if (compare != 0) {
            return compare;
        }
        return -1;
    }

    public boolean equals(Object other) {
        if (!(other instanceof SortedAttribute)) {
            return false;
        }
        SortedAttribute sorAtt = (SortedAttribute)other;
        return this.getValue().equals(sorAtt.getValue()) && this.getRecordNumber().equals(sorAtt.getRecordNumber());
    }

    public int hashCode() {
        return 17 + this.getValue().hashCode() + this.getRecordNumber().hashCode();
    }
}

