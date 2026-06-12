package ar.com.catgis;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        if (isCatmapStandaloneLaunch(args)) {
            ar.com.catgis.catmap.Main.main(stripStandaloneFlag(args));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            I18n.initialize();
            AppBranding.applyTaskbarIcon();

            SplashScreenWindow splash = new SplashScreenWindow();
            splash.showFor(1200, () -> {
                // Initialize EventBus wiring
                ar.com.catgis.service.EventBusInitializer.init();
                // Install global keyboard shortcuts
                ar.com.catgis.ShortcutsDialog.installGlobalShortcutListener();
                // Initialize plugin system
                ar.com.catgis.plugins.PluginManager.initialize();
                CatgisDesktopApp app = new CatgisDesktopApp();
                app.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    app.showStartupProjectCrsPromptIfNeeded();
                    // Show welcome page on first launch
                    if (AppBranding.isShowWelcomePage() && AppContext.project() == null) {
                        WelcomePageDialog.open();
                    }
                });
            });
        });
    }

    private static boolean isCatmapStandaloneLaunch(String[] args) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if ("--catmap-standalone".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static String[] stripStandaloneFlag(String[] args) {
        if (args == null || args.length == 0) {
            return new String[0];
        }

        java.util.List<String> filtered = new java.util.ArrayList<>();
        for (String arg : args) {
            if (!"--catmap-standalone".equals(arg)) {
                filtered.add(arg);
            }
        }
        return filtered.toArray(new String[0]);
    }

    private static void installLookAndFeel() {
        try {
            Class<?> themeClass = Class.forName("com.formdev.flatlaf.intellijthemes.FlatArcIJTheme");
            Object laf = themeClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) laf);
            return;
        } catch (Exception ignored) { CatgisLogger.warn("Main: operation failed", ignored); }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { CatgisLogger.warn("Main: operation failed", ignored); }
    }
}
