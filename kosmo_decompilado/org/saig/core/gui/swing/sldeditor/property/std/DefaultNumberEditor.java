/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class DefaultNumberEditor
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;
    JSpinner spnValue;
    double conversionFactor = 1.0;
    Class<? extends Number> valueClass;
    private boolean cyclic = false;

    public DefaultNumberEditor() {
        this(new Double(1.0), new Double(0.0), new Double(1000.0), new Double(0.5));
    }

    public DefaultNumberEditor(Number value, Number minimum, Number maximum, Number step) {
        this(value, minimum, maximum, step, 1.0);
    }

    public DefaultNumberEditor(Number value, Number minimum, Number maximum, Number step, double conversionFactor) {
        this.conversionFactor = conversionFactor;
        this.spnValue = new JSpinner(this.getSpinnerModel(value, minimum, maximum, step));
        this.spnValue.setMinimumSize(FormUtils.getSpinnerDimension());
        this.spnValue.setPreferredSize(FormUtils.getSpinnerDimension());
        this.setLayout(new BorderLayout());
        this.add(this.spnValue);
    }

    private SpinnerNumberModel getSpinnerModel(Number value, final Number minimum, final Number maximum, Number step) {
        SpinnerNumberModel model;
        if (value instanceof Integer) {
            this.valueClass = Integer.class;
            model = new SpinnerNumberModel((Integer)value, (Integer)minimum, (Integer)maximum, (Integer)step);
        } else {
            this.valueClass = Double.class;
            model = new SpinnerNumberModel((Double)value, (Double)minimum, (Double)maximum, (Double)step);
        }
        model.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (DefaultNumberEditor.this.cyclic && model.getValue().equals(maximum)) {
                    model.setValue(minimum);
                }
                DefaultNumberEditor.this.fireExpressionChanged(DefaultNumberEditor.this);
            }
        });
        return model;
    }

    public void setValue(Number value) {
        if (this.conversionFactor != 1.0) {
            double uiValue = value.doubleValue() * this.conversionFactor;
            if (this.valueClass == Integer.class) {
                this.spnValue.setValue(new Integer((int)Math.round(uiValue)));
            } else {
                this.spnValue.setValue(new Double(uiValue));
            }
        } else {
            this.spnValue.setValue(value);
        }
    }

    public Number getValue() {
        if (this.conversionFactor != 1.0) {
            return new Double(((Number)this.spnValue.getValue()).doubleValue() / this.conversionFactor);
        }
        return (Number)this.spnValue.getValue();
    }

    @Override
    public void setExpression(Expression exp) {
        try {
            this.setValue(new Double(Double.parseDouble(exp.toString())));
        }
        catch (Exception e) {
            this.setValue(new Double(0.0));
        }
    }

    @Override
    public Expression getExpression() {
        try {
            return styleBuilder.literalExpression(this.getValue());
        }
        catch (IllegalFilterException ife) {
            throw new RuntimeException(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultNumberEditor.this-should-not-happen"), ife);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.spnValue.setEnabled(enabled);
    }

    public boolean isCyclic() {
        return this.cyclic;
    }

    public void setCyclic(boolean cyclic) {
        if (this.cyclic == cyclic) {
            return;
        }
        this.cyclic = cyclic;
    }

    @Override
    public boolean canEdit(Expression expression) {
        if (expression instanceof LiteralExpression) {
            LiteralExpression le = (LiteralExpression)expression;
            Object literal = le.getLiteral();
            if (literal instanceof Number) {
                return true;
            }
            if (literal instanceof String) {
                try {
                    Double.parseDouble((String)literal);
                    return true;
                }
                catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return false;
    }
}

