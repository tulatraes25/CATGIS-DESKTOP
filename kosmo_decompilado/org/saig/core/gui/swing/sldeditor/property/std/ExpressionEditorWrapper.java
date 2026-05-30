/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JToggleButton;
import org.saig.core.filter.Expression;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.IExpressionChangedListener;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultExpressionEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ExpressionEditorWrapper
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;
    private boolean expertMode;
    private DefaultExpressionEditor expressionEditor;
    private ExpressionEditor simpleEditor;
    private ExpressionEditor currentEditor;
    private JToggleButton btnChoose;

    public ExpressionEditorWrapper(ExpressionEditor simpleExpressionEditor, FeatureSchema ft, boolean mode) {
        this.setLayout(new BorderLayout());
        this.btnChoose = new JToggleButton();
        this.add(simpleExpressionEditor);
        this.add((Component)this.btnChoose, "East");
        this.btnChoose.setSelected(false);
        this.btnChoose.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseReleased(MouseEvent e) {
                ExpressionEditorWrapper.this.toggleEditor();
            }
        });
        this.expertMode = mode;
        this.simpleEditor = simpleExpressionEditor;
        this.expressionEditor = new DefaultExpressionEditor(ft);
        this.currentEditor = simpleExpressionEditor;
        this.btnChoose.setVisible(this.expertMode);
        this.btnChoose.setPreferredSize(FormUtils.getButtonDimension());
        this.btnChoose.setMinimumSize(FormUtils.getButtonDimension());
    }

    private void toggleEditor() {
        Expression exp = this.currentEditor.getExpression();
        ExpressionEditor newEditor = null;
        if (this.currentEditor == this.simpleEditor) {
            newEditor = this.expressionEditor;
        } else {
            int result;
            if (exp != null && !this.simpleEditor.canEdit(exp) && (result = DialogFactory.showYesNoDialog(this, I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionEditorWrapper.the-simple-editor-cannot-fully-manage-the-current-expression-if-you-choose-ok"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionEditorWrapper.expression-editor"))) == 1) {
                this.btnChoose.setSelected(true);
                return;
            }
            newEditor = this.simpleEditor;
        }
        this.setCurrentEditor(newEditor, exp);
    }

    private void setCurrentEditor(ExpressionEditor newEditor, Expression exp) {
        newEditor.setExpression(exp);
        if (newEditor != this.currentEditor) {
            this.remove(this.currentEditor);
            this.add((Component)newEditor, "Center");
            this.currentEditor = newEditor;
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    public Expression getExpression() {
        return this.currentEditor.getExpression();
    }

    @Override
    public void setExpression(Expression expression) {
        if (this.simpleEditor.canEdit(expression)) {
            this.setCurrentEditor(this.simpleEditor, expression);
            this.btnChoose.setSelected(false);
        } else {
            this.setCurrentEditor(this.expressionEditor, expression);
            this.btnChoose.setSelected(true);
        }
    }

    @Override
    public boolean canEdit(Expression expression) {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.simpleEditor.setEnabled(enabled);
        this.expressionEditor.setEnabled(enabled);
        this.btnChoose.setEnabled(enabled);
    }

    @Override
    public void addExpressionChangedListener(IExpressionChangedListener listener) {
        this.simpleEditor.addExpressionChangedListener(listener);
    }

    @Override
    public void removeExpressionChangedListener(IExpressionChangedListener listener) {
        this.simpleEditor.removeExpressionChangedListener(listener);
    }
}

