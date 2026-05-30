/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class OddIntegerTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.OddIntegerTextFieldValidator.the-field-value-must-be-an-odd-integer");
    private boolean obligatorio;

    public OddIntegerTextFieldValidator(JDialog parent, JComponent c) {
        super(parent, c, DEFAULT_MESSAGE);
    }

    public OddIntegerTextFieldValidator(JDialog parent, JComponent c, String message) {
        super(parent, c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        int currentValue;
        String value;
        block3: {
            JTextField textField = (JTextField)c;
            value = "";
            currentValue = 1;
            try {
                value = textField.getText().trim();
                if (!value.equals("")) break block3;
                return !this.obligatorio;
            }
            catch (Exception e) {
                return false;
            }
        }
        currentValue = Integer.parseInt(value);
        return currentValue % 2 != 0;
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

