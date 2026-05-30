/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.locale;

import java.awt.Component;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.saig.core.util.LocaleIconFactory;

public class LanguageRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        Locale language = (Locale)value;
        this.setHorizontalAlignment(2);
        if (language != null) {
            this.setIcon(LocaleIconFactory.getIcon(language));
            this.setText(language.getDisplayName());
        }
        return this;
    }
}

