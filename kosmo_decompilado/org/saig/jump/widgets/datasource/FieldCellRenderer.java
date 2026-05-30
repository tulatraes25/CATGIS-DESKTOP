/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.datasource;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.saig.core.dao.datasource.dbdatasource.utils.Field;
import org.saig.jump.lang.I18N;

public class FieldCellRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        Field field = (Field)value;
        if (field != null) {
            Font font = this.getFont();
            Font newFont = null;
            String text = field.getName();
            Field.FieldType type = field.getType();
            if (type == Field.FieldType.PK_FIELD) {
                newFont = font.deriveFont(1);
                this.setForeground(Color.RED);
                text = String.valueOf(text) + " - " + I18N.getString(this.getClass(), "primary-key") + " (" + I18N.getString(this.getClass(), "recommended") + ")";
            } else if (type == Field.FieldType.UNIQUE_FIELD) {
                newFont = font.deriveFont(2);
                this.setForeground(Color.BLACK);
                text = String.valueOf(text) + " - " + I18N.getString(this.getClass(), "unique-key");
            } else if (type == Field.FieldType.REGULAR_FIELD) {
                this.setForeground(Color.BLACK);
                newFont = font;
            } else {
                newFont = font;
            }
            this.setFont(newFont);
            this.setText(text);
        }
        return this;
    }
}

