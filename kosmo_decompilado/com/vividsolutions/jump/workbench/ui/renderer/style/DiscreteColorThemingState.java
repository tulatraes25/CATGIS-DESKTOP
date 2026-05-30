/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingTableModel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.saig.jump.lang.I18N;

public class DiscreteColorThemingState
implements ColorThemingStylePanel.State {
    private JTable table;
    private JPanel panel = new JPanel(new GridBagLayout());

    @Override
    public String getAllOtherValuesDescription() {
        return "(" + I18N.getString("workbench.ui.renderer.style.DiscreteColorThemingState.all-other-values") + ")";
    }

    @Override
    public String getAttributeValueColumnTitle() {
        return I18N.getString("workbench.ui.renderer.style.DiscreteColorThemingState.attribute-values");
    }

    @Override
    public ColorScheme filterColorScheme(ColorScheme colorScheme) {
        return colorScheme;
    }

    @Override
    public Collection filterAttributeValues(SortedSet attributeValues) {
        return attributeValues;
    }

    @Override
    public JComponent getPanel() {
        this.panel.setPreferredSize(new Dimension(100, 1));
        this.panel.setMaximumSize(new Dimension(100, 1));
        return this.panel;
    }

    @Override
    public Map toExternalFormat(Map attributeValueToBasicStyleMap) {
        return attributeValueToBasicStyleMap;
    }

    @Override
    public Map fromExternalFormat(Map attributeValueToBasicStyleMap) {
        return attributeValueToBasicStyleMap;
    }

    @Override
    public void applyColorScheme(ColorScheme colorScheme) {
        ((ColorThemingTableModel)this.table.getModel()).apply(colorScheme, false);
    }

    @Override
    public Collection getColorSchemeNames() {
        return ColorScheme.discreteColorSchemeNames();
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    public DiscreteColorThemingState(JTable table) {
        this.table = table;
    }
}

