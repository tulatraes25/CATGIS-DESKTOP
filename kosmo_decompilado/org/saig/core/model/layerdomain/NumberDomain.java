/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.model.layerdomain;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.saig.core.model.layerdomain.EmptyDomain;
import org.saig.jump.lang.I18N;

public class NumberDomain
extends EmptyDomain {
    public static final String XML_TAG = "NumberDomain";
    public static final String HAS_MAX_XML_TAG = "HasMax";
    public static final String HAS_MIN_XML_TAG = "HasMin";
    public static final String MAX_XML_TAG = "Max";
    public static final String MIN_XML_TAG = "Min";
    private boolean hasMax;
    private boolean hasMin;
    private double max;
    private double min;
    private Number defaultValue;
    private boolean nullable;
    private String friendlyName;

    @Override
    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(String fiendlyName) {
        this.friendlyName = fiendlyName;
    }

    public boolean isHasMax() {
        return this.hasMax;
    }

    public void setHasMax(boolean hasMax) {
        this.hasMax = hasMax;
    }

    public boolean isHasMin() {
        return this.hasMin;
    }

    public void setHasMin(boolean hasMin) {
        this.hasMin = hasMin;
    }

    @Override
    public boolean isNullable() {
        return this.nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public Number getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Number defaultValue) {
        this.defaultValue = defaultValue;
    }

    public double getMax() {
        return this.max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return this.min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @Override
    public boolean test(Object obj) {
        Number n = (Number)obj;
        if (n == null) {
            return this.isNullable();
        }
        if (this.hasMax && n.doubleValue() > this.max) {
            return false;
        }
        return !this.hasMin || !(n.doubleValue() < this.min);
    }

    @Override
    public String getDescription() {
        String minMsg = "org.saig.core.model.layerdomain.NumberDomain.The-value-must-be-a-number-lower-than-{0}";
        String mayMsg = "org.saig.core.model.layerdomain.NumberDomain.The-value-must-be-a-number-greater-than-{0}";
        String minAndMaxMsg = "org.saig.core.model.layerdomain.NumberDomain.The-value-must-be-a-number-greater-than-{0}-and-lower-than-{1}";
        String msg = I18N.getString("org.saig.core.model.layerdomain.NumberDomain.The-value-must-be-a-number");
        if (this.hasMax && this.hasMin) {
            msg = I18N.getMessage(minAndMaxMsg, new Object[]{this.min, this.max});
        } else if (this.hasMax) {
            msg = I18N.getMessage(minMsg, new Object[]{this.max});
        } else if (this.hasMin) {
            msg = I18N.getMessage(mayMsg, new Object[]{this.min});
        }
        return msg;
    }

    @Override
    public Element createSAXElement() {
        DefaultElement element = new DefaultElement(XML_TAG);
        element.addAttribute(HAS_MAX_XML_TAG, new Boolean(this.isHasMax()).toString());
        element.addAttribute(HAS_MIN_XML_TAG, new Boolean(this.isHasMin()).toString());
        element.addAttribute(MAX_XML_TAG, ((Object)this.max).toString());
        element.addAttribute(MIN_XML_TAG, ((Object)this.min).toString());
        element.addAttribute("DefaultValue", this.defaultValue.toString());
        element.addAttribute("Nullable", new Boolean(this.isNullable()).toString());
        element.addAttribute("FriendlyName", this.friendlyName);
        this.addActionsToSAXElement((Element)element);
        return element;
    }

    @Override
    public void fillFromElement(Element element) {
        this.setDefaultValue(new Double(element.attributeValue("DefaultValue")));
        this.setFriendlyName(element.attributeValue("FriendlyName"));
        this.setHasMax(new Boolean(element.attributeValue(HAS_MAX_XML_TAG)));
        this.setHasMin(new Boolean(element.attributeValue(HAS_MIN_XML_TAG)));
        this.setMax(new Double(element.attributeValue(MAX_XML_TAG)));
        this.setMin(new Double(element.attributeValue(MIN_XML_TAG)));
        this.setNullable(new Boolean(element.attributeValue("Nullable")));
        this.readActionsFromSAXElement(element);
    }
}

