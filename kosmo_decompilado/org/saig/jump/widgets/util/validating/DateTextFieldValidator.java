/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class DateTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = String.valueOf(I18N.getString("org.saig.jump.widgets.util.validating.DateTextFieldValidator.the-date-must-have-the-format")) + " ";
    private DateFormat dateFormat;
    private boolean obligatorio = false;

    public DateTextFieldValidator(JDialog parent, JComponent c, String format) {
        super(parent, c, String.valueOf(DEFAULT_MESSAGE) + format);
        this.dateFormat = new SimpleDateFormat(format);
    }

    public DateTextFieldValidator(JDialog parent, JComponent c, String message, String format) {
        super(parent, c, message);
        this.dateFormat = new SimpleDateFormat(format);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        String value;
        block3: {
            JTextField textField = (JTextField)c;
            try {
                value = textField.getText().trim();
                if (!value.equals("")) break block3;
                return !this.obligatorio;
            }
            catch (Exception e) {
                return false;
            }
        }
        this.dateFormat.parse(value);
        return true;
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

