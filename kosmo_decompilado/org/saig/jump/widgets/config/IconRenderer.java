/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class IconRenderer
extends DefaultTableCellRenderer {
    IconRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Icon) {
            label.setText(null);
            label.setIcon((Icon)value);
        }
        return label;
    }
}

