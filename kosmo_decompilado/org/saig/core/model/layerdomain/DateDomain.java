/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.util.Calendar
 *  org.apache.log4j.Logger
 *  org.dom4j.Element
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.model.layerdomain;

import com.ibm.icu.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.saig.core.model.layerdomain.EmptyDomain;
import org.saig.jump.lang.I18N;

public class DateDomain
extends EmptyDomain {
    private static final Logger LOGGER = Logger.getLogger(DateDomain.class);
    public static final String XML_TAG_NAME = "DateDomain";
    private Date defaultValue;
    private boolean nullable;
    private String friendlyName;
    private String stringDateDomain;

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

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public Date getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Date defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean test(Object obj) {
        if (obj == null) {
            return this.isNullable();
        }
        return true;
    }

    @Override
    public String getDescription() {
        return I18N.getString("org.saig.core.model.layerdomain.DateDomain.The-value-must-be-a-date");
    }

    @Override
    public Element createSAXElement() {
        DefaultElement element = new DefaultElement(XML_TAG_NAME);
        Calendar c = Calendar.getInstance();
        String date = Integer.toString(c.get(5));
        String month = Integer.toString(c.get(2));
        String year = Integer.toString(c.get(1));
        element.addAttribute("FriendlyName", this.friendlyName);
        element.addAttribute("Nullable", new Boolean(this.isNullable()).toString());
        this.stringDateDomain = this.defaultValue == null ? new String(String.valueOf(date) + "/" + month + "/" + year) : this.defaultValue.toString();
        element.addAttribute("DefaultValue", this.stringDateDomain);
        this.addActionsToSAXElement((Element)element);
        return element;
    }

    @Override
    public void fillFromElement(Element element) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        this.setFriendlyName(element.attributeValue("FriendlyName"));
        this.setNullable(new Boolean(element.attributeValue("Nullable")));
        try {
            this.setDefaultValue(df.parse(element.attributeValue("DefaultValue")));
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.readActionsFromSAXElement(element);
    }
}

