/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.PString;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class EntityTableColumn
extends TableColumn {
    private static final long serialVersionUID = 1L;
    public static final int SCROLL_LOCKED = 1;
    public static final int UNSORTABLE = 2;
    public static final int HIDDEN = 4;
    public static final int EDIT_LOCKED = 8;
    public static final int RIGHT_ALIGNMENT_SORTABLE = 16;
    private String name = null;
    private Class<?> columnClass = null;
    private boolean locked = false;
    private boolean sortable = true;
    private boolean rightAligned = false;
    private boolean hidden = false;
    private boolean editable = true;

    public EntityTableColumn() {
        this.setLocked(false);
        this.setRightAlignmentSortable(false);
        this.setHidden(false);
        this.setEditable(true);
        this.setIdentifier(this);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass) {
        this();
        this.setName(columnName);
        this.setColumnClass(columnClass);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width) {
        this(columnName, columnClass);
        this.setWidth(width);
        this.setPreferredWidth(width);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, int attrs) {
        this(columnName, columnClass, width);
        this.setLocked((attrs & 1) == 1);
        this.setSortable((attrs & 2) != 2);
        this.setHidden((attrs & 4) == 4);
        this.setEditable((attrs & 8) != 8);
        this.setRightAlignmentSortable((attrs & 0x10) == 16);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellEditor editor) {
        this(columnName, columnClass, width);
        this.setCellEditor(editor);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellEditor editor, int attrs) {
        this(columnName, columnClass, width, attrs);
        this.setCellEditor(editor);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellRenderer renderer) {
        this(columnName, columnClass, width);
        this.setCellRenderer(renderer);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellRenderer renderer, int attrs) {
        this(columnName, columnClass, width, attrs);
        this.setCellRenderer(renderer);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellEditor editor, TableCellRenderer renderer) {
        this(columnName, columnClass, width, editor);
        this.setCellRenderer(renderer);
    }

    public EntityTableColumn(String columnName, Class<?> columnClass, int width, TableCellEditor editor, TableCellRenderer renderer, int attrs) {
        this(columnName, columnClass, width, editor, attrs);
        this.setCellRenderer(renderer);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = new String(n);
    }

    public Class<?> getColumnClass() {
        return this.columnClass;
    }

    public void setColumnClass(Class<?> c) {
        this.columnClass = c;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean newValue) {
        this.locked = newValue;
    }

    public boolean isSortable() {
        return this.sortable;
    }

    public void setSortable(boolean newValue) {
        this.sortable = newValue;
    }

    protected boolean isRightAlignmentSortable() {
        return this.rightAligned;
    }

    protected void setRightAlignmentSortable(boolean newValue) {
        this.rightAligned = newValue;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean newValue) {
        this.hidden = newValue;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean newValue) {
        this.editable = newValue;
    }

    @Override
    public TableCellEditor getCellEditor() {
        if (super.getCellEditor() == null && this.getColumnClass() == String.class) {
            class MyDefaultCellEditor
            extends DefaultCellEditor {
                private static final long serialVersionUID = 1L;
                private JTextField mEditField;

                public MyDefaultCellEditor(JTextField x) {
                    super(x);
                    this.mEditField = x;
                }

                @Override
                public boolean shouldSelectCell(EventObject anEvent) {
                    super.shouldSelectCell(anEvent);
                    Runnable getFocus = new Runnable(){

                        @Override
                        public void run() {
                            mEditField.requestFocus();
                        }
                    };
                    SwingUtilities.invokeLater(getFocus);
                    return true;
                }
            }
            this.setCellEditor(new MyDefaultCellEditor(new JTextField()));
        }
        return super.getCellEditor();
    }

    public boolean equals(Object o) {
        if (o instanceof EntityTableColumn) {
            EntityTableColumn c = (EntityTableColumn)o;
            return PString.areEqual(this.getName(), c.getName());
        }
        return false;
    }
}

