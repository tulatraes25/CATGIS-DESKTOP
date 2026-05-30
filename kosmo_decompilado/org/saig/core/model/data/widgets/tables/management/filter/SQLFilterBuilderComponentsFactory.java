/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.Component;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderConstants;
import org.saig.core.model.feature.Attribute;

public class SQLFilterBuilderComponentsFactory {
    public static JComboBox getLogicOperatorsCombo() {
        JComboBox cbbLogicConnector = new JComboBox(SQLFilterBuilderComponentsFactory.getLogicOperatorsComboModel());
        return cbbLogicConnector;
    }

    public static ComboBoxModel getLogicOperatorsComboModel() {
        Object[] values = new Object[]{"AND     ", "OR      "};
        return new DefaultComboBoxModel<Object>(values);
    }

    public static JComboBox getComparationOperatorsCombo(AttributeType type) {
        JComboBox combo = new JComboBox(SQLFilterBuilderComponentsFactory.getComparationOperatorsComboModel(type));
        return combo;
    }

    public static ComboBoxModel getComparationOperatorsComboModel(AttributeType type) {
        Vector<String> values = new Vector<String>();
        if (type.equals(AttributeType.GEOMETRY)) {
            values.add(SQLFilterBuilderConstants.OP_LIKE);
            values.add(SQLFilterBuilderConstants.OP_NOT_LIKE);
            values.add(SQLFilterBuilderConstants.OP_IS_BLANK);
            values.add(SQLFilterBuilderConstants.OP_IS_NOT_BLANK);
        } else {
            values.add(SQLFilterBuilderConstants.OP_EQUALS);
            values.add(SQLFilterBuilderConstants.OP_DOES_NOT_EQUALS);
            values.add(SQLFilterBuilderConstants.OP_IS_LESS_THAN);
            values.add(SQLFilterBuilderConstants.OP_IS_LESS_THAN_OR_EQUAL_TO);
            values.add(SQLFilterBuilderConstants.OP_IS_GREATER_THAN);
            values.add(SQLFilterBuilderConstants.OP_IS_GREATER_THAN_OR_EQUAL_TO);
            values.add(SQLFilterBuilderConstants.OP_IS_BLANK);
            values.add(SQLFilterBuilderConstants.OP_IS_NOT_BLANK);
            values.add(SQLFilterBuilderConstants.OP_BETWEEN);
            values.add(SQLFilterBuilderConstants.OP_NOT_BETWEEN);
            values.add(SQLFilterBuilderConstants.OP_IN);
            values.add(SQLFilterBuilderConstants.OP_NOT_IN);
            if (type.equals(AttributeType.STRING)) {
                values.add(SQLFilterBuilderConstants.OP_LIKE);
                values.add(SQLFilterBuilderConstants.OP_NOT_LIKE);
            } else if (!AttributeType.isNumeric(type)) {
                AttributeType.isDate(type);
            }
        }
        Collections.sort(values);
        DefaultComboBoxModel model = new DefaultComboBoxModel(values);
        return model;
    }

    public static JComboBox getAttributesCombo(FeatureSchema fs) {
        JComboBox cbbAttribute = new JComboBox(SQLFilterBuilderComponentsFactory.getAttributesComboModel(fs));
        cbbAttribute.setRenderer(new ListCellRenderer(){

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Attribute) {
                    String text = ((Attribute)value).getName();
                    JLabel label = new JLabel(text);
                    label.setToolTipText(text);
                    label.setOpaque(true);
                    if (isSelected) {
                        label.setBackground(list.getSelectionBackground());
                        label.setForeground(list.getSelectionForeground());
                    } else {
                        label.setBackground(list.getBackground());
                        label.setForeground(list.getForeground());
                    }
                    return label;
                }
                return new JLabel("");
            }
        });
        return cbbAttribute;
    }

    public static ComboBoxModel getAttributesComboModel(FeatureSchema fs) {
        Vector<Attribute> values = new Vector<Attribute>();
        Iterator<Attribute> it = fs.getAttributes().values().iterator();
        while (it.hasNext()) {
            values.add(it.next());
        }
        return new DefaultComboBoxModel(values);
    }
}

