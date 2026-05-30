/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.saig.core.filter.Expression;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.IExpressionChangedListener;

public abstract class ExpressionEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    protected List<IExpressionChangedListener> listeners = new ArrayList<IExpressionChangedListener>();

    public abstract Expression getExpression();

    public abstract void setExpression(Expression var1);

    public abstract boolean canEdit(Expression var1);

    public void addExpressionChangedListener(IExpressionChangedListener listener) {
        this.listeners.add(listener);
    }

    public void removeExpressionChangedListener(IExpressionChangedListener listener) {
        this.listeners.remove(listener);
    }

    public void fireExpressionChanged(ExpressionEditor expressionEditor) {
        for (IExpressionChangedListener currentListener : this.listeners) {
            currentListener.expressionChanged(expressionEditor);
        }
    }
}

