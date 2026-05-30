/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.jump.lang.I18N;

public class DefaultPercentEditor
extends ExpressionEditor
implements ChangeListener {
    private static final long serialVersionUID = 1L;
    protected JSlider sliderValue = new JSlider(0, 0, 100, 0);
    protected double conversionFactor = 1.0;

    public DefaultPercentEditor(double conversionFactor) {
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("0%"));
        labelTable.put(50, new JLabel("50%"));
        labelTable.put(100, new JLabel("100%"));
        this.sliderValue.setLabelTable(labelTable);
        this.sliderValue.setPaintLabels(true);
        this.sliderValue.setMajorTickSpacing(10);
        this.sliderValue.setMinorTickSpacing(5);
        this.sliderValue.setPaintTicks(true);
        this.sliderValue.setSnapToTicks(true);
        this.sliderValue.addChangeListener(this);
        this.sliderValue.setMinimumSize(new Dimension(150, 50));
        this.conversionFactor = conversionFactor;
        this.setLayout(new BorderLayout());
        this.add(this.sliderValue);
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
    public void setExpression(Expression expression) {
        try {
            this.setValue(new Double(Double.parseDouble(expression.toString())));
        }
        catch (Exception e) {
            this.setValue(new Double(0.0));
        }
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

    public Number getValue() {
        if (this.conversionFactor != 1.0) {
            return new Double(((Number)this.sliderValue.getValue()).doubleValue() / this.conversionFactor);
        }
        return this.sliderValue.getValue();
    }

    public void setValue(Number value) {
        if (this.conversionFactor != 1.0) {
            double uiValue = value.doubleValue() * this.conversionFactor;
            this.sliderValue.setValue(new Integer((int)Math.round(uiValue)));
        } else {
            this.sliderValue.setValue(value.intValue());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.sliderValue.setToolTipText(String.valueOf(this.sliderValue.getValue()) + "%");
    }
}

