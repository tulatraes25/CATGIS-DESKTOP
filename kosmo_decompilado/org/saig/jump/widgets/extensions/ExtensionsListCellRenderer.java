/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.extensions;

import com.vividsolutions.jump.workbench.plugin.Extension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ExtensionsListCellRenderer
extends JPanel
implements ListCellRenderer {
    private Font font = this.getFont().deriveFont(1);
    private JCheckBox checkbox = new JCheckBox();

    public ExtensionsListCellRenderer() {
        this.setOpaque(true);
        this.checkbox.setFont(this.font);
        this.checkbox.setOpaque(true);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.add(this.checkbox);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Extension extension = (Extension)value;
        this.checkbox.setText(String.valueOf(extension.getName()) + " - " + extension.getVersion());
        this.checkbox.setSelected(extension.isActive());
        if (isSelected) {
            this.checkbox.setForeground(list.getSelectionForeground());
            this.checkbox.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
            this.setBackground(list.getSelectionBackground());
        } else {
            this.checkbox.setForeground(list.getForeground());
            this.checkbox.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
        }
        this.setEnabled(list.isEnabled());
        this.setFont(this.font);
        return this.checkbox;
    }
}

