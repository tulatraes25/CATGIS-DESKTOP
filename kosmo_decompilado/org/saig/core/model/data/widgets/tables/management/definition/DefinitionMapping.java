/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.definition;

import java.util.HashMap;
import java.util.Map;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;

public class DefinitionMapping {
    private Map mapTables = new HashMap();

    public void addTable(TableDef table) {
        this.mapTables.put(table.getName(), table);
    }

    public TableDef getTable(String name) {
        return (TableDef)this.mapTables.get(name);
    }
}

