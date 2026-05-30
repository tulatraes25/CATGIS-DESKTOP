/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain.actions;

import org.dom4j.Element;
import org.saig.core.model.layerdomain.actions.Action;
import org.saig.core.model.layerdomain.conditions.Condition;

public abstract class DefaultAction
implements Action {
    protected Condition condition;

    public DefaultAction(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean eval() {
        if (this.condition != null) {
            return this.condition.eval();
        }
        return false;
    }

    @Override
    public void fireAction() {
        if (this.eval()) {
            this.action();
        }
    }

    @Override
    public abstract Element createSAXElement();

    @Override
    public abstract void action();

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}

