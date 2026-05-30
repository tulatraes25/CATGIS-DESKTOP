/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain.conditions;

import java.util.Properties;
import org.dom4j.Element;
import org.saig.core.model.layerdomain.actions.PropertiesFactory;
import org.saig.core.model.layerdomain.conditions.Condition;

public interface ConditionFactory {
    public static final String CURRENT_FIELD_PROPERTY = "current_field";

    public boolean acceptsTag(String var1);

    public Condition createConditionFromProperties(Properties var1);

    public Condition createConditionFromDom4jElement(Element var1);

    public PropertiesFactory createPropertiesFactory();

    public String getName();
}

