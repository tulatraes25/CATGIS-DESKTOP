package ar.com.catgis;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
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
        } catch (Exception ignored) {
        }
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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int arc = Math.max(8, size / 3);
            g2.setColor(new Color(20, 96, 182));
            g2.fillRoundRect(0, 0, size - 1, size - 1, arc, arc);

            g2.setColor(new Color(255, 255, 255, 52));
            g2.fillRoundRect(size / 10, size / 10, size - (size / 5), size / 3, arc, arc);

            g2.setColor(new Color(245, 158, 11));
            g2.setStroke(new BasicStroke(Math.max(2f, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int underlineY = (int) Math.round(size * 0.78);
            g2.drawLine((int) Math.round(size * 0.18), underlineY, (int) Math.round(size * 0.82), underlineY);

            String text = size >= 48 ? "CAT" : "C";
            Font font = new Font("SansSerif", Font.BOLD, size >= 48 ? size / 3 : size / 2);
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (size - metrics.stringWidth(text)) / 2;
            int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);
        } finally {
            g2.dispose();
        }
        return image;
    }
}
