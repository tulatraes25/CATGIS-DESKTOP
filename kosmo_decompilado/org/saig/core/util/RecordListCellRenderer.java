/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.saig.core.model.data.Record;
import org.saig.jump.lang.I18N;

public class RecordListCellRenderer
extends JLabel
implements ListCellRenderer {
    private static final long serialVersionUID = 998931616326375451L;
    private String name;

    public RecordListCellRenderer(String fieldName) {
        this.name = fieldName;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Color foreground;
        Color background;
        this.setText(this.buildJLabelText((Record)value));
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
            background = Color.RED;
            foreground = Color.WHITE;
        } else if (isSelected) {
            background = list.getSelectionBackground();
            foreground = Color.WHITE;
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }
        this.setBackground(background);
        this.setForeground(foreground);
        this.setEnabled(list.isEnabled());
        this.setFont(this.buildFont((Record)value, list));
        this.setOpaque(true);
        return this;
    }

    private Font buildFont(Record record, JList list) {
        Font fuente = list.getFont();
        if (record == null) {
            fuente = fuente.deriveFont(3);
            return fuente;
        }
        Object value = record.getAttribute(this.name);
        if (value == null) {
            fuente = fuente.deriveFont(2);
            return fuente;
        }
        return fuente;
    }

    protected String buildJLabelText(Record record) {
        if (record == null) {
            return I18N.getString(this.getClass(), "null-record");
        }
        Object value = record.getAttribute(this.name);
        if (value == null || value.toString().trim().isEmpty()) {
            return I18N.getString(this.getClass(), "empty-field");
        }
        return value.toString();
    }
}

