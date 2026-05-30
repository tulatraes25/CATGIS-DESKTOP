/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain.conditions;

import org.dom4j.Element;

public interface Condition {
    public boolean eval();

    public Element createSAXElement();
}

