/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 */
package org.saig.core.model.layerdomain;

import java.util.List;
import org.dom4j.Element;
import org.saig.core.model.layerdomain.actions.Action;

public interface Domain {
    public static final String FRIENDLY_NAME_XML_TAG = "FriendlyName";
    public static final String NULLABLE_XML_TAG = "Nullable";
    public static final String DEFAULT_VALUE_XML_TAG = "DefaultValue";

    public boolean test(Object var1);

    public Object getDefaultValue();

    public String getFriendlyName();

    public boolean isNullable();

    public boolean isVisible();

    public String getDescription();

    public Element createSAXElement();

    public void fillFromElement(Element var1);

    public List<Action> getActions();

    public void addAction(Action var1);

    public void fireActions();
}

