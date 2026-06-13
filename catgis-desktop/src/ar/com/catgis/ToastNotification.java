package ar.com.catgis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Non-blocking toast notification popup.
 * <p>
 * Appears at the bottom-right of the parent frame and auto-dismisses.
 * Supports INFO, WARNING, and SUCCESS severity levels.
 */
final class ToastNotification {

    enum Severity { INFO, WARNING, SUCCESS }

    private static final int DISPLAY_MS = 3500;
    private static final int FADE_MS = 400;
    private static final int PADDING = 16;
    private static final int WIDTH = 360;
    private static final int MAX_HEIGHT = 120;
    private static final int ARC = 12;

    private ToastNotification() {}

    static void show(Frame owner, String message, Severity severity) {
        JWindow window = new JWindow(owner);
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(backgroundFor(severity));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        JLabel iconLabel = new JLabel(iconFor(severity));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JLabel textLabel = new JLabel("<html><body style='width:260px'>" + escape(message) + "</body></html>");
        textLabel.setFont(textLabel.getFont().deriveFont(Font.PLAIN, 13f));
        textLabel.setForeground(foregroundFor(severity));

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textLabel, BorderLayout.CENTER);
        window.setContentPane(panel);

        window.pack();
        int w = Math.max(WIDTH, window.getWidth());
        int h = Math.min(MAX_HEIGHT, window.getHeight());
        window.setSize(w, h);

        Dimension ownerSize = owner.getSize();
        Point ownerLoc = owner.getLocationOnScreen();
        int x = ownerLoc.x + ownerSize.width - w - 20;
        int y = ownerLoc.y + ownerSize.height - h - 50;
        window.setLocation(x, y);
        window.setVisible(true);

        Timer dismiss = new Timer(DISPLAY_MS, e -> {
            Timer fade = new Timer(FADE_MS / 10, null);
            final float[] alpha = {1f};
            fade.addActionListener(ev -> {
                alpha[0] -= 0.1f;
                if (alpha[0] <= 0f) {
                    fade.stop();
                    window.dispose();
                } else {
                    window.setOpacity(Math.max(0f, alpha[0]));
                }
            });
            fade.start();
        });
        dismiss.setRepeats(false);
        dismiss.start();
    }

    private static Color backgroundFor(Severity s) {
        return switch (s) {
            case INFO -> new Color(33, 150, 243, 230);
            case WARNING -> new Color(255, 152, 0, 230);
            case SUCCESS -> new Color(76, 175, 80, 230);
        };
    }

    private static Color foregroundFor(Severity s) {
        return Color.WHITE;
    }

    private static String iconFor(Severity s) {
        return switch (s) {
            case INFO -> "\u2139\uFE0F";
            case WARNING -> "\u26A0\uFE0F";
            case SUCCESS -> "\u2705";
        };
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
