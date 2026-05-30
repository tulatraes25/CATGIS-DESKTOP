/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.model.DefaultObjectRenderer
 *  com.l2fprod.common.model.ObjectRenderer
 *  com.l2fprod.common.swing.renderer.DefaultCellRenderer
 */
package org.saig.jump.widgets.editing;

import com.l2fprod.common.model.DefaultObjectRenderer;
import com.l2fprod.common.model.ObjectRenderer;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTable;

public class ConstrainedCellRenderer
extends DefaultCellRenderer {
    private Color backgroundColor = Color.CYAN;
    private Color foregroundColor = Color.BLACK;
    private ObjectRenderer objectRenderer = new DefaultObjectRenderer();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setBackground(this.backgroundColor);
        this.setForeground(this.foregroundColor);
        this.setValue(value);
        return this;
    }

    public void setValue(Object value) {
        String text = this.convertToString(value);
        Icon icon = this.convertToIcon(value);
        this.setText(text == null ? "" : text);
        this.setIcon(icon);
    }

    protected String convertToString(Object value) {
        return this.objectRenderer.getText(value);
    }

    protected Icon convertToIcon(Object value) {
        return null;
    }
}

