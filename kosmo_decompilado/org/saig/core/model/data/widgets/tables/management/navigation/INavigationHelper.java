/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import java.util.List;
import org.saig.core.filter.Filter;

public interface INavigationHelper {
    public static final int TABLE_TYPE = 0;
    public static final int LAYER_TYPE = 1;
    public static final int TABLE_DATA_SOURCE_TYPE = 2;
    public static final int FEATURES_LIST_TYPE = 3;

    public List<Object> getElements(int var1, int var2);

    public int getNumElements();

    public void setFilter(Filter var1);

    public int getNavigationHelperType();

    public void setOrderBy(String[] var1);

    public void setAscendingOrdering(boolean var1);
}

