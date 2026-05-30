/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import java.util.Collection;
import java.util.List;

public interface AddRemoveListModel<T> {
    public void add(T var1);

    public void setItems(Collection<T> var1);

    public List<T> getItems();

    public void remove(T var1);

    public void sort();

    public boolean isSorted();

    public void setSorted(boolean var1);
}

