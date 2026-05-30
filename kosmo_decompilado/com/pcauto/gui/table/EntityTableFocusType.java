/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

public class EntityTableFocusType {
    public static final EntityTableFocusType TABLE_FOCUS = new EntityTableFocusType("t", "Table Focus");
    public static final EntityTableFocusType ROW_FOCUS = new EntityTableFocusType("r", "Row Focus");
    public static final EntityTableFocusType CELL_FOCUS = new EntityTableFocusType("c", "Cell Focus");
    public static final EntityTableFocusType[] ALL = new EntityTableFocusType[]{TABLE_FOCUS, ROW_FOCUS, CELL_FOCUS};
    private String name;
    private String description;

    public EntityTableFocusType[] getAll() {
        return ALL;
    }

    public String getDescription() {
        return this.description;
    }

    private EntityTableFocusType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

