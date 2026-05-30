/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import java.util.List;
import javax.swing.JComponent;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public class MultipleFormValidator
extends AbstractFormValidator {
    private AbstractFormValidator[] validators;

    public MultipleFormValidator(IStatusForm parent, JComponent c, AbstractFormValidator[] validators) {
        super(parent, c, "");
        this.validators = validators;
    }

    public MultipleFormValidator(IStatusForm parent, JComponent c, List<AbstractFormValidator> validatorsList) {
        super(parent, c, "");
        this.validators = new AbstractFormValidator[validatorsList.size()];
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

