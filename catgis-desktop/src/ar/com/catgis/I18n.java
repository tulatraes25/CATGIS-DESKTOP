package ar.com.catgis;

import javax.swing.JComponent;
import javax.swing.UIManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public final class I18n {

    private static final Preferences PREFS = Preferences.userNodeForPackage(I18n.class);
    private static final String LANGUAGE_KEY = "ui.language";
    private static final String BUNDLE_NAME = "ar.com.catgis.CatgisMessages";
    private static final String INSTALL_DEFAULTS_FILE = "catgis-defaults.properties";

    private static Language currentLanguage = Language.SPANISH;
    private static ResourceBundle bundle;

    private I18n() {
    }

    public static void initialize() {
        setLanguage(resolveInitialLanguage(), false);
    }

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(Language language) {
        setLanguage(language, true);
    }

    private static void setLanguage(Language language, boolean persist) {
        currentLanguage = language != null ? language : Language.SPANISH;

        Locale locale = currentLanguage.toLocale();
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(locale);

        bundle = currentLanguage == Language.ENGLISH
                ? ResourceBundle.getBundle(BUNDLE_NAME, locale)
                : null;

        applySwingDefaults();

        if (persist) {
            PREFS.put(LANGUAGE_KEY, currentLanguage.code);
        }
    }

    public static String t(String sourceText) {
        if (sourceText == null) {
            return "";
        }
        if (currentLanguage == Language.SPANISH || bundle == null) {
            return sourceText;
        }
        try {
            return bundle.getString(sourceText);
        } catch (MissingResourceException ignored) {
            return sourceText;
        }
    }

    public static String format(String sourcePattern, Object... arguments) {
        return MessageFormat.format(t(sourcePattern), arguments);
    }

    public static String languageMenuLabel() {
        return currentLanguage == Language.ENGLISH ? "Language" : "Idioma";
    }

    public static String languageSelectionLabel(Language language) {
        if (language == null) {
            return "";
        }
        return language == Language.SPANISH ? "Espa\u00F1ol" : "English";
    }

    private static Language resolveInitialLanguage() {
        String explicitProperty = System.getProperty("catgis.ui.language");
        if (explicitProperty != null && !explicitProperty.isBlank()) {
            return Language.fromCode(explicitProperty);
        }

        String storedLanguage = PREFS.get(LANGUAGE_KEY, null);
        if (storedLanguage != null && !storedLanguage.isBlank()) {
            return Language.fromCode(storedLanguage);
        }

        String installDefault = loadInstallDefaultLanguage();
        if (installDefault != null && !installDefault.isBlank()) {
            return Language.fromCode(installDefault);
        }

        return Language.SPANISH;
    }

    private static String loadInstallDefaultLanguage() {
        Path defaultsPath = resolveInstallDefaultsPath();
        if (defaultsPath == null || !Files.exists(defaultsPath)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(defaultsPath)) {
            properties.load(input);
            return properties.getProperty(LANGUAGE_KEY);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Path resolveInstallDefaultsPath() {
        String jpackageAppPath = System.getProperty("jpackage.app-path");
        if (jpackageAppPath != null && !jpackageAppPath.isBlank()) {
            Path executablePath = Paths.get(jpackageAppPath);
            if (executablePath.getParent() != null) {
                return executablePath.getParent().resolve("app").resolve(INSTALL_DEFAULTS_FILE);
            }
        }

        try {
            Path location = Paths.get(I18n.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path baseDir = Files.isDirectory(location) ? location : location.getParent();
            if (baseDir != null) {
                return baseDir.resolve(INSTALL_DEFAULTS_FILE);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static void applySwingDefaults() {
        UIManager.put("OptionPane.okButtonText", t("Aceptar"));
        UIManager.put("OptionPane.cancelButtonText", t("Cancelar"));
        UIManager.put("OptionPane.yesButtonText", t("Si"));
        UIManager.put("OptionPane.noButtonText", t("No"));

        UIManager.put("FileChooser.openButtonText", t("Abrir"));
        UIManager.put("FileChooser.saveButtonText", t("Guardar"));
        UIManager.put("FileChooser.cancelButtonText", t("Cancelar"));
        UIManager.put("FileChooser.updateButtonText", t("Actualizar"));
        UIManager.put("FileChooser.helpButtonText", t("Ayuda"));
        UIManager.put("FileChooser.directoryOpenButtonText", t("Abrir"));
        UIManager.put("FileChooser.lookInLabelText", t("Buscar en:"));
        UIManager.put("FileChooser.saveInLabelText", t("Guardar en:"));
        UIManager.put("FileChooser.fileNameLabelText", t("Nombre:"));
        UIManager.put("FileChooser.filesOfTypeLabelText", t("Tipo de archivo:"));
        UIManager.put("FileChooser.openDialogTitleText", t("Abrir"));
        UIManager.put("FileChooser.saveDialogTitleText", t("Guardar"));
        UIManager.put("FileChooser.upFolderToolTipText", t("Subir un nivel"));
        UIManager.put("FileChooser.homeFolderToolTipText", t("Inicio"));
        UIManager.put("FileChooser.newFolderToolTipText", t("Nueva carpeta"));
        UIManager.put("FileChooser.listViewButtonToolTipText", t("Vista de lista"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", t("Vista de detalles"));
    }

    public enum Language {
        SPANISH("es"),
        ENGLISH("en");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        private Locale toLocale() {
            return Locale.forLanguageTag(code);
        }

        private static Language fromCode(String code) {
            if (ENGLISH.code.equalsIgnoreCase(code)) {
                return ENGLISH;
            }
            return SPANISH;
        }
    }
}
