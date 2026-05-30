/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.saig.core.util.NumberFormatManager;

public class NumberCellRenderer
extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value == null) {
            this.setHorizontalAlignment(0);
            this.setIcon(IconLoader.icon("null.gif"));
        } else {
            this.setHorizontalAlignment(2);
            this.setIcon(null);
        }
        if (value instanceof Number) {
            this.setText(NumberFormatManager.getFormattedValue((Number)value));
        }
        return this;
    }
}

