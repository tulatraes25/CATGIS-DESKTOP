package ar.com.catgis.catmap.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.prefs.Preferences;

/**
 * Light/dark theme selection with persistence and HiDPI awareness for CATMAP.
 * Pure helpers stay testable; the Swing application is applied through
 * {@link #initialize()} and {@link #toggle(java.awt.Component)}.
 */
public final class ThemeSupport {

    private static final String DEFAULT_UI_SCALE_PROPERTY = "sun.java2d.uiScale.enabled";
    private static final double MIN_FONT_SCALE = 1.0;
    private static final double MAX_FONT_SCALE = 2.0;

    private static Boolean darkTheme;

    private ThemeSupport() {}

    /** Stored preference wins; otherwise follow the OS dark mode hint. */
    public static boolean resolveDark(Boolean storedPreference, boolean systemPrefersDark) {
        return storedPreference != null ? storedPreference : systemPrefersDark;
    }

    /** Clamp the UI font scale factor used to compensate HiDPI screens. */
    public static double fontScaleFor(double uiScale) {
        if (!Double.isFinite(uiScale) || uiScale <= 0) {
            return MIN_FONT_SCALE;
        }
        return Math.max(MIN_FONT_SCALE, Math.min(MAX_FONT_SCALE, uiScale));
    }

    /** Short human-readable description for the status bar/toast. */
    public static String describe(boolean dark, double uiScale) {
        return "Tema " + (dark ? "oscuro" : "claro") + " - HiDPI x"
                + String.format(java.util.Locale.ROOT, "%.2f", fontScaleFor(uiScale));
    }

    public static boolean isDark() {
        Boolean current = darkTheme;
        if (current != null) {
            return current;
        }
        return resolveDark(readStoredPreference(), systemPrefersDark());
    }

    /** Read the saved preference and apply the theme (call before creating windows). */
    public static void initialize() {
        // Java scales Swing automatically on HiDPI; make sure the flag is not disabled.
        if (System.getProperty(DEFAULT_UI_SCALE_PROPERTY) == null) {
            System.setProperty(DEFAULT_UI_SCALE_PROPERTY, "true");
        }
        applyTheme(isDark());
    }

    /** Switch light/dark, persist the choice and refresh open windows. */
    public static void toggle(java.awt.Component owner) {
        boolean next = !isDark();
        applyTheme(next);
        storePreference(next);
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
        String message = describe(next, uiScale());
        if (owner instanceof java.awt.Frame frame) {
            JOptionPane.showMessageDialog(frame, message, "Apariencia",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Effective UI scale of the primary screen (1.0 on standard displays). */
    public static double uiScale() {
        try {
            GraphicsConfiguration configuration = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration();
            return fontScaleFor(configuration.getDefaultTransform().getScaleX());
        } catch (Exception unavailable) {
            return 1.0;
        }
    }

    private static void applyTheme(boolean dark) {
        try {
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
            darkTheme = dark;
        } catch (Exception unsupported) {
            // Keep the current look and feel; the app remains usable.
            darkTheme = dark;
        }
    }

    private static Boolean readStoredPreference() {
        try {
            String value = preferences().get("theme", "");
            if ("dark".equalsIgnoreCase(value)) {
                return Boolean.TRUE;
            }
            if ("light".equalsIgnoreCase(value)) {
                return Boolean.FALSE;
            }
        } catch (Exception ignored) {
            // Preferences are optional (headless/sandboxed environments).
        }
        return null;
    }

    private static void storePreference(boolean dark) {
        try {
            preferences().put("theme", dark ? "dark" : "light");
        } catch (Exception ignored) {
            // Persisting is best-effort.
        }
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(ThemeSupport.class);
    }

    private static boolean systemPrefersDark() {
        // No portable OS hint in Java; default to the historical light shell.
        return false;
    }
}
