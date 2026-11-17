package ar.com.catgis.catmap.ui;

import ar.com.catgis.AppVersion;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Brief CATMAP beta splash (T33/B2). Purely painted (no external asset) so the
 * app-image does not depend on a PNG; it never blocks startup and is skipped in
 * headless environments.
 */
public final class CatmapSplash {

    private static final int WIDTH = 480;
    private static final int HEIGHT = 220;
    private static final int AUTO_CLOSE_MILLIS = 4000;

    private final JWindow window;

    private CatmapSplash(JWindow window) {
        this.window = window;
    }

    /** Open the splash, or return null when there is no display. */
    public static CatmapSplash open() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        JWindow window = new JWindow();
        window.setContentPane(new SplashPanel());
        window.setSize(WIDTH, HEIGHT);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        CatmapSplash splash = new CatmapSplash(window);
        splash.closeAfter(AUTO_CLOSE_MILLIS);
        return splash;
    }

    /** Dispose after the given delay; safe to call once. */
    public void closeAfter(int millis) {
        if (window == null) {
            return;
        }
        Timer timer = new Timer(Math.max(0, millis), event -> {
            window.dispose();
            ((Timer) event.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void close() {
        if (window != null) {
            window.dispose();
        }
    }

    /** Label shown by the splash (also used by the version contract). */
    public static String label() {
        return AppVersion.catmapVersionText();
    }

    /** Deterministic painted splash, reused by tests and by the window panel. */
    public static BufferedImage render(int width, int height) {
        BufferedImage image = new BufferedImage(Math.max(64, width),
                Math.max(64, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            paintBrand(g2, image.getWidth(), image.getHeight());
        } finally {
            g2.dispose();
        }
        return image;
    }

    private static void paintBrand(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0x0F2A44));
        g2.fillRect(0, 0, width, height);
        g2.setColor(new Color(0x1B4F72));
        g2.fillPolygon(new int[]{0, width, width, 0}, new int[]{height, height / 2, height, height}, 4);
        g2.setColor(new Color(0x8CC63F));
        g2.fillRect(0, height - 6, width, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(22, height / 7)));
        g2.drawString("CATMAP", width / 12, height / 3);
        g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11, height / 16)));
        g2.drawString("Compositor cartografico", width / 12, height / 3 + height / 7);
        g2.setColor(new Color(0xD5E8F0));
        g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(9, height / 20)));
        g2.drawString(label(), width / 12, height - height / 6);
    }

    private static final class SplashPanel extends JPanel {
        private SplashPanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setDoubleBuffered(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                paintBrand(g2, getWidth(), getHeight());
            } finally {
                g2.dispose();
            }
        }
    }
}
