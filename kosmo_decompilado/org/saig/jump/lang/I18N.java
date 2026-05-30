/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.lang;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.ResourceBundleFamily;

public class I18N {
    private static final Logger LOGGER = Logger.getLogger(I18N.class);
    public static final String LANG_FILE_PATH = "language/kosmo";
    public static final String DEFAULT_LANG_FILE = "language/kosmo";
    public static final String NO_STRING_FOUND = "<Cadena no encontrada/No string found>";
    public static ResourceBundleFamily rb = new ResourceBundleFamily();
    private static I18N instance;

    static {
        rb.addResourceBundle(ResourceBundle.getBundle("language/kosmo"));
        instance = null;
    }

    protected I18N() {
    }

    public static I18N getInstance() {
        if (instance == null) {
            instance = new I18N();
        }
        return instance;
    }

    public static void loadLanguageFile(String langCountry) {
        String lang = langCountry.split("_")[0];
        try {
            String country = langCountry.split("_")[1];
            Locale locale = new Locale(lang, country);
            rb.clearBundles();
            rb.addResourceBundle(ResourceBundle.getBundle("language/kosmo", locale));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.debug((Object)I18N.getMessage("org.saig.jump.lang.I18N.loaded-locale-without-country-{0}", new Object[]{lang}));
            Locale locale = new Locale(lang);
            rb.clearBundles();
            rb.addResourceBundle(ResourceBundle.getBundle("language/kosmo", locale));
        }
    }

    public static String getString(String label) {
        try {
            return rb.getString(label);
        }
        catch (MissingResourceException e) {
            try {
                String default_translation = ResourceBundle.getBundle("language/kosmo").getString(label);
                LOGGER.debug((Object)(String.valueOf(e.getMessage()) + " default_value:" + default_translation));
                return default_translation;
            }
            catch (MissingResourceException ex) {
                LOGGER.warn((Object)("<Cadena no encontrada/No string found> " + label));
                return NO_STRING_FOUND;
            }
        }
    }

    public static String getString(Class<?> obj, String label) {
        return I18N.getString(String.valueOf(I18N.getPrefix(obj)) + label);
    }

    private static String getPrefix(Class<?> obj) {
        String prefix = "";
        if (obj != null) {
            while (obj != null && obj.getCanonicalName() == null) {
                obj = obj.getEnclosingClass();
            }
            if (obj != null) {
                prefix = String.valueOf(obj.getCanonicalName()) + ".";
            }
        }
        return prefix;
    }

    public static String getLocaleString() {
        return String.valueOf(rb.getLocale().getLanguage()) + "_" + rb.getLocale().getCountry();
    }

    public static Locale getLocale() {
        return rb.getLocale();
    }

    public static String getLanguage() {
        if (JUMPWorkbench.I18N_SETLOCALE == "") {
            return rb.getLocale().getLanguage();
        }
        return JUMPWorkbench.I18N_SETLOCALE.split("_")[0];
    }

    public static String getMessage(String label, Object[] objects) {
        try {
            String translation = StringUtils.replace((String)rb.getString(label), (String)"'", (String)"''");
            MessageFormat mf = new MessageFormat(translation, rb.getLocale());
            return mf.format(objects);
        }
        catch (MissingResourceException e) {
            try {
                String default_translation = StringUtils.replace((String)ResourceBundle.getBundle("language/kosmo").getString(label), (String)"'", (String)"''");
                LOGGER.debug((Object)(String.valueOf(e.getMessage()) + " default_value:" + default_translation));
                MessageFormat mf = new MessageFormat(default_translation, rb.getLocale());
                return mf.format(objects);
            }
            catch (MissingResourceException ex) {
                LOGGER.warn((Object)("<Cadena no encontrada/No string found> " + label));
                return NO_STRING_FOUND;
            }
        }
    }

    public static String getMessage(Class<?> obj, String label, Object[] objects) {
        return I18N.getMessage(String.valueOf(I18N.getPrefix(obj)) + label, objects);
    }

    public static String getString(String pluginClassName, String label) {
        return I18N.getString(label);
    }

    public static String getMessage(String pluginClassName, String label, Object[] objects) {
        return I18N.getMessage(label, objects);
    }

    public static void setPlugInRessource(String extensionClassName, ClassLoader classLoader) throws MissingResourceException {
        String fileBaseName = "language." + StringUtil.classNameWithoutPackageQualifiers(extensionClassName).replaceAll("Extension", "");
        if (JUMPWorkbench.I18N_SETLOCALE == "") {
            LOGGER.debug((Object)(String.valueOf(I18N.getString("org.saig.jump.lang.I18N.selected-default-locale")) + Locale.getDefault()));
            rb.addResourceBundle(ResourceBundle.getBundle(fileBaseName, Locale.getDefault(), classLoader));
        } else {
            LOGGER.debug((Object)I18N.getMessage("org.saig.jump.lang.I18N.selected-{0}-locale", new Object[]{rb.getLocale()}));
            rb.addResourceBundle(ResourceBundle.getBundle(fileBaseName, rb.getLocale(), classLoader));
        }
    }
}

