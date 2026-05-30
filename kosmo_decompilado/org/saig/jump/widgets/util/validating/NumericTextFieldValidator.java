/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util.validating;

import com.vividsolutions.jump.feature.FeatureUtil;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class NumericTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.EvenIntegerTextFieldValidator.the-field-value-must-be-a-number");
    private boolean obligatorio;

    public NumericTextFieldValidator(JDialog parent, JComponent c) {
        super(parent, c, DEFAULT_MESSAGE);
    }

    public NumericTextFieldValidator(JDialog parent, JComponent c, String message) {
        super(parent, c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField textField = (JTextField)c;
        String value = "";
        try {
            value = StringUtils.trimToEmpty((String)textField.getText());
            if (StringUtils.isEmpty((String)value)) {
                return !this.obligatorio;
            }
            Double.valueOf(value).doubleValue();
        }
        catch (Exception e) {
            value = FeatureUtil.convert(value);
            try {
                Double.valueOf(value);
            }
            catch (Exception e2) {
                return false;
            }
        }
        return true;
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

