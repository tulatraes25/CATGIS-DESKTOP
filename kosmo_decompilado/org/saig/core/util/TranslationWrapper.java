/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.util.Locale;
import java.util.Map;

public class TranslationWrapper {
    private Map<Locale, String> translationMap;

    public TranslationWrapper() {
    }

    public TranslationWrapper(Map<Locale, String> translations) {
        this.translationMap = translations;
    }

    public Map<Locale, String> getTranslationMap() {
        return this.translationMap;
    }

    public void setTranslationMap(Map<Locale, String> translationMap) {
        this.translationMap = translationMap;
    }
}

