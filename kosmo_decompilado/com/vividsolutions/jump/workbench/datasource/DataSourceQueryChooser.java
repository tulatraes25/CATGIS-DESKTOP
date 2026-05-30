/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import java.awt.Component;
import java.util.Collection;

public interface DataSourceQueryChooser {
    public Component getComponent();

    public Collection<DataSourceQuery> getDataSourceQueries();

    public String toString();

    public boolean isInputValid();
}

