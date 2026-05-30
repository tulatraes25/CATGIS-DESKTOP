/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.combo;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import org.saig.core.gui.swing.dataComponents.layers.JLayerComboBox;
import org.saig.core.gui.swing.dataComponents.tables.JTableComboBox;
import org.saig.core.gui.swing.dataComponents.tables.JTableWithDataSourceComboBox;

public class ExtendedDefaultCellEditor
extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;

    public ExtendedDefaultCellEditor(final JTableComboBox comboBox) {
        super(comboBox);
        comboBox.removeActionListener(this.delegate);
        this.delegate = new DefaultCellEditor.EditorDelegate(this){
            private static final long serialVersionUID = 1L;

            @Override
            public void setValue(Object value) {
                comboBox.selectItemByValue(value);
            }

            @Override
            public Object getCellEditorValue() {
                return comboBox.getKeyValue();
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent)anEvent;
                    return e.getID() != 506;
                }
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                if (comboBox.isEditable()) {
                    comboBox.actionPerformed(new ActionEvent(ExtendedDefaultCellEditor.this, 0, ""));
                }
                return super.stopCellEditing();
            }
        };
        comboBox.addActionListener(this.delegate);
    }

    public ExtendedDefaultCellEditor(final JTableWithDataSourceComboBox comboBox) {
        super(comboBox);
        comboBox.removeActionListener(this.delegate);
        this.delegate = new DefaultCellEditor.EditorDelegate(this){
            private static final long serialVersionUID = 1L;

            @Override
            public void setValue(Object value) {
                comboBox.selectItemByValue(value);
            }

            @Override
            public Object getCellEditorValue() {
                return comboBox.getKeyValue();
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent)anEvent;
                    return e.getID() != 506;
                }
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                if (comboBox.isEditable()) {
                    comboBox.actionPerformed(new ActionEvent(ExtendedDefaultCellEditor.this, 0, ""));
                }
                return super.stopCellEditing();
            }
        };
        comboBox.addActionListener(this.delegate);
    }

    public ExtendedDefaultCellEditor(final JLayerComboBox comboBox) {
        super(comboBox);
        comboBox.removeActionListener(this.delegate);
        this.delegate = new DefaultCellEditor.EditorDelegate(this){
            private static final long serialVersionUID = 1L;

            @Override
            public void setValue(Object value) {
                comboBox.selectItemByValue(value);
            }

            @Override
            public Object getCellEditorValue() {
                return comboBox.getKeyValue();
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent)anEvent;
                    return e.getID() != 506;
                }
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                if (comboBox.isEditable()) {
                    comboBox.actionPerformed(new ActionEvent(ExtendedDefaultCellEditor.this, 0, ""));
                }
                return super.stopCellEditing();
            }
        };
        comboBox.addActionListener(this.delegate);
    }
}

