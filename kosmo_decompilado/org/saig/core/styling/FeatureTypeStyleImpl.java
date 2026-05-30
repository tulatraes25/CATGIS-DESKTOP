/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.util.LocaleManager;

public class FeatureTypeStyleImpl
implements FeatureTypeStyle,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling.");
    private List<Rule> ruleList = new ArrayList<Rule>();
    private Map<Locale, String> titleByLang = new HashMap<Locale, String>();
    private static final String DEFAULT_NAME = "name";
    private static final String DEFAULT_FEATURE_TYPE_NAME = "Feature";
    private static final String DEFAULT_ABSTRACT = "abstract";
    private String featureTypeName = "Feature";
    private String name = "name";
    private String abstractStr = "abstract";

    public FeatureTypeStyleImpl() {
    }

    public FeatureTypeStyleImpl(Rule[] rules) {
        this.setRules(rules);
    }

    @Override
    public String getFeatureTypeName() {
        return this.featureTypeName;
    }

    @Override
    public Rule[] getRules() {
        return this.ruleList.toArray(new Rule[0]);
    }

    @Override
    public String[] getSemantecTypeIdentifiers() {
        return new String[]{"generic:geometry"};
    }

    @Override
    public void setSemantecTypeIdentifiers(String[] types) {
    }

    @Override
    public void setRules(Rule[] rules) {
        this.ruleList.clear();
        int i = 0;
        while (i < rules.length) {
            this.addRule(rules[i]);
            ++i;
        }
    }

    @Override
    public void addRule(Rule rule) {
        this.ruleList.add(rule);
    }

    @Override
    public void setFeatureTypeName(String name) {
        if (name.equals("feature")) {
            LOGGER.warn((Object)"FeatureTypeStyle with typename 'feature' - you probably meant to say 'Feature' (capital F) for the 'generic' FeatureType");
        }
        this.featureTypeName = name;
    }

    @Override
    public String getAbstract() {
        return this.abstractStr;
    }

    @Override
    public void setAbstract(String abstractStr) {
        this.abstractStr = abstractStr;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return this.getTitle(LocaleManager.getActiveLocale());
    }

    @Override
    public void setTitle(String title) {
        this.setTitle(title, LocaleManager.getActiveLocale());
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        FeatureTypeStyleImpl clone = new FeatureTypeStyleImpl();
        clone.setAbstract(this.getAbstract());
        clone.setFeatureTypeName(this.getFeatureTypeName());
        clone.setName(this.getName());
        Rule[] ruleArray = new Rule[this.ruleList.size()];
        int i = 0;
        while (i < ruleArray.length) {
            Rule rule = this.ruleList.get(i);
            ruleArray[i] = (Rule)((RuleImpl)rule).clone();
            ++i;
        }
        clone.setRules(ruleArray);
        HashMap<Locale, String> titleByLangClone = new HashMap<Locale, String>();
        for (Locale key : this.titleByLang.keySet()) {
            titleByLangClone.put(key, this.titleByLang.get(key));
        }
        clone.setTitleByLang(titleByLangClone);
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.ruleList != null) {
            result = 1000003 * result + this.ruleList.hashCode();
        }
        if (this.featureTypeName != null) {
            result = 1000003 * result + this.featureTypeName.hashCode();
        }
        if (this.name != null) {
            result = 1000003 * result + this.name.hashCode();
        }
        if (this.getTitle() != null) {
            result = 1000003 * result + this.getTitle().hashCode();
        }
        if (this.abstractStr != null) {
            result = 1000003 * result + this.abstractStr.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof FeatureTypeStyleImpl) {
            FeatureTypeStyleImpl other = (FeatureTypeStyleImpl)oth;
            return Utilities.equals((Object)this.name, (Object)other.name) && Utilities.equals((Object)this.getTitle(), (Object)other.getTitle()) && Utilities.equals((Object)this.abstractStr, (Object)other.abstractStr) && Utilities.equals((Object)this.featureTypeName, (Object)other.featureTypeName) && Utilities.equals(this.ruleList, other.ruleList);
        }
        return false;
    }

    @Override
    public boolean isEmptyFeatureTypeStyle() {
        return this.getRules().length == 0;
    }

    @Override
    public Rule getRule(String ruleName) {
        Rule rule = null;
        Iterator<Rule> iter = this.ruleList.iterator();
        while (iter.hasNext() && rule == null) {
            Rule element = iter.next();
            if (!element.getName().equals(ruleName)) continue;
            rule = element;
        }
        return rule;
    }

    @Override
    public String toString() {
        return this.getTitle();
    }

    @Override
    public String getTitle(Locale locale) {
        if (!this.titleByLang.containsKey(locale)) {
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
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale)) {
            this.titleByLang.put(locale, this.name);
            for (Rule rule : this.ruleList) {
                rule.addLocale(locale);
            }
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
            for (Rule rule : this.ruleList) {
                rule.removeLocale(locale);
            }
        }
    }
}

