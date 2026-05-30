/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JComboBox;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;

public class DefaultFeatureAttributeChooser
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;
    protected JComboBox cmbAttributes;
    protected String[] attNames;
    protected Class<?>[] filterClass;

    public DefaultFeatureAttributeChooser(String selectedAttribute, FeatureSchema ft, Class<?>[] filterClass) {
        this.filterClass = filterClass;
        ArrayList<String> attNamesList = new ArrayList<String>();
        int i = 0;
        while (i < ft.getAttributeCount()) {
            if (filterClass != null) {
                AttributeType type = ft.getAttributeType(i);
                boolean hecho = false;
                int j = 0;
                while (j < filterClass.length && !hecho) {
                    if (filterClass[j].isAssignableFrom(type.toJavaClass())) {
                        attNamesList.add(ft.getAttributeName(i));
                        hecho = true;
                    }
                    ++j;
                }
            } else {
                attNamesList.add(ft.getAttributeName(i));
            }
            ++i;
        }
        this.attNames = new String[attNamesList.size()];
        attNamesList.toArray(this.attNames);
        this.cmbAttributes = new JComboBox<String>(this.attNames);
        if (selectedAttribute == null && this.attNames.length > 0) {
            selectedAttribute = this.attNames[0];
        }
        this.setChosenAttribute(selectedAttribute);
        this.setLayout(new BorderLayout());
        this.add(this.cmbAttributes);
    }

    public DefaultFeatureAttributeChooser(FeatureSchema ft) {
        this(null, ft, null);
    }

    public DefaultFeatureAttributeChooser(String selectedAttribute, FeatureSchema ft) {
        this(selectedAttribute, ft, null);
    }

    public void setChosenAttribute(String attribute) {
        int selectedIndex = -1;
        int i = 0;
        while (i < this.attNames.length) {
            if (this.attNames[i].equalsIgnoreCase(attribute)) {
                selectedIndex = i;
                break;
            }
            ++i;
        }
        if (selectedIndex != -1) {
            this.cmbAttributes.setSelectedIndex(selectedIndex);
        }
    }

    public String getChosenAttribute() {
        return (String)this.cmbAttributes.getSelectedItem();
    }

    @Override
    public void setExpression(Expression exp) {
        if (exp instanceof AttributeExpression) {
            AttributeExpression ae = (AttributeExpression)exp;
            this.setChosenAttribute(ae.getAttributePath());
        } else if (exp instanceof LiteralExpression) {
            LiteralExpression le = (LiteralExpression)exp;
            this.setChosenAttribute(le.getLiteral().toString());
        }
    }

    @Override
    public Expression getExpression() {
        String chosen = this.getChosenAttribute();
        if (chosen == null) {
            return null;
        }
        try {
            return styleBuilder.attributeExpression(chosen);
        }
        catch (IllegalFilterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean canEdit(Expression expression) {
        return expression instanceof AttributeExpression;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.cmbAttributes.setEnabled(enabled);
    }
}

