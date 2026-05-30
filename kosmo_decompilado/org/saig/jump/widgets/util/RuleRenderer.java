/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.lang.StringUtils;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;

public class RuleRenderer
extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;
    public static final int ICON_SIZE = 15;
    private static final Color SELECTION_BG_COLOR = new Color(51, 153, 255);
    private static final Color SELECTION_FG_COLOR = Color.WHITE;
    private Feature sample = null;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Rule rule;
        if (value instanceof Rule) {
            rule = (Rule)((RuleImpl)value).clone();
            rule.setName(String.valueOf(rule.getName()) + (rule.getTitle() != null ? " - " + rule.getTitle() : ""));
            super.getTreeCellRendererComponent(tree, rule, sel, expanded, leaf, row, hasFocus);
        } else {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
        if (this.selected) {
            this.setForeground(SELECTION_FG_COLOR);
            this.setBackground(SELECTION_BG_COLOR);
        } else {
            this.setForeground(tree.getForeground());
            this.setBackground(tree.getBackground());
        }
        if (value instanceof Rule) {
            rule = (Rule)value;
            AbstractLayerable layer = null;
            if (tree.getPathForRow(row) != null) {
                Object component = tree.getPathForRow(row).getParentPath().getLastPathComponent();
                if (component instanceof Layer) {
                    layer = (Layer)component;
                    String layerName = layer.getName();
                    if (!layerName.equals(rule.getTitle())) {
                        this.setText(rule.getTitle());
                    } else {
                        this.setText("");
                    }
                }
                if (StringUtils.isNotEmpty((String)rule.getAbstract())) {
                    this.setToolTipText(rule.getAbstract());
                } else {
                    this.setToolTipText(null);
                }
            }
            if (layer != null) {
                if (layer.isEnabled()) {
                    if (this.sample == null || !this.sample.getSchema().hasAttribute("IMAGE")) {
                        this.setIcon(LegendIconMaker.makeLegendIcon(15, rule, this.sample));
                    } else {
                        this.setIcon(null);
                    }
                } else {
                    this.setIcon(null);
                }
            } else if (this.sample == null || !this.sample.getSchema().hasAttribute("IMAGE")) {
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

    public void setFeatureSample(Feature sample) {
        this.sample = sample;
    }
}

