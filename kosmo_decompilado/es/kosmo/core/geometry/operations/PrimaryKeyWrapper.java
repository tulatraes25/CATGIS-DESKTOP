/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.geometry.operations;

public class PrimaryKeyWrapper {
    protected Object pkValue;

    public PrimaryKeyWrapper(Object pk) {
        this.pkValue = pk;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PrimaryKeyWrapper)) {
            return false;
        }
        PrimaryKeyWrapper wrap = (PrimaryKeyWrapper)obj;
        if (this.pkValue != null) {
            return this.pkValue.equals(wrap.pkValue);
        }
        return wrap.pkValue == null;
    }
}

