/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.workbench.ui.ColorPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import org.saig.core.filter.Expression;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.jump.lang.I18N;

public class DefaultColorEditor
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;
    protected JButton btnColor = new JButton("...");
    protected ColorPanel colorPanel = new ColorPanel();

    public DefaultColorEditor() {
        this(Color.GRAY);
    }

    public DefaultColorEditor(Color color) {
        this.colorPanel.setMinimumSize(new Dimension(45, 15));
        this.colorPanel.setPreferredSize(new Dimension(45, 15));
        this.colorPanel.setMaximumSize(new Dimension(45, 15));
        this.colorPanel.setLineColor(Color.BLACK);
        this.colorPanel.setFillColor(color);
        this.btnColor.setMinimumSize(new Dimension(25, 15));
        this.btnColor.setPreferredSize(new Dimension(25, 15));
        this.btnColor.setMaximumSize(new Dimension(25, 15));
        this.setColor(color);
        this.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.colorPanel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.btnColor, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        this.add((Component)new JLabel(), gridBagConstraints);
        this.setColor(color);
        this.btnColor.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultColorEditor.select-color"));
        this.btnColor.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(DefaultColorEditor.this, I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultColorEditor.choose-a-color"), DefaultColorEditor.this.getColor());
                if (newColor != null) {
                    DefaultColorEditor.this.setColor(newColor);
                    DefaultColorEditor.this.fireExpressionChanged(DefaultColorEditor.this);
                }
            }
        });
    }

    public Color getColor() {
        return this.colorPanel.getFillColor();
    }

    @Override
    public Expression getExpression() {
        return styleBuilder.colorExpression(this.getColor());
    }

    public void setColor(Color col) {
        this.colorPanel.setFillColor(col);
        this.colorPanel.repaint();
    }

    @Override
    public void setExpression(Expression e) {
        if (e == null) {
            this.setColor(Color.GRAY);
        } else {
            try {
                String color = e.toString().replaceAll("'", "");
                this.setColor(Color.decode(color));
            }
            catch (NumberFormatException nfe) {
                this.setColor(Color.GRAY);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.btnColor.setEnabled(enabled);
        this.colorPanel.setEnabled(enabled);
    }

    @Override
    public boolean canEdit(Expression expression) {
        if (expression instanceof LiteralExpression) {
            LiteralExpression le = (LiteralExpression)expression;
            Object literal = le.getLiteral();
            if (literal instanceof Color) {
                return true;
            }
            if (literal instanceof String) {
                try {
                    Color.decode((String)literal);
                    return true;
                }
                catch (NumberFormatException nfe) {
                    return false;
                }
            }
            return false;
        }
        return false;
    }
}

