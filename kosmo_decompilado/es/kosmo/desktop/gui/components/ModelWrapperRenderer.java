/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ModelWrapperRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            return this;
        }
        this.setText(value.toString());
        return this;
    }
}

