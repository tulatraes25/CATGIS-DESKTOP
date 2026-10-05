package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public final class WindowLayoutSupport {

    private static final int DEFAULT_HORIZONTAL_MARGIN = 44;
    private static final int DEFAULT_VERTICAL_MARGIN = 72;

    private WindowLayoutSupport() {
    }

    public static JScrollPane createVerticalScrollPane(JComponent content, int preferredWidth, int preferredHeight) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        return scrollPane;
    }

    public static void fitDialogToScreen(JDialog dialog,
                                         int preferredWidth,
                                         int preferredHeight,
                                         int minimumWidth,
                                         int minimumHeight) {
        if (dialog == null) {
            return;
        }

        Rectangle bounds = getMaximumWindowBounds();
        int availableWidth = Math.max(360, bounds.width - DEFAULT_HORIZONTAL_MARGIN);
        int availableHeight = Math.max(280, bounds.height - DEFAULT_VERTICAL_MARGIN);

        int targetWidth = Math.min(availableWidth, Math.max(minimumWidth, preferredWidth));
        int targetHeight = Math.min(availableHeight, Math.max(minimumHeight, preferredHeight));
        int appliedMinWidth = Math.min(targetWidth, Math.max(320, minimumWidth));
        int appliedMinHeight = Math.min(targetHeight, Math.max(240, minimumHeight));

        dialog.setMinimumSize(new Dimension(appliedMinWidth, appliedMinHeight));
        dialog.setSize(targetWidth, targetHeight);
    }

    public static void fitFrameToScreen(JFrame frame,
                                        int preferredWidth,
                                        int preferredHeight,
                                        int minimumWidth,
                                        int minimumHeight) {
        if (frame == null) {
            return;
        }

        Rectangle bounds = getMaximumWindowBounds();
        int availableWidth = Math.max(720, bounds.width - DEFAULT_HORIZONTAL_MARGIN);
        int availableHeight = Math.max(520, bounds.height - DEFAULT_VERTICAL_MARGIN);

        int targetWidth = Math.min(availableWidth, Math.max(minimumWidth, preferredWidth));
        int targetHeight = Math.min(availableHeight, Math.max(minimumHeight, preferredHeight));
        int appliedMinWidth = Math.min(targetWidth, Math.max(680, minimumWidth));
        int appliedMinHeight = Math.min(targetHeight, Math.max(480, minimumHeight));

        frame.setMinimumSize(new Dimension(appliedMinWidth, appliedMinHeight));
        frame.setSize(targetWidth, targetHeight);
    }

    private static Rectangle getMaximumWindowBounds() {
        if (GraphicsEnvironment.isHeadless()) {
            return new Rectangle(0, 0, 1366, 768);
        }
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
            return new Rectangle(0, 0, 1366, 768);
        }
        return bounds;
    }
}
