package ar.com.catgis;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AppBranding {

    private static final String[] SPLASH_CANDIDATES = {
            "splash_catgis.png",
            "imagen de inicio.png",
            "catgis_splash.png",
            "catgis_inicio.png"
    };

    private static volatile List<Image> appIconImages;

    private static boolean showWelcomePage = true;

    private AppBranding() {
    }

    public static String getAppVersion() {
        Package pkg = AppBranding.class.getPackage();
        String ver = pkg != null ? pkg.getImplementationVersion() : null;
        return ver != null ? ver : "1.0.0";
    }

    public static boolean isShowWelcomePage() {
        return showWelcomePage;
    }

    public static void setShowWelcomePage(boolean show) {
        showWelcomePage = show;
    }

    public static Image loadSplashImage() {
        for (String candidate : SPLASH_CANDIDATES) {
            URL url = AppBranding.class.getResource("/" + candidate);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        }

        for (String candidate : SPLASH_CANDIDATES) {
            File file = new File(candidate);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }

        return null;
    }

    public static void applyFrameBranding(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setIconImages(getApplicationIconImages());
        applyTaskbarIcon();
    }

    public static void applyTaskbarIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) {
                return;
            }
            Taskbar taskbar = Taskbar.getTaskbar();
            if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                return;
            }
            List<Image> images = getApplicationIconImages();
            if (!images.isEmpty()) {
                taskbar.setIconImage(images.get(images.size() - 1));
            }
        } catch (Exception ignored) { CatgisLogger.warn("AppBranding: operation failed", ignored); }
    }

    public static List<Image> getApplicationIconImages() {
        if (appIconImages == null) {
            synchronized (AppBranding.class) {
                if (appIconImages == null) {
                    List<Image> generated = new ArrayList<>();
                    for (int size : new int[]{16, 24, 32, 48, 64, 128, 256}) {
                        generated.add(createBrandIcon(size));
                    }
                    appIconImages = Collections.unmodifiableList(generated);
                }
            }
        }
        return appIconImages;
    }

    private static BufferedImage createBrandIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int arc = Math.max(10, size / 3);

            // Deep navy gradient background
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(15, 23, 42), size, size, new Color(30, 58, 138));
            g2.setPaint(bgGradient);
            g2.fillRoundRect(0, 0, size - 1, size - 1, arc, arc);

            // Subtle top gloss
            if (size >= 32) {
                GradientPaint gloss = new GradientPaint(0, 0, new Color(255, 255, 255, 40), 0, size / 2f, new Color(255, 255, 255, 0));
                g2.setPaint(gloss);
                g2.fillRoundRect(0, 0, size - 1, size / 2, arc, arc);
            }

            // Gold crosshair/compass lines (subtle, behind text)
            if (size >= 32) {
                g2.setColor(new Color(217, 164, 47, 60));
                g2.setStroke(new BasicStroke(Math.max(1f, size / 40f)));
                int cx = size / 2, cy = size / 2;
                int r = (int)(size * 0.32);
                g2.drawLine(cx - r, cy, cx + r, cy);
                g2.drawLine(cx, cy - r, cx, cy + r);
                // Small diamond at center
                int d = Math.max(2, size / 16);
                g2.drawLine(cx - d, cy, cx, cy - d);
                g2.drawLine(cx, cy - d, cx + d, cy);
                g2.drawLine(cx + d, cy, cx, cy + d);
                g2.drawLine(cx, cy + d, cx - d, cy);
            }

            // CAT text
            String text = size >= 48 ? "CAT" : (size >= 32 ? "CA" : "C");
            float fontSize = size >= 48 ? size / 3.2f : size / 2.2f;
            Font font = new Font("SansSerif", Font.BOLD, Math.round(fontSize));
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (size - metrics.stringWidth(text)) / 2;
            int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y - (size >= 48 ? 1 : 0));

            // Gold accent bar at bottom
            int barY = (int) Math.round(size * 0.80);
            int barH = Math.max(2, size / 18);
            g2.setColor(new Color(217, 164, 47));
            g2.fillRoundRect((int)(size * 0.18), barY, (int)(size * 0.64), barH, barH, barH);
        } finally {
            g2.dispose();
        }
        return image;
    }
}
