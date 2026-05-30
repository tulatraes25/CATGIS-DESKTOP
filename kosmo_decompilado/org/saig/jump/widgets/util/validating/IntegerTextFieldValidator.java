/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import com.vividsolutions.jump.feature.FeatureUtil;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class IntegerTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.IntegerTextFieldValidator.the-field-value-must-be-an-integer");
    private boolean obligatorio;

    public IntegerTextFieldValidator(JDialog parent, JComponent c) {
        super(parent, c, DEFAULT_MESSAGE);
    }

    public IntegerTextFieldValidator(JDialog parent, JComponent c, String message) {
        super(parent, c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        String value;
        block5: {
            JTextField textField = (JTextField)c;
            value = "";
            value = textField.getText().trim();
            if (!value.equals("")) break block5;
            return !this.obligatorio;
        }
        try {
            Integer.parseInt(value);
        }
        catch (Exception e) {
            value = FeatureUtil.convert(value);
            try {
                Integer.parseInt(value);
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

