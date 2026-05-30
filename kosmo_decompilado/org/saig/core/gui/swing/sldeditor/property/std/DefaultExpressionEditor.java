/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor;
import org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class DefaultExpressionEditor
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DefaultExpressionEditor.class);
    private JTextField txtExpression;
    private Exception lastException;
    private String lastInput;
    private JButton btnWizard;
    private FeatureSchema featureType;

    public DefaultExpressionEditor(FeatureSchema featureType) {
        this.featureType = featureType;
        this.setLayout(new BorderLayout());
        this.txtExpression = new JTextField();
        this.btnWizard = new JButton(DefaultFilterEditor.WIZARD_ICON);
        this.btnWizard.setPreferredSize(FormUtils.getButtonDimension());
        this.add(this.txtExpression);
        this.add((Component)this.btnWizard, "East");
        this.btnWizard.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultExpressionEditor.this.openDialog();
            }
        });
    }

    public DefaultExpressionEditor(Expression e, FeatureSchema featureType) {
        this(featureType);
        this.setExpression(e);
    }

    public void openDialog() {
        ExpressionDialog dialog = null;
        Window w = FormUtils.getWindowForComponent(this);
        dialog = w instanceof Frame ? new ExpressionDialog((Frame)w, true, this.featureType) : new ExpressionDialog((Dialog)w, true, this.featureType);
        dialog.setRawText(this.txtExpression.getText());
        dialog.setVisible(true);
        if (dialog.exitOk()) {
            this.txtExpression.setText(dialog.getRawText());
        }
    }

    @Override
    public Expression getExpression() {
        Expression result = null;
        this.lastInput = this.txtExpression.getText();
        try {
            result = (Expression)ExpressionBuilder.parse(this.lastInput);
            this.lastException = null;
        }
        catch (ParseException e) {
            this.lastException = e;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)ExpressionBuilder.getFormattedErrorMessage(e, this.lastInput));
            }
            result = null;
        }
        catch (Exception e) {
            this.lastException = e;
            result = null;
        }
        return result;
    }

    public String getFormattedErrorMessage() {
        if (this.lastException == null) {
            return null;
        }
        if (this.lastException instanceof ParseException) {
            return ExpressionBuilder.getFormattedErrorMessage((ParseException)this.lastException, this.lastInput);
        }
        return I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExpressionEditor.current-input-is-a-filter-not-an-expression");
    }

    @Override
    public void setExpression(Expression expression) {
        if (expression != null) {
            this.txtExpression.setText(expression.toString());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.txtExpression.setEnabled(enabled);
        this.btnWizard.setEnabled(enabled);
    }

    @Override
    public boolean canEdit(Expression expression) {
        return true;
    }
}

