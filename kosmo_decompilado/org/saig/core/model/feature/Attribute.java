/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.model.feature;

import com.vividsolutions.jump.feature.AttributeType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;

public class Attribute
implements Comparable<Attribute>,
ITranslatable {
    private String name;
    private boolean visibility;
    private AttributeType type;
    private boolean primaryKey;
    private Map<Locale, String> titleByLang = new HashMap<Locale, String>();

    public Attribute() {
    }

    public Attribute(String name, String publicName, boolean visibility, AttributeType type) {
        this.name = name;
        this.setPublicName(publicName);
        this.visibility = visibility;
        this.type = type;
    }

    public Attribute(String name, AttributeType type) {
        this.name = name;
        this.setPublicName(name);
        this.visibility = true;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicName() {
        Locale locale = LocaleManager.getActiveLocale();
        return this.getTitle(locale);
    }

    public void setPublicName(String publicName) {
        Locale locale = LocaleManager.getActiveLocale();
        this.setTitle(publicName, locale);
    }

    public AttributeType getType() {
        return this.type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public boolean isVisibility() {
        return this.visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }
        Attribute otherAttribute = (Attribute)other;
        boolean check = true;
        Iterator<Locale> iterator = this.titleByLang.keySet().iterator();
        while (iterator.hasNext() && check) {
            Locale locale = iterator.next();
            String title = this.titleByLang.get(locale);
            String otherTitle = otherAttribute.getTitle(locale);
            boolean bl = check = title == null && otherTitle == null || title != null && title.equals(otherTitle);
        }
        return this.getName().equals(otherAttribute.getName()) && this.getType().equals(otherAttribute.getType());
    }

    public int hashCode() {
        return ((17 + this.getName().hashCode()) * 37 + this.getType().hashCode()) * 37;
    }

    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String toString() {
        return this.getPublicName();
    }

    @Override
    public int compareTo(Attribute arg0) {
        return this.getPublicName().compareTo(arg0.getPublicName());
    }

    public boolean isCalculated() {
        return false;
    }

    @Override
    public String getTitle(Locale locale) {
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public String getTitle() {
        Locale locale = LocaleManager.getActiveLocale();
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public void setTitle(String title, Locale locale) {
        this.titleByLang.put(locale, title);
    }

    @Override
    public Map<Locale, String> getTitleByLang() {
        for (Locale locale : LocaleManager.getAvailablesLocales()) {
            if (this.titleByLang.containsKey(locale) && !StringUtils.isEmpty((String)this.titleByLang.get(locale))) continue;
            this.titleByLang.put(locale, this.name);
        }
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.titleByLang.put(locale, this.name);
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
        }
    }

    public Object clone() {
        Attribute clone = new Attribute();
        clone.setName(this.name);
        clone.setPrimaryKey(this.primaryKey);
        clone.setPublicName(this.getPublicName());
        clone.setTitleByLang(this.getTitleByLang());
        clone.setType(this.type);
        clone.setVisibility(this.visibility);
        return clone;
    }
}

