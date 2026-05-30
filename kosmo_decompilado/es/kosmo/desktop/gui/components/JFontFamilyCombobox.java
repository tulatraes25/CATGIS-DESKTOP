/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

public class JFontFamilyCombobox
extends JComboBox {
    private static final long serialVersionUID = 1L;

    public JFontFamilyCombobox(int fontSize) {
        this.setRenderer(new JFontChooserComboBoxRenderer(fontSize));
        this.setModel(new DefaultComboBoxModel<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
    }

    private class JFontChooserComboBoxRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        protected int fontSize;

        public JFontChooserComboBoxRenderer(int size) {
            this.fontSize = size;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JComponent comp = (JComponent)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            Font f = this.getFontForFontItem((String)value);
            comp.setFont(f);
            return comp;
        }

        private Font getFontForFontItem(String txt) {
            Font f = new Font(txt, 0, this.fontSize);
            return f;
        }
    }
}

