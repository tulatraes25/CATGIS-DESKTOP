/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain.actions;

import java.util.Properties;
import org.dom4j.Element;
import org.saig.core.model.layerdomain.actions.Action;
import org.saig.core.model.layerdomain.actions.PropertiesFactory;

public interface ActionFactory {
    public static final String CURRENT_FIELD_PROPERTY = "current_field";

    public String getName();

    public boolean acceptsTag(String var1);

    public Action createActionFromDom4jElement(Element var1);

    public Action createActionFromProperties(Properties var1);

    public PropertiesFactory getPropertiesFactory();
}

