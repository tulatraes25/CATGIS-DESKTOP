/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Rule;

public class ListRuleRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private static final int ICON_SIZE = 15;
    private static final Color SELECTION_BG_COLOR = new Color(51, 153, 255);
    private static final Color SELECTION_FG_COLOR = Color.WHITE;
    private Feature sample = null;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        if (isSelected) {
            this.setForeground(SELECTION_FG_COLOR);
            this.setBackground(SELECTION_BG_COLOR);
        } else {
            this.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
        }
        if (value instanceof Rule) {
            Rule rule = (Rule)value;
            if (this.sample == null || !this.sample.getSchema().hasAttribute("IMAGE")) {
                this.setIcon(LegendIconMaker.makeLegendIcon(15, rule, this.sample));
            } else {
                this.setIcon(null);
            }
            if (!rule.isEnabled()) {
                this.setForeground(Color.LIGHT_GRAY);
            }
        }
        this.setOpaque(true);
        return this;
    }
}

