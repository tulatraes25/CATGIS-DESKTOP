/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import com.vividsolutions.jump.feature.FeatureUtil;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.saig.core.util.NumberFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public class BetweenFormValidator
extends AbstractFormValidator {
    private double minimumValue = 0.0;
    private double maximumValue = 0.0;

    public BetweenFormValidator(IStatusForm parent, JTextField textField, double minValue, double maxValue) {
        super(parent, (JComponent)textField, I18N.getMessage("org.saig.jump.widgets.util.validating.BetweenValidator.Field-value-must-be-between-{0}-and-{1}", new Object[]{new Double(minValue), new Double(maxValue)}));
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
    }

    public BetweenFormValidator(IStatusForm parent, JTextField textField, double minValue, double maxValue, String message) {
        super(parent, (JComponent)textField, message);
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        String value;
        double currentValue;
        block7: {
            JTextField textField = (JTextField)c;
            currentValue = 0.0;
            value = "";
            value = textField.getText().trim();
            if (!value.equals("")) break block7;
            return true;
        }
        try {
            currentValue = Double.parseDouble(value);
        }
        catch (Exception e) {
            try {
                currentValue = NumberFormatManager.getDefaultNumberFormat().parse(value).doubleValue();
            }
            catch (Exception e1) {
                value = FeatureUtil.convert(value);
                try {
                    currentValue = Double.valueOf(value);
                }
                catch (Exception e2) {
                    return false;
                }
            }
        }
        return this.minimumValue <= currentValue && currentValue <= this.maximumValue;
    }
}

