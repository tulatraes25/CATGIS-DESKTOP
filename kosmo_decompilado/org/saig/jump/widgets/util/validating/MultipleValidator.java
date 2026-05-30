/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class MultipleValidator
extends AbstractValidator {
    AbstractValidator[] validators;

    public MultipleValidator(JDialog parent, JComponent c, AbstractValidator[] validators) {
        super(parent, c, "");
        this.validators = validators;
    }

    public MultipleValidator(JDialog parent, JComponent c, List<AbstractValidator> validatorsList) {
        super(parent, c, "");
        this.validators = new AbstractValidator[validatorsList.size()];
        validatorsList.toArray(this.validators);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        int i = 0;
        while (i < this.validators.length) {
            if (!this.validators[i].validationCriteria(c)) {
                this.setMessage(this.validators[i].getMessage());
                return false;
            }
            ++i;
        }
        return true;
    }
}

