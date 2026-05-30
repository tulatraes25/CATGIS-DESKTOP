/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents;

import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataComponent;

public interface DataListComponent<T>
extends DataComponent<T> {
    public void selectItemByValue(Object var1);

    public List<T> getRowsByValue(Object var1);

    public List<T> getValues();

    public Object getKeyValue();

    public T getValueByKey(Object var1);

    public void setFilter(Filter var1);
}

