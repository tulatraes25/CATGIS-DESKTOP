/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class ValidatingTextField
extends JTextField {
    public static final Validator LONG_VALIDATOR = new Validator(){

        @Override
        public boolean isValid(String text) {
            try {
                Long.parseLong(String.valueOf(text.trim()) + "0");
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    };
    public static final Validator INTEGER_VALIDATOR = new Validator(){

        @Override
        public boolean isValid(String text) {
            try {
                Integer.parseInt(String.valueOf(text.trim()) + "0");
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    };
    public static final Validator DOUBLE_VALIDATOR = new Validator(){

        @Override
        public boolean isValid(String text) {
            try {
                Double.parseDouble(String.valueOf(text.trim()) + "0");
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    };
    public static Cleaner DUMMY_CLEANER = new Cleaner(){

        @Override
        public String clean(String text) {
            return text;
        }
    };
    public static Cleaner NUMBER_CLEANER = new Cleaner(){

        @Override
        public String clean(String text) {
            try {
                Double.parseDouble(text.trim());
                return text;
            }
            catch (NumberFormatException e) {
                return "0";
            }
        }
    };
    public static Validator DUMMY_VALIDATOR = new Validator(){

        @Override
        public boolean isValid(String text) {
            return true;
        }
    };
    private Cleaner cleaner;

    public ValidatingTextField(String text, int columns, Validator validator) {
        this(text, columns, 2, validator, DUMMY_CLEANER);
    }

    public ValidatingTextField(String text, int columns, int horizontalAlignment, Validator validator, Cleaner cleaner) {
        super(text, columns);
        this.setHorizontalAlignment(horizontalAlignment);
        this.cleaner = cleaner;
        ValidatingTextField.installValidationBehavior(this, validator, cleaner);
        this.setText(cleaner.clean(this.getText()));
        this.setMinimumSize(this.getPreferredSize());
    }

    public void setValidator(Validator validator) {
        if (this.cleaner == null) {
            this.cleaner = DUMMY_CLEANER;
        }
        this.setValidator(validator, this.cleaner);
    }

    public void setValidator(Validator validator, Cleaner cleaner) {
        ValidatingTextField.installValidationBehavior(this, validator, cleaner);
        this.setText(cleaner.clean(this.getText()));
    }

    public static void installValidationBehavior(final JTextField textField, final Validator validator, final Cleaner cleaner) {
        final boolean[] validating = new boolean[]{true};
        textField.setDocument(new PlainDocument(){

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (!validating[0]) {
                    super.insertString(offs, str, a);
                    return;
                }
                String currentText = this.getText(0, this.getLength());
                String beforeOffset = currentText.substring(0, offs);
                String afterOffset = currentText.substring(offs, currentText.length());
                String proposedResult = String.valueOf(beforeOffset) + str + afterOffset;
                if (validator.isValid(cleaner.clean(proposedResult))) {
                    super.insertString(offs, str, a);
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                String proposedResult;
                if (!validating[0]) {
                    super.remove(offs, len);
                    return;
                }
                String currentText = this.getText(0, this.getLength());
                String beforeOffset = currentText.substring(0, offs);
                String afterOffset = "";
                if (len + offs < currentText.length()) {
                    afterOffset = currentText.substring(len + offs, currentText.length());
                }
                if (validator.isValid(cleaner.clean(proposedResult = String.valueOf(beforeOffset) + afterOffset))) {
                    super.remove(offs, len);
                }
            }
        });
        textField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                validating[0] = false;
                try {
                    textField.setText(cleaner.clean(textField.getText()));
                }
                finally {
                    validating[0] = true;
                }
            }
        });
    }

    @Override
    public String getText() {
        return this.cleaner.clean(super.getText());
    }

    public double getDouble() {
        return Double.parseDouble(this.getText().trim());
    }

    public int getInteger() {
        return Integer.parseInt(this.getText().trim());
    }

    public static class BlankCleaner
    implements Cleaner {
        private String replacement;

        public BlankCleaner(String replacement) {
            this.replacement = replacement;
        }

        @Override
        public String clean(String text) {
            return text.trim().length() == 0 ? this.getReplacement() : text;
        }

        protected String getReplacement() {
            return this.replacement;
        }
    }

    public static class BoundedDoubleValidator
    extends CompositeValidator {
        public BoundedDoubleValidator(double min, boolean includeMin, double max, boolean includeMax) {
            super(new Validator[]{DOUBLE_VALIDATOR, includeMin ? new GreaterThanOrEqualValidator(min) : new GreaterThanValidator(min), includeMax ? new LessThanOrEqualValidator(max) : new LessThanValidator(max)});
            Assert.isTrue((min < max ? 1 : 0) != 0);
        }
    }

    public static class BoundedIntValidator
    extends CompositeValidator {
        public BoundedIntValidator(int min, int max) {
            super(new Validator[]{INTEGER_VALIDATOR, new GreaterThanOrEqualValidator(min), new LessThanOrEqualValidator(max)});
            Assert.isTrue((min < max ? 1 : 0) != 0);
        }
    }

    public static interface Cleaner {
        public String clean(String var1);
    }

    public static class CompositeCleaner
    implements Cleaner {
        private Cleaner[] cleaners;

        public CompositeCleaner(Cleaner[] cleaners) {
            this.cleaners = cleaners;
        }

        @Override
        public String clean(String text) {
            String result = text;
            int i = 0;
            while (i < this.cleaners.length) {
                result = this.cleaners[i].clean(result);
                ++i;
            }
            return result;
        }
    }

    public static class CompositeValidator
    implements Validator {
        private Validator[] validators;

        public CompositeValidator(Validator[] validators) {
            this.validators = validators;
        }

        @Override
        public boolean isValid(String text) {
            int i = 0;
            while (i < this.validators.length) {
                if (!this.validators[i].isValid(text)) {
                    return false;
                }
                ++i;
            }
            return true;
        }
    }

    public static class GreaterThanOrEqualValidator
    implements Validator {
        private double threshold;

        public GreaterThanOrEqualValidator(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isValid(String text) {
            try {
                return Double.parseDouble(text.trim()) >= this.threshold;
            }
            catch (NumberFormatException e) {
                return true;
            }
        }
    }

    public static class GreaterThanValidator
    implements Validator {
        private double threshold;

        public GreaterThanValidator(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isValid(String text) {
            try {
                return Double.parseDouble(text.trim()) > this.threshold;
            }
            catch (NumberFormatException e) {
                return true;
            }
        }
    }

    public static class LessThanOrEqualValidator
    implements Validator {
        private double threshold;

        public LessThanOrEqualValidator(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isValid(String text) {
            try {
                return Double.parseDouble(text.trim()) <= this.threshold;
            }
            catch (NumberFormatException e) {
                return true;
            }
        }
    }

    public static class LessThanValidator
    implements Validator {
        private double threshold;

        public LessThanValidator(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isValid(String text) {
            try {
                return Double.parseDouble(text.trim()) < this.threshold;
            }
            catch (NumberFormatException e) {
                return true;
            }
        }
    }

    public static class MaxIntCleaner
    implements Cleaner {
        private int maximum;

        public MaxIntCleaner(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public String clean(String text) {
            return "" + Math.min(this.maximum, Integer.parseInt(text));
        }
    }

    public static class MinIntCleaner
    implements Cleaner {
        private int minimum;

        public MinIntCleaner(int minimum) {
            this.minimum = minimum;
        }

        @Override
        public String clean(String text) {
            return "" + Math.max(this.minimum, Integer.parseInt(text));
        }
    }

    public static class NumberCleaner
    implements Cleaner {
        private String replacement;

        public NumberCleaner(String replacement) {
            this.replacement = replacement;
        }

        @Override
        public String clean(String text) {
            try {
                Double.parseDouble(text.trim());
                return text.trim();
            }
            catch (NumberFormatException e) {
                try {
                    Double.parseDouble(String.valueOf(text.trim()) + "0");
                    return this.replacement;
                }
                catch (NumberFormatException e2) {
                    return text.trim();
                }
            }
        }

        protected String getReplacement() {
            return this.replacement;
        }
    }

    public static interface Validator {
        public boolean isValid(String var1);
    }
}

