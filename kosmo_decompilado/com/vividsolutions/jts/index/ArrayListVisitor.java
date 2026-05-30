/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jts.index;

import com.vividsolutions.jts.index.ItemVisitor;
import java.util.ArrayList;

public class ArrayListVisitor<T>
implements ItemVisitor<T> {
    private ArrayList<T> items = new ArrayList();

    @Override
    public void visitItem(T item) {
        this.items.add(item);
    }

    public ArrayList<T> getItems() {
        return this.items;
    }
}

