/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.renderer.label;

import org.saig.core.renderer.lite.LabelCacheItem;

public class LabelCacheItemWrapper {
    private LabelCacheItem item;

    public LabelCacheItem getItem() {
        return this.item;
    }

    public void setItem(LabelCacheItem item) {
        this.item = item;
    }

    public LabelCacheItemWrapper(LabelCacheItem item) {
        this.item = item;
    }

    public boolean equals(Object obj) {
        if (obj instanceof LabelCacheItemWrapper) {
            return ((LabelCacheItemWrapper)obj).item == this.item;
        }
        return false;
    }

    public int hashCode() {
        return (int)this.item.getId();
    }
}

