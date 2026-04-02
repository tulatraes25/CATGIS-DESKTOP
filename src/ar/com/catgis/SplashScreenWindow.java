package ar.com.catgis;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

public class SplashScreenWindow extends JWindow {

    private static final int SPLASH_WIDTH = 1100;
    private static final int SPLASH_HEIGHT = 620;

    public SplashScreenWindow() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        Image image = loadSplashImage();

        if (image != null) {
            add(new ImagePanel(image), BorderLayout.CENTER);
        } else {
            JLabel fallback = new JLabel("CATGIS Desktop", SwingConstants.CENTER);
            fallback.setOpaque(true);
            fallback.setBackground(new Color(9, 34, 52));
            fallback.setForeground(Color.WHITE);
            fallback.setPreferredSize(new Dimension(SPLASH_WIDTH, SPLASH_HEIGHT));
            add(fallback, BorderLayout.CENTER);
        }

        pack();
        setSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        setLocationRelativeTo(null);
    }

    public void showFor(long millis, Runnable afterClose) {
        setVisible(true);

        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
            }

            SwingUtilities.invokeLater(() -> {
                setVisible(false);
                dispose();
                if (afterClose != null) {
                    afterClose.run();
                }
            });
        }, "catgis-splash-thread");

        worker.setDaemon(true);
        worker.start();
    }

    private Image loadSplashImage() {
        String[] candidates = {
                "splash_catgis.png",
                "imagen de inicio.png",
                "catgis_splash.png",
                "catgis_inicio.png"
        };

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }

        for (String candidate : candidates) {
            URL url = SplashScreenWindow.class.getResource("/" + candidate);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        }

        return null;
    }

    private static class ImagePanel extends JLabel {
        private final Image image;

        public ImagePanel(Image image) {
            this.image = image;
            setPreferredSize(new Dimension(SPLASH_WIDTH, SPLASH_HEIGHT));
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
