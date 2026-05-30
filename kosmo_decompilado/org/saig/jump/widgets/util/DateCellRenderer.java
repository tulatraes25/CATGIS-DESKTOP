/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Component;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.saig.core.util.DateFormatManager;

public class DateCellRenderer
extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Timestamp) {
            String strDate = DateFormatManager.getDateTimeFormat().format((Timestamp)value);
            this.setText(strDate);
        } else if (value instanceof Date) {
            String strDate = DateFormatManager.getDateFormat().format((Date)value);
            this.setText(strDate);
        }
        return this;
    }
}

