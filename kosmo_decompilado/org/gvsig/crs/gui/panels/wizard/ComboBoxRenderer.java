/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.swing.JComboBox
 */
package org.gvsig.crs.gui.panels.wizard;

import com.iver.utiles.swing.JComboBox;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComboBoxRenderer
extends JComboBox
implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public ComboBoxRenderer(String[] items) {
        super((Object[])items);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            this.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            this.setForeground(table.getForeground());
            this.setBackground(table.getBackground());
        }
        this.setSelectedItem(value);
        return this;
    }
}

