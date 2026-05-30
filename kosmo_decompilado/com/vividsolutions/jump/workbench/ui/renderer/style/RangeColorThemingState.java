/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.Range;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingTableModel;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class RangeColorThemingState
implements ColorThemingStylePanel.State {
    private ColorThemingStylePanel stylePanel;
    private static final String RANGE_COUNT_KEY = String.valueOf(RangeColorThemingState.class.getName()) + " - RANGE COUNT";
    private JPanel panel = new JPanel(new GridBagLayout()){

        @Override
        public void setEnabled(boolean enabled) {
            RangeColorThemingState.this.comboBox.setEnabled(enabled);
            RangeColorThemingState.this.label.setEnabled(enabled);
            RangeColorThemingState.this.reverseButton.setEnabled(enabled);
            super.setEnabled(enabled);
        }
    };
    private JLabel label;
    private JComboBox comboBox;
    private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
    private JButton reverseButton;
    private boolean reversingColorScheme = false;
    private TableModelListener tableModelListener = new TableModelListener(){

        @Override
        public void tableChanged(TableModelEvent e) {
            if (e instanceof ColorThemingTableModel.AttributeValueTableModelEvent) {
                Object attributeValue = RangeColorThemingState.this.stylePanel.tableModel().getValueAt(e.getFirstRow(), 1);
                RangeColorThemingState.this.stylePanel.tableModel().sort(RangeColorThemingState.this.stylePanel.tableModel().wasLastSortAscending());
            }
        }
    };

    public RangeColorThemingState(final ColorThemingStylePanel stylePanel) {
        this.stylePanel = stylePanel;
        this.addComboBoxItems();
        this.label = new JLabel(String.valueOf(I18N.getString("workbench.ui.renderer.style.RangeColorThemingState.range-count")) + ":");
        this.comboBox = new JComboBox(this.comboBoxModel);
        this.comboBox.setSelectedItem(stylePanel.getLayer().getLayerManager().getBlackboard().get(RANGE_COUNT_KEY, new Integer(5)));
        this.comboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                stylePanel.populateTable();
                stylePanel.getLayer().getLayerManager().getBlackboard().put(RANGE_COUNT_KEY, RangeColorThemingState.this.comboBox.getSelectedItem());
            }
        });
        this.reverseButton = new JButton(I18N.getString("workbench.ui.renderer.style.RangeColorThemingState.reverse-colors"));
        this.reverseButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                RangeColorThemingState.this.reversingColorScheme = !RangeColorThemingState.this.reversingColorScheme;
                stylePanel.applyColorScheme();
            }
        });
        FormUtils.addRowInGBL((JComponent)this.panel, 0, 0, (JComponent)this.label, false, true);
        FormUtils.addRowInGBL((JComponent)this.panel, 0, 1, (JComponent)this.comboBox, false, true);
        FormUtils.addRowInGBL((JComponent)this.panel, 0, 2, (JComponent)this.reverseButton, false, true);
    }

    @Override
    public String getAllOtherValuesDescription() {
        return "(" + I18N.getString("workbench.ui.renderer.style.RangeColorThemingState.values-below-these-values") + ")";
    }

    @Override
    public String getAttributeValueColumnTitle() {
        return I18N.getString("workbench.ui.renderer.style.RangeColorThemingState.minimum-attribute-values");
    }

    private int getRangeCount() {
        return (Integer)this.comboBox.getSelectedItem();
    }

    @Override
    public Collection filterAttributeValues(SortedSet attributeValues) {
        int maxFilteredSize = this.getRangeCount() - 1;
        ArrayList attributeValueList = new ArrayList(attributeValues);
        TreeSet filteredValues = new TreeSet();
        CollectionUtil.stretch(attributeValueList, filteredValues, maxFilteredSize);
        return filteredValues;
    }

    private void addComboBoxItems() {
        int maxColorSchemeSize = -1;
        for (String rangeColorSchemeName : ColorScheme.rangeColorSchemeNames()) {
            maxColorSchemeSize = Math.max(maxColorSchemeSize, ColorScheme.create(rangeColorSchemeName).getColors().size());
        }
        int i = 3;
        while (i <= maxColorSchemeSize) {
            this.comboBoxModel.addElement(new Integer(i));
            ++i;
        }
    }

    @Override
    public JComponent getPanel() {
        return this.panel;
    }

    @Override
    public Map fromExternalFormat(Map attributeValueToBasicStyleMap) {
        TreeMap newMap = new TreeMap();
        for (Range range : attributeValueToBasicStyleMap.keySet()) {
            newMap.put(range.getMin(), attributeValueToBasicStyleMap.get(range));
        }
        return newMap;
    }

    @Override
    public Map toExternalFormat(Map attributeValueToBasicStyleMap) {
        if (attributeValueToBasicStyleMap.isEmpty()) {
            return attributeValueToBasicStyleMap;
        }
        Assert.isTrue((boolean)(attributeValueToBasicStyleMap instanceof SortedMap));
        Range.RangeTreeMap newMap = new Range.RangeTreeMap();
        Object previousValue = null;
        for (Object value : attributeValueToBasicStyleMap.keySet()) {
            try {
                if (previousValue == null) continue;
                newMap.put(new Range(previousValue, true, value, false), attributeValueToBasicStyleMap.get(previousValue));
            }
            finally {
                previousValue = value;
            }
        }
        newMap.put(new Range(previousValue, true, new Range.PositiveInfinity(), false), attributeValueToBasicStyleMap.get(previousValue));
        return newMap;
    }

    @Override
    public void applyColorScheme(ColorScheme colorScheme) {
        this.stylePanel.tableModel().apply(new ColorScheme(null, CollectionUtil.stretch(colorScheme.getColors(), new ArrayList(), this.stylePanel.tableModel().getRowCount())), false);
    }

    @Override
    public Collection getColorSchemeNames() {
        return ColorScheme.rangeColorSchemeNames();
    }

    private int row(Object attributeValue) {
        int i = 0;
        while (i < this.stylePanel.tableModel().getRowCount()) {
            Object otherAttributeValue = this.stylePanel.tableModel().getValueAt(i, 1);
            if (attributeValue == null && otherAttributeValue == null) {
                return i;
            }
            if (attributeValue != null && attributeValue.equals(otherAttributeValue)) {
                return i;
            }
            ++i;
        }
        Assert.shouldNeverReachHere();
        return -1;
    }

    @Override
    public void activate() {
        this.stylePanel.tableModel().addTableModelListener(this.tableModelListener);
    }

    @Override
    public void deactivate() {
        this.stylePanel.tableModel().removeTableModelListener(this.tableModelListener);
    }

    @Override
    public ColorScheme filterColorScheme(ColorScheme colorScheme) {
        if (!this.reversingColorScheme) {
            return colorScheme;
        }
        ArrayList<Color> colors = new ArrayList<Color>(colorScheme.getColors());
        Collections.reverse(colors);
        return new ColorScheme(colorScheme.getName(), colors);
    }
}

