/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.data;

public class AttributesSelection {
    private Object[] values;

    public AttributesSelection(Object[] values) {
        this.values = values;
    }

    public int size() {
        if (this.values == null) {
            return -1;
        }
        return this.values.length;
    }

    public Object getValue(int index) {
        return this.values[index];
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AttributesSelection)) {
            return false;
        }
        AttributesSelection selection = (AttributesSelection)obj;
        if (selection.size() != this.size()) {
            return false;
        }
        int i = 0;
        while (i < this.values.length) {
            if (!selection.getValue(i).equals(this.values[i])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = 0;
        int i = 0;
        while (i < this.values.length) {
            hashCode += this.values[i].hashCode();
            ++i;
        }
        return hashCode;
    }
}

