/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.widgets.tables.management.navigation.INavigationHelper;

public abstract class AbstractNavigationHelper
implements INavigationHelper {
    int type;
    Filter filter;
    String[] orderBy;
    boolean ascending;

    @Override
    public abstract List<Object> getElements(int var1, int var2);

    @Override
    public int getNavigationHelperType() {
        return this.type;
    }

    @Override
    public abstract int getNumElements();

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public void setOrderBy(String[] attributes) {
        this.orderBy = attributes;
    }

    @Override
    public void setAscendingOrdering(boolean ascending) {
        this.ascending = ascending;
    }
}

