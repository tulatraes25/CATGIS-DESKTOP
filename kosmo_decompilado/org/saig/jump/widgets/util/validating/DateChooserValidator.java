/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package org.saig.jump.widgets.util.validating;

import com.toedter.calendar.JDateChooser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class DateChooserValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = String.valueOf(I18N.getString("org.saig.jump.widgets.util.validating.DateTextFieldValidator.the-date-must-have-the-format")) + " ";
    private DateFormat dateFormat;

    public DateChooserValidator(JDialog parent, JComponent c, String format) {
        super(parent, c, String.valueOf(DEFAULT_MESSAGE) + format);
        this.dateFormat = new SimpleDateFormat(format);
    }

    public DateChooserValidator(JDialog parent, JComponent c, String message, String format) {
        super(parent, c, message);
        this.dateFormat = new SimpleDateFormat(format);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JDateChooser dateChooser = (JDateChooser)c;
        Date value = dateChooser.getDate();
        if (value == null) {
            return ((JTextField)dateChooser.getDateEditor().getUiComponent()).getText().equals("__/__/____");
        }
        return true;
    }
}

