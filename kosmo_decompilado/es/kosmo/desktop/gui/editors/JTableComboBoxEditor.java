/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.editors;

import javax.swing.DefaultCellEditor;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.tables.JTableComboBox;
import org.saig.core.model.data.Table;

public class JTableComboBoxEditor
extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;

    public JTableComboBoxEditor(Table table, String keyField, String fieldToShow, String fieldOrderer) {
        super(new JTableComboBox(table, keyField, fieldToShow, fieldOrderer));
        ((JTableComboBox)this.editorComponent).setSelectedItem("----------");
    }

    public void setFilter(Filter filter) {
        JTableComboBox cBox = (JTableComboBox)this.editorComponent;
        cBox.setFilter(filter);
        if (cBox.getValue() == null) {
            cBox.setSelectedItem("----------");
        }
    }
}

