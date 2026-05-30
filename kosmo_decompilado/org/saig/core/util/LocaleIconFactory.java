/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.Icon;

public class LocaleIconFactory {
    private static Map<String, Icon> iconsByCodeLang;
    private static Map<Locale, Icon> iconsByLocale;

    static {
        new LocaleIconFactory();
    }

    private LocaleIconFactory() {
        iconsByCodeLang = new HashMap<String, Icon>();
        iconsByLocale = new HashMap<Locale, Icon>();
        this.setLangIcon("ar_AE");
        this.setLangIcon("ar_BH");
        this.setLangIcon("ar_DZ");
        this.setLangIcon("ar_EG");
        this.setLangIcon("ar_IQ");
        this.setLangIcon("ar_JO");
        this.setLangIcon("ar_KW");
        this.setLangIcon("ar_LB");
        this.setLangIcon("ar_LY");
        this.setLangIcon("ar_MA");
        this.setLangIcon("ar_OM");
        this.setLangIcon("ar_QA");
        this.setLangIcon("ar_SA");
        this.setLangIcon("ar_SD");
        this.setLangIcon("ar_SY");
        this.setLangIcon("ar_TN");
        this.setLangIcon("ar_YE");
        this.setLangIcon("be");
        this.setLangIcon("bg");
        this.setLangIcon("ca");
        this.setLangIcon("cs");
        this.setLangIcon("da");
        this.setLangIcon("de");
        this.setLangIcon("de_AT");
        this.setLangIcon("de_CH");
        this.setLangIcon("de_LU");
        this.setLangIcon("el");
        this.setLangIcon("en");
        this.setLangIcon("en_AU");
        this.setLangIcon("en_CA");
        this.setLangIcon("en_IE");
        this.setLangIcon("en_US");
        this.setLangIcon("en_ZA");
        this.setLangIcon("es");
        this.setLangIcon("es_AR");
        this.setLangIcon("es_BO");
        this.setLangIcon("es_CL");
        this.setLangIcon("es_CO");
        this.setLangIcon("es_CR");
        this.setLangIcon("es_DO");
        this.setLangIcon("es_EC");
        this.setLangIcon("es_GT");
        this.setLangIcon("es_HN");
        this.setLangIcon("es_MX");
        this.setLangIcon("es_NI");
        this.setLangIcon("es_PA");
        this.setLangIcon("es_PE");
        this.setLangIcon("es_PR");
        this.setLangIcon("es_PY");
        this.setLangIcon("es_SV");
        this.setLangIcon("es_UY");
        this.setLangIcon("es_VE");
        this.setLangIcon("es_EU");
        this.setLangIcon("et");
        this.setLangIcon("fa");
        this.setLangIcon("fi");
        this.setLangIcon("fj");
        this.setLangIcon("fo");
        this.setLangIcon("fr");
        this.setLangIcon("fr_BE");
        this.setLangIcon("fr_CA");
        this.setLangIcon("fr_CH");
        this.setLangIcon("fr_LU");
        this.setLangIcon("hi_IN");
        this.setLangIcon("hr");
        this.setLangIcon("hu");
        this.setLangIcon("hy");
        this.setLangIcon("in");
        this.setLangIcon("is");
        this.setLangIcon("it");
        this.setLangIcon("iw");
        this.setLangIcon("ja");
        this.setLangIcon("ka");
        this.setLangIcon("kl");
        this.setLangIcon("km");
        this.setLangIcon("ko_KP");
        this.setLangIcon("ko_KR");
        this.setLangIcon("lo");
        this.setLangIcon("lt");
        this.setLangIcon("lv");
        this.setLangIcon("mk");
        this.setLangIcon("ms");
        this.setLangIcon("mt");
        this.setLangIcon("nl");
        this.setLangIcon("no");
        this.setLangIcon("pl");
        this.setLangIcon("pt");
        this.setLangIcon("pt_BR");
        this.setLangIcon("ro");
        this.setLangIcon("ru");
        this.setLangIcon("sh");
        this.setLangIcon("sk");
        this.setLangIcon("sl");
        this.setLangIcon("sr");
        this.setLangIcon("sq");
        this.setLangIcon("sv");
        this.setLangIcon("th");
        this.setLangIcon("tr");
        this.setLangIcon("uk");
        this.setLangIcon("vi");
        this.setLangIcon("zh");
        this.setLangIcon("zh_HK");
        this.setLangIcon("zh_TW");
    }

    private void setLangIcon(String langCode) {
        iconsByCodeLang.put(langCode, IconLoader.icon("flags/" + langCode + ".gif"));
    }

    private static Icon getIconByCode(String langCode) {
        return iconsByCodeLang.get(langCode);
    }

    private static Icon getDefaultIcon() {
        return IconLoader.icon("flags/default.gif");
    }

    public static Icon getIcon(String s) {
        return LocaleIconFactory.getIconByCode(s);
    }

    public static Icon getIcon(Locale locale) {
        Icon icon = iconsByLocale.get(locale);
        if (icon == null) {
            if (locale.toString().length() == 0) {
                icon = LocaleIconFactory.getDefaultIcon();
            } else {
                if (locale.getCountry().length() > 0) {
                    icon = LocaleIconFactory.getIconByCode(locale.toString());
                    if (icon == null) {
                        icon = LocaleIconFactory.getIconByCode(locale.getLanguage());
                    }
                } else {
                    icon = LocaleIconFactory.getIconByCode(locale.getLanguage());
                }
                if (icon == null) {
                    return null;
                }
            }
            iconsByLocale.put(locale, icon);
        }
        return icon;
    }
}

