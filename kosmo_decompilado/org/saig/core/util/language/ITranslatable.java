/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util.language;

import java.util.Locale;
import java.util.Map;

public interface ITranslatable {
    public String getTitle(Locale var1);

    public String getTitle();

    public void setTitle(String var1, Locale var2);

    public Map<Locale, String> getTitleByLang();

    public void setTitleByLang(Map<Locale, String> var1);

    public void addLocale(Locale var1);

    public void removeLocale(Locale var1);
}

