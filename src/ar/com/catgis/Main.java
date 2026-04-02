package ar.com.catgis;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();

            SplashScreenWindow splash = new SplashScreenWindow();
            splash.showFor(2800, () -> {
                CatgisDesktopApp app = new CatgisDesktopApp();
                app.setVisible(true);
            });
        });
    }

    private static void installLookAndFeel() {
        try {
            Class<?> themeClass = Class.forName("com.formdev.flatlaf.intellijthemes.FlatArcIJTheme");
            Object laf = themeClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) laf);
            return;
        } catch (Throwable ignored) {
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
