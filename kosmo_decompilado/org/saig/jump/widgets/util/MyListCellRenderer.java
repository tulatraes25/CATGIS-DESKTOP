/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class MyListCellRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private Font font = this.getFont().deriveFont(1);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        if (index % 2 == 0) {
            this.setBackground(new Color(227, 254, 221));
        } else {
            this.setBackground(new Color(214, 236, 238));
        }
        if (isSelected) {
            this.setBackground(Color.BLUE);
            this.setForeground(Color.WHITE);
        } else {
            this.setBorder(BorderFactory.createLineBorder(this.getBackground(), 2));
            this.setForeground(Color.BLACK);
        }
        this.setFont(this.font);
        return this;
    }
}

