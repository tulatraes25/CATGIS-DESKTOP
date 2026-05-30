/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Collection;
import java.util.List;
import org.saig.core.filter.Filter;

public interface Manager {
    public void doOperations(List var1, List var2, List var3) throws Exception;

    public FeatureSchema getSchema();

    public Object getValue(int var1, Object var2);

    public void setValue(int var1, Object var2, Object var3);

    public Object getNewEntity();

    public List getDataList();

    public List getDataList(String var1, Filter var2) throws Exception;

    public Collection getRelations();
}

