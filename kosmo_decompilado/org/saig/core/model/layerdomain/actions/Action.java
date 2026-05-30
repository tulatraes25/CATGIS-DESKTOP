/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain.actions;

import org.dom4j.Element;
import org.saig.core.model.layerdomain.conditions.Condition;

public interface Action {
    public boolean eval();

    public void fireAction();

    public Element createSAXElement();

    public void action();

    public void setCondition(Condition var1);
}

