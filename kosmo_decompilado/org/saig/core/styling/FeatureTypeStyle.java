/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.Rule;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.util.language.ITranslatable;

public interface FeatureTypeStyle
extends ITranslatable {
    public String getName();

    public void setName(String var1);

    @Override
    public String getTitle();

    public void setTitle(String var1);

    public String getAbstract();

    public void setAbstract(String var1);

    public String toString();

    public String getFeatureTypeName();

    public void setFeatureTypeName(String var1);

    public String[] getSemantecTypeIdentifiers();

    public void setSemantecTypeIdentifiers(String[] var1);

    public Rule[] getRules();

    public void setRules(Rule[] var1);

    public void addRule(Rule var1);

    public void accept(StyleVisitor var1);

    public boolean isEmptyFeatureTypeStyle();

    public Rule getRule(String var1);
}

