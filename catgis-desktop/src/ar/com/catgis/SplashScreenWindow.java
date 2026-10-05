package ar.com.catgis;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
public class SplashScreenWindow extends JWindow {

    private static final int FALLBACK_SPLASH_WIDTH = 1100;
    private static final int FALLBACK_SPLASH_HEIGHT = 620;
    private static final int MAX_SPLASH_WIDTH = 1200;
    private static final int MAX_SPLASH_HEIGHT = 800;
    private final AtomicBoolean closeRequested = new AtomicBoolean(false);
    private Runnable pendingAfterClose;
    private final Dimension splashSize;

    public SplashScreenWindow() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        Image image = loadSplashImage();
        splashSize = resolveSplashSize(image);

        if (image != null) {
            add(new ImagePanel(image, splashSize), BorderLayout.CENTER);
        } else {
            add(new GradientSplashPanel(splashSize), BorderLayout.CENTER);
        }

        installDismissInteraction();
        pack();
        setSize(splashSize);
        setLocationRelativeTo(null);
    }

    public void showFor(long millis, Runnable afterClose) {
        closeRequested.set(false);
        pendingAfterClose = afterClose;
        setVisible(true);
        toFront();
        requestFocus();

        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                CatgisLogger.warn("SplashScreenWindow: sleep interrupted", e);
            }

            SwingUtilities.invokeLater(this::dismiss);
        }, "catgis-splash-thread");

        worker.setDaemon(true);
        worker.start();
    }

    private void installDismissInteraction() {
        MouseAdapter clickToDismiss = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dismiss();
            }
        };
        addMouseListener(clickToDismiss);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                        || e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    dismiss();
                }
            }
        });
        setFocusableWindowState(true);
    }

    private void dismiss() {
        if (!closeRequested.compareAndSet(false, true)) {
            return;
        }
        setVisible(false);
        dispose();
        Runnable afterClose = pendingAfterClose;
        pendingAfterClose = null;
        if (afterClose != null) {
            afterClose.run();
        }
    }

    private Image loadSplashImage() {
        return AppBranding.loadSplashImage();
    }

    private static Dimension resolveSplashSize(Image image) {
        if (image == null) {
            return new Dimension(FALLBACK_SPLASH_WIDTH, FALLBACK_SPLASH_HEIGHT);
        }
        int imgW = image.getWidth(null);
        int imgH = image.getHeight(null);
        if (imgW <= 0 || imgH <= 0) {
            return new Dimension(FALLBACK_SPLASH_WIDTH, FALLBACK_SPLASH_HEIGHT);
        }
        double scale = Math.min((double) MAX_SPLASH_WIDTH / imgW, (double) MAX_SPLASH_HEIGHT / imgH);
        scale = Math.min(scale, 1.0d);
        int width = Math.max(720, (int) Math.round(imgW * scale));
        int height = Math.max(480, (int) Math.round(imgH * scale));
        return new Dimension(width, height);
    }

    private static class GradientSplashPanel extends JPanel {
        private final Dimension prefSize;

        GradientSplashPanel(Dimension prefSize) {
            this.prefSize = new Dimension(prefSize);
            setPreferredSize(prefSize);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth(), h = getHeight();

            // Deep gradient background
            GradientPaint bg = new GradientPaint(0, 0, new Color(15, 23, 42), w, h, new Color(30, 58, 138));
            g2.setPaint(bg);
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            // Subtle grid pattern
            g2.setColor(new Color(255, 255, 255, 12));
            g2.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x < w; x += 30) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 30) g2.drawLine(0, y, w, y);

            // Gold accent line
            g2.setColor(new Color(217, 164, 47));
            g2.fillRect(w / 2 - 60, h / 2 + 60, 120, 3);

            // Title
            g2.setFont(new Font("SansSerif", Font.BOLD, 42));
            g2.setColor(Color.WHITE);
            String title = "CATGIS";
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 20);

            // Subtitle
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.setColor(new Color(148, 163, 184));
            String sub = "Sistema de Informaci\u00F3n Geogr\u00E1fica";
            fm = g2.getFontMetrics();
            g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 + 10);

            // Version
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(100, 116, 139));
            String ver = "v" + AppBranding.getAppVersion();
            fm = g2.getFontMetrics();
            g2.drawString(ver, (w - fm.stringWidth(ver)) / 2, h / 2 + 35);

            g2.dispose();
        }
    }

    private static class ImagePanel extends JLabel {
        private final Image image;
        private final Dimension preferredSize;

        public ImagePanel(Image image, Dimension preferredSize) {
            this.image = image;
            this.preferredSize = preferredSize != null
                    ? new Dimension(preferredSize)
                    : new Dimension(FALLBACK_SPLASH_WIDTH, FALLBACK_SPLASH_HEIGHT);
            setPreferredSize(this.preferredSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = image.getWidth(this);
            int imgH = image.getHeight(this);

            if (imgW > 0 && imgH > 0) {
                double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int x = (panelW - drawW) / 2;
                int y = (panelH - drawH) / 2;
                g2.drawImage(image, x, y, drawW, drawH, this);
            }

            g2.dispose();
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
