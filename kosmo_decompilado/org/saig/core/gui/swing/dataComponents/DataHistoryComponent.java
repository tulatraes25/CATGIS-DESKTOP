/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents;

import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataComponent;

public interface DataHistoryComponent<T>
extends DataComponent<T> {
    public void setHistoryKeyValue(Object var1);

    public Object getHistoryKeyValue();

    public List<T> getHistoryOfElement();

    public String applyPattern(Object[] var1);

    public List<T> getValues();

    public void setFilter(Filter var1);
}

