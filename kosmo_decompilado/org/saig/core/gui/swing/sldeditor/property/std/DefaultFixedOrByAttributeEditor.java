/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.GridBagLayout;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.FixedOrByAttributeExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFeatureAttributeChooser;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultNumberEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultFixedOrByAttributeEditor
extends FixedOrByAttributeExpressionEditor
implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private boolean hasNumericAttributes;
    private JRadioButton fixedValueRadioButton;
    private DefaultNumberEditor numberEditor;
    private JRadioButton attributeValueRadioButton;
    private DefaultFeatureAttributeChooser featureAttributeChooser;
    private boolean isEnabled = true;
    private JLabel unitsLabel;
    private JComboBox units;

    public DefaultFixedOrByAttributeEditor(FeatureSchema fs, boolean allowUOMSelection) {
        this.setLayout(new GridBagLayout());
        this.fixedValueRadioButton = new JRadioButton(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFixedOrByAttributeEditor.Fixed-value")) + ":");
        this.numberEditor = new DefaultNumberEditor();
        this.fixedValueRadioButton.setSelected(true);
        this.fixedValueRadioButton.addChangeListener(this);
        this.attributeValueRadioButton = new JRadioButton(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFixedOrByAttributeEditor.By-attribute")) + ":");
        this.featureAttributeChooser = new DefaultFeatureAttributeChooser(null, fs, new Class[]{Number.class});
        this.hasNumericAttributes = this.featureSchemaHasNumericAttributes(fs);
        this.attributeValueRadioButton.addChangeListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(this.fixedValueRadioButton);
        group.add(this.attributeValueRadioButton);
        if (allowUOMSelection) {
            this.unitsLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Units"));
            this.units = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        }
        FormUtils.addRowInGBL((JComponent)this, 0, 0, (JComponent)this.fixedValueRadioButton, this.numberEditor, 0.0, true);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, (JComponent)this.attributeValueRadioButton, this.featureAttributeChooser, 0.0, true);
        if (allowUOMSelection) {
            FormUtils.addRowInGBL((JComponent)this, 2, 0, (JComponent)this.unitsLabel, this.units, 0.0, true);
        }
        FormUtils.addFiller(this, 3, 0);
        this.refreshGUI();
    }

    public DefaultFixedOrByAttributeEditor(FeatureSchema fs, Number defaultValue, Number minimum, Number maximum, Number step, boolean allowUOMSelection) {
        this.setLayout(new GridBagLayout());
        this.fixedValueRadioButton = new JRadioButton(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFixedOrByAttributeEditor.Fixed-value")) + ":");
        this.numberEditor = new DefaultNumberEditor(defaultValue, minimum, maximum, step);
        this.fixedValueRadioButton.setSelected(true);
        this.fixedValueRadioButton.addChangeListener(this);
        this.attributeValueRadioButton = new JRadioButton(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFixedOrByAttributeEditor.By-attribute")) + ":");
        this.featureAttributeChooser = new DefaultFeatureAttributeChooser(null, fs, new Class[]{Number.class});
        this.hasNumericAttributes = this.featureSchemaHasNumericAttributes(fs);
        this.attributeValueRadioButton.addChangeListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(this.fixedValueRadioButton);
        group.add(this.attributeValueRadioButton);
        if (allowUOMSelection) {
            this.unitsLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Units"));
            this.units = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        }
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.fixedValueRadioButton, (JComponent)this.numberEditor);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.attributeValueRadioButton, (JComponent)this.featureAttributeChooser);
        if (allowUOMSelection) {
            FormUtils.addRowInGBL((JComponent)this, 2, 0, this.unitsLabel, (JComponent)this.units);
        }
        FormUtils.addFiller(this, 3, 0);
        this.refreshGUI();
    }

    protected boolean featureSchemaHasNumericAttributes(FeatureSchema fs) {
        boolean hasNumericAttributes = false;
        Iterator<AttributeType> itAttrTypes = fs.getAttributeTypes().iterator();
        while (itAttrTypes.hasNext() && !hasNumericAttributes) {
            AttributeType currentType = itAttrTypes.next();
            hasNumericAttributes = AttributeType.isNumeric(currentType);
        }
        return hasNumericAttributes;
    }

    @Override
    public boolean canEdit(Expression expression) {
        if (expression instanceof LiteralExpression) {
            LiteralExpression le = (LiteralExpression)expression;
            Object literal = le.getLiteral();
            if (literal instanceof Number) {
                return true;
            }
            if (literal instanceof String) {
                try {
                    Double.parseDouble((String)literal);
                    return true;
                }
                catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return expression instanceof AttributeExpression;
    }

    @Override
    public Expression getExpression() {
        Expression expr = null;
        expr = this.fixedValueRadioButton.isSelected() ? this.numberEditor.getExpression() : this.featureAttributeChooser.getExpression();
        return expr;
    }

    @Override
    public void setExpression(Expression expression) {
        if (expression instanceof LiteralExpression) {
            LiteralExpression literalExpr = (LiteralExpression)expression;
            this.fixedValueRadioButton.setSelected(true);
            this.numberEditor.setExpression(literalExpr);
        } else {
            AttributeExpression attrExpr = (AttributeExpression)expression;
            this.attributeValueRadioButton.setSelected(true);
            this.featureAttributeChooser.setExpression(attrExpr);
        }
        this.refreshGUI();
    }

    protected void refreshGUI() {
        this.numberEditor.setEnabled(this.isEnabled && this.fixedValueRadioButton.isSelected());
        this.featureAttributeChooser.setEnabled(this.isEnabled && this.hasNumericAttributes && this.attributeValueRadioButton.isSelected());
        this.fixedValueRadioButton.setEnabled(this.isEnabled);
        this.attributeValueRadioButton.setEnabled(this.isEnabled);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.refreshGUI();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        this.refreshGUI();
    }

    @Override
    public String getUnitsOfMeasurement() {
        if (this.units != null) {
            return (String)this.units.getSelectedItem();
        }
        return null;
    }

    @Override
    public void setUnitsOfMeasurement(String unitsOfMeasurement) {
        if (this.units != null) {
            if (StringUtils.isNotEmpty((String)unitsOfMeasurement)) {
                this.units.setSelectedItem(unitsOfMeasurement);
            } else {
                this.units.setSelectedItem("pixel");
            }
        }
    }
}

