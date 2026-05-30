/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import javax.swing.JComponent;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public abstract class PatternTextFieldFormValidator
extends AbstractFormValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString(PatternTextFieldFormValidator.class, "field-value-must-satisfy-the-pattern");
    protected String pattern = "";

    public PatternTextFieldFormValidator(IStatusForm parent, JTextField t, String pattern) {
        this(parent, t, String.valueOf(DEFAULT_MESSAGE) + " (" + pattern + ")", pattern);
    }

    public PatternTextFieldFormValidator(IStatusForm parent, JTextField t, String message, String pattern) {
        super(parent, (JComponent)t, message);
        this.pattern = pattern;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField t = (JTextField)c;
        return this.checkPattern(t.getText());
    }

    protected abstract boolean checkPattern(String var1);
}

