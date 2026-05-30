/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.ImmutableFirstElementList;
import com.vividsolutions.jump.workbench.ui.ColumnBasedTableModel;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel;
import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class ColorThemingTableModel
extends ColumnBasedTableModel {
    public static final String COLOR_COLUMN_TITLE = I18N.getString("workbench.ui.renderer.style.ColorThemingTableModel.colour");
    public static final String ATTRIBUTE_COLUMN_TITLE = I18N.getString("workbench.ui.renderer.style.ColorThemingTableModel.attribute-value");
    protected String attributeName;
    public static final int COLOR_COLUMN = 0;
    public static final int ATTRIBUTE_COLUMN = 1;
    private boolean lastSortAscending = true;
    protected List<AttributeMapping> attributeMappings;

    public ColorThemingTableModel(BasicStyle defaultStyle, String attributeName, Map<Object, BasicStyle> attributeValueToBasicStyleMap, FeatureSchema schema) {
        this.attributeMappings = new ImmutableFirstElementList<AttributeMapping>(new AttributeMapping(null, defaultStyle));
        this.attributeName = attributeName;
        this.setAttributeValueToBasicStyleMap(attributeValueToBasicStyleMap);
        this.setColumns(this.createColumns(schema));
    }

    public void setAttributeValueToBasicStyleMap(Map<Object, BasicStyle> map) {
        this.attributeMappings.clear();
        for (Object attributeValue : map.keySet()) {
            this.attributeMappings.add(new AttributeMapping(attributeValue, map.get(attributeValue)));
        }
        this.fireTableChanged(new TableModelEvent(this));
    }

    public void clear() {
        this.attributeMappings.clear();
        this.fireTableChanged(new TableModelEvent(this));
    }

    public boolean containsNullAttributeValues() {
        for (AttributeMapping attributeMapping : this.nonDefaultAttributeMappings()) {
            if (attributeMapping.getAttributeValue() != null) continue;
            return true;
        }
        return false;
    }

    protected AttributeMapping attributeMapping(int i) {
        return this.attributeMappings.get(i);
    }

    public BasicStyle getDefaultStyle() {
        return this.attributeMapping(0).getBasicStyle();
    }

    public Object findDuplicateAttributeValue() {
        TreeSet<Object> set = new TreeSet<Object>();
        for (AttributeMapping attributeMapping : this.nonDefaultAttributeMappings()) {
            if (attributeMapping.getAttributeValue() == null) continue;
            if (set.contains(attributeMapping.getAttributeValue())) {
                return attributeMapping.getAttributeValue();
            }
            set.add(attributeMapping.getAttributeValue());
        }
        return null;
    }

    protected List<ColumnBasedTableModel.Column> createColumns(final FeatureSchema schema) {
        ArrayList<ColumnBasedTableModel.Column> columns = new ArrayList<ColumnBasedTableModel.Column>();
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(COLOR_COLUMN_TITLE, COLOR_COLUMN_TITLE, true, null), BasicStyle.class){

            @Override
            public Object getValueAt(int rowIndex) {
                return ColorThemingTableModel.this.attributeMapping(rowIndex).getBasicStyle();
            }

            @Override
            public void setValueAt(Object value, int rowIndex) {
                ColorThemingTableModel.this.attributeMapping(rowIndex).setBasicStyle((BasicStyle)value);
                ColorThemingTableModel.this.fireTableChanged(new TableModelEvent(ColorThemingTableModel.this, rowIndex));
            }
        });
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(ATTRIBUTE_COLUMN_TITLE, ATTRIBUTE_COLUMN_TITLE, true, null), null){

            @Override
            public Class<?> getDataClass() {
                return ColorThemingTableModel.this.attributeName == null || !schema.hasAttribute(ColorThemingTableModel.this.attributeName) ? Object.class : schema.getAttributeType(ColorThemingTableModel.this.attributeName).toJavaClass();
            }

            @Override
            public Object getValueAt(int rowIndex) {
                return ColorThemingTableModel.this.attributeMapping(rowIndex).getAttributeValue();
            }

            @Override
            public void setValueAt(Object value, int rowIndex) {
                ColorThemingTableModel.this.attributeMapping(rowIndex).setAttributeValue(value);
                ColorThemingTableModel.this.fireTableChanged(new AttributeValueTableModelEvent(ColorThemingTableModel.this, rowIndex));
            }
        });
        return columns;
    }

    public void apply(ColorScheme colorScheme, boolean skipDefaultAttributeMapping) {
        BasicStyle defaultStyle = ColorThemingStylePanel.getDefaultBasicStyle();
        for (AttributeMapping attributeMapping : skipDefaultAttributeMapping ? this.nonDefaultAttributeMappings() : this.attributeMappings) {
            Color currentColor = colorScheme.next();
            BasicStyle style = new BasicStyle(currentColor);
            style.setFillColor(currentColor);
            style.setLineWidth(defaultStyle.getLineWidth());
            style.setRenderingFill(defaultStyle.isRenderingFill());
            style.setRenderingLine(defaultStyle.isRenderingLine());
            style.setRenderingLinePattern(defaultStyle.isRenderingLinePattern());
            style.setRenderingFillPattern(defaultStyle.isRenderingFillPattern());
            style.setAlpha(defaultStyle.getAlpha());
            style.setFillPattern((Paint)((BasicFillPattern)defaultStyle.getFillPattern()).clone());
            style.setLinePattern(defaultStyle.getLinePattern());
            attributeMapping.setBasicStyle(style);
        }
        this.fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public int getRowCount() {
        return this.attributeMappings.size();
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Map<Object, BasicStyle> getAttributeValueToBasicStyleMap() {
        TreeMap<Object, BasicStyle> attributeValueToBasicStyleMap = new TreeMap<Object, BasicStyle>();
        for (AttributeMapping attributeMapping : this.nonDefaultAttributeMappings()) {
            attributeValueToBasicStyleMap.put(attributeMapping.getAttributeValue(), attributeMapping.getBasicStyle());
        }
        return attributeValueToBasicStyleMap;
    }

    public boolean wasLastSortAscending() {
        return this.lastSortAscending;
    }

    public void sort() {
        this.sort(!this.lastSortAscending);
    }

    public void sort(boolean ascending) {
        if (ascending) {
            Collections.sort(this.nonDefaultAttributeMappings());
        } else {
            Collections.sort(this.nonDefaultAttributeMappings(), Collections.reverseOrder());
        }
        this.lastSortAscending = ascending;
        this.fireTableChanged(new TableModelEvent(this));
    }

    public void removeAttributeValues(int[] rows) {
        for (Integer row : CollectionUtil.reverseSortedSet(rows)) {
            this.attributeMappings.remove(row);
            this.fireTableChanged(new TableModelEvent(this, row, row, -1, -1));
        }
    }

    public int insertAttributeValue(int row, ColorScheme colorScheme) {
        BasicStyle defaultStyle = ColorThemingStylePanel.getDefaultBasicStyle();
        Color currentColor = colorScheme.next();
        BasicStyle style = new BasicStyle(currentColor);
        style.setFillColor(currentColor);
        style.setLineWidth(defaultStyle.getLineWidth());
        style.setRenderingFill(defaultStyle.isRenderingFill());
        style.setRenderingLine(defaultStyle.isRenderingLine());
        style.setRenderingLinePattern(defaultStyle.isRenderingLinePattern());
        style.setRenderingFillPattern(defaultStyle.isRenderingFillPattern());
        style.setAlpha(defaultStyle.getAlpha());
        style.setFillPattern(defaultStyle.getFillPattern());
        style.setLinePattern(defaultStyle.getLinePattern());
        AttributeMapping attributeMapping = new AttributeMapping(null, style);
        this.attributeMappings.add(row, attributeMapping);
        this.fireTableChanged(new TableModelEvent(this, row, row, -1, 1));
        return row;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex != 0 || columnIndex != 1;
    }

    protected List<AttributeMapping> nonDefaultAttributeMappings() {
        return this.attributeMappings.subList(1, this.attributeMappings.size());
    }

    public Map<Object, Object> getLegende() {
        TreeMap<Object, Object> mapLegende = new TreeMap<Object, Object>();
        for (AttributeMapping attribute : this.nonDefaultAttributeMappings()) {
            mapLegende.put(attribute.getAttributeValue(), attribute.getAttributeValue());
        }
        return mapLegende;
    }

    protected static class AttributeMapping
    implements Comparable<AttributeMapping> {
        private Object attributeValue;
        private BasicStyle basicStyle;

        public AttributeMapping(Object attributeValue, BasicStyle basicStyle) {
            this.attributeValue = attributeValue;
            this.basicStyle = basicStyle;
        }

        public Object getAttributeValue() {
            return this.attributeValue;
        }

        public BasicStyle getBasicStyle() {
            return this.basicStyle;
        }

        @Override
        public int compareTo(AttributeMapping o) {
            if (this.attributeValue == null) {
                return -1;
            }
            if (o.attributeValue == null) {
                return 1;
            }
            return ((Comparable)this.attributeValue).compareTo((Comparable)o.attributeValue);
        }

        public void setAttributeValue(Object object) {
            this.attributeValue = object;
        }

        public void setBasicStyle(BasicStyle style) {
            this.basicStyle = style;
        }
    }

    public static class AttributeValueTableModelEvent
    extends TableModelEvent {
        private static final long serialVersionUID = 1L;

        public AttributeValueTableModelEvent(TableModel source, int row) {
            super(source, row);
        }
    }
}

