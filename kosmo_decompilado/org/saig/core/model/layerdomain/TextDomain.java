/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.dom4j.Element
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.model.layerdomain;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.saig.core.model.layerdomain.EmptyDomain;
import org.saig.jump.lang.I18N;

public class TextDomain
extends EmptyDomain {
    public static final String XML_TAG = "TextDomain";
    private String defaultValue;
    private boolean nullable;
    private String friendlyName;

    @Override
    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(String fiendlyName) {
        this.friendlyName = fiendlyName;
    }

    @Override
    public boolean isNullable() {
        return this.nullable;
    }

    public String stringNullable() {
        if (this.nullable) {
            return "true";
        }
        return "false";
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean test(Object obj) {
        if (StringUtils.isEmpty((String)((String)obj))) {
            return this.isNullable();
        }
        return true;
    }

    @Override
    public String getDescription() {
        return I18N.getString("org.saig.core.model.layerdomain.TextDomain.The-value-must-be-a-string");
    }

    @Override
    public Element createSAXElement() {
        DefaultElement element = new DefaultElement(XML_TAG);
        element.addAttribute("DefaultValue", this.defaultValue);
        element.addAttribute("FriendlyName", this.friendlyName);
        element.addAttribute("Nullable", this.stringNullable());
        this.addActionsToSAXElement((Element)element);
        return element;
    }

    @Override
    public void fillFromElement(Element element) {
        this.setDefaultValue(element.attributeValue("DefaultValue"));
        this.setFriendlyName(element.attributeValue("FriendlyName"));
        this.setNullable(new Boolean("Nullable"));
        this.readActionsFromSAXElement(element);
    }
}

