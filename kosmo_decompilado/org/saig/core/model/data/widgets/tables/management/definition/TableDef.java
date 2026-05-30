/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.definition;

import java.util.HashMap;
import java.util.Map;
import org.saig.core.model.data.widgets.tables.management.definition.Column;

public class TableDef {
    private String name = null;
    private boolean pkEditable = true;
    private Map<String, Column> mapColumn = new HashMap<String, Column>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addColumn(Column column) {
        this.mapColumn.put(column.getName(), column);
    }

    public Column getColumn(String name) {
        return this.mapColumn.get(name);
    }

    public Map<String, Column> getColumns() {
        return this.mapColumn;
    }

    public boolean isPkEditable() {
        return this.pkEditable;
    }

    public void setPkEditable(boolean editable) {
        this.pkEditable = editable;
    }
}

