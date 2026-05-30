/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.AbstractValidator;
import org.saig.jump.widgets.util.validating.BetweenValidator;
import org.saig.jump.widgets.util.validating.GreaterOrEqualThanTextFieldValidator;
import org.saig.jump.widgets.util.validating.IntegerTextFieldValidator;
import org.saig.jump.widgets.util.validating.MultipleValidator;
import org.saig.jump.widgets.util.validating.NumericTextFieldValidator;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;
import org.saig.jump.widgets.util.validating.forms.BetweenFormValidator;
import org.saig.jump.widgets.util.validating.forms.GreaterOrEqualThanTextFieldFormValidator;
import org.saig.jump.widgets.util.validating.forms.IntegerTextFieldFormValidator;
import org.saig.jump.widgets.util.validating.forms.MultipleFormValidator;
import org.saig.jump.widgets.util.validating.forms.NumericTextFieldFormValidator;

public class ValidatingUtils {
    public static void assignNumericTextFieldValidator(JTextField textField, JDialog parent, boolean obligatory, double[] ranges) {
        NumericTextFieldValidator numericValidator = new NumericTextFieldValidator(parent, textField);
        numericValidator.setObligatorio(obligatory);
        AbstractValidator rangeValidator = null;
        if (ranges == null) {
            GreaterOrEqualThanTextFieldValidator goetValidator = new GreaterOrEqualThanTextFieldValidator(parent, (JComponent)textField, 0.0);
            goetValidator.setObligatorio(obligatory);
            rangeValidator = goetValidator;
        } else {
            BetweenValidator betweenValidator = new BetweenValidator(parent, textField, ranges[0], ranges[1]);
            rangeValidator = betweenValidator;
        }
        MultipleValidator validator = new MultipleValidator(parent, (JComponent)textField, new AbstractValidator[]{numericValidator, rangeValidator});
        textField.setInputVerifier(validator);
    }

    public static void assignIntegerTextFieldValidator(JTextField textField, JDialog parent, boolean obligatory, double[] ranges) {
        IntegerTextFieldValidator numericValidator = new IntegerTextFieldValidator(parent, textField);
        numericValidator.setObligatorio(obligatory);
        AbstractValidator rangeValidator = null;
        if (ranges == null) {
            GreaterOrEqualThanTextFieldValidator goetValidator = new GreaterOrEqualThanTextFieldValidator(parent, (JComponent)textField, 0.0);
            goetValidator.setObligatorio(obligatory);
            rangeValidator = goetValidator;
        } else {
            BetweenValidator betweenValidator = new BetweenValidator(parent, textField, ranges[0], ranges[1]);
            rangeValidator = betweenValidator;
        }
        MultipleValidator validator = new MultipleValidator(parent, (JComponent)textField, new AbstractValidator[]{numericValidator, rangeValidator});
        textField.setInputVerifier(validator);
    }

    public static void assignIntegerTextFieldFormValidator(JTextField textField, IStatusForm parent, boolean obligatory, double[] ranges) {
        IntegerTextFieldFormValidator numericValidator = new IntegerTextFieldFormValidator(parent, textField);
        numericValidator.setObligatorio(obligatory);
        AbstractFormValidator rangeValidator = null;
        if (ranges == null) {
            GreaterOrEqualThanTextFieldFormValidator goetValidator = new GreaterOrEqualThanTextFieldFormValidator(parent, (JComponent)textField, 0.0);
            goetValidator.setObligatorio(obligatory);
            rangeValidator = goetValidator;
        } else {
            BetweenFormValidator betweenValidator = new BetweenFormValidator(parent, textField, ranges[0], ranges[1]);
            rangeValidator = betweenValidator;
        }
        MultipleFormValidator validator = new MultipleFormValidator(parent, (JComponent)textField, new AbstractFormValidator[]{numericValidator, rangeValidator});
        textField.setInputVerifier(validator);
    }

    public static void assignNumericTextFieldFormValidator(JTextField textField, IStatusForm parent, boolean obligatory, double[] ranges) {
        NumericTextFieldFormValidator numericValidator = new NumericTextFieldFormValidator(parent, textField);
        numericValidator.setObligatorio(obligatory);
        AbstractFormValidator rangeValidator = null;
        if (ranges == null) {
            GreaterOrEqualThanTextFieldFormValidator goetValidator = new GreaterOrEqualThanTextFieldFormValidator(parent, (JComponent)textField, 0.0);
            goetValidator.setObligatorio(obligatory);
            rangeValidator = goetValidator;
        } else {
            BetweenFormValidator betweenValidator = new BetweenFormValidator(parent, textField, ranges[0], ranges[1]);
            rangeValidator = betweenValidator;
        }
        MultipleFormValidator validator = new MultipleFormValidator(parent, (JComponent)textField, new AbstractFormValidator[]{numericValidator, rangeValidator});
        textField.setInputVerifier(validator);
    }
}

